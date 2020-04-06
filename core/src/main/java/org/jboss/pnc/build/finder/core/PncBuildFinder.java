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

import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import io.vertx.core.impl.ConcurrentHashSet;
import org.jboss.pnc.build.finder.koji.KojiBuild;
import org.jboss.pnc.build.finder.pnc.EnhancedArtifact;
import org.jboss.pnc.build.finder.pnc.PncBuild;
import org.jboss.pnc.build.finder.pnc.client.PncClient;
import org.jboss.pnc.build.finder.pnc.client.PncUtils;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.ArtifactQuality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.jboss.pnc.build.finder.core.AnsiUtils.green;

/**
 * Build Finder for PNC
 *
 * @author Jakub Bartecek
 */
public class PncBuildFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PncBuildFinder.class);

    private final PncClient pncClient;

    private final BuildFinderUtils buildFinderUtils;

    public PncBuildFinder(PncClient pncClient, BuildFinderUtils buildFinderUtils) {
        this.pncClient = pncClient;
        this.buildFinderUtils = buildFinderUtils;
    }

    // FIXME solve the exception inside the method. Do not throw it
    public Map<BuildSystemInteger, KojiBuild> findBuildsPnc(Map<Checksum, Collection<String>> checksumTable)
            throws RemoteResourceException {
        if (checksumTable == null || checksumTable.isEmpty()) {
            LOGGER.warn("Checksum table is empty");
            return Collections.emptyMap();
        }

        Set<EnhancedArtifact> artifacts = lookupArtifactsInPnc(checksumTable);

        ConcurrentMap<String, PncBuild> pncBuilds = groupArtifactsAsPncBuilds(artifacts);

        populatePncBuildsMetadata(pncBuilds);

        ConcurrentMap<BuildSystemInteger, KojiBuild> foundBuilds = convertPncBuildsToKojiBuilds(pncBuilds);

        // TODO Add caching to the pncclient - create a CachingPncClient
        // TODO - move caching to the PncClient
        // TODO - cache both not found entries and found entries

        return foundBuilds;
    }

    private ConcurrentMap<BuildSystemInteger, KojiBuild> convertPncBuildsToKojiBuilds(Map<String, PncBuild> pncBuilds) {
        // TODO switch to parallel execution
        ConcurrentMap<BuildSystemInteger, KojiBuild> foundBuilds = new ConcurrentHashMap<>();
        pncBuilds.values().forEach((pncBuild -> {
            KojiBuild kojiBuild = convertPncBuildToKojiBuild(pncBuild);
            foundBuilds.put(new BuildSystemInteger(kojiBuild.getBuildInfo().getId()), kojiBuild);
        }));
        return foundBuilds;
    }

    private void populatePncBuildsMetadata(ConcurrentMap<String, PncBuild> pncBuilds) throws RemoteResourceException {
        // FIXME should not throw exception
        // TODO Switch to parallel execution
        for (PncBuild pncBuild : pncBuilds.values()) {
            Build build = pncBuild.getBuild();
            pncBuild.setProductVersion(pncClient.getProductVersion(build.getProductMilestone()));
            pncBuild.setBuildPushResult(pncClient.getBuildPushResult(build.getId()));
        }
    }

    private Set<EnhancedArtifact> lookupArtifactsInPnc(Map<Checksum, Collection<String>> checksumTable) {
        // TODO Switch to parallel execution
        Set<EnhancedArtifact> artifacts = new ConcurrentHashSet<>();
        checksumTable.entrySet().forEach((entry) -> {
            try {
                Artifact artifact = findArtifactInPnc(entry.getKey(), entry.getValue());

                if (artifact != null) {
                    EnhancedArtifact enhancedArtifact = new EnhancedArtifact(
                            artifact,
                            entry.getKey(),
                            entry.getValue());
                    artifacts.add(enhancedArtifact);
                }
            } catch (RemoteResourceException e) {
                LOGGER.warn("Communication with PNC failed! ", e);
            }
        });
        return artifacts;
    }

    /**
     * A build produces multiple artifacts. This methods associates all the artifacts to the one PncBuild
     *
     * @param artifacts All found artifacts
     * @return A map pncBuildId,pncBuild
     */
    private ConcurrentMap<String, PncBuild> groupArtifactsAsPncBuilds(Set<EnhancedArtifact> artifacts) {
        ConcurrentMap<String, PncBuild> pncBuilds = new ConcurrentHashMap<>();
        artifacts.forEach((artifact) -> {
            Build build = artifact.getArtifact().getBuild();

            if (build != null) {
                if (pncBuilds.containsKey(build.getId())) {
                    pncBuilds.get(build.getId()).getArtifacts().add(artifact);
                } else {
                    PncBuild pncBuild = new PncBuild(build);
                    pncBuild.getArtifacts().add(artifact);
                    pncBuilds.put(build.getId(), pncBuild);
                }
            } else {
                // FIXME solve case when artifact is not associated with any build
                artifact.getArtifact();
            }
        });
        return pncBuilds;
    }

    private Artifact findArtifactInPnc(Checksum checksum, Collection<String> fileNames) throws RemoteResourceException {
        if (buildFinderUtils.shouldSkipChecksum(checksum, fileNames)) {
            LOGGER.debug("Skipped checksum {} for fileNames {}", checksum, fileNames);
            return null;
        }
        LOGGER.debug("PNC: checksum={}", checksum);

        // Lookup Artifacts and associated builds in PNC
        Collection<Artifact> artifacts = lookupPncArtifactsByChecksum(checksum);
        if (artifacts == null || artifacts.isEmpty()) {
            // FIXME Solve the case when an artifact is not available in PNC
            checksum.getType();
        }

        // FIXME Review logic of choosing the best artifact
        return getBestPncArtifact(artifacts);
    }

    private Collection<Artifact> lookupPncArtifactsByChecksum(Checksum checksum) throws RemoteResourceException {
        switch (checksum.getType()) {
            case md5:
                return pncClient.getArtifactsByMd5(checksum.getValue()).getAll();

            case sha1:
                return pncClient.getArtifactsBySha1(checksum.getValue()).getAll();

            case sha256:
                return pncClient.getArtifactsBySha256(checksum.getValue()).getAll();

            default:
                throw new IllegalArgumentException(
                        "Unsupported checksum type requested! " + "Checksum type " + checksum.getType()
                                + " is not supported.");
        }
    }

    private int getArtifactQuality(Object obj) {
        Artifact a = (Artifact) obj;
        ArtifactQuality quality = a.getArtifactQuality();

        switch (quality) {
            case NEW:
                return 1;
            case VERIFIED:
                return 2;
            case TESTED:
                return 3;
            case DEPRECATED:
                return -1;
            case BLACKLISTED:
                return -2;
            case TEMPORARY:
                return -3;
            case DELETED:
                return -4;
            default:
                LOGGER.warn("Unsupported ArtifactQuality! Got: " + quality);
                return 0;
        }
    }

    private Artifact getBestPncArtifact(Collection<Artifact> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) {
            throw new IllegalArgumentException("No artifacts provided!");
        }

        if (artifacts.size() == 1) {
            return artifacts.iterator().next();
        } else {
            return artifacts.stream()
                    .sorted(Comparator.comparing(this::getArtifactQuality).reversed())
                    .filter(artifact -> artifact.getBuild() != null)
                    .findFirst()
                    .orElse(artifacts.iterator().next());
        }
    }

    private KojiBuild convertPncBuildToKojiBuild(PncBuild pncBuild) {
        KojiBuild kojibuild = PncUtils.pncBuildToKojiBuild(pncBuild);

        for (EnhancedArtifact artifact : pncBuild.getArtifacts()) {

            KojiArchiveInfo kojiArchive = PncUtils.artifactToKojiArchiveInfo(pncBuild, artifact.getArtifact());
            PncUtils.fixNullVersion(kojibuild, kojiArchive);
            buildFinderUtils.addArchiveToBuild(kojibuild, kojiArchive, artifact.getFilenames());

            // TODO review logic of buildZero - is it needed?
            // KojiBuild buildZero = builds.get(new BuildSystemInteger(0, BuildSystem.none));
            //
            // buildZero.getArchives()
            // .removeIf(
            // a -> a.getChecksums()
            // .stream()
            // .anyMatch(
            // c -> c.getType().equals(checksum.getType())
            // && c.getValue().equals(checksum.getValue())));

            LOGGER.info(
                    "Found build in Pnc: id: {} nvr: {} checksum: ({}) {} archive: {}",
                    green(pncBuild.getBuild().getId()),
                    green(PncUtils.getNVRFromBuildRecord(pncBuild.getBuild())),
                    green(artifact.getChecksum().getType()),
                    green(artifact.getChecksum().getValue()),
                    green(artifact.getFilenames()));
        }

        return kojibuild;
    }

}
