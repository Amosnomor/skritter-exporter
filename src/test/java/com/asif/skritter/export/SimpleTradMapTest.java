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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTradMapTest {

    private final SimpleTradMap simpleTradMap;

    SimpleTradMapTest() throws URISyntaxException, IOException {
        HttpClientMock clientMock = new HttpClientMock();
        ApiClient.setHttpClient(clientMock);
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("get_simpletradmap_response.json"));
        simpleTradMap = new ApiClient().getSimpleTraditionalMap();
    }

    @Test
    void mapSimplifiedToTraditionalTest() {
        assertThat(simpleTradMap.convertSimplifiedToTraditional("个")).isEqualTo("個");
    }

    @Test
    void mapTraditionalToTraditionalTest() {
        assertThat(simpleTradMap.convertSimplifiedToTraditional("個")).isEqualTo("個");
    }

    @Test
    void mapEnglishToEnglishTest() {
        assertThat(simpleTradMap.convertSimplifiedToTraditional("hi mom")).isEqualTo("hi mom");
    }

    @Test
    @Disabled("the skritter map doesn't handle this correctly")
    void mapMixedSimplfiedAndTraditionalTest() {
        assertThat(simpleTradMap.convertSimplifiedToTraditional("大后天")).isEqualTo("大後天");
    }
}
