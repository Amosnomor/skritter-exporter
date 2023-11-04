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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExporterTest {

    @Test
    void noSimplifiedTest() {

        String reading = "ni3hao3";
        String pinyinWithToneMarks = "nǐhǎo";
        String writing = "你好";
        String definition = "hello";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.BOTH, writing);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER +
                writing + '\t' + '\t' + pinyinWithToneMarks + '\t' + definition + '\n');
    }

    @Test
    void simplifiedTest() {

        String reading = "ge4";
        String pinyinWithToneMarks = "gè";
        String simplifiedWriting = "个";
        String traditionalWriting = "個";
        String definition = "hello";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.SIMPLIFIED, simplifiedWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + simplifiedWriting + '\t'
                + pinyinWithToneMarks + '\t'
                + definition + '\n');
    }

    @Test
    void traditionalTest() {

        String reading = "ge4";
        String pinyinWithToneMarks = "gè";
        String simplifiedWriting = "个";
        String traditionalWriting = "個";
        String definition = "hello";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.TRADITIONAL, traditionalWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + simplifiedWriting + '\t'
                + pinyinWithToneMarks + '\t'
                + definition + '\n');
    }

    @Test
    void traditionalMatchesSimplifiedTest() {

        String reading = "gan1";
        String pinyinWithToneMarks = "gān";
        String simplifiedWriting = "干";
        String traditionalWriting = "乾";
        String definition = "dry";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.TRADITIONAL, traditionalWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);
        builder.setId("zh-干-3");

        Vocab gan1 = builder.build();

        Exporter exporter = new Exporter(List.of(gan1));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + simplifiedWriting + '\t'
                + pinyinWithToneMarks + '\t'
                + definition + '\n');
    }

    @Test
    void convertDefinitionNewlinesTest() {

        String reading = "le5";
        String pinyinWithToneMarks = "le";
        String simplifiedWriting = "了";
        String traditionalWriting = "了";
        String definition = "a\nb\nc\nd";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.BOTH, simplifiedWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + '\t'
                + pinyinWithToneMarks + '\t'
                + definition.replaceAll("\\n", Exporter.DEFINITION_NEWLINE_REPLACEMENT)
                + '\n');
    }

    @Test
    void convertDefinitionMultipleWhitespaceTest() {

        String reading = "le5";
        String pinyinWithToneMarks = "le";
        String simplifiedWriting = "了";
        String traditionalWriting = "了";
        String definition = "a b  c   d";
        String expectedDefinition = "a b c d";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.BOTH, simplifiedWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + '\t'
                + pinyinWithToneMarks + '\t'
                + expectedDefinition
                + '\n');
    }

    @Test
    void leadingWhitespaceTest() {

        String reading = "le5";
        String pinyinWithToneMarks = "le";
        String simplifiedWriting = "了";
        String traditionalWriting = "了";
        String definition = " something or other";
        String expectedDefinition = "something or other";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.BOTH, simplifiedWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + '\t'
                + pinyinWithToneMarks + '\t'
                + expectedDefinition
                + '\n');
    }

    @Test
    void trailingWhitespaceTest() {

        String reading = "le5";
        String pinyinWithToneMarks = "le";
        String simplifiedWriting = "了";
        String traditionalWriting = "了";
        String definition = "something or other ";
        String expectedDefinition = "something or other";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.BOTH, simplifiedWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + '\t'
                + pinyinWithToneMarks + '\t'
                + expectedDefinition
                + '\n');
    }

    @Test
    void leadingAndTrailingWhitespaceTest() {

        String reading = "le5";
        String pinyinWithToneMarks = "le";
        String simplifiedWriting = "了";
        String traditionalWriting = "了";
        String definition = " something or other ";
        String expectedDefinition = "something or other";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.BOTH, simplifiedWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + '\t'
                + pinyinWithToneMarks + '\t'
                + expectedDefinition
                + '\n');
    }

    @Test
    void customDefinitionTest() {

        String reading = "ge4";
        String pinyinWithToneMarks = "gè";
        String simplifiedWriting = "个";
        String traditionalWriting = "個";
        String definition = "hello";
        String customDefinition = "goodbye";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.TRADITIONAL, traditionalWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);
        builder.setCustomDefinition(customDefinition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export();
        assertThat(exported).isEqualTo(Exporter.HEADER
                + traditionalWriting + '\t'
                + simplifiedWriting + '\t'
                + pinyinWithToneMarks + '\t'
                + customDefinition + '\n');
    }

    @Test
    void skritterModeExportTest() {

        String reading = "ge4";
        String simplifiedWriting = "个";
        String traditionalWriting = "個";
        String definition = "hello";
        String customDefinition = "goodbye";

        Vocab.Builder builder = new Vocab.Builder();
        builder.setReading(reading);
        builder.setWriting(Vocab.WritingStyle.TRADITIONAL, traditionalWriting);
        builder.addDefinition(Constants.SKRITTER_LANGUAGE_ENGLISH, definition);
        builder.setCustomDefinition(customDefinition);

        Vocab ni3Hao3 = builder.build();

        Exporter exporter = new Exporter(List.of(ni3Hao3));
        String exported = exporter.export(Exporter.ExportStyle.SKRITTER);
        assertThat(exported).isEqualTo(
                simplifiedWriting + '\t'
                + traditionalWriting + '\t'
                + reading + '\t'
                + customDefinition + '\n');
    }
}
