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

package de.sovity.edc.ext.brokerserver.services.refreshing;

import de.sovity.edc.ext.brokerserver.services.queue.ConnectorQueue;
import org.eclipse.edc.spi.system.configuration.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectorRefreshExecutorPool {
    private static final int MAX_THREADS = 1;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final String SETTING_MAX_THREADS = "broker.server.refreshing.maxThreads";
    private final ExecutorService executorService;
    private final ConnectorUpdater connectorUpdater;
    private final ConnectorQueue connectorQueue;

    public ConnectorRefreshExecutorPool(Config config, ConnectorUpdater connectorUpdater, ConnectorQueue connectorQueue) {
        this.connectorUpdater = connectorUpdater;
        this.connectorQueue = connectorQueue;

        var maxThreads = config.getInteger(SETTING_MAX_THREADS, MAX_THREADS);
        executorService = new ThreadPoolExecutor(maxThreads, maxThreads, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, new PriorityBlockingQueue<>());
    }

    public void execute() {
        for (var i = 0; i < ((ThreadPoolExecutor) executorService).getMaximumPoolSize(); i++) {
            if (connectorQueue.isEmpty()) {
                break;
            }
            new Thread(() -> {
                executorService.execute(() -> connectorUpdater.updateConnector(connectorQueue.poll()));
            }).start();
        }
    }
}
