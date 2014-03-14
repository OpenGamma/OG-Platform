/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import java.net.URI;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * URIs for web-based exchanges.
 */
public class WebExchangeUris {

  /**
   * The data.
   */
  private final WebExchangeData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebExchangeUris(WebExchangeData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI base() {
    return exchanges();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI exchanges() {
    return WebExchangesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param identifier  the identifier to search for, may be null
   * @return the URI
   */
  public URI exchanges(final ExternalId identifier) {
    return WebExchangesResource.uri(_data, ExternalIdBundle.of(identifier));
  }

  /**
   * Gets the URI.
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI
   */
  public URI exchanges(final ExternalIdBundle identifiers) {
    return WebExchangesResource.uri(_data, identifiers);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI exchange() {
    return WebExchangeResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param exchange  the exchange, not null
   * @return the URI
   */
  public URI exchange(final Exchange exchange) {
    return WebExchangeResource.uri(_data, exchange.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI exchangeVersions() {
    return WebExchangeVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI exchangeVersion() {
    return WebExchangeVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param exchange  the exchange, not null
   * @return the URI
   */
  public URI exchangeVersion(final Exchange exchange) {
    return WebExchangeVersionResource.uri(_data, exchange.getUniqueId());
  }

}
