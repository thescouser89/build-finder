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
package com.redhat.red.build.finder.report;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import j2html.tags.ContainerTag;

public abstract class Report {
    private static final Logger LOGGER = LoggerFactory.getLogger(Report.class);

    private String description;

    private String baseName;

    private File outputDirectory;

    public String renderText() {
        return null;
    }

    public boolean outputText() {
        String text = renderText();

        if (text == null) {
            return false;
        }

        try {
            FileUtils.writeStringToFile(new File(outputDirectory, baseName + ".txt"), text, "UTF-8", false);
        } catch (IOException e) {
            LOGGER.error("File output error", e);
            return false;
        }

        return true;
    }

    public ContainerTag toHTML() {
        return null;
    }

    public boolean outputHTML() {
        String html = renderText();

        if (html == null) {
            return false;
        }

        try {
            FileUtils.writeStringToFile(new File(outputDirectory, baseName + ".html"), html, "UTF-8", false);
        } catch (IOException e) {
            LOGGER.error("File output error", e);
            return false;
        }

        return true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
