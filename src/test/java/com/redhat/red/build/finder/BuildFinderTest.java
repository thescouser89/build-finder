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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class BuildFinderTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void verifyDebug() throws Exception {
        // Currently the configuration does not allow a full path to the configuration files to be set.
        ProcessBuilder builder = new ProcessBuilder
            ("java", "-cp", System.getProperty("java.class.path"), "com.redhat.red.build.finder.BuildFinder", "-d", "test").
            directory(temp.newFolder()).
            redirectErrorStream(true);
        Process p = builder.start();

        List<String> debug = new BufferedReader(new InputStreamReader(p.getInputStream())).lines().filter( s -> s.contains("DEBUG")).collect(Collectors.toList());
        p.waitFor();

        System.out.println ("Found debug " + debug);
        assertTrue (debug.size() > 0);
    }


    @Test
    public void verifyDirectory() throws Exception
    {
        File target = new File (TestUtils.resolveFileResource( "./", "" )
                                          .getParentFile().getParentFile(), "pom.xml" );
        File folder = temp.newFolder();
        try
        {
            BuildFinder.main( new String [] { "-k", "-o", folder.getAbsolutePath(), target.getAbsolutePath() } );
        }
        finally
        {
            new File("config.json").delete();
        }

        File[] f = folder.listFiles();
        assertTrue( f != null && f.length == 1 );
        assertTrue( f[0].getName().equals("checksums-md5.json"));
    }
}