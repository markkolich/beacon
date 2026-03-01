/*
 * Copyright (c) 2026 Mark S. Kolich
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

package com.kolich.beacon.components.aws.route53;

import com.kolich.beacon.components.aws.AwsClientConfig;
import com.kolich.beacon.components.aws.AwsConfig;
import com.kolich.beacon.components.aws.AwsCredentials;
import curacao.annotations.Component;
import curacao.annotations.Injectable;
import curacao.components.ComponentDestroyable;
import software.amazon.awssdk.services.route53.Route53Client;

@Component
public final class BeaconRoute53Client implements ComponentDestroyable {

    private final Route53Client route53_;

    @Injectable
    public BeaconRoute53Client(
            final AwsConfig awsConfig,
            final AwsCredentials awsCredentials,
            final AwsClientConfig awsClientConfig) {
        route53_ = Route53Client.builder()
                .credentialsProvider(awsCredentials.getCredentialsProvider())
                .overrideConfiguration(awsClientConfig.getClientOverrideConfiguration())
                .region(awsConfig.getAwsRoute53Region())
                .build();
    }

    public Route53Client getRoute53Client() {
        return route53_;
    }

    @Override
    public void destroy() throws Exception {
        route53_.close();
    }

}
