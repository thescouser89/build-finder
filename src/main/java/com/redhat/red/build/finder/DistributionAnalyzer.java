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
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileExtensionSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributionAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionAnalyzer.class);

    private final List<File> files;

    private final String algorithm;

    private MultiValuedMap<String, String> map;

    private final MessageDigest md;
    private final FileExtensionSelector fes;

    public DistributionAnalyzer(List<File> files, String algorithm) {
        this.files = files;
        this.algorithm = algorithm;
        this.md = DigestUtils.getDigest(algorithm);
        this.map = new ArrayListValuedHashMap<>();
        this.fes = new FileExtensionSelector("jar", "ear", "har", "jar", "par", "sar", "war", "gz", "tar", "zip", "xz", "bz2", "tgz");

        URLConnection.setFileNameMap(new NullFileNameMap());
    }

    public void checksumFiles() throws IOException {
        for (File file : files) {
            FileObject fo = VFS.getManager().resolveFile(file.getAbsolutePath());
            listChildren(fo);
        }
    }

    private void listChildren(FileObject fo) throws IOException {
        // LOGGER.debug("#### FileObject::name {} ", fo.toString());
        // LOGGER.debug("#### listKid exist {} and file {}  ", fo.exists(), fo.getContent().getFile().exists());
        FileContent fc = fo.getContent();

        if (fo.getType().getName().equals(FileType.FILE.getName())) {
            byte[] digest = DigestUtils.digest(md, fc.getInputStream());
            String checksum = Hex.encodeHexString(digest);
            map.put(checksum, fo.getName().getFriendlyURI());
            LOGGER.info("Checksum: {} {}", checksum, fo.getName().getFriendlyURI());
        }

        if (fo.getType().getName().equals(FileType.FOLDER.getName())
            || fo.getType().getName().equals(FileType.FILE_OR_FOLDER.getName())) {
            for (FileObject fileO : fo.getChildren()) {
                listChildren(fileO);
            }
        } else {
            FileObject[] archives = fo.findFiles(fes);
            for (FileObject archiveFile : archives) {
                LOGGER.debug("### Attempting to create file system for {} ", archiveFile);
                FileObject zipRoot = VFS.getManager()
                    .createFileSystem(remapExtension(archiveFile.getName().getExtension()), archiveFile);
                listChildren(zipRoot);
            }
        }
    }

    private String remapExtension(String extension) {
        // TODO: Complete normalise extension types into jar / gz / tar / zip etc.
        switch (extension) {
            case "war":
            case "ear":
            case "har":
            case "par":
            case "sar":
                return "jar";
            default:
                return extension;
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

    // Following is from https://stackoverflow.com/questions/16427142/how-to-configure-commons-vfs-to-automatically-detect-gz-files
    private static class NullFileNameMap implements FileNameMap {
        private FileNameMap delegate = URLConnection.getFileNameMap();

        @Override
        public String getContentTypeFor(String fileName) {
            String contentType = delegate.getContentTypeFor(fileName);
            if ("application/octet-stream".equals(contentType)) {
                // Sun's java classifies zip and gzip as application/octet-stream,
                // which VFS then uses, instead of looking at its extension
                // map for a more specific mime type
                return null;
            }
            return contentType;
        }
    }
}
