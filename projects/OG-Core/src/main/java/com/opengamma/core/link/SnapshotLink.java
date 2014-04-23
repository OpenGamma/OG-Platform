/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.service.ServiceContext;

/**
 * Represents a link to a Snapshot object using the object's name which is resolved on demand.
 * Use of links allows provision of Snapshot objects by remote servers while maintaining the
 * ability to capture updates to the linked resources on each subsequent resolution.
 *
 * @param <T> type of the snapshot
 */
public abstract class SnapshotLink<T> implements Link<T> {

  /**
   * No arg constructor for the use of subclasses in the package.
   */
  /* package */ SnapshotLink() {
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to access a
   * pre-configured service context containing the SnapshotSource and VersionCorrectionProvider
   * necessary to resolve the provided bundle into the target object.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the snapshot object, not null
   * @param type the type of object being linked to, not null
   * @return a snapshot link, not null
   */
  public static <C> SnapshotLink<C> resolvable(String name, Class<C> type) {
    return new ResolvableSnapshotLink<>(name, type, new ServiceContextSnapshotLinkResolver<C>());
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to access a
   * pre-configured service context containing the SnapshotSource and VersionCorrectionProvider
   * necessary to resolve the provided bundle into the target object.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the snapshot object, not null
   * @param type the type of object being linked to, not null
   * @param serviceContext a service context containing the snapshotSource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return a snapshot link, not null
   */
  public static <C> SnapshotLink<C> resolvable(String name, Class<C> type, ServiceContext serviceContext) {
    return new ResolvableSnapshotLink<>(name, type, new ServiceContextSnapshotLinkResolver<C>(serviceContext));
  }

  /**
   * Creates a link that embeds the provided object directly. Note that if the
   * embedded object has come from a source, there is no way of listening for
   * changes to the object.
   *
   * @param <C> the type of the object being linked to, not null
   * @param snapshot the snapshot to embed in the link, not null
   * @return the snapshot link, not null
   */
  public static <C> SnapshotLink<C> resolved(C snapshot) {
    return new ResolvedSnapshotLink<>(snapshot);
  }
}
