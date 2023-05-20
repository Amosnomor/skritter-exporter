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

public class Constants {
    public static final String SKRITTER_ID_FIELD = "id";
    public static final String SKRITTER_CREATED_FIELD = "created";
    public static final String SKRITTER_TOTAL_REQUESTS_FIELD = "totalRequests";
    public static final String SKRITTER_RUNNING_REQUESTS_FIELD = "runningRequests";
    public static final String SKRITTER_PATH_FIELD = "path";
    public static final String SKRITTER_METHOD_FIELD = "method";
    public static final String SKRITTER_STATUS_CODE_FIELD = "statusCode";
    public static final String SKRITTER_SPAWNED_BY_FIELD = "spawnedBy";
    public static final String SKRITTER_RESPONSE_FIELD = "response";
    public static final String SKRITTER_STYLE_FIELD = "style";
    public static final String SKRITTER_WRITING_FIELD = "writing";
    public static final String SKRITTER_READING_FIELD = "reading";
    public static final String SKRITTER_DEFINITIONS_FIELD = "definitions";
    public static final String SKRITTER_CUSTOM_DEFINITION_FIELD = "customDefinition";
    public static final String SKRITTER_BANNED_PARTS_FIELD = "bannedParts";
    public static final String SKRITTER_VOCAB_IDS_FIELD = "vocabIds";
    public static final String SKRITTER_DONE_FIELD = "done";
    public static final String SKRITTER_REQUESTS_ARRAY_NAME = "Requests";
    public static final String SKRITTER_ITEMS_ARRAY_NAME = "Items";
    public static final String SKRITTER_VOCABS_ARRAY_NAME = "Vocabs";
    public static final String SKRITTER_SIMPLE_TRAD_MAP_OBJECT_NAME = "SimpTradMap";
    public static final String SKRITTER_PARAMS_OBJECT_NAME = "params";
    public static final String SKRITTER_IDS_PARAMETER = "ids";
    public static final String SKRITTER_FIELDS_PARAMETER = "fields";
    public static final String SKRITTER_VOCAB_SORT_BANNED_PARAMETER = "banned";
    public static final String SKRITTER_REQUEST_IDS_PARAMETER = "request_ids";
    public static final String SKRITTER_VOCAB_OBJECT_NAME = "Vocab";
    public static final String SKRITTER_BATCH_OBJECT_NAME = "Batch";
    public static final String SKRITTER_ITEM_ID_DEFINITION_SUFFIX = "defn";
    public static final String SKRITTER_ITEM_ID_WRITING_SUFFIX = "rune";
    public static final String SKRITTER_ITEM_ID_READING_SUFFIX = "rdng";
    public static final String SKRITTER_ITEM_ID_TONE_SUFFIX = "tone";

    public static final String SKRITTER_ITEMS_PATH = "api/v0/items";
    public static final String SKRITTER_VOCABS_PATH = "api/v0/vocabs";
    public static final String SKRITTER_GET_METHOD = "GET";
    public static final String ENDPOINT_BASE = "https://skritter.com/api/v0";
    public static final String BATCH_ENDPOINT = ENDPOINT_BASE + "/batch";
    public static final String VOCABS_ENDPOINT = ENDPOINT_BASE + "/vocabs";
    public static final String ITEMS_ENDPOINT = ENDPOINT_BASE + "/items";
    public static final String SIMPLE_TRAD_MAP_ENDPOINT = ENDPOINT_BASE + "/simptradmap";

    public static final String SKRITTER_LANGUAGE_ENGLISH = "en";

    public static final String SKRITTER_WRITING_STYLE_SIMPLE = "simp";
    public static final String SKRITTER_WRITING_STYLE_TRADITIONAL = "trad";
    public static final String SKRITTER_WRITING_STYLE_BOTH = "both";
    public static final String ANKI_IMPORT_PREFIX = "anki_import";
    public static final String SKRITTER_EXPORT_PREFIX = "skritter_export";
    public static final String TABBED_DELIMITER_SUFFIX = ".tsv";
}
