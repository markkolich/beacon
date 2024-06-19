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

package com.kolich.beacon.controllers;

import com.kolich.beacon.components.BeaconConfig;
import com.kolich.beacon.components.unifi.BeaconUdmConfig;
import com.kolich.beacon.components.unifi.UdmClient;
import com.kolich.beacon.entities.freemarker.FreeMarkerContent;
import curacao.annotations.Controller;
import curacao.annotations.Injectable;
import curacao.annotations.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public final class Index {

    private static final Logger LOG = LoggerFactory.getLogger(Index.class);

    private static final String TEMPLATE_ATTR_IP = "ip";

    private final BeaconConfig beaconConfig_;
    private final BeaconUdmConfig beaconUdmConfig_;

    private final UdmClient udmClient_;

    @Injectable
    public Index(
            final BeaconConfig beaconConfig,
            final BeaconUdmConfig beaconUdmConfig,
            final UdmClient udmClient) {
        beaconConfig_ = beaconConfig;
        beaconUdmConfig_ = beaconUdmConfig;
        udmClient_ = udmClient;
    }

    @RequestMapping("^/$")
    public FreeMarkerContent index() throws Exception {
        final String jwtAuthToken = udmClient_.getJwtAuthToken(
                beaconUdmConfig_.getUsername(),
                beaconUdmConfig_.getPassword());
        final String udmUplinkIp = udmClient_.getUdmUplinkIp(jwtAuthToken);

        return new FreeMarkerContent.Builder("templates/index.ftl")
                .withAttr(TEMPLATE_ATTR_IP, udmUplinkIp)
                .build();
    }

}
