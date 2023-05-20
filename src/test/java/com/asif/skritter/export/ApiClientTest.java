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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;


class ApiClientTest {

    private final HttpClientMock clientMock;
    private final ApiClient apiClient;

    ApiClientTest() {
        clientMock = new HttpClientMock();
        ApiClient.setHttpClient(clientMock);
        apiClient = new ApiClient();
    }

    @BeforeEach
    void preTest() {
        clientMock.reset();
    }

    @Test
    void missingAccessTokenPropertyTest() {
        Properties properties = new Properties();
        Throwable thrown = catchThrowable(() -> new ApiClient(properties));
        assertThat(thrown).isInstanceOf(SkritterException.class);
        assertThat(thrown).hasMessage(ApiClient.ERROR_MISSING_BEARER_TOKEN_PROPERTY);
    }

    @Test
    void getItemIdsTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);
        assertThat(itemIds).hasSize(20);
    }

    @Test
    void getVocabIdsTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);

        Items items = new Items(apiClient);
        itemIds = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        Set<String> vocabIds =
                items.convertItemIdsToVocabIds(itemIds, Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX);

        clientMock.reset();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs_status1.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs_status2.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs_data.json"));

        List<Vocab> vocabs = apiClient.getVocabs(vocabIds);
        assertThat(vocabs).hasSize(2);
    }

    @Test
    void getBannedVocabsTest() throws URISyntaxException, IOException {
        clientMock.reset();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("batch_get_banned_vocabs.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_banned_vocabs_status.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_banned_vocabs_data.json"));

        Map<String, Vocab> vocabs = apiClient.getBannedVocabs();
        assertThat(vocabs).hasSize(5);
        assertThat(vocabs).containsOnlyKeys(
                "zh-一只小猫-2", "zh-马老师-1", "zh-几-2", "zh-立-0", "zh-后天-0");
    }

    @Test
    void getSimpleTradMapTest() throws URISyntaxException, IOException {
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("get_simpletradmap_response.json"));

        SimpleTradMap simpleTradMap = apiClient.getSimpleTraditionalMap();
        assertThat(simpleTradMap.numEntries()).isEqualTo(2623);
        assertThat(simpleTradMap.getNumMappings("个")).isEqualTo(2);
        assertThat(simpleTradMap.getNumMappings("国")).isEqualTo(1);
    }

    @Test
    void batchTimeoutTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);

        Items items = new Items(apiClient);
        itemIds = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        Set<String> vocabIds =
                items.convertItemIdsToVocabIds(itemIds, Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX);

        clientMock.reset();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs_status1.json"));

        try {
            apiClient.setBatchTimeoutSeconds(1);
            Throwable thrown = catchThrowable(() -> apiClient.getVocabs(vocabIds));
            assertThat(thrown).isInstanceOf(SkritterException.class);
            assertThat(thrown).hasMessageStartingWith(ApiClient.ERROR_TIMED_OUT_WAITING);
        } finally {
            apiClient.restoreBatchTimeoutSeconds();
        }
    }

    @Test
    void batchResponseErrorTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);

        Items items = new Items(apiClient);
        itemIds = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        Set<String> vocabIds =
                items.convertItemIdsToVocabIds(itemIds, Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX);

        clientMock.reset();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("authorization_failure.json"));
        Throwable thrown = catchThrowable(() -> apiClient.getVocabs(vocabIds));
        assertThat(thrown).isInstanceOf(SkritterException.class);
        assertThat(thrown).hasMessageContaining("AuthenticationException");
        assertThat(thrown).hasMessageContaining("401");
        assertThat(thrown).hasMessageContaining("User authorization required.");
    }

    @Test
    void batchPutFailedTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);

        Items items = new Items(apiClient);
        itemIds = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        Set<String> vocabIds =
                items.convertItemIdsToVocabIds(itemIds, Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX);
        String errorMessage = "yada yada and yet more yada";

        clientMock.reset();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnWithStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);
        Throwable thrown = catchThrowable(() -> apiClient.getVocabs(vocabIds));
        assertThat(thrown).isInstanceOf(SkritterException.class);
        assertThat(thrown).hasMessageContaining(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(thrown).hasMessageContaining(errorMessage);
    }

    @Test
    void batchStatusFailedTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);

        Items items = new Items(apiClient);
        itemIds = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        Set<String> vocabIds =
                items.convertItemIdsToVocabIds(itemIds, Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX);
        String errorMessage = "yada yada and yet more yada";

        clientMock.reset();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs.json"));
        clientMock.onGet()
                .doReturnWithStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);
        Throwable thrown = catchThrowable(() -> apiClient.getVocabs(vocabIds));
        assertThat(thrown).isInstanceOf(SkritterException.class);
        assertThat(thrown).hasMessageContaining(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(thrown).hasMessageContaining(errorMessage);
    }

    @Test
    void batchDataFailedTest() throws URISyntaxException, IOException {
        Set<String> itemIds = Util.getItemIds(clientMock, apiClient);

        Items items = new Items(apiClient);
        itemIds = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX));
        Set<String> vocabIds =
                items.convertItemIdsToVocabIds(itemIds, Constants.SKRITTER_ITEM_ID_DEFINITION_SUFFIX);
        String errorMessage = "yada yada and yet more yada";

        clientMock.reset();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs.json"));
        clientMock.onGet()
                .withPath(containsString("5883192233295872/status"))
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs_status2.json"));
        clientMock.onGet()
                .withPath(endsWith("5883192233295872"))
                .doReturnWithStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);
        Throwable thrown = catchThrowable(() -> apiClient.getVocabs(vocabIds));
        assertThat(thrown).isInstanceOf(SkritterException.class);
        assertThat(thrown).hasMessageContaining(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(thrown).hasMessageContaining(errorMessage);
    }

    @Test
    void nonPaginatedDataFailsTest() {
        String errorMessage = "yada yada and yet more yada";
        clientMock.onGet()
                .doReturnWithStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorMessage);
        Throwable thrown = catchThrowable(apiClient::getSimpleTraditionalMap);
        assertThat(thrown).isInstanceOf(SkritterException.class);
        assertThat(thrown).hasMessageContaining(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(thrown).hasMessageContaining(errorMessage);
    }
}
