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
package org.jboss.pnc.build.finder.koji;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.commonjava.o11yphant.metrics.api.MetricRegistry;
import org.commonjava.util.jhttpc.auth.MemoryPasswordManager;
import org.commonjava.util.jhttpc.auth.PasswordManager;

import com.redhat.red.build.koji.KojiClient;
import com.redhat.red.build.koji.KojiClientException;
import com.redhat.red.build.koji.KojiClientHelper;
import com.redhat.red.build.koji.config.KojiConfig;
import com.redhat.red.build.koji.config.SimpleKojiConfigBuilder;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveQuery;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveType;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildTypeInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiIdOrName;
import com.redhat.red.build.koji.model.xmlrpc.KojiRpmInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiSessionInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTagInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskRequest;
import com.redhat.red.build.koji.model.xmlrpc.messages.Constants;

public final class KojiClientSession extends KojiClient implements ClientSession {
    private static final int DEFAULT_MAX_CONNECTIONS = 13;

    private static final int DEFAULT_THREAD_COUNT = 1;

    private static final int DEFAULT_TIMEOUT = (int) TimeUnit.MINUTES.toSeconds(15L);

    private KojiSessionInfo session;

    private final KojiClientHelper helper;

    public KojiClientSession(KojiConfig config, PasswordManager passwordManager, ExecutorService executorService)
            throws KojiClientException {
        super(config, passwordManager, executorService);
        helper = new KojiClientHelper(this);
    }

    public KojiClientSession(
            KojiConfig config,
            PasswordManager passwordManager,
            ExecutorService executorService,
            MetricRegistry metricRegistry) throws KojiClientException {
        super(config, passwordManager, executorService, metricRegistry);
        helper = new KojiClientHelper(this);
    }

    public KojiClientSession(URL url, ExecutorService executorService) throws KojiClientException {
        super(
                new SimpleKojiConfigBuilder().withMaxConnections(DEFAULT_MAX_CONNECTIONS)
                        .withTimeout(DEFAULT_TIMEOUT)
                        .withConnectionPoolTimeout(DEFAULT_TIMEOUT)
                        .withKojiURL(url.toExternalForm())
                        .build(),
                new MemoryPasswordManager(),
                executorService);
        helper = new KojiClientHelper(this);
    }

    public KojiClientSession(URL url) throws KojiClientException {
        this(url, Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT));
    }

    public KojiClientSession(
            URL url,
            String krbService,
            String krbPrincipal,
            String krbPassword,
            Path krbCCache,
            Path krbKeytab) throws KojiClientException {
        super(
                new SimpleKojiConfigBuilder().withMaxConnections(DEFAULT_MAX_CONNECTIONS)
                        .withTimeout(DEFAULT_TIMEOUT)
                        .withConnectionPoolTimeout(DEFAULT_TIMEOUT)
                        .withKojiURL(url != null ? url.toExternalForm() : null)
                        .withKrbService(krbService)
                        .withKrbCCache(krbCCache != null ? krbCCache.toAbsolutePath().toString() : null)
                        .withKrbKeytab(krbKeytab != null ? krbKeytab.toAbsolutePath().toString() : null)
                        .withKrbPrincipal(krbPrincipal)
                        .withKrbPassword(krbPassword)
                        .build(),
                new MemoryPasswordManager(),
                Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT));
        session = login();
        helper = new KojiClientHelper(this);
    }

    @Override
    public List<KojiArchiveInfo> listArchives(KojiArchiveQuery query) throws KojiClientException {
        return listArchives(query, session);
    }

    @Override
    public Map<String, KojiArchiveType> getArchiveTypeMap() throws KojiClientException {
        return getArchiveTypeMap(session);
    }

    @Override
    public KojiBuildInfo getBuild(int buildId) throws KojiClientException {
        return getBuildInfo(buildId, session);
    }

    @Override
    public KojiTaskInfo getTaskInfo(int taskId, boolean request) throws KojiClientException {
        return getTaskInfo(taskId, request, session);
    }

    @Override
    public KojiTaskRequest getTaskRequest(int taskId) throws KojiClientException {
        return getTaskRequest(taskId, session);
    }

    @Override
    public List<KojiTagInfo> listTags(int id) throws KojiClientException {
        return listTags(id, session);
    }

    @Override
    public void enrichArchiveTypeInfo(List<KojiArchiveInfo> archiveInfos) throws KojiClientException {
        enrichArchiveTypeInfo(archiveInfos, session);
    }

    @Override
    public List<List<KojiArchiveInfo>> listArchives(List<KojiArchiveQuery> queries) throws KojiClientException {
        return helper.listArchives(queries, session);
    }

    @Override
    public List<KojiBuildInfo> getBuild(List<KojiIdOrName> idsOrNames) throws KojiClientException {
        int size = idsOrNames.size();
        List<Object> args = new ArrayList<>(size);

        for (KojiIdOrName idOrName : idsOrNames) {
            Integer id = idOrName.getId();
            String name = idOrName.getName();

            if (id != null) {
                args.add(id);
            } else if (name != null) {
                args.add(name);
            } else {
                throw new KojiClientException("Invalid KojiIdOrName: " + idOrName);
            }
        }

        List<KojiBuildInfo> buildInfos = multiCall(Constants.GET_BUILD, args, KojiBuildInfo.class, session);

        if (buildInfos.isEmpty()) {
            return buildInfos;
        }

        List<KojiBuildTypeInfo> buildTypeInfos = multiCall(
                Constants.GET_BUILD_TYPE,
                args,
                KojiBuildTypeInfo.class,
                session);

        if (buildInfos.size() != buildTypeInfos.size()) {
            throw new KojiClientException("Sizes must be equal");
        }

        Iterator<KojiBuildInfo> it = buildInfos.iterator();
        Iterator<KojiBuildTypeInfo> it2 = buildTypeInfos.iterator();

        while (it.hasNext()) {
            KojiBuildTypeInfo.addBuildTypeInfo(it2.next(), it.next());
        }

        return buildInfos;
    }

    @Override
    public List<KojiRpmInfo> getRPM(List<KojiIdOrName> idsOrNames) throws KojiClientException {
        int size = idsOrNames.size();
        List<Object> args = new ArrayList<>(size);

        for (KojiIdOrName idOrName : idsOrNames) {
            Integer id = idOrName.getId();
            String name = idOrName.getName();

            if (id != null) {
                args.add(id);
            } else if (name != null) {
                args.add(name);
            } else {
                throw new KojiClientException("Invalid KojiIdOrName: " + idOrName);
            }
        }

        return multiCall(Constants.GET_RPM, args, KojiRpmInfo.class, session);
    }

    @Override
    public List<List<KojiRpmInfo>> listBuildRPMs(List<KojiIdOrName> idsOrNames) throws KojiClientException {
        return helper.listBuildRPMs(idsOrNames, session);
    }

    @Override
    public List<KojiTaskInfo> getTaskInfo(List<Integer> taskIds, List<Boolean> requests) throws KojiClientException {
        int taskIdsSize = taskIds.size();
        List<Object> args = new ArrayList<>(taskIdsSize);

        for (int i = 0; i < taskIdsSize; i++) {
            args.add(Collections.unmodifiableList(Arrays.asList(taskIds.get(i), requests.get(i))));
        }

        return multiCall(Constants.GET_TASK_INFO, Collections.unmodifiableList(args), KojiTaskInfo.class, session);
    }

    @Override
    public List<List<KojiTagInfo>> listTags(List<KojiIdOrName> idsOrNames) throws KojiClientException {
        List<Integer> buildIds = idsOrNames.stream().map(KojiIdOrName::getId).toList();
        return helper.listTagsByIds(buildIds, session);
    }

    public KojiSessionInfo getSession() {
        return session;
    }
}
