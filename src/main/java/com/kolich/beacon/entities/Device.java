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

package com.kolich.beacon.entities;

import static com.google.common.base.Preconditions.checkNotNull;

public interface Device {

    String getId();

    String getHostname();

    String getUsername();

    String getPassword();

    String getCronExpression();

    default Builder toBuilder() {
        return new Builder()
                .setId(getId())
                .setHostname(getHostname())
                .setUsername(getUsername())
                .setPassword(getPassword())
                .setCronExpression(getCronExpression());
    }

    final class Builder {

        private String id_;

        private String hostname_;

        private String username_;
        private String password_;

        private String cronExpression_;

        public Builder setId(
                final String id) {
            id_ = id;
            return this;
        }

        public Builder setHostname(
                final String hostname) {
            hostname_ = hostname;
            return this;
        }

        public Builder setUsername(
                final String username) {
            username_ = username;
            return this;
        }

        public Builder setPassword(
                final String password) {
            password_ = password;
            return this;
        }

        public Builder setCronExpression(
                final String cronExpression) {
            cronExpression_ = cronExpression;
            return this;
        }

        public Device build() {
            checkNotNull(id_, "ID cannot be null.");
            checkNotNull(hostname_, "Hostname cannot be null.");
            checkNotNull(username_, "Username cannot be null.");
            checkNotNull(password_, "Password cannot be null.");
            checkNotNull(cronExpression_, "Cron expression cannot be null.");

            return new Device() {
                @Override
                public String getId() {
                    return id_;
                }

                @Override
                public String getHostname() {
                    return hostname_;
                }

                @Override
                public String getUsername() {
                    return username_;
                }

                @Override
                public String getPassword() {
                    return password_;
                }

                @Override
                public String getCronExpression() {
                    return cronExpression_;
                }
            };
        }

    }

}
