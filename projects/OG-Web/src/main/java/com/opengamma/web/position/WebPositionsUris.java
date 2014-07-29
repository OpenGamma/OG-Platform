/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.net.URI;

import com.opengamma.master.position.ManageablePosition;

/**
 * URIs for web-based positions.
 */
public class WebPositionsUris {

  /**
   * The data.
   */
  private final WebPositionsData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebPositionsUris(WebPositionsData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return positions();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI positions() {
    return WebPositionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI position() {
    return WebPositionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param position  the position, not null
   * @return the URI
   */
  public URI position(final ManageablePosition position) {
    return WebPositionResource.uri(_data, position.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI positionVersions() {
    return WebPositionVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI positionVersion() {
    return WebPositionVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param position  the position, not null
   * @return the URI
   */
  public URI positionVersion(final ManageablePosition position) {
    return WebPositionVersionResource.uri(_data, position.getUniqueId());
  }

}
