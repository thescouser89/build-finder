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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.redhat.red.build.koji.model.xmlrpc.KojiChecksumType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

public class DistributionAnalyzerTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void verifySize() throws IOException {
        ArrayList<File> af = new ArrayList<>();
        File test = temp.newFile();
        af.add(test);

        RandomAccessFile f = new RandomAccessFile(test, "rw");
        f.setLength(FileUtils.ONE_GB * 2);

        DistributionAnalyzer da = new DistributionAnalyzer(af, KojiChecksumType.md5.getAlgorithm());
        da.checksumFiles();
    }

    @Test
    public void loadNestedZIP() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("nested.zip").getFile());

        List<File> target = Collections.singletonList(file);
        DistributionAnalyzer da = new DistributionAnalyzer(target, KojiChecksumType.md5.getAlgorithm());
        da.checksumFiles();

        int result = StringUtils.countMatches(systemOutRule.getLog(), "Checksum");
        assertTrue(result == 25);
        assertFalse(systemOutRule.getLog().contains("zip:zip:file:"));
        assertFalse(systemOutRule.getLog().contains("target/test-classes"));
    }

    @Test
    public void loadNestedWAR() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("nested.war").getFile());

        List<File> target = Collections.singletonList(file);
        DistributionAnalyzer da = new DistributionAnalyzer(target, KojiChecksumType.md5.getAlgorithm());
        da.checksumFiles();

        int result = StringUtils.countMatches(systemOutRule.getLog(), "Checksum");
        assertTrue(result == 7);
    }
}
