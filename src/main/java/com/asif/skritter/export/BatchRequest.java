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

import java.util.Map;

public class BatchRequest {
    String id;
    Long created;
    Long spawnedBy;
    Long done;
    Map<String, Object> params;
    String path;
    String method;
    Map<String, Object> response;

    private BatchRequest() {
    }

    record Builder(Map<String, Object> batchRequestMap) {

        BatchRequest build() {
            BatchRequest batchRequest = new BatchRequest();
            batchRequest.id = (String) batchRequestMap.get(Constants.SKRITTER_ID_FIELD);
            //noinspection unchecked
            batchRequest.params = (Map<String, Object>)
                    batchRequestMap.get(Constants.SKRITTER_PARAMS_OBJECT_NAME);
            batchRequest.path = (String) batchRequestMap.get(Constants.SKRITTER_PATH_FIELD);
            batchRequest.method = (String) batchRequestMap.get(Constants.SKRITTER_METHOD_FIELD);
            batchRequest.created = (Long) batchRequestMap.get(Constants.SKRITTER_CREATED_FIELD);
            batchRequest.spawnedBy = (Long) batchRequestMap.get(Constants.SKRITTER_SPAWNED_BY_FIELD);

            Object doneObject = batchRequestMap.get(Constants.SKRITTER_DONE_FIELD);
            if (doneObject instanceof Boolean) {
                batchRequest.done = 0L;
            } else {
                batchRequest.done = (Long) batchRequestMap.get(Constants.SKRITTER_DONE_FIELD);
            }

            Object responseObj = batchRequestMap.get(Constants.SKRITTER_RESPONSE_FIELD);
            if (responseObj != null) {
                if (responseObj instanceof String) {
                    assert ((String) responseObj).isEmpty()
                            : ((String) responseObj);
                } else {
                    assert responseObj instanceof Map : responseObj.getClass();
                    //noinspection unchecked
                    batchRequest.response = (Map<String, Object>)
                            batchRequestMap.get(Constants.SKRITTER_RESPONSE_FIELD);
                }
            }

            return batchRequest;
        }
    }
}
