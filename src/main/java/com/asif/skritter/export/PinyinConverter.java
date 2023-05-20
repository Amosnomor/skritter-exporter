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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// From Ezequiel Santiago Sánchez
// https://stackoverflow.com/a/56557489
//
public class PinyinConverter {

    private final Map<String, String> pinyinToneMarks = new HashMap<>();
    private final Pattern pattern;

    PinyinConverter() {
        pinyinToneMarks.put("a", "āáǎà");
        pinyinToneMarks.put("e", "ēéěè");
        pinyinToneMarks.put("i", "īíǐì");
        pinyinToneMarks.put("o", "ōóǒò");
        pinyinToneMarks.put("u", "ūúǔù");
        pinyinToneMarks.put("ü", "ǖǘǚǜ");
        pinyinToneMarks.put("A", "ĀÁǍÀ");
        pinyinToneMarks.put("E", "ĒÉĚÈ");
        pinyinToneMarks.put("I", "ĪÍǏÌ");
        pinyinToneMarks.put("O", "ŌÓǑÒ");
        pinyinToneMarks.put("U", "ŪÚǓÙ");
        pinyinToneMarks.put("Ü", "ǕǗǙǛ");
        pattern = Pattern.compile("([aeiouüvÜ]{1,3})(n?g?r?)([012345])");
    }

    private int getTonePosition(String r) {
        String lowerCase = r.toLowerCase();

        // exception to the rule
        if (lowerCase.equals("ou")) {
            return 0;
        }

        // higher precedence, both never go together
        int preferencePosition = lowerCase.indexOf('a');
        if (preferencePosition >= 0) {
            return preferencePosition;
        }
        preferencePosition = lowerCase.indexOf('e');
        if (preferencePosition >= 0) {
            return preferencePosition;
        }

        // otherwise the last one takes the tone mark
        return lowerCase.length() - 1;
    }

    public String getCharacter(String string, int position) {
        char[] characters = string.toCharArray();
        return String.valueOf(characters[position]);
    }

    public String toPinyin(String asciiPinyin) {
        Matcher matcher = pattern.matcher(asciiPinyin);
        StringBuilder s = new StringBuilder();
        int start = 0;

        while (matcher.find(start)) {
            s.append(asciiPinyin, start, matcher.start(1));
            int tone = Integer.parseInt(matcher.group(3)) % 5;
            String r = matcher.group(1).replace("v", "ü").replace("V", "Ü");
            if (tone != 0) {
                int pos = getTonePosition(r);
                s.append(r, 0, pos).append(getCharacter(
                        pinyinToneMarks.get(getCharacter(r, pos)),tone - 1)).append(r, pos + 1, r.length());
            } else {
                s.append(r);
            }
            s.append(matcher.group(2));
            start = matcher.end(3);
        }
        if (start != asciiPinyin.length()) {
            s.append(asciiPinyin, start, asciiPinyin.length());
        }
        return s.toString();
    }
}
