/*-
 * -\-\-
 * locales-http-examples
 * --
 * Copyright (C) 2016 - 2024 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.i18n.locales.http.handlers.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

/**
 * Abstract implementation of an {@link HttpHandler}. Only created as a helper, in order to expose a
 * static field and a method, to classes extending it.
 */
public abstract class AbstractHttpHandler implements HttpHandler {

  protected static final String LS = System.getProperty("line.separator");

  @Override
  public final void handle(HttpExchange exchange) {
    handleAsync(exchange).toCompletableFuture().join();
  }

  protected abstract CompletionStage<Void> handleAsync(HttpExchange exchange);

  protected void writeResponse(HttpExchange exchange, String responseBody) {
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.add("Content-Type", "text/plain; charset=utf-8");
    try {
      byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    } catch (IOException e) {
      throw new RuntimeException("An unexpected error has occurred", e);
    }
  }
}
