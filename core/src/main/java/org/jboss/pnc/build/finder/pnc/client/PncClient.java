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
package org.jboss.pnc.build.finder.pnc.client;

import org.jboss.pnc.build.finder.core.BuildConfig;
import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.ProductMilestoneClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductVersion;

/**
 * Adapter to communicate with PNC Orchestrator REST API
 *
 * @author Jakub Bartecek
 */
public class PncClient {

    private final BuildClient buildClient;

    private final ArtifactClient artifactClient;

    private final ProductVersionClient productVersionClient;

    private final ProductMilestoneClient productMilestoneClient;

    public PncClient(BuildConfig config) {
        Configuration.ConfigurationBuilder configurationBuilder = Configuration.builder();

        configurationBuilder.protocol(config.getPncURL().getProtocol());
        configurationBuilder.host(config.getPncURL().getHost());
        configurationBuilder.port(config.getPncURL().getPort());
        configurationBuilder.pageSize(config.getPncPartitionSize());
        Configuration clientConfiguration = configurationBuilder.build();

        artifactClient = new ArtifactClient(clientConfiguration);
        productVersionClient = new ProductVersionClient(clientConfiguration);
        productMilestoneClient = new ProductMilestoneClient(clientConfiguration);
        buildClient = new BuildClient(clientConfiguration);
    }

    /**
     * Get a list of artifacts with matching md5. Returns empty list if no matching artifacts
     *
     * @param md5 md5 value
     * @return list of artifacts
     *
     * @throws RemoteResourceException Thrown in case something goes wrong
     */
    public RemoteCollection<Artifact> getArtifactsByMd5(String md5) throws RemoteResourceException {
        return artifactClient.getAll(null, md5, null);
    }

    /**
     * Get a list of artifacts with matching sha1. Returns empty list if no matching artifacts
     *
     * @param sha1 sha1 value
     * @return list of artifacts
     *
     * @throws RemoteResourceException Thrown in case something goes wrong
     */
    public RemoteCollection<Artifact> getArtifactsBySha1(String sha1) throws RemoteResourceException {
        return artifactClient.getAll(null, null, sha1);
    }

    /**
     * Get a list of artifacts with matching sha256. Returns empty list if no matching artifacts
     *
     * @param sha256 sha256 value
     * @return list of artifacts
     *
     * @throws PncClientException Thrown in case something goes wrong
     */
    public RemoteCollection<Artifact> getArtifactsBySha256(String sha256) throws RemoteResourceException {
        return artifactClient.getAll(sha256, null, null);
    }

    public BuildPushResult getBuildPushResult(String buildId) throws RemoteResourceException {
        return buildClient.getPushResult(buildId);
    }

    public ProductVersion getProductVersion(ProductMilestoneRef productMilestoneRef)
            throws RemoteResourceException {
        ProductMilestone productMilestone = productMilestoneClient.getSpecific(productMilestoneRef.getId());
        return productVersionClient.getSpecific(productMilestone.getProductVersion().getId());
    }
}
