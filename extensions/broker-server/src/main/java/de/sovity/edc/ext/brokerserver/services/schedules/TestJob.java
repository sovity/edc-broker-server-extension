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

package de.sovity.edc.ext.brokerserver.services.schedules;

import lombok.RequiredArgsConstructor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@RequiredArgsConstructor
public class TestJob implements Job {
    private final Monitor monitor;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        monitor.severe("Hello from the other side!");
    }
}
