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

import com.github.paweladamski.httpclientmock.HttpClientMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ItemsTest {

    private final HttpClientMock clientMock;
    private final ApiClient apiClient;

    private final Items items;

    ItemsTest() {
        clientMock = new HttpClientMock();
        ApiClient.setHttpClient(clientMock);
        apiClient = new ApiClient();
        items = new Items(apiClient);
    }

    @BeforeEach
    void preTest() {
        clientMock.reset();
    }

    @Test
    void filterItemIdsTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);
        assertThat(itemIds).allMatch(id ->
                (id.endsWith(Constants.SKRITTER_ITEM_ID_READING_SUFFIX) ||
                        id.endsWith(Constants.SKRITTER_ITEM_ID_WRITING_SUFFIX) ||
                        id.endsWith(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX) ||
                        id.endsWith(Constants.SKRITTER_ITEM_ID_TONE_SUFFIX)));
        Set<String> pinyinOnly = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_TONE_SUFFIX));
        assertThat(pinyinOnly).allMatch(id -> id.endsWith(Constants.SKRITTER_ITEM_ID_TONE_SUFFIX));
        Set<String> rw = items.filterItemIds(itemIds,
                List.of(Constants.SKRITTER_ITEM_ID_READING_SUFFIX, Constants.SKRITTER_ITEM_ID_WRITING_SUFFIX));
        assertThat(rw).allMatch(id ->
                (id.endsWith(Constants.SKRITTER_ITEM_ID_READING_SUFFIX) ||
                        id.endsWith(Constants.SKRITTER_ITEM_ID_WRITING_SUFFIX)));
        Set<String> definition =
                items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        assertThat(definition).allMatch(id -> id.endsWith(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
    }

    @Test
    void convertItemIdsToVocabIdsTest() throws URISyntaxException, IOException {
        Set<String> itemIds = items.filterItemIds(Util.getItemIds(clientMock, apiClient),
                List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        Set<String> vocabIds = items.convertItemIdsToVocabIds(
                itemIds, Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX);
        final Set<String> idSet = new HashSet<>();
        // Test that the IDs are all unique
        vocabIds.forEach(id -> assertThat(idSet.add(id)).isTrue());

        idSet.forEach(System.out::println);
    }

    @Test
    public void itemToStringTest() throws URISyntaxException, IOException {
        List<Item> items = Parser.parseItems(Util.getJsonResource("items.json"));
        assertThat(items).hasSize(200);
        Item item = items.get(0);

        String expectedId = "234179586-zh-没关系-2-rune";
        String expectedStyle = Vocab.WritingStyle.TRADITIONAL.toSkritterString();
        String expectedVocabIds = "zh-没关系-2";

        assertThat(item.toString()).isEqualTo(
                String.join(":", expectedId, expectedStyle, expectedVocabIds));


    }

}
