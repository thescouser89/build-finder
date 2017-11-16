package com.redhat.red.build.finder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

        // System.out.println ("Found debug " + debug);
        assertTrue (debug.size() > 0);
    }
}