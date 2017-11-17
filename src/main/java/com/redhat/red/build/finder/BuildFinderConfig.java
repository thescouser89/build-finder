/**
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.red.build.finder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import com.redhat.red.build.koji.model.xmlrpc.KojiChecksumType;
import com.virtlink.commons.configuration2.jackson.JsonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildFinderConfig {
    private static final String CONFIG_FILENAME = "config.json";

    private static final Map<String, Object> OPTIONS = new LinkedHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildFinder.class);

    static {
            OPTIONS.put("checksum-only", false);
            OPTIONS.put("checksum-type", KojiChecksumType.md5.name());
            OPTIONS.put("archive-types", new String[] {"jar", "xml", "pom", "so", "dll", "dylib"});
            OPTIONS.put("excludes", new String[] {"^(?!.*/pom\\.xml$).*/.*\\.xml$"});
            OPTIONS.put("koji-hub-url", "http://kojihub.my.host/kojihub");
            OPTIONS.put("koji-web-url", "https://kojiweb.my.host/koji");
    }

    private JsonConfiguration config;

    private FileBasedConfigurationBuilder<JsonConfiguration> builder = new FileBasedConfigurationBuilder<>(JsonConfiguration.class);

    public BuildFinderConfig() {
        Parameters params = new Parameters();

        try {
            Path path = Paths.get(CONFIG_FILENAME);
            boolean createDefaultConfig = !Files.exists(path);

            File configFile = new File(CONFIG_FILENAME);

            if (createDefaultConfig) {
                config = new JsonConfiguration();
            } else {
                config = builder.configure(params.fileBased().setFile(configFile)).getConfiguration();
            }

            OPTIONS.forEach((key, value) -> {
                if (!config.containsKey(key)) {
                    config.setProperty(key, value);
                }
            });

            if (createDefaultConfig) {
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8))) {
                    config.write(writer);
                }
            }

            LOGGER.debug("Using configuration {}", OPTIONS);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object getDefaultValue(String key) {
        return OPTIONS.get(key);
    }

    public boolean getBoolean(String key) {
        return config.getBoolean(key, (boolean) OPTIONS.get(key));
    }

    public String getString(String key) {
        return config.getString(key, (String) OPTIONS.get(key));
    }

    public List<String> getStringList(String key) {
        return config.getList(String.class, key, Arrays.asList((String[]) OPTIONS.get(key)));
    }

    public String getFilename() {
        return builder.getFileHandler().getPath();
    }

    public void setProperty(String key, Object value) {
        config.setProperty(key, value);
    }

    public void save() {
        try {
            builder.save();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
}
