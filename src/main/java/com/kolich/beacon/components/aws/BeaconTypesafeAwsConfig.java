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

package com.kolich.beacon.components.aws;

import com.amazonaws.regions.Regions;
import com.kolich.beacon.components.BeaconConfig;
import com.typesafe.config.Config;
import curacao.annotations.Component;
import curacao.annotations.Injectable;

import java.util.concurrent.TimeUnit;

@Component
public final class BeaconTypesafeAwsConfig implements AwsConfig {

    private final Config config_;

    @Injectable
    public BeaconTypesafeAwsConfig(
            final BeaconConfig beaconConfig) {
        config_ = beaconConfig.getBeaconConfig().getConfig(AWS_CONFIG_PATH);
    }

    @Override
    public String getAwsAccessKey() {
        return config_.getString(AWS_ACCESS_KEY_PROP);
    }

    @Override
    public String getAwsSecretKey() {
        return config_.getString(AWS_SECRET_KEY_PROP);
    }

    // Route53 config

    @Override
    public Regions getAwsRoute53Region() {
        return Regions.fromName(config_.getString(AWS_ROUTE_53_REGION_PROP));
    }

    @Override
    public String getAwsRoute53HostedZoneId() {
        return config_.getString(AWS_ROUTE_53_HOSTED_ZONE_ID_PROP);
    }

    @Override
    public String getAwsRoute53ResourceRecordUpsertName() {
        return config_.getString(AWS_ROUTE_53_RESOURCE_RECORD_UPSERT_NAME_PROP);
    }

    @Override
    public long getAwsRoute53ResourceRecordUpsertTtl(
            final TimeUnit timeUnit) {
        return config_.getDuration(AWS_ROUTE_53_RESOURCE_RECORD_UPSERT_TTL_PROP, timeUnit);
    }

}
