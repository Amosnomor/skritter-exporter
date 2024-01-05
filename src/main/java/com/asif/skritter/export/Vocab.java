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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class Vocab {

    private static final Logger LOGGER = LoggerFactory.getLogger(Vocab.class);

    static final String ERROR_INVALID_WRITING_STYLE = "Invalid WritingStyle: {0}";

    enum WritingStyle {
        SIMPLIFIED(Constants.SKRITTER_WRITING_STYLE_SIMPLE),
        TRADITIONAL(Constants.SKRITTER_WRITING_STYLE_TRADITIONAL),
        BOTH(Constants.SKRITTER_WRITING_STYLE_BOTH);

        private final String style;

        WritingStyle(String style) {
            this.style = style;
        }

        static WritingStyle stringToEnum(String writingStyle) {
            return switch (writingStyle) {
            case Constants.SKRITTER_WRITING_STYLE_SIMPLE -> SIMPLIFIED;
            case Constants.SKRITTER_WRITING_STYLE_TRADITIONAL -> TRADITIONAL;
            case Constants.SKRITTER_WRITING_STYLE_BOTH -> BOTH;
            default ->
                throw new SkritterException(
                        MessageFormat.format(ERROR_INVALID_WRITING_STYLE, writingStyle));
            };
        }

        String toSkritterString() {
            return style;
        }
    }

    String id;
    String lang;
    long priority;
    WritingStyle writingStyle;
    String audio;
    long toughness;
    String[] sentenceIds;
    long created;
    String[] bannedParts;
    String creator;
    String ilk;
    String writing;
//    Audio[] audios;
    String[] dictionaryLinks;
    String[] containedVocabIds;
    String audioURL;
    String toughnessString;
    Map<String, String> definitions;
    String customDefinition;
    boolean starred;
    String reading;

    private Vocab() {

    }

    @Override
    public String toString() {
        return String.join(":", writing, reading, definitions.get(Constants.SKRITTER_LANGUAGE_ENGLISH));
    }

    // Helper class to construct a Vocab from a json-io map
    static class Builder {

        private String id;
        private String reading;
        private String writing;
        private WritingStyle writingStyle;
        private String customDefinition;
        private final Map<String, String> definitions = new HashMap<>();

        Vocab build() {
            return buildFromAttributes();
        }

        static Vocab build(Map<String, Object> vocabMap) {
            Vocab vocab = new Vocab();

            vocab.id = (String) vocabMap.get(Constants.SKRITTER_ID_FIELD);
            LOGGER.debug("Building Vocab for {}", vocab.id);

            String style = (String) vocabMap.get(Constants.SKRITTER_STYLE_FIELD);
            assert style != null;
            vocab.writingStyle = WritingStyle.stringToEnum(style);

            vocab.lang = (String) vocabMap.get("lang");
            vocab.priority = getLongValue(vocabMap.get("priority"));
            vocab.audio = (String) vocabMap.get("audio");
            vocab.toughness = getLongValue(vocabMap.get("toughness"));
            // sentenceIds
            vocab.created = getLongValue(vocabMap.get(Constants.SKRITTER_CREATED_FIELD));

            Object[] bannedPartsArray = (Object[])vocabMap.get(Constants.SKRITTER_BANNED_PARTS_FIELD);
            if (bannedPartsArray != null) {
                int size = bannedPartsArray.length;
                vocab.bannedParts = new String[size];
                for (int i = 0; i < size; i++) {
                    vocab.bannedParts[i] = (String)bannedPartsArray[i];
                }
            }
            vocab.creator = (String) vocabMap.get("creator");
            vocab.ilk = (String) vocabMap.get("ilk");
            vocab.writing = (String) vocabMap.get(Constants.SKRITTER_WRITING_FIELD);
            // audios
            // dictionaryLinks
            // containedVocabIds
            vocab.audioURL = (String) vocabMap.get("audioURL");
            vocab.toughnessString = (String) vocabMap.get("toughnessString");
            // definitions
            vocab.customDefinition = (String)vocabMap.get(Constants.SKRITTER_CUSTOM_DEFINITION_FIELD);
            //noinspection unchecked
            vocab.definitions = (Map<String, String>)
                    vocabMap.get(Constants.SKRITTER_DEFINITIONS_FIELD);
            vocab.starred = getBooleanValue(vocabMap.get("starred"));
            vocab.reading = (String) vocabMap.get(Constants.SKRITTER_READING_FIELD);

            return vocab;
        }

        void setId(String id) {
            this.id = id;
        }

        void setWriting(WritingStyle writingStyle, String writing) {
            this.writing = writing;
            this.writingStyle = writingStyle;
        }

        void setReading(String reading) {
            this.reading = reading;
        }

        void addDefinition(String language, String definition) {
            this.definitions.put(language, definition);
        }

        void setCustomDefinition(String customDefinition) {
            this.customDefinition = customDefinition;
        }

        private Vocab buildFromAttributes() {
            assert writing != null;
            assert reading != null;
            assert !definitions.isEmpty();

            Vocab vocab = new Vocab();
            vocab.writing = writing;
            vocab.reading = reading;
            vocab.writingStyle = writingStyle;
            vocab.definitions = new HashMap<>();
            vocab.definitions.putAll(definitions);
            vocab.customDefinition = customDefinition;

            vocab.id = (id == null) ? "" : id;
            vocab.lang = "";
            vocab.priority = 0;
            vocab.audio = "";
            vocab.toughness = 0;
            // sentenceIds
            vocab.created = 0;
            // bannedParts
            vocab.creator = "";
            vocab.ilk = "";
            vocab.audioURL = "";
            vocab.toughnessString = "";
            // definitions

            return vocab;
        }
    }

    private static long getLongValue(Object longElement) {
        return longElement == null ? 0 : (Long)longElement;
    }

    private static boolean getBooleanValue(Object booleanElement) {
        return booleanElement != null && (Boolean) booleanElement;
    }
}
