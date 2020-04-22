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

import org.jboss.pnc.client.RemoteCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Static implementation of RemoteCollection, which loads all the remote content and stores it inside the entity
 *
 * @author Jakub Bartecek
 */
public class StaticRemoteCollection<T> implements RemoteCollection<T> {
    private Collection<T> staticCollection;

    public StaticRemoteCollection(RemoteCollection<T> remoteCollection) {
        this.staticCollection = Collections.unmodifiableCollection(remoteCollection.getAll());
    }

    public StaticRemoteCollection(Collection<T> collection) {
        this.staticCollection = collection;
    }

    @Override
    public int size() {
        return staticCollection.size();
    }

    @Override
    public Collection<T> getAll() {
        return staticCollection;
    }

    @Override
    public Iterator<T> iterator() {
        return staticCollection.iterator();
    }
}
