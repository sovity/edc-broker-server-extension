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

import de.sovity.edc.ext.brokerserver.services.DatabaseSettingsProvider;
import de.sovity.edc.ext.brokerserver.services.schedules.utils.CronJobRef;
import de.sovity.edc.ext.brokerserver.services.schedules.utils.JobFactoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.spi.monitor.Monitor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Collection;

@RequiredArgsConstructor
public class QuartzScheduleInitializer {
    private final Monitor monitor;
    private final Collection<CronJobRef<?>> jobs;
    private final DatabaseSettingsProvider databaseSettingsProvider;

    @SneakyThrows
    public void startSchedules() {
        var jobFactory = new JobFactoryImpl(jobs);
        var scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.setJobFactory(jobFactory);

        jobs.forEach(job -> scheduleCronJob(scheduler, job));
        scheduler.start();
    }

    @SneakyThrows
    private void scheduleCronJob(Scheduler scheduler, CronJobRef<?> cronJobRef) {
        // CRON property name doubles as job name
        var jobName = cronJobRef.configPropertyName();

        // Skip scheduling if property not provided to ensure tests have no schedules running
        var cronTrigger = (String) databaseSettingsProvider.getSetting(jobName, "");
        if (StringUtils.isBlank(cronTrigger)) {
            monitor.info("No cron trigger configured for %s. Skipping.".formatted(jobName));
            return;
        }

        monitor.info("Starting schedule %s=%s.".formatted(jobName, cronTrigger));
        var job = JobBuilder.newJob(cronJobRef.clazz())
                .withIdentity(jobName)
                .build();
        var trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronTrigger))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
