/*
 * Copyright (c) 2024 Mark S. Kolich
 * https://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.beacon.components.quartz;

import com.kolich.beacon.components.aws.AwsConfig;
import com.kolich.beacon.components.aws.route53.Route53Client;
import com.kolich.beacon.components.unifi.BeaconUdmConfig;
import com.kolich.beacon.components.unifi.UdmClient;
import curacao.annotations.Component;
import curacao.annotations.Injectable;
import curacao.components.CuracaoComponent;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import static com.kolich.beacon.components.quartz.BeaconJob.BEACON_AWS_CONFIG_DATA_MAP_KEY;
import static com.kolich.beacon.components.quartz.BeaconJob.BEACON_AWS_ROUTE53_CLIENT_DATA_MAP_KEY;
import static com.kolich.beacon.components.quartz.BeaconJob.BEACON_UDM_CLIENT_DATA_MAP_KEY;
import static com.kolich.beacon.components.quartz.BeaconJob.BEACON_UDM_CONFIG_DATA_MAP_KEY;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public final class BeaconScheduler implements CuracaoComponent {

    private final Scheduler quartzScheduler_;

    @Injectable
    public BeaconScheduler(
            final BeaconUdmConfig beaconUdmConfig,
            final BeaconQuartzConfig beaconQuartzConfig,
            final BeaconSchedulerFactory beaconSchedulerFactory,
            final UdmClient udmClient,
            final AwsConfig awsConfig,
            final Route53Client route53Client) throws Exception {
        quartzScheduler_ = beaconSchedulerFactory.getNewScheduler();

        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BEACON_UDM_CONFIG_DATA_MAP_KEY, beaconUdmConfig);
        jobDataMap.put(BEACON_UDM_CLIENT_DATA_MAP_KEY, udmClient);
        jobDataMap.put(BEACON_AWS_CONFIG_DATA_MAP_KEY, awsConfig);
        jobDataMap.put(BEACON_AWS_ROUTE53_CLIENT_DATA_MAP_KEY, route53Client.getRoute53Client());

        final JobDetail job = newJob(BeaconJob.class)
                .setJobData(jobDataMap)
                .build();
        final Trigger trigger = newTrigger()
                .withSchedule(cronSchedule(beaconQuartzConfig.getCronExpression()))
                .build();

        quartzScheduler_.scheduleJob(job, trigger);
    }

    @Override
    public void initialize() throws Exception {
        // Starts the scheduler.
        quartzScheduler_.start();
    }

    @Override
    public void destroy() throws Exception {
        // Clears any pending jobs in prep for shutdown.
        quartzScheduler_.clear();
        quartzScheduler_.shutdown();
    }

}
