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

import java.util.concurrent.TimeUnit;

public interface AwsConfig {

    String AWS_CONFIG_PATH = "aws";

    String AWS_ACCESS_KEY_PROP = "access-key";
    String AWS_SECRET_KEY_PROP = "secret-key";

    String AWS_ROUTE_53_REGION_PROP = "route-53.region";
    String AWS_ROUTE_53_HOSTED_ZONE_ID_PROP = "route-53.hosted-zone-id";
    String AWS_ROUTE_53_RESOURCE_RECORD_UPSERT_NAME_PROP = "route-53.resource-record-upsert-name";
    String AWS_ROUTE_53_RESOURCE_RECORD_UPSERT_TTL_PROP = "route-53.resource-record-upsert-ttl";

    String getAwsAccessKey();

    String getAwsSecretKey();

    // Route53 config

    Regions getAwsRoute53Region();

    String getAwsRoute53HostedZoneId();

    String getAwsRoute53ResourceRecordUpsertName();

    long getAwsRoute53ResourceRecordUpsertTtl(
            final TimeUnit timeUnit);

}
