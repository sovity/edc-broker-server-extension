/*
 *  Copyright (c) 2023 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */

package de.sovity.edc.ext.brokerserver.services.queue;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;

public class ConnectorQueue {
    private final PriorityBlockingQueue<ConnectorQueueEntry> queue = new PriorityBlockingQueue<>();

    /**
     * Get the next item. Waits by blocking current thread.
     *
     * @return the next item
     * @throws InterruptedException on thread interruption
     */
    public String take() throws InterruptedException {
        return queue.take().getEndpoint();
    }

    /**
     * Enqueues connectors for update.
     *
     * @param endpoints connector endpoints
     * @param priority  priority from {@link ConnectorRefreshPriority}
     */
    public void addAll(Collection<String> endpoints, int priority) {
        var entries = endpoints.stream()
                .map(endpoint -> new ConnectorQueueEntry(endpoint, priority))
                .toList();
        queue.addAll(entries);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
