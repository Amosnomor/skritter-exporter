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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_SKRITTER_PROPERTIES_FILE = "skritter.properties";
    private static String SKRITTER_PROPERTIES_FILE = DEFAULT_SKRITTER_PROPERTIES_FILE;

    private static final Exporter.ExportStyle DEFAULT_EXPORT_STYLE = Exporter.ExportStyle.ANKI;
    private static Exporter.ExportStyle EXPORT_STYLE = DEFAULT_EXPORT_STYLE;

    static final String ERROR_PROPERTY_FILE_NOT_FOUND = "Required properties file {0} cannot be found";

    public static void main(String[] args) throws URISyntaxException, IOException {

        // Load  properties
        Properties skritterProperties = loadProperties();

        ApiClient apiClient = new ApiClient(skritterProperties);
        Items items = new Items(apiClient);

        // Get item ids for all studied words
        Set<String> itemIds = items.getItemIds();
        // Remove all but the rune versions
        itemIds = items.filterItemIds(itemIds, List.of(Constants.SKRITTER_ITEM_ID_WRITING_SUFFIX));
        // Convert them to vocab ids
        Set<String> vocabIds =
                items.convertItemIdsToVocabIds(itemIds, Constants.SKRITTER_ITEM_ID_WRITING_SUFFIX);
        // Fetch banned words
        Map<String, Vocab> bannedVocabs = apiClient.getBannedVocabs();
        // Remove banned words from the list
        apiClient.removeBannedVocabIds(bannedVocabs, vocabIds);
        // Fetch all of the remaining vocabs
        List<Vocab> vocabs = apiClient.getVocabs(vocabIds);

        // Download the simple to traditional map
        SimpleTradMap simpleTradMap = apiClient.getSimpleTraditionalMap();
        Exporter.ExportStyle exportStyle = EXPORT_STYLE;
        // Export the data
        String exportData = new Exporter(simpleTradMap, vocabs).export(exportStyle);
        createImportFile(exportStyle, exportData);
    }

    static void createImportFile(Exporter.ExportStyle exportStyle, String exportData) throws IOException {

        String fileName;

        if (exportStyle == Exporter.ExportStyle.ANKI) {
            fileName = Constants.ANKI_IMPORT_PREFIX;
        } else {
            assert exportStyle == Exporter.ExportStyle.SKRITTER : "unknown ExportStyle: " + exportStyle;
            fileName = Constants.SKRITTER_EXPORT_PREFIX;
        }

        fileName = generateFileName(fileName, Constants.TABBED_DELIMITER_SUFFIX);

        Path filePath = Paths.get(fileName);
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        Files.writeString(filePath, exportData);

    }

    static void setSkritterPropertiesFile(String fileName) {
        SKRITTER_PROPERTIES_FILE = fileName;
    }

    static void restoreSkritterPropertiesFile() {
        SKRITTER_PROPERTIES_FILE = DEFAULT_SKRITTER_PROPERTIES_FILE;
    }

    static void setSkritterExportStyle(Exporter.ExportStyle exportStyle) {
        EXPORT_STYLE = exportStyle;
    }

    static void restoreExportStyle() {
        EXPORT_STYLE = DEFAULT_EXPORT_STYLE;
    }

    static String generateFileName(String fileName, String suffix) {
        String timestamp = ZonedDateTime.now(ZoneId.systemDefault()).
                format(DateTimeFormatter.ofPattern("uuMMdd-HHmm-ss"));
        return fileName + '-' + timestamp + suffix;
    }

    static Properties loadProperties() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL propertiesFile = classLoader.getResource(SKRITTER_PROPERTIES_FILE);

        if (propertiesFile == null) {
            throw new SkritterException(MessageFormat.format(
                    ERROR_PROPERTY_FILE_NOT_FOUND, SKRITTER_PROPERTIES_FILE));
        }

        Properties properties = new Properties();

        //noinspection EmptyFinallyBlock
        try (InputStream is = propertiesFile.openStream()) {
            properties.load(is);
        }
        catch (IOException e) {
            throw new SkritterException("Failed loading properties file",  e);
        }
        finally {
        }

        return properties;
    }
}
