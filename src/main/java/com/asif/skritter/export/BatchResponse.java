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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchResponse {
    String id;
    Long totalRequests;
    Long created;
    Long runningRequests;
    final List<BatchRequest> requests = new ArrayList<>();

    private BatchResponse() {

    }

    @Override
    public String toString() {
        return "BatchResponse id:" + id
                + ", totalRequests:" + totalRequests
                + ", runningRequests:" + runningRequests;
    }

    record Builder(Map<String, Object> batchResponseMap) {

        BatchResponse build() {
                BatchResponse batchResponse = new BatchResponse();
                batchResponse.id = (String) batchResponseMap.get(Constants.SKRITTER_ID_FIELD);
                batchResponse.totalRequests = (Long) batchResponseMap.get(Constants.SKRITTER_TOTAL_REQUESTS_FIELD);
                batchResponse.runningRequests = (Long) batchResponseMap.get(Constants.SKRITTER_RUNNING_REQUESTS_FIELD);
                batchResponse.created = (Long) batchResponseMap.get(Constants.SKRITTER_CREATED_FIELD);

                Object[] requestsArray = (Object[]) batchResponseMap.get(Constants.SKRITTER_REQUESTS_ARRAY_NAME);

                if (requestsArray != null) {
                    for (Object requestObject : requestsArray) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> requestMap = (Map<String, Object>) requestObject;
                        batchResponse.requests.add(new BatchRequest.Builder(requestMap).build());
                    }
                }
                return batchResponse;
            }
        }
}
