/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

import java.net.URI;

import com.opengamma.id.UniqueId;

/**
 * URIs for web-based convention management.
 */
public class WebConventionUris {

  /**
   * The data.
   */
  private final WebConventionData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebConventionUris(WebConventionData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI base() {
    return conventions();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI conventions() {
    return WebConventionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI convention() {
    return WebConventionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param conventionId  the convention id, not null
   * @return the URI
   */
  public URI convention(final UniqueId conventionId) {
    return WebConventionResource.uri(_data, conventionId);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI conventionVersions() {
    return WebConventionVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI conventionVersion() {
    return WebConventionVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param conventionId  the convention id, not null
   * @return the URI
   */
  public URI conventionVersion(final UniqueId conventionId) {
    return WebConventionVersionResource.uri(_data, conventionId);
  }

}
