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

package com.kolich.beacon.components.unifi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.kolich.beacon.BuildVersion;
import com.kolich.beacon.components.jackson.BeaconJacksonObjectMapper;
import com.kolich.beacon.entities.unifi.request.UdmAuthLoginRequest;
import com.kolich.beacon.entities.unifi.response.UdmDevice;
import com.kolich.beacon.entities.unifi.response.UdmDeviceStatResponse;
import com.kolich.beacon.exceptions.BeaconException;
import curacao.annotations.Component;
import curacao.annotations.Injectable;
import curacao.core.servlet.HttpStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.*;
import org.asynchttpclient.netty.ssl.JsseSslEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asynchttpclient.Dsl.asyncHttpClient;

@Component
public final class BeaconUdmClient implements UdmClient {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconUdmClient.class);

    private static final String USER_AGENT_FORMAT = "Beacon/%s";

    private static final String JSON_UTF_8 = MediaType.JSON_UTF_8.toString();

    private static final String API_AUTH_LOGIN_PATH = "/api/auth/login";
    private static final String API_PROXY_DEVICE_STAT_PATH = "/proxy/network/api/s/default/stat/device";

    private static final String AUTH_TOKEN_COOKIE_NAME = "TOKEN";

    private static final String UDM_DEVICE_NAME = "UDM";

    private final BeaconUdmConfig beaconUdmConfig_;

    private final ObjectMapper objectMapper_;

    @Injectable
    public BeaconUdmClient(
            final BeaconUdmConfig beaconUdmConfig,
            final BeaconJacksonObjectMapper beaconJacksonObjectMapper) {
        beaconUdmConfig_ = beaconUdmConfig;
        objectMapper_ = beaconJacksonObjectMapper.getObjectMapper();
    }

    @Override
    public String getJwtAuthToken(
            final String username,
            final String password) {
        checkNotNull(username, "Username cannot be null.");
        checkNotNull(password, "Password cannot be null.");

        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(buildAsyncHttpClientConfig())) {
            final UdmAuthLoginRequest udmAuthLoginRequest = new UdmAuthLoginRequest.Builder()
                    .setUsername(username)
                    .setPassword(password)
                    .build();

            final String apiBaseUrl = beaconUdmConfig_.getApiBaseUrl();
            final String authLoginApiUrl = String.format("%s%s", apiBaseUrl, API_AUTH_LOGIN_PATH);

            final String authLoginRequestBody =
                    objectMapper_.writeValueAsString(udmAuthLoginRequest);

            final ListenableFuture<Response> futureResponse = asyncHttpClient.preparePost(authLoginApiUrl)
                    .setHeader(HttpHeaders.CONTENT_TYPE, JSON_UTF_8)
                    .setHeader(HttpHeaders.ACCEPT, JSON_UTF_8)
                    .setBody(authLoginRequestBody)
                    .execute();

            final long apiClientTimeoutInMs =
                    beaconUdmConfig_.getApiClientTimeout(TimeUnit.MILLISECONDS);

            final Response response = futureResponse.get(apiClientTimeoutInMs, TimeUnit.MILLISECONDS);
            final int statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new BeaconException(String.format("Unsuccessful status code from UDM controller API: %s: %s",
                        API_AUTH_LOGIN_PATH, response.getStatusCode()));
            }

            final List<Cookie> authCookies = response.getCookies();
            return authCookies.stream()
                    .filter(c -> AUTH_TOKEN_COOKIE_NAME.equals(c.name()))
                    .findFirst()
                    .orElseThrow()
                    .value();
        } catch (final Exception e) {
            LOG.error("Failed to authenticate with controller.", e);
            return null;
        }
    }

    @Override
    public String getUdmUplinkIp(
            final String jwtAuthToken) {
        checkNotNull(jwtAuthToken, "JWT auth token cannot be null.");

        try (AsyncHttpClient asyncHttpClient = asyncHttpClient(buildAsyncHttpClientConfig())) {
            final String apiBaseUrl = beaconUdmConfig_.getApiBaseUrl();
            final String deviceStatApiUrl = String.format("%s%s", apiBaseUrl, API_PROXY_DEVICE_STAT_PATH);

            final ListenableFuture<Response> futureResponse = asyncHttpClient.prepareGet(deviceStatApiUrl)
                    .setHeader(HttpHeaders.ACCEPT, JSON_UTF_8)
                    .addCookie(new DefaultCookie(AUTH_TOKEN_COOKIE_NAME, jwtAuthToken))
                    .execute();

            final long apiClientTimeoutInMs =
                    beaconUdmConfig_.getApiClientTimeout(TimeUnit.MILLISECONDS);

            final Response response = futureResponse.get(apiClientTimeoutInMs, TimeUnit.MILLISECONDS);
            final int statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new BeaconException(String.format("Unsuccessful status code from UDM controller API: %s: %s",
                        API_PROXY_DEVICE_STAT_PATH, response.getStatusCode()));
            }

            final String responseBody = response.getResponseBody(StandardCharsets.UTF_8);
            final UdmDeviceStatResponse responseEntity =
                    objectMapper_.readValue(responseBody, UdmDeviceStatResponse.class);

            return responseEntity.getDevices().stream()
                    .filter(d -> UDM_DEVICE_NAME.equals(d.getName()))
                    .map(UdmDevice::getUplink)
                    .findFirst()
                    .orElseThrow()
                    .getIp();
        } catch (final Exception e) {
            LOG.error("Failed to get UDM uplink controller IP.", e);
            return null;
        }
    }

    private AsyncHttpClientConfig buildAsyncHttpClientConfig() throws Exception {
        final BuildVersion buildVersion = BuildVersion.getInstance();

        final String userAgent = String.format(USER_AGENT_FORMAT,
                StringUtils.defaultIfBlank(buildVersion.getBuildNumber(), "Dev"));

        final X509ExtendedTrustManager trustManager = new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(
                    final X509Certificate[] chain,
                    final String authType) throws CertificateException {
                // No-op, intentional.
            }

            @Override
            public void checkClientTrusted(
                    final X509Certificate[] chain,
                    final String authType,
                    final Socket socket) throws CertificateException {
                // No-op, intentional.
            }

            @Override
            public void checkClientTrusted(
                    final X509Certificate[] chain,
                    final String authType,
                    final SSLEngine engine) throws CertificateException {
                // No-op, intentional.
            }

            @Override
            public void checkServerTrusted(
                    final X509Certificate[] chain,
                    final String authType) throws CertificateException {
                // No-op, intentional.
            }

            @Override
            public void checkServerTrusted(
                    final X509Certificate[] chain,
                    final String authType,
                    final Socket socket) throws CertificateException {
                // No-op, intentional.
            }

            @Override
            public void checkServerTrusted(
                    final X509Certificate[] chain,
                    final String authType,
                    final SSLEngine engine) throws CertificateException {
                // No-op, intentional.
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { trustManager }, new SecureRandom());

        return new DefaultAsyncHttpClientConfig.Builder()
                .setUserAgent(userAgent)
                .setSslEngineFactory(new JsseSslEngineFactory(context))
                .build();
    }

}
