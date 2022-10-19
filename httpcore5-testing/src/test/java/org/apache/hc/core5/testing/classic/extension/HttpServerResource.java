/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.testing.classic.extension;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.testing.SSLTestContexts;
import org.apache.hc.core5.testing.classic.LoggingExceptionListener;
import org.apache.hc.core5.testing.classic.LoggingHttp1StreamListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerResource implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerResource.class);

    private final URIScheme scheme;
    private final Consumer<ServerBootstrap> bootstrapCustomizer;

    private HttpServer server;

    public HttpServerResource(final URIScheme scheme, final Consumer<ServerBootstrap> bootstrapCustomizer) {
        this.scheme = scheme;
        this.bootstrapCustomizer = bootstrapCustomizer;
    }

    @Override
    public void beforeEach(final ExtensionContext extensionContext) throws Exception {
        LOG.debug("Starting up test server");

        final ServerBootstrap bootstrap = ServerBootstrap.bootstrap()
                .setSslContext(scheme == URIScheme.HTTPS ? SSLTestContexts.createServerSSLContext() : null)
                .setExceptionListener(LoggingExceptionListener.INSTANCE)
                .setStreamListener(LoggingHttp1StreamListener.INSTANCE);
        bootstrapCustomizer.accept(bootstrap);
        server = bootstrap.create();
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) throws Exception {
        LOG.debug("Shutting down test server");
        if (server != null) {
            try {
                server.close(CloseMode.IMMEDIATE);
            } catch (final Exception ignore) {
            }
        }
    }

    public HttpServer start() throws IOException {
        Assertions.assertNotNull(server);
        server.start();
        return server;
    }

}
