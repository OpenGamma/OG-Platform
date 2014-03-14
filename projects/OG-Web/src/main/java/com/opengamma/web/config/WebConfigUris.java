/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.net.URI;

import com.opengamma.id.UniqueId;

/**
 * URIs for web-based configuration management.
 */
public class WebConfigUris {

  /**
   * The data.
   */
  private final WebConfigData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebConfigUris(WebConfigData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return configs();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI configs() {
    return WebConfigsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI config() {
    return WebConfigResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param configId  the config id, not null
   * @return the URI
   */
  public URI config(final UniqueId configId) {
    return WebConfigResource.uri(_data, configId);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI configVersions() {
    return WebConfigVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI configVersion() {
    return WebConfigVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param configId  the config id, not null
   * @return the URI
   */
  public URI configVersion(final UniqueId configId) {
    return WebConfigVersionResource.uri(_data, configId);
  }

}
