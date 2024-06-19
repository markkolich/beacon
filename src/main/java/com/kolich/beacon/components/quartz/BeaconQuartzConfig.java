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

import com.kolich.beacon.components.BeaconConfig;
import com.typesafe.config.Config;
import curacao.annotations.Component;
import curacao.annotations.Injectable;

@Component
public final class BeaconQuartzConfig {

    private static final String QUARTZ_CONFIG_PATH = "quartz";

    private static final String THREAD_POOL_SIZE_PROP = "thread-pool.size";
    private static final String THREAD_POOL_USE_DAEMONS_PROP = "thread-pool.use-daemons";

    private static final String CRON_EXPRESSION_PROP = "cron-expression";

    private final Config quartzConfig_;

    @Injectable
    public BeaconQuartzConfig(
            final BeaconConfig beaconConfig) {
        quartzConfig_ = beaconConfig.getBeaconConfig()
                .getConfig(QUARTZ_CONFIG_PATH);
    }

    public Config getQuartzConfig() {
        return quartzConfig_;
    }

    public int getThreadPoolSize() {
        return quartzConfig_.getInt(THREAD_POOL_SIZE_PROP);
    }

    public boolean getThreadPoolUseDaemons() {
        return quartzConfig_.getBoolean(THREAD_POOL_USE_DAEMONS_PROP);
    }

    public String getCronExpression() {
        return quartzConfig_.getString(CRON_EXPRESSION_PROP);
    }

}
