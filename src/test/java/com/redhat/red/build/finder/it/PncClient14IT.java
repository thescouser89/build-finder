package com.redhat.red.build.finder.it;

import com.redhat.red.build.finder.pnc.client.PncClient14;
import com.redhat.red.build.finder.pnc.client.PncClientException;
import com.redhat.red.build.finder.pnc.client.models.Artifact;
import com.redhat.red.build.finder.pnc.client.models.BuildRecord;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PncClient14IT {

    private static final String PROPERTY = "com.redhat.red.build.finder.it.pnc.url";
    private static final String URL = System.getProperty(PROPERTY);


    private static final int CONNECTION_TIMEOUT = 300000;
    private static final int READ_TIMEOUT = 900000;

    @Test
    public void testDefaultPncClient14() throws PncClientException {
        PncClient14 client = new PncClient14(URL);
        getAnArtifactAndBuildRecord(client);
    }

    @Test
    public void testPncClient14WithTimeouts() throws PncClientException {
        PncClient14 client = new PncClient14(URL, CONNECTION_TIMEOUT, READ_TIMEOUT);
        getAnArtifactAndBuildRecord(client);
    }

    @Test
    public void testReturnEmptyListIfNoMatchingSha() throws PncClientException {

        PncClient14 client = new PncClient14(URL);

        List<Artifact> artifactsMd5 = client.getArtifactsByMd5("do-not-exist");
        assertNotNull(artifactsMd5);
        assertTrue(artifactsMd5.isEmpty());

        List<Artifact> artifactsSha1 = client.getArtifactsBySha1("do-not-exist");
        assertNotNull(artifactsSha1);
        assertTrue(artifactsSha1.isEmpty());

        List<Artifact> artifactsSha256 = client.getArtifactsBySha256("do-not-exist");
        assertNotNull(artifactsSha256);
        assertTrue(artifactsSha256.isEmpty());
    }

    @Test
    public void testReturnNullIfNoMatchingBuildRecord() throws PncClientException {

        PncClient14 client = new PncClient14(URL);
        BuildRecord record = client.getBuildRecordById(-1);
        assertNull(record);

    }


    private void getAnArtifactAndBuildRecord(PncClient14 client) throws PncClientException {

        // sha content for classworlds-1.1.jar
        String md5 = "c20629baa65f1f2948b37aa393b0310b";
        String sha1 = "60c708f55deeb7c5dfce8a7886ef09cbc1388eca";
        String sha256 = "4e3e0ad158ec60917e0de544c550f31cd65d5a97c3af1c1968bf427e4a9df2e4";

        Artifact artifactMd5 = client.getArtifactsByMd5(md5).get(0);
        Artifact artifactSha1 = client.getArtifactsBySha1(sha1).get(0);
        Artifact artifactSha256 = client.getArtifactsBySha256(sha256).get(0);

        // make sure that all the shas point to the same artifact
        assertTrue(artifactMd5.getId().equals(artifactSha1.getId()));
        assertTrue(artifactSha1.getId().equals(artifactSha256.getId()));

        // get the buildrecord and see if we get all the contents
        List<Integer> buildRecordIds = artifactMd5.getDependantBuildRecordIds();

        BuildRecord record = client.getBuildRecordById(buildRecordIds.get(0));
        assertNotNull(record);
        assertNotNull(record.getProjectName());
    }
}
