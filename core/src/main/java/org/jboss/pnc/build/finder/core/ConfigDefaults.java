/*
 * Copyright (C) 2017 Red Hat, Inc.
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
package org.jboss.pnc.build.finder.core;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public abstract class ConfigDefaults {
    public static final List<String> ARCHIVE_TYPES = Collections
            .unmodifiableList(Arrays.asList("jar", "xml", "pom", "so", "dll", "dylib"));
    public static final List<String> ARCHIVE_EXTENSIONS = Collections.unmodifiableList(
            Arrays.asList(
                    "dll",
                    "dylib",
                    "ear",
                    "jar",
                    "jdocbook",
                    "jdocbook-style",
                    "kar",
                    "plugin",
                    "pom",
                    "rar",
                    "sar",
                    "so",
                    "war",
                    "xml"));
    public static final List<BuildSystem> BUILD_SYSTEMS = Collections
            .unmodifiableList(Arrays.asList(BuildSystem.pnc, BuildSystem.koji));
    public static final Long CACHE_LIFESPAN = TimeUnit.HOURS.toMillis(1L);
    public static final Long CACHE_MAX_IDLE = TimeUnit.HOURS.toMillis(1L);
    public static final Boolean CHECKSUM_ONLY = Boolean.FALSE;
    public static final Set<ChecksumType> CHECKSUM_TYPES = Collections
            .unmodifiableSet(EnumSet.allOf(ChecksumType.class));
    public static final String CONFIG_FILE = "config.json";
    public static final String CONFIG_PATH = FileUtils.getUserDirectoryPath() + File.separator + ".build-finder";
    public static final String CONFIG = CONFIG_PATH + File.separator + CONFIG_FILE;
    public static final Boolean DISABLE_CACHE = Boolean.FALSE;
    public static final Boolean DISABLE_RECURSION = Boolean.FALSE;
    public static final List<Pattern> EXCLUDES = Collections
            .unmodifiableList(Collections.singletonList(Pattern.compile("^(?!.*/pom\\.xml$).*/.*\\.xml$")));
    public static final URL KOJI_HUB_URL = null;
    public static final Integer KOJI_MULTICALL_SIZE = 8;
    public static final Integer KOJI_NUM_THREADS = 12;
    public static final URL KOJI_WEB_URL = null;
    public static final String OUTPUT_DIR = ".";
    public static final Integer PNC_PARTITION_SIZE = 18;
    public static final URL PNC_URL = null;
    public static final Boolean USE_BUILDS_FILE = Boolean.FALSE;
    public static final Boolean USE_CHECKSUMS_FILE = Boolean.FALSE;

    private ConfigDefaults() {

    }
}
