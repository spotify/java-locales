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

package com.spotify.i18n.locales.http.handlers;

import com.spotify.i18n.locales.common.ContextBasedLocalesResolver;
import com.spotify.i18n.locales.common.LocalesHelpersFactory;
import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.impl.LocalesHelpersFactoryBaseImpl;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.http.handlers.handlers.ResolveLocaleHandler;
import com.spotify.i18n.locales.http.handlers.handlers.SupportedLocalesHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Showcase implementation of a Java-locales locale resolution logic implementation, which starts a
 * new HTTP server locally, on port 8080.
 *
 * <h1>Audiences
 *
 * <p>This implementation considers the following 2 different audiences:
 *
 * <ul>
 *   <li>{@link Audience#DEFAULT}: Default audience (equivalent to production), only exposed to the
 *       locales which are supported in production today.
 *   <li>{@link Audience#QUALITY_ASSURANCE}: Audience consisting of users responsible for ensuring
 *       the quality of the localized experience, exposed to both locales which are supported in
 *       production today, and locales for which upcoming support is currently being worked on.
 * </ul>
 *
 * <h1>Available endpoints
 *
 * <p>The HTTP server accepts requests on 3 different endpoints. You find their description below,
 * along with links to try them out in your browser, and curl commands to try them out in your
 * terminal.
 *
 * <h2>{@code /supported-locales?audience=[audience]}</h2>
 *
 * <p>Lists the supported locales for the given `[audience]`.
 *
 * <p><strong>Lists the supported locales for the `DEFAULT` audience</strong>
 *
 * <ul>
 *   <li><a
 *       href="http://localhost:8080/supported-locales?audience=DEFAULT">http://localhost:8080/supported-locales?audience=DEFAULT</a>
 *   <li>{@code curl "http://localhost:8080/supported-locales?audience=DEFAULT"}
 * </ul>
 *
 * <p><strong>Lists the supported locales for the `QUALITY_ASSURANCE` audience</strong>
 *
 * <ul>
 *   <li><a
 *       href="http://localhost:8080/supported-locales?audience=QUALITY_ASSURANCE">http://localhost:8080/supported-locales?audience=QUALITY_ASSURANCE</a>
 *   <li>{@code curl "http://localhost:8080/supported-locales?audience=QUALITY_ASSURANCE"}
 * </ul>
 *
 * <h2>{@code /accept-language?audience=[audience]}</h2>
 *
 * <p>Displays the resolved locale for `Accept-Language` value, provided as part of the HTTP
 * headers, for the given `[audience]`.
 *
 * <p><strong>Displays the resolved locale for the `DEFAULT` audience</strong>
 *
 * <ul>
 *   <li><a
 *       href="http://localhost:8080/accept-language?audience=DEFAULT">http://localhost:8080/accept-language?audience=DEFAULT</a>
 *   <li>{@code curl --header "Accept-Language: es-CL,ja-JP,fr-BE"
 *       "http://localhost:8080/accept-language?audience=DEFAULT"}
 * </ul>
 *
 * <p><strong>Displays the resolved locale for the `QUALITY_ASSURANCE` audience</strong>
 *
 * <ul>
 *   <li><a
 *       href="http://localhost:8080/accept-language?audience=QUALITY_ASSURANCE">http://localhost:8080/accept-language?audience=QUALITY_ASSURANCE</a>
 *   <li>{@code curl --header "Accept-Language: es-CL,ja-JP,fr-BE"
 *       "http://localhost:8080/accept-language?audience=QUALITY_ASSURANCE"}
 * </ul>
 *
 * <h2>{@code /country/[country-code]?audience=[audience]}</h2>
 *
 * <p>Displays the resolved locale for Accept-Language value, for the given audience. When no
 * Accept-Language value is present, it will resolve the locale based on the given country code (2
 * positions)
 *
 * <p><strong>Display the resolved locale for the `DEFAULT` audience</strong>
 *
 * <ul>
 *   <li><a
 *       href="http://localhost:8080/country/BE?audience=DEFAULT">http://localhost:8080/country/BE?audience=DEFAULT</a>
 *   <li>{@code curl "http://localhost:8080/country/BE?audience=DEFAULT"}
 *   <li>{@code curl --header "Accept-Language: es-CL,ja-JP,fr-BE"
 *       "http://localhost:8080/country/BE?audience=DEFAULT"}
 * </ul>
 *
 * <p><strong>Display the resolved locale for the `QUALITY_ASSURANCE` audience</strong>
 *
 * <ul>
 *   <li><a
 *       href="http://localhost:8080/country/BE?audience=QUALITY_ASSURANCE">http://localhost:8080/country/BE?audience=QUALITY_ASSURANCE</a>
 *   <li>{@code curl "http://localhost:8080/country/BE?audience=QUALITY_ASSURANCE"}
 *   <li>{@code curl --header "Accept-Language: es-CL,ja-JP,fr-BE"
 *       "http://localhost:8080/country/BE?audience=QUALITY_ASSURANCE"}
 * </ul>
 *
 * @author Eric Fjøsne
 */
public class Main {

  // Server configuration
  private static final int HTTP_SERVER_PORT = 8080;

  // URL
  private static final String HEADER_ACCEPT_LANGUAGE_KEY = "Accept-Language";
  private static final String AUDIENCE_QUERY_PARAMETER = "audience";

  private static final ResolvedLocale DEFAULT_RESOLVED_LOCALE =
      ResolvedLocale.fromLanguageTags("en", Collections.emptyList(), "en");

  public static void main(String[] args) throws IOException {
    LocalesHelpersFactory<HttpExchange> factory = buildLocalesHelpersFactory();

    HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_SERVER_PORT), 0);
    server.setExecutor(Executors.newFixedThreadPool(20));
    server
        .createContext("/supported-locales")
        .setHandler(new SupportedLocalesHandler(factory.buildSupportedLocalesSupplier()));
    server
        .createContext("/accept-language")
        .setHandler(new ResolveLocaleHandler(factory.buildLocalesResolver()));

    LocalesHelpersFactory<HttpExchange> factoryWithFallback =
        buildLocalesHelpersFactoryWithFallbackToCountryCode();
    server
        .createContext("/country/")
        .setHandler(new ResolveLocaleHandler(factoryWithFallback.buildLocalesResolver()));

    server.start();
    System.out.println("Ready to serve requests");
  }

  // START OF BASE HELPERS DEFINITION

  /**
   * Build the {@link LocalesHelpersFactory} which will create helpers that will operate based on a
   * given {@link HttpExchange}, to resolve locales based on the Accept-Language value present in
   * the {@link HttpExchange} header.
   */
  public static LocalesHelpersFactory<HttpExchange> buildLocalesHelpersFactory() {
    return LocalesHelpersFactoryBaseImpl.builder()
        .contextToAudience(httpExchangeToAudience())
        .audienceToSupportedLocalesSupplier(audienceToSupportedLocalesSupplier())
        .contextToAcceptLanguage(httpExchangeToAcceptLanguageFromHeader())
        .contextToDefaultResolvedLocale(httpExchangeToDefaultResolvedLocalee())
        .build();
  }

  /** Function that returns the {@link Audience} based on the {@link HttpExchange} URI path */
  private static Function<HttpExchange, CompletionStage<Audience>> httpExchangeToAudience() {
    return (exchange) ->
        CompletableFuture.completedFuture(
            URLEncodedUtils.parse(exchange.getRequestURI(), StandardCharsets.UTF_8).stream()
                .filter(nameValuePair -> nameValuePair.getName().equals(AUDIENCE_QUERY_PARAMETER))
                .findFirst()
                .map(
                    nvp ->
                        Arrays.stream(Audience.values())
                            .filter(audience -> audience.name().equalsIgnoreCase(nvp.getValue()))
                            .findFirst()
                            .orElse(Audience.DEFAULT))
                .orElse(Audience.DEFAULT));
  }

  /**
   * Function that returns the {@link SupportedLocalesSupplier} corresponding to a given {@link
   * Audience}.
   */
  private static Function<Audience, SupportedLocalesSupplier> audienceToSupportedLocalesSupplier() {
    return (audience) -> audience.getSupportedLocalesSupplier();
  }

  /**
   * Function that returns the optional {@code Accept-Language} value from the {@link HttpExchange}
   * corresponding header
   */
  private static Function<HttpExchange, CompletionStage<Optional<String>>>
      httpExchangeToAcceptLanguageFromHeader() {
    return (exchange) ->
        CompletableFuture.completedFuture(
            Optional.ofNullable(exchange.getRequestHeaders().getFirst(HEADER_ACCEPT_LANGUAGE_KEY)));
  }

  /**
   * Function that returns the default {@link ResolvedLocale} for a given ignored {@link
   * HttpExchange}
   */
  private static Function<HttpExchange, CompletionStage<ResolvedLocale>>
      httpExchangeToDefaultResolvedLocalee() {
    return (ignore) -> CompletableFuture.completedFuture(DEFAULT_RESOLVED_LOCALE);
  }

  // START OF COUNTRY BASED HELPERS DEFINITION

  /**
   * Build the {@link LocalesHelpersFactory} which will create helpers that will operate based on a
   * given {@link HttpExchange}, to resolve locales:
   *
   * <ul>
   *   <li>1st: based on the Accept-Language value present in the {@link HttpExchange} header first
   *   <li>2nd: based on the country code present in the URL path.
   * </ul>
   *
   * In order to achieve this, we actually create 2 locales resolver, with the Accept-Language one
   * configured to make use of the country based one as fallback.
   */
  public static LocalesHelpersFactory<HttpExchange>
      buildLocalesHelpersFactoryWithFallbackToCountryCode() {

    // Locales resolver operating based on the country code
    final ContextBasedLocalesResolver<HttpExchange> resolver =
        LocalesHelpersFactoryBaseImpl.builder()
            .contextToAudience(httpExchangeToAudience())
            .contextToAcceptLanguage(httpExchangeToAcceptLanguageBasedOnCountryCodeInPath())
            .contextToDefaultResolvedLocale(httpExchangeToDefaultResolvedLocalee())
            .audienceToSupportedLocalesSupplier(audienceToSupportedLocalesSupplier())
            .build()
            .buildLocalesResolver();

    // Create the fallback to default resolved locale based on the resolver's output
    final Function<HttpExchange, CompletionStage<ResolvedLocale>> contextToDefaultResolvedLocale =
        resolver::resolve;

    // Locales resolver operating based on the Accept-Language value
    return LocalesHelpersFactoryBaseImpl.builder()
        .contextToAudience(httpExchangeToAudience())
        .contextToAcceptLanguage(httpExchangeToAcceptLanguageFromHeader())
        // Here, we make use of the function we just created, to chain locale resolution to the
        // fallback resolver, in case no match could be found
        .contextToDefaultResolvedLocale(contextToDefaultResolvedLocale)
        .audienceToSupportedLocalesSupplier(audienceToSupportedLocalesSupplier())
        .build();
  }

  /**
   * Returns the function that returns the optional {@code Accept-Language} value from the {@link
   * HttpExchange} path, by creating an extended language range from the country code coming from
   * the path. The added wildcard will indicate that we wish to get the best matching language for
   * this country code, when locale resolution is performed.
   *
   * @return the function as described above
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc4647#section-2.2">Extended language
   *     range</a>
   */
  private static Function<HttpExchange, CompletionStage<Optional<String>>>
      httpExchangeToAcceptLanguageBasedOnCountryCodeInPath() {
    return (exchange) ->
        CompletableFuture.completedFuture(
            uriToCountryCode(exchange.getRequestURI())
                .map(countryCode -> "*-".concat(countryCode)));
  }

  /**
   * Returns the optional country code (2 positions), from the URI path. It is expected that the
   * country code is present in the last path segment.
   *
   * @param uri The URI from which we want to extract the country code
   * @return the optional country code (2 positions)
   */
  private static Optional<String> uriToCountryCode(final URI uri) {
    final Path path = Paths.get(uri.getPath());
    Iterable<Path> iterable = () -> path.iterator();
    return StreamSupport.stream(iterable.spliterator(), false)
        // Keep only the last segment
        .reduce((ignore, keep) -> keep)
        .map(Path::toString)
        .filter(s -> s.length() == 2);
  }
}
