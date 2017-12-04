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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.redhat.red.build.koji.model.xmlrpc.KojiChecksumType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DistributionAnalyzerTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void verifyDefaults() throws IOException {
        ArrayList<File> af = new ArrayList<>();
        File test = temp.newFile();
        af.add(test);

        // https://stackoverflow.com/questions/245251/create-file-with-given-size-in-java
        RandomAccessFile f = new RandomAccessFile(test, "rw");
        f.setLength(2048L * 1024L * 1024L);

        DistributionAnalyzer da = new DistributionAnalyzer(af, KojiChecksumType.md5.getAlgorithm());
        da.checksumFiles();
    }
}
