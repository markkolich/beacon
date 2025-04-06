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

package com.kolich.beacon.components.nextdns;

import com.kolich.beacon.BuildVersion;
import com.kolich.beacon.exceptions.BeaconException;
import curacao.annotations.Component;
import curacao.annotations.Injectable;
import curacao.core.servlet.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asynchttpclient.Dsl.asyncHttpClient;

@Component
public final class BeaconNextDnsClient implements NextDnsClient {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconNextDnsClient.class);

    private static final String USER_AGENT_FORMAT = "Beacon/%s";

    private final BeaconNextDnsConfig beaconNextDnsConfig_;

    @Injectable
    public BeaconNextDnsClient(
            final BeaconNextDnsConfig beaconNextDnsConfig) {
        beaconNextDnsConfig_ = beaconNextDnsConfig;
    }

    @Override
    public void setLinkedIp(
            final String linkedIp) {
        checkNotNull(linkedIp, "Linked IP cannot be null.");

        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(buildAsyncHttpClientConfig())) {
            final String linkedIpApiUrl = beaconNextDnsConfig_.getApiLinkedIpUrl();

            final ListenableFuture<Response> futureResponse = asyncHttpClient.prepareGet(linkedIpApiUrl)
                    .execute();

            final long apiClientTimeoutInMs =
                    beaconNextDnsConfig_.getApiClientTimeout(TimeUnit.MILLISECONDS);

            final Response response = futureResponse.get(apiClientTimeoutInMs, TimeUnit.MILLISECONDS);
            final int statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new BeaconException(String.format("Unsuccessful status code from NextDNS linked IP API: %s: %s",
                        linkedIpApiUrl, response.getStatusCode()));
            }

            final String responseBody = response.getResponseBody(StandardCharsets.UTF_8);
            if (StringUtils.isBlank(responseBody)) {
                throw new BeaconException(String.format("Blank/empty response from NextDNS linked IP API: %s",
                        linkedIpApiUrl));
            } else if (!linkedIp.equals(responseBody)) {
                throw new BeaconException(String.format("IP from UDM controller did not match "
                        + "returned NextDNS linked IP: %s != %s", linkedIp, responseBody));
            }

            LOG.debug("Successfully updated NextDNS linked IP with UDM uplink IP: {}", linkedIp);
        } catch (final Exception e) {
            LOG.error("Failed to set NextDNS linked IP.", e);
        }
    }

    private AsyncHttpClientConfig buildAsyncHttpClientConfig() throws Exception {
        final BuildVersion buildVersion = BuildVersion.getInstance();

        final String userAgent = String.format(USER_AGENT_FORMAT,
                StringUtils.defaultIfBlank(buildVersion.getBuildNumber(), "Dev"));

        return new DefaultAsyncHttpClientConfig.Builder()
                .setUserAgent(userAgent)
                .build();
    }

}
