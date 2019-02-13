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
package com.redhat.red.build.finder.pnc.client;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.redhat.red.build.finder.pnc.client.model.Artifact;
import com.redhat.red.build.finder.pnc.client.model.BuildRecord;
import com.redhat.red.build.finder.pnc.client.model.PageParameterArtifact;
import com.redhat.red.build.finder.pnc.client.model.PageParameterBuildRecord;

/**
 * PncClient14 class for PNC 1.4.x APIs
 *
 */
public class PncClient14 {
    private String url;

    /**
     * Create a new PncClient14 object
     *
     * Default connection timeout is 10000 ms
     * Default read timeout is 60000 ms
     *
     * @param url: Base url of PNC app e.g http://orch.is.here/
     */
    public PncClient14(String url) {
        unirestSetup();
        this.url = url;
    }

    /**
     * Create a new PncClient14 object
     *
     * Default connection timeout is 10000 ms
     * Default read timeout is 60000 ms
     *
     * @param url: Base url of PNC app e.g http://orch.is.here/
     * @param connectionTimeout specify new connection timeout in milliseconds
     * @param readTimeout specify new socket/read timeout in milliseconds
     *
     */
    public PncClient14(String url, int connectionTimeout, int readTimeout) {
        this(url);
        Unirest.setTimeouts(connectionTimeout, readTimeout);
    }

    /**
     * Setup Unirest to automatically convert JSON into DTO
     */
    private static void unirestSetup() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Get a list of artifacts with matching md5. Returns empty list if no matching artifacts
     *
     * @param value md5 value
     * @return list of artifacts
     *
     * @throws PncClientException in case something goes wrong
     */
    public List<Artifact> getArtifactsByMd5(String value) throws PncClientException {
        return getArtifacts("md5", value);
    }

    /**
     * Get a list of artifacts with matching sha1. Returns empty list if no matching artifacts
     *
     * @param value sha1 value
     * @return list of artifacts
     *
     * @throws PncClientException in case something goes wrong
     */
    public List<Artifact> getArtifactsBySha1(String value) throws PncClientException {
        return getArtifacts("sha1", value);
    }

    /**
     * Get a list of artifacts with matching sha256. Returns empty list if no matching artifacts
     *
     * @param value sha256 value
     * @return list of artifacts
     *
     * @throws PncClientException in case something goes wrong
     */
    public List<Artifact> getArtifactsBySha256(String value) throws PncClientException {
        return getArtifacts("sha256", value);
    }

    private List<Artifact> getArtifacts(String key, String value) throws PncClientException {
        try {
            StringBuilder urlRequest = new StringBuilder();

            urlRequest.append(url).append("/pnc-rest/rest/artifacts").append("?").append(key).append("=").append(value);

            HttpResponse<PageParameterArtifact> artifacts = Unirest.get(urlRequest.toString()).asObject(PageParameterArtifact.class);
            PageParameterArtifact artifactData = artifacts.getBody();

            if (artifactData == null) {
                return Collections.emptyList();
            } else {
                return artifactData.getContent();
            }
        } catch (UnirestException e) {
            throw new PncClientException(e);
        }

    }

    /**
     * Get the BuildRecord object from the buildrecord id. Returns null if no buildrecord found
     *
     * @param id buildrecord id
     * @return buildrecord DTO
     *
     * @throws PncClientException if something goes wrong
     */
    public BuildRecord getBuildRecordById(int id) throws PncClientException {
        try {
            StringBuilder urlRequest = new StringBuilder();

            urlRequest.append(url).append("/pnc-rest/rest/build-records/").append(id);

            HttpResponse<PageParameterBuildRecord> buildRecords = Unirest.get(urlRequest.toString()).asObject(PageParameterBuildRecord.class);
            PageParameterBuildRecord buildRecordData = buildRecords.getBody();

            if (buildRecordData == null) {
                return null;
            } else {
                return buildRecordData.getContent();
            }

        } catch (UnirestException e) {
            throw new PncClientException(e);
        }
    }

    public String getUrl() {
        return url;
    }
}
