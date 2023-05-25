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
package de.sovity.edc.ext.brokerserver.factories;

import de.sovity.edc.ext.brokerserver.services.queue.ConnectorQueue;
import de.sovity.edc.ext.brokerserver.services.queue.ConnectorQueueJob;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.configuration.Config;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import static de.sovity.edc.ext.brokerserver.BrokerServerExtension.CRON_TRIGGER;

public class SchedulerFactory {
    public static void initalizeScheduler(Config config, ConnectorQueue connectorQueue) {
        var cronTrigger = config.getString(CRON_TRIGGER, "5 * * ? * ?"); // default: every 5 minutes

        try {
            var schedulerFactory = new StdSchedulerFactory();
            var scheduler = schedulerFactory.getScheduler();

            var job = JobBuilder.newJob(ConnectorQueueJob.class)
                    .withIdentity("connectorRefreshQueue", "group1")
                    .usingJobData(new JobDataMap() {
                        {
                            put("connectorQueue", connectorQueue);
                        }
                    }).build();

            var trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger3", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronTrigger))
                    .forJob("connectorRefreshQueue", "group1")
                    .build();

            scheduler.start();
        } catch (SchedulerException e) {
            throw new EdcException("Could not create scheduler", e);
        }
    }
}
