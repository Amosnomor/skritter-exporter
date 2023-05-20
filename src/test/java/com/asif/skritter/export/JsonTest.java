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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.cedarsoftware.util.io.JsonWriter.objectToJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;

public class JsonTest {
    private long testLong;
    private static final String CUSTOMER_FILE = "customers.json";
    private static final String PRODUCT_CURSOR_START_FILE = "product-cursor-start.json";

    @Test
    public void simpleRoundTrip() {
        String myJson = objectToJson(this);
        JsonTest roundTrip = (JsonTest)JsonReader.jsonToJava(myJson);
        assertThat(testLong).isEqualTo(roundTrip.testLong);
        testLong = ~0;
        assertThat(testLong).isNotEqualTo(roundTrip.testLong);
        myJson = objectToJson(this);
        roundTrip = (JsonTest)JsonReader.jsonToJava(myJson);
        assertThat(testLong).isEqualTo(roundTrip.testLong);
    }

    @Test
    public void itemsTest() throws URISyntaxException, IOException {
        List<Item> items = Parser.parseItems(Util.getJsonResource("items.json"));
        assertThat(items).hasSize(200);
        Item item = items.get(0);
        assertThat(item.id).isEqualTo("234179586-zh-没关系-2-rune");
        assertThat(item.lang).isEqualTo("zh");
        assertThat(item.style).isEqualTo("trad");
        assertThat(item.changed).isEqualTo(1681405367);
        assertThat(item.last).isEqualTo(1681405323);
        assertThat(item.created).isEqualTo(1681146878);
        assertThat(item.successes).isEqualTo(4);
        assertThat(item.timeStudied).isEqualTo(727);
        assertThat(item.interval).isEqualTo(86400);
        // sectionIds
        assertThat(item.next).isEqualTo(1681491723);
        assertThat(item.reviews).isEqualTo(15);
//        assertThat(item.previousInterval).isEqualTo(86400);
        assertThat(item.part).isEqualTo("rune");
        // vocabListIds
        // vocabIds
        assertThat(item.previousSuccess).isTrue();

        item = items.get(199);
        assertThat(item.id).isEqualTo("234179586-zh-条-0-rdng");
        assertThat(item.lang).isEqualTo("zh");
        assertThat(item.style).isEqualTo("trad");
        assertThat(item.changed).isEqualTo(1681264149);
        assertThat(item.last).isEqualTo(1681263081);
        assertThat(item.created).isEqualTo(1677095609);
        assertThat(item.successes).isEqualTo(5);
        assertThat(item.timeStudied).isEqualTo(21);
        assertThat(item.interval).isEqualTo(9416576);
        // sectionIds
        assertThat(item.next).isEqualTo(1690679657);
        assertThat(item.reviews).isEqualTo(5);
//        assertThat(item.previousInterval).isEqualTo(2975891);
        assertThat(item.part).isEqualTo("rdng");
        // vocabListIds
        // vocabIds
        assertThat(item.previousSuccess).isTrue();
    }

    @Test
    public void vocabTest() throws URISyntaxException, IOException {
        Vocab vocab = Parser.parseVocab(Util.getJsonResource("vocab.json"));
        assertThat(vocab.id).isEqualTo("zh-啤酒-0");
        assertThat(vocab.lang).isEqualTo("zh");
        assertThat(vocab.priority).isEqualTo(0);
        assertThat(vocab.writingStyle).isEqualTo(Vocab.WritingStyle.BOTH);
        assertThat(vocab.audio).isEqualTo(
           "http://storage.googleapis.com/skritter_audio/zh/xiao-lu/5604481132265472.mp3");
        assertThat(vocab.toughness).isEqualTo(2);
        assertThat(vocab.sentenceIds).isNull();
        assertThat(vocab.created).isEqualTo(1290940640);
        assertThat(vocab.bannedParts).hasSize(0);
        assertThat(vocab.creator).isEqualTo("CPAPI");
        assertThat(vocab.ilk).isEqualTo("word");
        assertThat(vocab.writing).isEqualTo("啤酒");
//        assertThat(vocab.audios).isNull();
        assertThat(vocab.dictionaryLinks).isNull();
        assertThat(vocab.containedVocabIds).isNull();
        assertThat(vocab.audioURL).isEqualTo(
                "http://storage.googleapis.com/skritter_audio/zh/xiao-lu/5604481132265472.mp3");
        assertThat(vocab.toughnessString).isEqualTo("easier");
        assertThat(vocab.definitions).containsExactly(entry("en", "beer"));
        assertThat(vocab.starred).isFalse();
        assertThat(vocab.reading).isEqualTo("pi2jiu3");
    }

    @Test
    public void batchResponseTest() throws URISyntaxException, IOException {
        BatchResponse batchResponse =
                Parser.parseBatchResponse(Util.getJsonResource("batch_get_vocabs.json"));
        assertThat(batchResponse.id).isEqualTo("5883192233295872");
        assertThat(batchResponse.totalRequests).isEqualTo(1);
        assertThat(batchResponse.runningRequests).isEqualTo(1);
        assertThat(batchResponse.created).isEqualTo(1682637856L);
        assertThat(batchResponse.requests).hasSize(1);
        BatchRequest request = batchResponse.requests.get(0);
        assertThat(request.id).isEqualTo("5077606289768448");
        assertThat(request.created).isEqualTo(1682637856L);
        assertThat(request.spawnedBy).isEqualTo(5883192233295872L);
        assertThat(request.done).isEqualTo(0);
        assertThat(request.params).hasSize(2);
        assertThat(request.params).containsOnlyKeys(
                Constants.SKRITTER_FIELDS_PARAMETER, Constants.SKRITTER_IDS_PARAMETER);
        assertThat(request.params.get(Constants.SKRITTER_FIELDS_PARAMETER)).isEqualTo(String.join(",",
                        Constants.SKRITTER_READING_FIELD,
                        Constants.SKRITTER_WRITING_FIELD,
                        Constants.SKRITTER_DEFINITIONS_FIELD));
        assertThat(request.params.get(Constants.SKRITTER_IDS_PARAMETER)).isEqualTo("zh-场-0|zh-艮-0");
        assertThat(request.path).isEqualTo(Constants.SKRITTER_VOCABS_PATH);
        assertThat(request.method).isEqualTo(Constants.SKRITTER_GET_METHOD);
    }

    @Test
    public void generateBatchRequestTest() {
        ApiClient.Request request = new ApiClient.Request();
        ApiClient.ItemsParams params = new ApiClient.ItemsParams();

        params.ids_only = "true";
        params.limit = 3L;

        request.path = Constants.SKRITTER_ITEMS_PATH;
        request.method = Constants.SKRITTER_GET_METHOD;
        request.params = params;
        request.spawner = true;

        Map<String, Object> jsonWriterArgs = Map.of(
                JsonWriter.TYPE, false,
                JsonWriter.PRETTY_PRINT, true
        );
        String json = JsonWriter.objectToJson(request, jsonWriterArgs);
        System.out.println(json);

        ApiClient.Request[] requests = { request };
        json = JsonWriter.objectToJson(requests, jsonWriterArgs);
        System.out.println(json);
    }
}
