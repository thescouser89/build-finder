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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.redhat.red.build.koji.KojiClient;
import com.redhat.red.build.koji.KojiClientException;
import com.redhat.red.build.koji.config.KojiConfig;
import com.redhat.red.build.koji.config.SimpleKojiConfigBuilder;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveQuery;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveType;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildQuery;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildType;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildTypeInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildTypeQuery;
import com.redhat.red.build.koji.model.xmlrpc.KojiImageBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiSessionInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTagInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskRequest;
import com.redhat.red.build.koji.model.xmlrpc.KojiWinBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.messages.GetArchiveTypeRequest;
import org.commonjava.util.jhttpc.auth.MemoryPasswordManager;
import org.commonjava.util.jhttpc.auth.PasswordManager;

public class KojiClientSession extends KojiClient {
    private static final int DEFAULT_THREAD_COUNT = 1;
    private KojiSessionInfo session;

    public KojiClientSession(KojiConfig config, PasswordManager passwordManager, ExecutorService executorService) {
        super(config, passwordManager, executorService);
    }

    public KojiClientSession(String url) throws KojiClientException {
        super(new SimpleKojiConfigBuilder().withKojiURL(url).build(), new MemoryPasswordManager(), Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT));
    }

    public KojiClientSession(String url, String krbService, String krbPrincipal, String krbPassword, String krbCCache, String krbKeytab) throws KojiClientException {
        super(new SimpleKojiConfigBuilder().withKojiURL(url).withKrbService(krbService).withKrbCCache(krbCCache).withKrbKeytab(krbKeytab).withKrbPrincipal(krbPrincipal).withKrbPassword(krbPassword).build(), new MemoryPasswordManager(), Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT));

        if (krbService != null) {
            if ((krbPrincipal != null && krbPassword != null) || krbCCache != null || krbKeytab != null) {
                session = login();
            }
        }
    }

    public List<KojiArchiveInfo> listArchives(KojiArchiveQuery query) throws KojiClientException {
        return listArchives(query, session);
    }

    public KojiArchiveType getArchiveType(GetArchiveTypeRequest request) throws KojiClientException {
        return getArchiveType(request, session);
    }

    public Map<String, KojiArchiveType> getArchiveTypeMap() throws KojiClientException {
        return getArchiveTypeMap(session);
    }

    public KojiBuildInfo getBuild(Integer buildId) throws KojiClientException {
        return getBuildInfo(buildId, session);
    }

    public KojiTaskInfo getTaskInfo(int taskId) throws KojiClientException {
        return getTaskInfo(taskId, session);
    }

    public KojiTaskInfo getTaskInfo(int taskId, boolean request) throws KojiClientException {
        return getTaskInfo(taskId, request, session);
    }

    public KojiTaskRequest getTaskRequest(int taskId) throws KojiClientException {
        return getTaskRequest(taskId, session);
    }

    public KojiImageBuildInfo getImageBuild(int buildId) throws KojiClientException {
        return getImageBuildInfo(buildId, session);
    }

    public KojiWinBuildInfo getWinBuild(int buildId) throws KojiClientException {
        return getWinBuildInfo(buildId, session);
    }

    public List<KojiBuildType> listBTypes() throws KojiClientException {
        return listBuildTypes(session);
    }

    public List<KojiBuildType> listBTypes(KojiBuildTypeQuery query) throws KojiClientException {
        return listBuildTypes(query, session);
    }

    public List<KojiBuildInfo> listBuilds(KojiBuildQuery query) throws KojiClientException {
        return listBuilds(query, session);
    }

    public KojiBuildTypeInfo getBuildType(int id) throws KojiClientException {
        return getBuildTypeInfo(id, session);
    }

    public List<KojiTagInfo> listTags(int id) throws KojiClientException {
        return listTags(id, session);
    }
}
