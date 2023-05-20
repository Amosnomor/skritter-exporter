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

import com.cedarsoftware.util.io.JsonWriter;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Manage interactions with the Skritter API
 */
public class ApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);
    private static final String BEARER_TOKEN_PROPERTY = "Bearer-Token";

    private static final long BATCH_TIMEOUT_SECONDS_DEFAULT = 180;
    private static long BATCH_TIMEOUT_SECONDS = BATCH_TIMEOUT_SECONDS_DEFAULT;
    private static final long BATCH_STATUS_SLEEP_MILLISECONDS = 250;
    private final String bearerToken;
    private static CloseableHttpClient httpClient;

    static final String ERROR_TIMED_OUT_WAITING = "Timed out waiting for ";
    static final String ERROR_MISSING_BEARER_TOKEN_PROPERTY =
            "Missing " + BEARER_TOKEN_PROPERTY + " property";

    /**
     * Constructor for use standard usage interacting with Skritter.
     * @param properties Proprerties file containing the user's API access token.
     */
    ApiClient(final Properties properties) {
        bearerToken = properties.getProperty(BEARER_TOKEN_PROPERTY);
        auditAPIProperties();
    }

    /**
     * Constructor for unit test usage with mock.
     */
    ApiClient() {
        bearerToken = "fake-token";
        auditAPIProperties();
    }

    /**
     * Accessor to unit tests to insert mock http client.
     * @param httpClient
     */
    static void setHttpClient(CloseableHttpClient httpClient) {
        ApiClient.httpClient = httpClient;
    }

    void removeBannedVocabIds(Map<String, Vocab> bannedVocabIds, Set<String> vocabIds) {

        for (String id : bannedVocabIds.keySet()) {
            LOGGER.debug("Removing banned vocab {}", id);
            vocabIds.remove(id);
        }
    }

    BatchResponse postBatchRequest(String path, Params params) throws IOException, URISyntaxException {

        LOGGER.info("Posting batch request to {}", path);

        try (final CloseableHttpClient httpclient = build()) {

            Request request = new Request();
            request.path = path;
            request.method = Constants.SKRITTER_GET_METHOD;
            request.params = params;
            request.spawner = Boolean.TRUE;

            // put batch request

            Request[] requests = { request };
            String json = JsonWriter.objectToJson(requests, Map.of(JsonWriter.TYPE, false));

            final ClassicHttpRequest httpPut = ClassicRequestBuilder.post(Constants.BATCH_ENDPOINT)
                    .addHeader(HttpHeaders.AUTHORIZATION,  getAuthorizationHeaderValue())
                    .setEntity(json, ContentType.APPLICATION_JSON)
                    .build();

            LOGGER.debug("{} to {}", httpPut.getMethod(), httpPut.getUri());

            final Result result = httpclient.execute(httpPut, response -> {
                LOGGER.debug("REQUEST -> {}", httpPut);
                LOGGER.debug("RESPONSE -> {}", new StatusLine(response));
                // Process response message and convert it into a value object
                return new Result(response);
            });

            if (result.status != HttpStatus.SC_OK) {
                throw new SkritterException("POST batch request failed. " + result);
            }

            LOGGER.debug("RESPONSE DATA -> {}", JsonWriter.formatJson(result.content));

            return Parser.parseBatchResponse(result.content);
        }
    }

    List<Vocab> getVocabs(Set<String> vocabIds) throws IOException, URISyntaxException {

        LOGGER.info("Getting Vocabs");

        List<Vocab> vocabs = new ArrayList<>();
        List<String> vocabIdList = new ArrayList<>(vocabIds);

        // Split into 100 vocab entries per batch request

        final int batchLimit = 100;
        int start = 0;
        int end;

        while (start < vocabIds.size()) {
            end = Math.min(start + batchLimit, vocabIds.size());
            List<String> batchVocabList = vocabIdList.subList(start, end);
            LOGGER.debug("Get vocab entries {} through {}", start, end);
            vocabs.addAll(doGetVocabs(batchVocabList));
            start = end;
        }

        return vocabs;
    }

    private List<Vocab> doGetVocabs(List<String> vocabIds) throws IOException, URISyntaxException {

        VocabsParams vocabsParams = new VocabsParams();
        vocabsParams.fields = String.join(",",
                Constants.SKRITTER_ID_FIELD,
                Constants.SKRITTER_STYLE_FIELD,
                Constants.SKRITTER_READING_FIELD,
                Constants.SKRITTER_WRITING_FIELD,
                Constants.SKRITTER_DEFINITIONS_FIELD,
                Constants.SKRITTER_CUSTOM_DEFINITION_FIELD);

        vocabsParams.ids = String.join("|", vocabIds);

        BatchResponse batchResponse =
                postBatchRequest(Constants.SKRITTER_VOCABS_PATH, vocabsParams);

        waitForBatchCompletion(batchResponse);

        // get responses

        List<Vocab> vocabs = new ArrayList<>();

        for (BatchRequest batchRequest : getBatchData(batchResponse.id)) {
            if (batchRequest.response != null) {
                Map<String, Object> vocabsResponseMap = batchRequest.response;

                Object[] vocabsArray = (Object[])vocabsResponseMap.get(
                        Constants.SKRITTER_VOCABS_ARRAY_NAME);
                for (Object vocabObj : vocabsArray) {
                    @SuppressWarnings("unchecked")
                    Vocab vocab = Vocab.Builder.build((Map<String, Object>) vocabObj);
                    vocabs.add(vocab);
                }
            }
        }

        return vocabs;
    }

    Map<String, Vocab> getBannedVocabs() throws IOException, URISyntaxException {

        LOGGER.info("Getting banned vocabs");
        BannedVocabsParams bannedVocabsParams = new BannedVocabsParams();
        bannedVocabsParams.sort = Constants.SKRITTER_VOCAB_SORT_BANNED_PARAMETER;

        BatchResponse batchResponse = postBatchRequest(
                Constants.SKRITTER_VOCABS_PATH, bannedVocabsParams);

        waitForBatchCompletion(batchResponse);

        // get responses
        Map<String, Vocab> vocabs = new HashMap<>();

        for (BatchRequest batchRequest : getBatchData(batchResponse.id)) {
            if (batchRequest.response != null) {
                Map<String, Object> vocabsResponseMap = batchRequest.response;

                Object[] vocabsArray = (Object[]) vocabsResponseMap.get(
                        Constants.SKRITTER_VOCABS_ARRAY_NAME);
                for (Object vocabObj : vocabsArray) {
                    @SuppressWarnings("unchecked")
                    Vocab vocab = Vocab.Builder.build((Map<String, Object>) vocabObj);
                    assert ! vocabs.containsKey(vocab.id) : "Duplicate banned vocab id: " + vocab.id;
                    vocabs.put(vocab.id, vocab);
                }
            }
        }
        return vocabs;
    }

    SimpleTradMap getSimpleTraditionalMap() throws IOException, URISyntaxException {
        LOGGER.info("Getting SimpTrad map");
        String json = getNonPaginatedData(Constants.SIMPLE_TRAD_MAP_ENDPOINT);
        return Parser.parseSimpleTradMap(json);
    }

    void waitForBatchCompletion(BatchResponse batchResponse) throws IOException, URISyntaxException {

        StringBuilder spawnedRequestIds = new StringBuilder();

        for (BatchRequest request : batchResponse.requests) {
            spawnedRequestIds.append((spawnedRequestIds.length() == 0) ? "" : ",");
            spawnedRequestIds.append(request.id);
        }

        LOGGER.info("Polling for batch completion");

        BatchResponse statusResponse = getBatchStatus(batchResponse.id, spawnedRequestIds.toString());

        // get status until done

        long loopIterationTimedOutLimit =
                TimeUnit.SECONDS.toMillis(BATCH_TIMEOUT_SECONDS) / BATCH_STATUS_SLEEP_MILLISECONDS;
        long loopIteration = 0;

        while (statusResponse.runningRequests > 0) {

            if (loopIteration++ > loopIterationTimedOutLimit) {
                throw new SkritterException(ERROR_TIMED_OUT_WAITING + statusResponse);
            }

            nap(BATCH_STATUS_SLEEP_MILLISECONDS);
            statusResponse = getBatchStatus(batchResponse.id, spawnedRequestIds.toString());
        }

        LOGGER.info("Batch is complete");
    }

    void setBatchTimeoutSeconds(long seconds) {
        BATCH_TIMEOUT_SECONDS = seconds;
    }

    void restoreBatchTimeoutSeconds() {
        BATCH_TIMEOUT_SECONDS = BATCH_TIMEOUT_SECONDS_DEFAULT;
    }



    BatchResponse getBatchStatus(String batchRequestId, String requestIds) throws IOException, URISyntaxException {

        String statusEndpoint = Constants.BATCH_ENDPOINT + "/" + batchRequestId + "/status";

        try (final CloseableHttpClient httpclient = build()) {

            final ClassicHttpRequest httpGet = ClassicRequestBuilder.get(statusEndpoint)
                    .addHeader(HttpHeaders.AUTHORIZATION,  getAuthorizationHeaderValue())
                    .addParameter(Constants.SKRITTER_REQUEST_IDS_PARAMETER, requestIds)
                    .build();

            LOGGER.debug("{} to {}", httpGet.getMethod(), httpGet.getUri());

            final Result result = httpclient.execute(httpGet, response -> {
                LOGGER.debug("REQUEST -> {}", httpGet);
                LOGGER.debug("RESPONSE -> {}", new StatusLine(response));
                // Process response message and convert it into a value object
                return new Result(response);
            });

            if (result.status != HttpStatus.SC_OK) {
                throw new SkritterException("GET batch status failed. " + result);
            }

            LOGGER.debug("RESPONSE DATA -> {}", JsonWriter.formatJson(result.content));
            return Parser.parseBatchResponse(result.content);
        }
    }

    List<BatchRequest> getBatchData(String batchRequestId) throws IOException, URISyntaxException {

        String endpoint = Constants.BATCH_ENDPOINT + "/" + batchRequestId;

        try (final CloseableHttpClient httpclient = build()) {
            final ClassicHttpRequest httpGet = ClassicRequestBuilder.get(endpoint)
                    .addHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderValue())
                    .build();

            LOGGER.debug("{} to {}", httpGet.getMethod(), httpGet.getUri());

            LOGGER.info("Getting batch data");

            final Result result = httpclient.execute(httpGet, response -> {
                LOGGER.debug("REQUEST -> {}", httpGet);
                LOGGER.debug("RESPONSE -> {}", new StatusLine(response));

                // Process response message and convert it into a value object
                return new Result(response);
            });

            if (result.status != HttpStatus.SC_OK) {
                throw new SkritterException("GET batch data failed. " + result);
            }

            LOGGER.debug("RESPONSE DATA -> {}", JsonWriter.formatJson(result.content));

            BatchResponse batchResponse = Parser.parseBatchResponse(result.content);
            return new ArrayList<>(batchResponse.requests);
        }
    }

    String getAuthorizationHeaderValue() {
        return "Bearer " + bearerToken;
    }

    String getNonPaginatedData(String endpoint) throws IOException, URISyntaxException {
        try (final CloseableHttpClient httpclient = build()) {

            final ClassicHttpRequest httpGet = ClassicRequestBuilder.get(endpoint)
                    .addHeader(HttpHeaders.AUTHORIZATION,  getAuthorizationHeaderValue())
                    .build();

            LOGGER.info("Executing request {} {}", httpGet.getMethod(), httpGet.getUri());

            final Result result = httpclient.execute(httpGet, response -> {
                LOGGER.debug("REQUEST -> {}", httpGet);
                LOGGER.debug("RESPONSE -> {}", new StatusLine(response));
                return new Result(response);
            });

            if (result.status != HttpStatus.SC_OK) {
                throw new SkritterException("GET " + endpoint + "failed. " + result);
            }

            LOGGER.debug("RESPONSE DATA -> {}", JsonWriter.formatJson(result.content));

            return result.content;
        }
    }

    static class Result {

        final int status;
        final String  content;

        Result(ClassicHttpResponse response) throws IOException, ParseException {
            status = response.getCode();
            if (status == HttpStatus.SC_OK) {
                content = EntityUtils.toString(response.getEntity());
            } else {
                content = response.getReasonPhrase();
            }
        }

        @Override
        public String toString() {
            return "Status: " + status + ", " + content;
        }
    }

    CloseableHttpClient build() {

        return ((httpClient != null) ? httpClient :
                // HttpClients.createDefault()
                // https://stackoverflow.com/a/40697322
                HttpClients.custom()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setCookieSpec(StandardCookieSpec.IGNORE).build())
                        .build());
    }

    private void auditAPIProperties() {
        if (bearerToken == null) {
            throw new SkritterException(ERROR_MISSING_BEARER_TOKEN_PROPERTY);
        }
    }

    private void nap(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {}
    }

    interface Params {

    }

    static class ItemsParams implements Params {
        String ids_only = "false";
        String include_vocabs = "false";
        Long limit = null;
        String fields;
    }

    static class VocabsParams implements Params {
        String ids;
        String fields;
    }

    static class BannedVocabsParams implements Params {
        String sort;
    }

    static class Request {
        String path;
        String method;
        Params params;
        boolean spawner;
    }
}
