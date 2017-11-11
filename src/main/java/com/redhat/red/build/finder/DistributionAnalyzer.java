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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributionAnalyzer {
    private static Logger LOGGER = LoggerFactory.getLogger(DistributionAnalyzer.class);

    private List<File> files;

    private String algorithm;

    MultiValuedMap<String, String> map;

    public DistributionAnalyzer(List<File> files, String algorithm) {
        this.files = files;
        this.algorithm = algorithm;
        this.map = new ArrayListValuedHashMap<>();
    }

    public void checksumFile(byte[] bytes, String name) {
        String checksum = Hex.encodeHexString(DigestUtils.getDigest(algorithm).digest(bytes));

        map.put(checksum, name);

        LOGGER.info("Checksum: {} {}", checksum, name);

        ArchiveInputStream ainput = null;
        byte[] toRead = null;

        try {
            CompressorInputStream cinput = null;

            try {
                cinput = new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(bytes));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(cinput, bos);
                toRead = bos.toByteArray();
                String newName = name + "!/" + name;
                checksumFile(bos.toByteArray(), newName);
            } catch (CompressorException e) {
                toRead = bytes;
            } finally {
                IOUtils.closeQuietly(cinput);
            }

            ainput = new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(toRead));
            ArchiveEntry entry = null;

            while ((entry = ainput.getNextEntry()) != null) {
                if (!ainput.canReadEntryData(entry) || entry.isDirectory()) {
                    continue;
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(ainput, bos);
                String newName = name + "!/" + entry.getName();
                checksumFile(bos.toByteArray(), newName);
            }
        } catch (ArchiveException e) {

        } catch (IOException e) {

        } finally {
            IOUtils.closeQuietly(ainput);
        }
    }

    public void checksumFiles() throws IOException {
        for (File file : files) {
            byte[] b = FileUtils.readFileToByteArray(file);
            String name = FilenameUtils.getName(file.getName());
            checksumFile(b, name);
        }
    }

    public String toJSON() {
        return JSONUtils.dumpString(map.asMap());
    }

    public boolean outputToFile(File file) {
        try {
            FileUtils.writeStringToFile(file, toJSON(), "UTF-8", false);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public MultiValuedMap<String, String> getMap() {
        return map;
    }

    public void setMap(MultiValuedMap<String, String> map) {
        this.map = map;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}
