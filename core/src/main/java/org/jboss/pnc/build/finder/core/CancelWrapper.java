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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper object that will let code know whether it is cancelled and stop running. Object uses AtomicBoolean and is
 * therefore Thread safe.
 *
 * It is very similar to the idea of the interrupt signal for a thread when a future / thread is cancelled. However, the
 * interrupt signal doesn't work for CompletableFutures, only Futures.
 */
public class CancelWrapper {

    private AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * Set cancel "signal" to true.
     */
    public void cancel() {
        this.cancelled.set(true);
    }

    /**
     * Check whether the cancel "signal" is set to true.
     *
     * @return whether it is cancelled
     */
    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Checks if cancel is set to true. If yes, throw a new CancelException to stop everything
     */
    public void checkIfWeNeedToStop() {

        if (cancelled.get()) {
            throw new CancelException();
        }
    }
}
