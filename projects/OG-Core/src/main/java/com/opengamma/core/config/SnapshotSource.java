/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.snapshot.impl.SnapshotItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of configuration elements as accessed by the main application.
 * <p>
 * This interface provides a simple view of configuration as used by most parts of the application.
 * This may be backed by a full-featured config master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface SnapshotSource extends Source<SnapshotItem<?>>, ChangeProvider {

  /**
   * Gets a configuration element by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single configuration at a single version-correction.
   *
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  @Override
  SnapshotItem<?> get(UniqueId uniqueId);

  /**
   * Gets a configuration element by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single configuration at a single version-correction.
   *
   * @param objectId  the object identifier to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  @Override
  SnapshotItem<?> get(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets configuration elements by name and version-correction.
   * <p>
   * Each configuration element has a name and this method allows lookup by name.
   * 
   * @param <T> the type of configuration element
   * @param clazz the configuration element type, not null
   * @param configName the configuration name, not null
   * @param versionCorrection the version-correction, not null
   * @return the elements matching the name, empty if no matches, not null
   * @throws IllegalArgumentException if the name or version-correction is invalid
   * @throws RuntimeException if an error occurs
   */
  <T> Collection<SnapshotItem<T>> get(Class<T> clazz, String configName, VersionCorrection versionCorrection);


}
