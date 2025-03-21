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
package org.jboss.pnc.build.finder.core.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.commonjava.o11yphant.metrics.util.NameUtils.name;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections4.MultiValuedMap;
import org.commonjava.o11yphant.metrics.api.Timer;
import org.commonjava.o11yphant.metrics.api.Timer.Context;
import org.jboss.pnc.build.finder.core.BuildFinder;
import org.jboss.pnc.build.finder.core.BuildSystemInteger;
import org.jboss.pnc.build.finder.core.ChecksumType;
import org.jboss.pnc.build.finder.core.DistributionAnalyzer;
import org.jboss.pnc.build.finder.core.LocalFile;
import org.jboss.pnc.build.finder.core.Utils;
import org.jboss.pnc.build.finder.koji.ClientSession;
import org.jboss.pnc.build.finder.koji.KojiBuild;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class KojiBuildFinderIT extends AbstractKojiIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KojiBuildFinderIT.class);

    private static final String PROPERTY = "distribution.url";

    private static final String URL = System.getProperty(PROPERTY);

    @Test
    void testChecksumsAndFindBuilds(@TempDir Path folder) throws Exception {
        assertThat(URL)
                .as("You must set the property %s pointing to the URL of the distribution to test with", PROPERTY)
                .isNotNull();

        Timer timer = REGISTRY.timer(name(KojiBuildFinderIT.class, "checksums"));
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            DistributionAnalyzer analyzer;

            Future<Map<ChecksumType, MultiValuedMap<String, LocalFile>>> futureChecksum;

            try (Context ignored = timer.time()) {
                analyzer = new DistributionAnalyzer(Collections.singletonList(URL), getConfig());
                futureChecksum = pool.submit(analyzer);
            }

            Timer timer2 = REGISTRY.timer(name(KojiBuildFinderIT.class, "builds"));

            try (Context ignored = timer2.time()) {
                ClientSession session = getSession();
                BuildFinder finder = new BuildFinder(session, getConfig(), analyzer, null, getPncClient());
                finder.setOutputDirectory(folder);
                Future<Map<BuildSystemInteger, KojiBuild>> futureBuilds = pool.submit(finder);
                Map<BuildSystemInteger, KojiBuild> builds = futureBuilds.get();
                Map<ChecksumType, MultiValuedMap<String, LocalFile>> map = futureChecksum.get();

                assertThat(getConfig().getChecksumTypes()).hasSizeGreaterThanOrEqualTo(1);
                assertThat(map).hasSize(getConfig().getChecksumTypes().size());
                assertThat(builds).hasSizeGreaterThanOrEqualTo(1);

                LOGGER.info("Map size: {}", map.size());
                LOGGER.info("Builds size: {}", builds.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        } finally {
            Utils.shutdownAndAwaitTermination(pool);
        }
    }
}
