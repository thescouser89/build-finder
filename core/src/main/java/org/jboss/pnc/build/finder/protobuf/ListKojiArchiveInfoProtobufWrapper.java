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
package org.jboss.pnc.build.finder.protobuf;

import java.util.ArrayList;
import java.util.List;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;

/**
 * Class to wrap around a List of KojiArchiveInfo. This is used so that Protostream can properly marshall / unmarshall
 * the list and avoid this Protostream bug (https://issues.redhat.com/browse/IPROTO-219).
 */
public class ListKojiArchiveInfoProtobufWrapper {
    /**
     * We cannot use Collections.emptyList because Protostream has no idea how to marshall it. Instead, we use an empty
     * ArrayList.
     */
    private List<KojiArchiveInfo> data = new ArrayList<>(0);

    public ListKojiArchiveInfoProtobufWrapper() {

    }

    @ProtoFactory
    public ListKojiArchiveInfoProtobufWrapper(List<KojiArchiveInfo> data) {
        this.data = data;
    }

    @ProtoField(1)
    public List<KojiArchiveInfo> getData() {
        return data;
    }
}
