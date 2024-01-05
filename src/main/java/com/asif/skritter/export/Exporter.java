// MIT License
//
// Copyright (c) 2023 David Stone <ds.skritter.export@asif.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.asif.skritter.export;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class Exporter {

    enum ExportStyle {
        SKRITTER,
        ANKI
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Exporter.class);

    static final String HEADER = """
            #separator:Tab
            #columns:Traditional\tSimplified\tPinyin\tEnglish
            #notetype:Chinese-Basic
            """;

    static final String DEFINITION_NEWLINE_REPLACEMENT = "; ";

    final Collection<Vocab> vocabs;

    private final PinyinConverter pinyinConverter = new PinyinConverter();

    private final SimpleTradMap simpleTradMap;

    Exporter(Collection<Vocab> vocabs) {
        this.vocabs = vocabs;
        this.simpleTradMap = null;
    }

    Exporter(SimpleTradMap simpleTradMap, Collection<Vocab> vocabs) {
        this.vocabs = vocabs;
        this.simpleTradMap = simpleTradMap;
    }

    String export(ExportStyle exportStyle) {
        return doExport(exportStyle);
    }

    String export() {
        return doExport(ExportStyle.ANKI);
    }

    String doExport(ExportStyle exportStyle) {

        LOGGER.debug("Exporting");

        StringBuilder output = new StringBuilder();

        if (exportStyle == ExportStyle.ANKI) {
            output.append(HEADER);
        }

        for (Vocab vocab : vocabs) {

            // Use custom definition if available
            String definition = (vocab.customDefinition != null) ?
                    vocab.customDefinition : vocab.definitions.get(Constants.SKRITTER_LANGUAGE_ENGLISH);

            // Replace newlines in definitions with "; "
            definition = definition.replaceAll("\\n", DEFINITION_NEWLINE_REPLACEMENT);
            // Replace multiple spaces with single space
            definition = definition.replaceAll(" +", " ");
            // Remove leading white space
            definition = definition.replaceFirst("^ +", "");
            // Remove trailing white space
            definition = definition.replaceFirst(" +$", "");

            String traditional;
            String simplified;

            switch (vocab.writingStyle) {
                case SIMPLIFIED -> {
                    simplified = vocab.writing;
                    if (simpleTradMap != null) {
                        traditional = simpleTradMap.convertSimplifiedToTraditional(simplified);
                    } else {
                        traditional = ZhConverterUtil.toTraditional(simplified);
                    }
                    if (simplified.equals(traditional)) {
                        LOGGER.warn("Simplified == Traditional for {}", vocab);
                        simplified = "";
                    }
                }
                case TRADITIONAL -> {
                    traditional = vocab.writing;
                    simplified = ZhConverterUtil.toSimple(traditional);
                    if (simplified.equals(traditional)) {
                        // May be due to multiple possible conversions.
                        // Try converting the id
                        simplified = vocab.id.replaceAll("^zh-(.*)-[0-9]", "$1");
                        // If it is still the same, don't emit the simplified
                        if (simplified.equals(traditional)) {
                            simplified = "";
                        } else {
                            LOGGER.warn("Traditional == Simplified for {}, converting {} instead", vocab, vocab.id);
                        }
                    }
                }
                default -> {
                    assert vocab.writingStyle == Vocab.WritingStyle.BOTH :
                            "unknown writingStyle: " + vocab.writingStyle;
                    traditional = vocab.writing;
                    // Don't emit simplified when it is the same
                    simplified = "";
                }
            }

            // remove white space from characters
            traditional = traditional.replaceAll(" ", "");
            simplified = simplified.replaceAll(" ", "");

            if (exportStyle == ExportStyle.ANKI) {
                output.append(traditional);
                output.append('\t');
                output.append(simplified);
                output.append('\t');
                // Convert numeric Pinyin to tone marks.
                output.append(pinyinConverter.toPinyin(vocab.reading));
            } else {
                assert exportStyle == ExportStyle.SKRITTER : exportStyle;
                output.append(simplified.isEmpty() ? vocab.writing : simplified);
                output.append('\t');
                output.append(traditional);
                output.append('\t');
                output.append(vocab.reading);
            }

            output.append('\t');
            output.append(definition);
            output.append('\n');
        }

        return output.toString();
    }
}
