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

package com.kolich.beacon.components.freemarker;

import com.google.common.collect.ImmutableMap;
import com.kolich.beacon.BuildVersion;
import com.kolich.beacon.components.BeaconConfig;
import curacao.annotations.Component;
import curacao.annotations.Injectable;
import curacao.freemarker.components.AbstractFreeMarkerContentRenderer;

import javax.annotation.Nonnull;
import java.util.Map;

@Component
public final class FreeMarkerContentRenderer extends AbstractFreeMarkerContentRenderer {

    private static final String BUILD_VERSION_ATTR = "buildVersion";
    private static final String CONTEXT_PATH_ATTR = "contextPath";
    private static final String FULL_URI_ATTR = "fullUri";
    private static final String DEV_MODE_ATTR = "devMode";

    private final BeaconConfig beaconConfig_;

    @Injectable
    public FreeMarkerContentRenderer(
            final BeaconConfig beaconConfig,
            final BeaconFreeMarkerConfig beaconFreeMarkerConfig) {
        super(beaconFreeMarkerConfig.getFreeMarkerConfig());
        beaconConfig_ = beaconConfig;
    }

    @Nonnull
    @Override
    public Map<String, Object> getGlobalDataMap() {
        final ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();

        // Application build version
        final BuildVersion buildVersion = BuildVersion.getInstance();
        map.put(BUILD_VERSION_ATTR, buildVersion);

        // Shared application/service properties
        map.put(CONTEXT_PATH_ATTR, beaconConfig_.getViewSafeContentPath());
        map.put(FULL_URI_ATTR, beaconConfig_.getViewSafeFullUri());
        map.put(DEV_MODE_ATTR, beaconConfig_.isDevMode());

        return map.build();
    }

}
