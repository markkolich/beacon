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

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import com.kolich.beacon.components.aws.AwsConfig;
import com.kolich.beacon.components.nextdns.BeaconNextDnsConfig;
import com.kolich.beacon.components.nextdns.NextDnsClient;
import com.kolich.beacon.components.unifi.BeaconUdmConfig;
import com.kolich.beacon.components.unifi.UdmClient;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class BeaconJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconJob.class);

    public static final String BEACON_UDM_CONFIG_DATA_MAP_KEY = "beacon.udmConfig";
    public static final String BEACON_UDM_CLIENT_DATA_MAP_KEY = "beacon.udmClient";
    public static final String BEACON_AWS_CONFIG_DATA_MAP_KEY = "beacon.awsConfig";
    public static final String BEACON_AWS_ROUTE53_CLIENT_DATA_MAP_KEY = "beacon.awsRoute53Client";
    public static final String BEACON_NEXT_DNS_CONFIG_DATA_MAP_KEY = "beacon.nextDnsConfig";
    public static final String BEACON_NEXT_DNS_CLIENT_DATA_MAP_KEY = "beacon.nextDnsClient";

    @Override
    public void execute(
            final JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        final BeaconUdmConfig beaconUdmConfig =
                (BeaconUdmConfig) jobDataMap.get(BEACON_UDM_CONFIG_DATA_MAP_KEY);
        final UdmClient udmClient =
                (UdmClient) jobDataMap.get(BEACON_UDM_CLIENT_DATA_MAP_KEY);
        final AwsConfig awsConfig =
                (AwsConfig) jobDataMap.get(BEACON_AWS_CONFIG_DATA_MAP_KEY);
        final AmazonRoute53 route53 =
                (AmazonRoute53) jobDataMap.get(BEACON_AWS_ROUTE53_CLIENT_DATA_MAP_KEY);
        final BeaconNextDnsConfig beaconNextDnsConfig =
                (BeaconNextDnsConfig) jobDataMap.get(BEACON_NEXT_DNS_CONFIG_DATA_MAP_KEY);
        final NextDnsClient nextDnsClient =
                (NextDnsClient) jobDataMap.get(BEACON_NEXT_DNS_CLIENT_DATA_MAP_KEY);

        try {
            final String jwtAuthToken = udmClient.getJwtAuthToken(
                    beaconUdmConfig.getUsername(),
                    beaconUdmConfig.getPassword());
            if (StringUtils.isBlank(jwtAuthToken)) {
                throw new JobExecutionException("Failed to authenticate with UDM - job failed.");
            }

            final String udmUplinkIp = udmClient.getUdmUplinkIp(jwtAuthToken);
            if (StringUtils.isBlank(udmUplinkIp)) {
                throw new JobExecutionException("UDM uplink IP was blank/empty - job failed.");
            }

            LOG.debug("Successfully extracted uplink IP from UDM: {}", udmUplinkIp);

            final ListResourceRecordSetsRequest lrrsRequest = new ListResourceRecordSetsRequest()
                    .withHostedZoneId(awsConfig.getAwsRoute53HostedZoneId());
            final ListResourceRecordSetsResult lrrsResult = route53.listResourceRecordSets(lrrsRequest);
            final List<ResourceRecordSet> rrs = lrrsResult.getResourceRecordSets();

            final String recordSetUpsertName = awsConfig.getAwsRoute53ResourceRecordUpsertName();
            final ResourceRecordSet resourceRecordSet = rrs.stream()
                    .filter(r -> recordSetUpsertName.equals(r.getName()))
                    .findFirst()
                    .orElseThrow();

            final ResourceRecord recordToUpsert = resourceRecordSet.getResourceRecords().stream()
                    .findFirst()
                    .orElseThrow();

            // If the current IP in DNS matches that of the WAN uplink IP on the UDM,
            // then there's nothing to update so bail early.
            if (udmUplinkIp.equals(recordToUpsert.getValue())) {
                LOG.debug("Uplink IP matches DNS ({}) record in Route53, nothing to update: {}",
                        recordSetUpsertName, udmUplinkIp);
                return;
            }

            // Set the new UDM uplink IP.
            recordToUpsert.setValue(udmUplinkIp);

            final ResourceRecordSet recordSetToUpsert = new ResourceRecordSet()
                    .withName(resourceRecordSet.getName())
                    .withType(resourceRecordSet.getType())
                    .withTTL(awsConfig.getAwsRoute53ResourceRecordUpsertTtl(TimeUnit.SECONDS))
                    .withResourceRecords(recordToUpsert);

            final Change change = new Change()
                    .withAction(ChangeAction.UPSERT)
                    .withResourceRecordSet(recordSetToUpsert);
            final ChangeBatch changeBatch = new ChangeBatch()
                    .withChanges(change);
            final ChangeResourceRecordSetsRequest crrsRequest = new ChangeResourceRecordSetsRequest()
                    .withHostedZoneId(awsConfig.getAwsRoute53HostedZoneId())
                    .withChangeBatch(changeBatch);

            final ChangeResourceRecordSetsResult crrsResult =
                    route53.changeResourceRecordSets(crrsRequest);
            LOG.debug("Successfully updated Route53 DNS with UDM uplink IP: {}: {}",
                    crrsResult.getChangeInfo().getId(), udmUplinkIp);

            // Set new linked IP with NextDNS.
            if (beaconNextDnsConfig.isUpdateLinkedIpEnabled()) {
                nextDnsClient.setLinkedIp(udmUplinkIp);
            }
        } catch (final Exception e) {
            LOG.error("Failed to run beacon job.", e);
        }
    }

}
