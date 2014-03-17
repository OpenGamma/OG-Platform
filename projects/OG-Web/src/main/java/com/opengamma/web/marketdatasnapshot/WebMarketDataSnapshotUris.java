/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.marketdatasnapshot;

import java.net.URI;

import com.opengamma.id.UniqueId;

/**
 * URIs for web-based market data snapshot management.
 */
public class WebMarketDataSnapshotUris {

  /**
   * The data.
   */
  private final WebMarketDataSnapshotData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebMarketDataSnapshotUris(WebMarketDataSnapshotData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return snapshots();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI snapshots() {
    return WebMarketDataSnapshotsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI snapshot() {
    return WebMarketDataSnapshotResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param snapshotId  the snapshot id, not null
   * @return the URI
   */
  public URI snapshot(final UniqueId snapshotId) {
    return WebMarketDataSnapshotResource.uri(_data, snapshotId);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI snapshotVersions() {
    return WebMarketDataSnapshotVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI snapshotVersion() {
    return WebMarketDataSnapshotVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param snapshotId  the snapshot id, not null
   * @return the URI
   */
  public URI snapshotVersion(final UniqueId snapshotId) {
    return WebMarketDataSnapshotVersionResource.uri(_data, snapshotId);
  }

}
