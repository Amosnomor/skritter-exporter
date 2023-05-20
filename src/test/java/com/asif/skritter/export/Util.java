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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

public class Util {
    static String getJsonResource(String fileName) throws URISyntaxException, IOException {
        return Files.readString(Paths.get(Objects.requireNonNull(
                Util.class.getClassLoader().getResource(fileName)).toURI()));
    }

    static Set<String> getItemIds(HttpClientMock clientMock, ApiClient apiClient)
            throws URISyntaxException, IOException {
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("batch_get_item_ids.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_item_ids_status.json"));
        clientMock.onGet()
                .doReturnJSON(Util.getJsonResource("batch_get_item_ids_data.json"));

        return new Items(apiClient).getItemIds();
    }
}
