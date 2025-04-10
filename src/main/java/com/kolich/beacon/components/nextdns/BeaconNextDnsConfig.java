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

import com.kolich.beacon.components.BeaconConfig;
import com.typesafe.config.Config;
import curacao.annotations.Component;
import curacao.annotations.Injectable;

import java.util.concurrent.TimeUnit;

@Component
public final class BeaconNextDnsConfig {

    private static final String NEXT_DNS_CONFIG_PATH = "next-dns";

    private static final String UPDATE_LINKED_IP_ENABLED_PROP = "update-linked-ip-enabled";
    private static final String API_LINKED_IP_URL_PROP = "api-linked-ip-url";
    private static final String API_CLIENT_TIMEOUT_PROP = "api-client-timeout";

    private final Config config_;

    @Injectable
    public BeaconNextDnsConfig(
            final BeaconConfig beaconConfig) {
        config_ = beaconConfig.getBeaconConfig().getConfig(NEXT_DNS_CONFIG_PATH);
    }

    public Config getNextDnsConfig() {
        return config_;
    }

    public boolean isUpdateLinkedIpEnabled() {
        return config_.getBoolean(UPDATE_LINKED_IP_ENABLED_PROP);
    }

    public String getApiLinkedIpUrl() {
        return config_.getString(API_LINKED_IP_URL_PROP);
    }

    public long getApiClientTimeout(
            final TimeUnit timeUnit) {
        return config_.getDuration(API_CLIENT_TIMEOUT_PROP, timeUnit);
    }

}
