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

package com.kolich.beacon;

import com.google.common.io.Resources;
import com.kolich.beacon.exceptions.BeaconException;

import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * This singleton represents an immutable set of attributes specific to each application build.
 * At build time, the `buildnumber-maven-plugin` as wired into the Maven reactor generates a set
 * of attributes including build number, timestamp, build version, and the build branch.
 * These attributes are written to the Java {@link Properties} file found at
 * {@link #BUILD_PROPERTIES_RESOURCE} which is consumed by this class.
 */
public final class BuildVersion {

    private static final String BUILD_PROPERTIES_RESOURCE = "beacon/version/build.properties";

    private static final String BUILD_PROPERTY_BUILD_NUMBER = "buildNumber";
    private static final String BUILD_PROPERTY_TIMESTAMP = "timestamp";
    private static final String BUILD_PROPERTY_VERSION = "version";
    private static final String BUILD_PROPERTY_BRANCH = "branch";
    private static final String BUILD_PROPERTY_UNKNOWN = "unknown";

    private final Properties buildProperties_ = new Properties();

    private BuildVersion() {
        try {
            final URL propertiesResource = Resources.getResource(BUILD_PROPERTIES_RESOURCE);
            try (InputStream is = Resources.asByteSource(propertiesResource).openStream()) {
                buildProperties_.load(is);
            }
        } catch (final Exception e) {
            throw new BeaconException("Failed to cleanly parse build properties: "
                    + BUILD_PROPERTIES_RESOURCE, e);
        }
    }

    private static final class LazyHolder {
        private static final BuildVersion INSTANCE = new BuildVersion();
    }

    public static BuildVersion getInstance() {
        return LazyHolder.INSTANCE;
    }

    // ------------------------------------------------------------------------
    // Build version properties; generated by Maven at build time
    // ------------------------------------------------------------------------

    /**
     * Returns the build number of the running application, represented by a SHA-1 Git hash
     * from the top of the branch where the build was executed.
     */
    public String getBuildNumber() {
        return buildProperties_.getProperty(BUILD_PROPERTY_BUILD_NUMBER, BUILD_PROPERTY_UNKNOWN);
    }

    /**
     * Returns an ISO-8601 compliant string representing the instant when the build was initiated.
     * Parse using {@link DateTimeFormatter#ISO_DATE_TIME}.
     */
    public String getTimestamp() {
        return buildProperties_.getProperty(BUILD_PROPERTY_TIMESTAMP, BUILD_PROPERTY_UNKNOWN);
    }

    /**
     * Returns the Maven version of the build that was found at build time.
     */
    public String getVersion() {
        return buildProperties_.getProperty(BUILD_PROPERTY_VERSION, BUILD_PROPERTY_UNKNOWN);
    }

    /**
     * Returns the name of the Git branch from where the build was executed.
     */
    public String getBranch() {
        return buildProperties_.getProperty(BUILD_PROPERTY_BRANCH, BUILD_PROPERTY_UNKNOWN);
    }

}
