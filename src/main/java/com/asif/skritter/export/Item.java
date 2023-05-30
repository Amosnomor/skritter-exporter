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

public class Item {
    String id;
    String lang;
    String style;
    long changed;
    long last;
    long created;
    long successes;
    long timeStudied;
    long interval;
    String[] sectionIds;
    long next;
    long reviews;
    long previousInterval;
    String part;
    String[] vocabListIds;
    String[] vocabIds;
    boolean previousSuccess;

    private Item() {
    }

    // Helper class to construct an Item from a json-io map
    static class Builder {
        private Builder() {
        }

        static Item build(Map<String, Object> itemMap) {
            Item item = new Item();
            item.id = (String)itemMap.get(Constants.SKRITTER_ID_FIELD);
            item.lang = (String)itemMap.get("lang");
            item.style = (String)itemMap.get("style");
            item.changed = getLongValue(itemMap.get("changed"));
            item.last = getLongValue(itemMap.get("last"));
            item.created = getLongValue(itemMap.get(Constants.SKRITTER_CREATED_FIELD));
            item.successes = getLongValue(itemMap.get("successes"));
            item.timeStudied = getLongValue(itemMap.get("timeStudied"));
            item.interval = getLongValue(itemMap.get("interval"));
            item.next = getLongValue(itemMap.get("next"));
            item.reviews = getLongValue(itemMap.get("reviews"));
            item.previousInterval = getLongValue(itemMap.get("previousInterval"));
            item.part = (String)itemMap.get("part");
            item.previousSuccess = getBooleanValue(itemMap.get("previousSuccess"));
            //FIX THIS, DS: populate.  skipped because not needed for anki import
//            item.sectionIds = (String[])itemMap.get("sectionIds");
            //FIX THIS, DS: populate.  skipped because not needed for anki import
//            String vocabListIds[];
            Object[] vocabIdsArray = (Object[])itemMap.get(Constants.SKRITTER_VOCAB_IDS_FIELD);
            if (vocabIdsArray != null) {
                int size = vocabIdsArray.length;
                item.vocabIds = new String[size];
                for (int i = 0; i < size; i++) {
                    item.vocabIds[i] = (String)vocabIdsArray[i];
                }
            }


            return item;
        }

        private static long getLongValue(Object longElement) {
            return longElement == null ? 0 : (Long)longElement;
        }

        private static boolean getBooleanValue(Object booleanElement) {
            return booleanElement != null && (Boolean) booleanElement;
        }
    }

    @Override
    public String toString() {
        return String.join(":", id, style, String.join(", ", vocabIds));
    }
}
