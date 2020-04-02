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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BuildFinderTest {
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private File target;

    @Before
    public void setTarget() throws IOException {
        this.target = new File(TestUtils.resolveFileResource("./", "").getParentFile().getParentFile(), "pom.xml");
    }

    @Test
    public void verifyDirectory() throws IOException {
        File folder = temp.newFolder();
        ChecksumType checksumType = ChecksumType.sha1;
        BuildConfig config = new BuildConfig();

        config.setChecksumTypes(EnumSet.of(checksumType));
        config.setOutputDirectory(folder.getAbsolutePath());

        DistributionAnalyzer da = new DistributionAnalyzer(
                Collections.singletonList(new File(target.getAbsolutePath())),
                config);
        da.checksumFiles();
        da.outputToFile(checksumType);

        File[] file = folder.listFiles();

        assertTrue(file != null && file.length == 1);
        assertEquals(file[0].getCanonicalPath(), da.getChecksumFile(checksumType).getCanonicalPath());
    }

    @Test
    public void verifyloadChecksumsFile() throws IOException {
        File folder = temp.newFolder();
        ChecksumType checksumType = ChecksumType.md5;

        BuildConfig config = new BuildConfig();

        config.setChecksumTypes(EnumSet.of(checksumType));
        config.setOutputDirectory(folder.getAbsolutePath());

        DistributionAnalyzer da = new DistributionAnalyzer(
                Collections.singletonList(new File(target.getAbsolutePath())),
                config);
        da.checksumFiles();
        da.outputToFile(checksumType);

        Map<String, Collection<String>> checksums = JSONUtils.loadChecksumsFile(da.getChecksumFile(checksumType));

        assertEquals(1, checksums.size());
    }
}
