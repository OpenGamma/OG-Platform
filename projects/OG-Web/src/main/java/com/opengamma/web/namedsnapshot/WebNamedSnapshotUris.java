/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.namedsnapshot;

import java.net.URI;

import com.opengamma.id.UniqueId;

/**
 * URIs for web-based snapshot management.
 */
public class WebNamedSnapshotUris {

  /**
   * The data.
   */
  private final WebNamedSnapshotData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebNamedSnapshotUris(WebNamedSnapshotData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
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
    return WebNamedSnapshotsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI snapshot() {
    return WebNamedSnapshotResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param snapshotId  the snapshot id, not null
   * @return the URI
   */
  public URI snapshot(final UniqueId snapshotId) {
    return WebNamedSnapshotResource.uri(_data, snapshotId);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI snapshotVersions() {
    return WebNamedSnapshotVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI snapshotVersion() {
    return WebNamedSnapshotVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param snapshotId  the snapshot id, not null
   * @return the URI
   */
  public URI snapshotVersion(final UniqueId snapshotId) {
    return WebNamedSnapshotVersionResource.uri(_data, snapshotId);
  }

}
