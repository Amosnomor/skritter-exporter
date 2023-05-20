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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Items {

    private static final Logger LOGGER = LoggerFactory.getLogger(Items.class);

    private final ApiClient apiClient;

    Items(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    Set<String> getItemIds() throws URISyntaxException, IOException {
        return getItemIds(null);
    }

    Set<String> getItemIds(Long limit) throws URISyntaxException, IOException {

        LOGGER.info("Getting Item IDs");
        ApiClient.ItemsParams itemsParams = new ApiClient.ItemsParams();
        itemsParams.ids_only = "true";
        itemsParams.limit = limit;
        Set<String> itemIds = new HashSet<>();

        BatchResponse batchResponse = apiClient.postBatchRequest(
                Constants.SKRITTER_ITEMS_PATH, itemsParams);

        apiClient.waitForBatchCompletion(batchResponse);

        // get responses

        for (BatchRequest batchRequest : apiClient.getBatchData(batchResponse.id)) {
            if (batchRequest.response != null) {
                Map<String, Object> itemsResponseMap = batchRequest.response;

                Object[] itemIdsArray = (Object[]) itemsResponseMap.get(
                        Constants.SKRITTER_ITEMS_ARRAY_NAME);
                for (Object itemIdObj : itemIdsArray) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemIdMap = (Map<String, Object>) itemIdObj;
                    String id = (String) itemIdMap.get(Constants.SKRITTER_ID_FIELD);

                    if (!itemIds.add(id)) {
                        LOGGER.warn("Duplicate item id: {}", id);
                    }
                }
            }
        }
        return itemIds;
    }

    Set<String> filterItemIds(Set<String> itemIds, List<String> wantedSuffixes) {
        final Set<String> results = new HashSet<>();

        wantedSuffixes.forEach(suffix -> itemIds.forEach(id -> {
            if (id.endsWith("-" + suffix)) {
                results.add(id);
            }
        }));

        return results;
    }

    Set<String> convertItemIdsToVocabIds(Set<String> itemIds, String suffix) {
        return convertItemIdsToVocabIds(itemIds, List.of(suffix));
    }


    Set<String> convertItemIdsToVocabIds(Set<String> itemIds, List<String> suffixes) {
        final Set<String> vocabIds = new HashSet<>();

        for (String suffix : suffixes) {
            // Validate date
            String regex = "^[0-9]*-(zh-.*-[0-9])-" + suffix + "$";
            Pattern pattern = Pattern.compile(regex);

            for (String itemId : itemIds) {
                Matcher matcher = pattern.matcher(itemId);
                if (!matcher.find()) {
                    throw new SkritterException("itemId not matched from " + itemId);
                }

                vocabIds.add(matcher.group(1));
            }
        }

        return vocabIds;
    }
}
