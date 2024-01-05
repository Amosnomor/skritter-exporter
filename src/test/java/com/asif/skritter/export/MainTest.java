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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.*;

class MainTest {

    private static final String TEST_CLASSES_PATH = "target/test-classes";

    private final HttpClientMock clientMock;

    @BeforeEach
    void beforeEach() throws IOException {
        removeOutputFiles();
    }

    @AfterAll
    static void afterAll() throws IOException {
        removeOutputFiles();
    }

    MainTest() {
        clientMock = new HttpClientMock();
        ApiClient.setHttpClient(clientMock);
    }

    @Test
    void propertiesNotFoundTest() {
        String propertiesFile = "notGonnaBeFound.properties";

        try {
            Main.setSkritterPropertiesFile(propertiesFile);
            
            Throwable thrown = catchThrowable(() -> Main.main(new String[0]));
            assertThat(thrown).isInstanceOf(SkritterException.class);
            assertThat(thrown).hasMessage(MessageFormat.format(
                    Main.ERROR_PROPERTY_FILE_NOT_FOUND, propertiesFile));
        } finally {
            Main.restoreSkritterPropertiesFile();
        }
    }

    @Test
    void unreadablePropertiesFileTest() throws IOException {
        String propertiesFile = "cannotBeRead.properties";
        Path path = Paths.get(TEST_CLASSES_PATH, propertiesFile);
        Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_WRITE);

        try {
            Main.setSkritterPropertiesFile(propertiesFile);
            Files.createFile(path, PosixFilePermissions.asFileAttribute(perms));

            Throwable thrown = catchThrowable(() -> Main.main(new String[0]));
            assertThat(thrown).isInstanceOf(SkritterException.class);
            assertThat(thrown).hasMessageContaining(path.toString());
        } finally {
            Main.restoreSkritterPropertiesFile();
            Files.delete(path);
        }
    }

    @Test
    void mainTest() throws URISyntaxException, IOException {
        String expectedVocabLinesA =  "場\t场\tchǎng, cháng	courtyard; place; field;"
                + " (mw for games, performances, etc.); threshing floor\n"
                + "艮\t\tgěn, gèn\tblunt; straightforward; tough; chewy;"
                + " one of the Eight Trigrams, symbolizing mountain (Kangxi Radical 138)\n";
        String expectedVocabLinesB = "艮\t\tgěn, gèn\tblunt; straightforward; tough; chewy;"
                + " one of the Eight Trigrams, symbolizing mountain (Kangxi Radical 138)\n"
                + "場\t场\tchǎng, cháng	courtyard; place; field;"
                + " (mw for games, performances, etc.); threshing floor\n";

        setupMainMock();
        Main.main(new String[0]);
        String exportedData = readGeneratedFile();
        assertThat(exportedData).satisfiesAnyOf(
                s -> assertThat(s).isEqualTo(Exporter.HEADER + expectedVocabLinesA),
                s -> assertThat(s).isEqualTo(Exporter.HEADER + expectedVocabLinesB));
    }

    @Test
    void skritterExportStyleTest() throws URISyntaxException, IOException {
        try {
            String expectedVocabLinesA =
                    "场\t場\tchang3, chang2	courtyard; place; field;"
                            + " (mw for games, performances, etc.); threshing floor\n"
                            + "艮\t艮\tgen3, gen4	blunt; straightforward; tough; chewy;"
                            + " one of the Eight Trigrams, symbolizing mountain (Kangxi Radical 138)\n";
            String expectedVocabLinesB = "艮\t艮\tgen3, gen4	blunt; straightforward; tough; chewy;"
                    + " one of the Eight Trigrams, symbolizing mountain (Kangxi Radical 138)\n"
                    + "场\t場\tchang3, chang2	courtyard; place; field;"
                    + " (mw for games, performances, etc.); threshing floor\n";
            Main.setSkritterExportStyle(Exporter.ExportStyle.SKRITTER);
            setupMainMock();
            Main.main(new String[0]);
            String exportedData = readGeneratedFile();
            assertThat(exportedData).satisfiesAnyOf(
                    s -> assertThat(s).isEqualTo(expectedVocabLinesA),
                    s -> assertThat(s).isEqualTo(expectedVocabLinesB));
        } finally {
            Main.restoreExportStyle();
        }
    }

    private void setupMainMock() throws URISyntaxException, IOException {
        // getItemIds();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .withBody(containsString("\"ids_only\":\"true\""))
                .doReturnJSON(Util.getJsonResource("batch_get_item_ids.json"));
        clientMock.onGet()
                .withPath(containsString("5210785105444864/status"))
                .doReturnJSON(Util.getJsonResource("batch_get_item_ids_status.json"));
        clientMock.onGet()
                .withPath(endsWith("5210785105444864"))
                .doReturnJSON(Util.getJsonResource("batch_get_item_ids_data.json"));
        // getBannedVocabs();
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .withBody(containsString(Constants.SKRITTER_VOCAB_SORT_BANNED_PARAMETER))
                .doReturnJSON(Util.getJsonResource("batch_get_banned_vocabs.json"));
        clientMock.onGet()
                .withPath(containsString("5854829368180736/status"))
                .doReturnJSON(Util.getJsonResource("batch_get_banned_vocabs_status.json"));
        clientMock.onGet()
                .withPath(endsWith("5854829368180736"))
                .doReturnJSON(Util.getJsonResource("batch_get_banned_vocabs_data.json"));
        // getVocabs(vocabIds);
        clientMock.onPost(Constants.BATCH_ENDPOINT)
                .withBody(allOf(
                        containsString(String.join(",",
                                Constants.SKRITTER_ID_FIELD,
                                Constants.SKRITTER_STYLE_FIELD,
                                Constants.SKRITTER_READING_FIELD,
                                Constants.SKRITTER_WRITING_FIELD,
                                Constants.SKRITTER_DEFINITIONS_FIELD,
                                Constants.SKRITTER_CUSTOM_DEFINITION_FIELD)),
                        not(containsString(Constants.SKRITTER_VOCAB_SORT_BANNED_PARAMETER))))
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs.json"));
        clientMock.onGet()
                .withPath(containsString("5883192233295872/status"))
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs_status2.json"));
        clientMock.onGet()
                .withPath(endsWith("5883192233295872"))
                .doReturnJSON(Util.getJsonResource("batch_get_vocabs_data.json"));
        // getSimpleTraditionalMap();
        clientMock.onGet(Constants.SIMPLE_TRAD_MAP_ENDPOINT)
                .doReturnJSON(Util.getJsonResource("get_simpletradmap_response.json"));
    }

    private String readGeneratedFile() throws IOException {
        List<Path> generatedFiles = getGeneratedOutputFilePaths();
        assertThat(generatedFiles).hasSize(1);
        return Files.readString(generatedFiles.get(0));
    }

    private static void removeOutputFiles() throws IOException {

        for (Path p : getGeneratedOutputFilePaths()) {
            Files.delete(p);
        }
    }

    private static List<Path> getGeneratedOutputFilePaths() throws IOException {

        final List<Path> paths = new ArrayList<>();

        Files.list(Paths.get("."))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    String fileName = p.getFileName().toString();
                    if (fileName.endsWith(Constants.TABBED_DELIMITER_SUFFIX) &&
                            (fileName.startsWith(Constants.ANKI_IMPORT_PREFIX)
                                    || fileName.startsWith(Constants.SKRITTER_EXPORT_PREFIX))) {
                        paths.add(p);
                    }
                });

        return paths;
    }
}
