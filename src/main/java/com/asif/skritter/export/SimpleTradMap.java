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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTradMap {

    private final Map<String, MapEntry> map = new HashMap<>();

    private SimpleTradMap() {

    }

    int numEntries() {
        return map.size();
    }

    String convertSimplifiedToTraditional(String simplified) {
        // Break the input into a String for each character
        String[] characters = simplified.codePoints()
                .mapToObj(Character::toString)
                .toArray(String[]::new);

        StringBuilder traditional = new StringBuilder();

        for (String simpleChar : characters) {
            MapEntry entry = map.get(simpleChar);

            if (entry == null) {
                traditional.append(simpleChar);
            } else {
                traditional.append(entry.getFirstMapping(simpleChar));
            }
        }

        return traditional.toString();
    }

    int getNumMappings(String simplified) {
        MapEntry mapEntry = map.get(simplified);
        return (mapEntry == null) ? 0 : mapEntry.numMappings();
    }

    interface MapEntry {
        String getFirstMapping(String simplifiedChar);
        int numMappings();
    }

    record OneToOne(String traditional) implements MapEntry {

        @Override
            public String getFirstMapping(String simplifiedChar) {
                return traditional;
            }

            @Override
            public int numMappings() {
                return 1;
            }
        }

    static class OneToMany implements MapEntry {

        final List<String> traditionalMappings = new ArrayList<>();

        @Override
        public String getFirstMapping(String simplifiedChar) {
            assert traditionalMappings.size() > 0;
            return traditionalMappings.get(0);
        }

        @Override
        public int numMappings() {
            return traditionalMappings.size();
        }
    }

    // Helper to construct a SimpleTradMap from a json-io map
    record Builder(Map<String, Object> mapEntries) {

        SimpleTradMap build() {
                SimpleTradMap simpleTradMap = new SimpleTradMap();

                mapEntries.forEach((k, v) -> {
                    assert !simpleTradMap.map.containsKey(k) : "Unexpected duplicate key: " + k;

                    if (v instanceof String) {
                        simpleTradMap.map.put(k, new OneToOne((String) v));
                    }
                    else {
                        OneToMany oneToMany = new OneToMany();
                        assert v instanceof Object[] : "Expected Object[], got: " + v;
                        assert ((Object[]) v).length > 0 : "Empty mappings array";
                        for (Object obj : (Object[]) v) {
                            assert obj instanceof String;
                            oneToMany.traditionalMappings.add((String) obj);
                        }
                        simpleTradMap.map.put(k, oneToMany);
                    }
                });
                return simpleTradMap;
            }
        }
}
