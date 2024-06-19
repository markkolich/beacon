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

package com.kolich.beacon.entities.unifi.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonDeserialize(builder = UdmDevice.Builder.class)
public interface UdmDevice {

    @JsonProperty("name")
    String getName();

    @JsonProperty("uplink")
    UdmDeviceUplink getUplink();

    @JsonIgnore
    default Builder toBuilder() {
        return new UdmDevice.Builder()
                .setName(getName())
                .setUplink(getUplink());
    }

    final class Builder {

        private String name_;
        private UdmDeviceUplink uplink_;

        @JsonProperty("name")
        public Builder setName(
                final String name) {
            name_ = name;
            return this;
        }

        @JsonProperty("uplink")
        public Builder setUplink(
                final UdmDeviceUplink uplink) {
            uplink_ = uplink;
            return this;
        }

        public UdmDevice build() {
            checkNotNull(name_, "Name cannot be null.");
            checkNotNull(uplink_, "Uplink cannot be null.");

            return new UdmDevice() {
                @Override
                public String getName() {
                    return name_;
                }

                @Override
                public UdmDeviceUplink getUplink() {
                    return uplink_;
                }
            };
        }

    }

}
