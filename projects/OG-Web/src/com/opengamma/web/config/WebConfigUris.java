/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.net.URI;

import com.opengamma.id.UniqueIdentifier;

/**
 * URIs for web-based configuration management.
 */
public class WebConfigUris {

  /**
   * The data.
   */
  private final WebConfigData<?> _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebConfigUris(WebConfigData<?> data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
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
  public URI configTypes() {
    return WebConfigTypesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param typeStr  the element type, may be null
   * @return the URI
   */
  public URI configTypes(final String typeStr) {
    return WebConfigTypesResource.uri(_data, typeStr);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI configType() {
    return WebConfigTypeResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param configId  the config id, not null
   * @return the URI
   */
  public URI configType(final UniqueIdentifier configId) {
    return WebConfigTypeResource.uri(_data, configId);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI configTypeVersions() {
    return WebConfigTypeVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI configTypeVersion() {
    return WebConfigTypeVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param configId  the config id, not null
   * @return the URI
   */
  public URI configTypeVersion(final UniqueIdentifier configId) {
    return WebConfigTypeVersionResource.uri(_data, configId);
  }

}
