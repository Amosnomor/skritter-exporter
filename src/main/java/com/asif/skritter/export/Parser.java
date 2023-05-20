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

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    static List<Item> parseItems(String json) {

        LOGGER.debug("Items -> {}", JsonWriter.formatJson(json));

        List<Item> itemList = new ArrayList<>();

        Object obj = JsonReader.jsonToJava(json, Map.of(JsonReader.USE_MAPS, true));
        @SuppressWarnings("unchecked")
        Map<String, Object> itemsMap = (Map<String, Object>) obj;
        Object[] items = (Object[])itemsMap.get(Constants.SKRITTER_ITEMS_ARRAY_NAME);

        for (Object item : items) {
            // noinspection unchecked
            itemList.add(Item.Builder.build((Map<String, Object>)item));

        }

        return itemList;
    }

    static Vocab parseVocab(String json) {
        LOGGER.debug("Vocab -> {}", JsonWriter.formatJson(json));
        Object obj = JsonReader.jsonToJava(json, Map.of(JsonReader.USE_MAPS, true));
        @SuppressWarnings("unchecked")
        Map<String, Object> vocabMap = (Map<String, Object>) obj;
        @SuppressWarnings("unchecked")
        Map<String, Object> vocab = (Map<String, Object>)
            vocabMap.get(Constants.SKRITTER_VOCAB_OBJECT_NAME);
        return Vocab.Builder.build(vocab);
    }

    static SimpleTradMap parseSimpleTradMap(String json) {
        LOGGER.debug("SimpleTradMap -> {}", JsonWriter.formatJson(json));
        Object jsonMap = JsonReader.jsonToJava(json, Map.of(JsonReader.USE_MAPS, true));
        assert jsonMap instanceof Map;
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) jsonMap;
        @SuppressWarnings("unchecked")
        Map<String, Object> simpleTradMap =
                (Map<String, Object>) responseMap.get(Constants.SKRITTER_SIMPLE_TRAD_MAP_OBJECT_NAME);

        return new SimpleTradMap.Builder(simpleTradMap).build();
    }

    static BatchResponse parseBatchResponse(String json) {

        LOGGER.debug("Batch Response -> {}", JsonWriter.formatJson(json));

        Object obj = JsonReader.jsonToJava(json, Map.of(JsonReader.USE_MAPS, true));
        @SuppressWarnings("unchecked")
        Map<String, Object> batchMap = (Map<String, Object>) obj;
        long statusCode = (Long)batchMap.get(Constants.SKRITTER_STATUS_CODE_FIELD);
        if (statusCode != HttpStatus.SC_OK) {
            throw new SkritterException("Unexpected HTTP failure: " + json);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> batch = (Map<String, Object>)batchMap.get(
            Constants.SKRITTER_BATCH_OBJECT_NAME);

        return new BatchResponse.Builder(batch).build();
    }
}
