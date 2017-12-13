package com.redhat.red.build.finder;

import java.util.List;
import java.util.Map;

import com.redhat.red.build.koji.KojiClientException;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveQuery;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveType;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTagInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskRequest;

public interface ClientSession {
    List<KojiArchiveInfo> listArchives(KojiArchiveQuery query) throws KojiClientException;

    Map<String, KojiArchiveType> getArchiveTypeMap() throws KojiClientException;

    KojiBuildInfo getBuild(Integer buildId) throws KojiClientException;

    KojiTaskInfo getTaskInfo(int taskId, boolean request) throws KojiClientException;

    KojiTaskRequest getTaskRequest(int taskId) throws KojiClientException;

    List<KojiTagInfo> listTags(int id) throws KojiClientException;

    void close();
}
