/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.SourceWithExternalBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of convention information as accessed by the main application.
 * <p>
 * This interface provides a simple read-only view of conventions.
 * This may be backed by a full-featured convention master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface ConventionSource
    extends SourceWithExternalBundle<Convention> {
  // NOTE: overrides require DataNotFoundException to be thrown

  /**
   * Gets an object by unique identifier, ensuring it is of a particular type.
   * <p>
   * This retrieves the object stored using the unique identifier.
   * If not found, an exception is thrown.
   * 
   * @param <T>  the type of the convention to get
   * @param uniqueId  the unique identifier to search for, not null
   * @param type  the type of the convention to get, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  <T extends Convention> T get(UniqueId uniqueId, Class<T> type);

  /**
   * Gets an object by object identifier and version-correction, ensuring it is of a particular type.
   * <p>
   * This retrieves the object stored using the object identifier at the instant
   * specified by the version-correction. If not found, an exception is thrown.
   * In combination, the object identifier and version-correction are equivalent to
   * a unique identifier.
   * 
   * @param <T>  the type of the convention to get
   * @param objectId  the object identifier to search for, not null
   * @param type  the type of the convention to get, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  <T extends Convention> T get(ObjectId objectId, VersionCorrection versionCorrection, Class<T> type);

  /**
   * Gets an object by external identifier bundle and version-correction.
   * <p>
   * This retrieves the object stored using the external identifier at the instant
   * specified by the version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier bundle represents those keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param bundle  the external identifier bundle to search for, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  @Override
  Convention getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  /**
   * Gets an object by external identifier bundle and version-correction, ensuring it is of a particular type.
   * <p>
   * This retrieves the object stored using the external identifier at the instant
   * specified by the version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier bundle represents those keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param <T>  the type of the convention to get
   * @param bundle  the external identifier bundle to search for, not null
   * @param versionCorrection  the version-correction, not null
   * @param type  the type of the convention to get, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  <T extends Convention> T getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection, Class<T> type);

  //-------------------------------------------------------------------------
  // these methods are primarily for use by the engine
  // which locks the version-correction behind the scenes
  /**
   * Gets an object by external identifier at the latest version-correction.
   * <p>
   * This retrieves the object stored using the external identifier at the latest
   * version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier represents one of the keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param externalId  the external identifier to search for, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  Convention getSingle(ExternalId externalId);

  /**
   * Gets an object by external identifier at the latest version-correction, ensuring it is of a particular type.
   * <p>
   * This retrieves the object stored using the external identifier at the latest
   * version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier represents one of the keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param <T>  the type of the convention to get
   * @param externalId  the external identifier to search for, not null
   * @param type  the type of the convention to get, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  <T extends Convention> T getSingle(ExternalId externalId, Class<T> type);

  /**
   * Gets an object by external identifier bundle at the latest version-correction.
   * <p>
   * This retrieves the object stored using the external identifier at the latest
   * version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier bundle represents those keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param bundle  the external identifier bundle to search for, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  @Override
  Convention getSingle(ExternalIdBundle bundle);

  /**
   * Gets an object by external identifier bundle at the latest version-correction, ensuring it is of a particular type.
   * <p>
   * This retrieves the object stored using the external identifier at the latest
   * version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier bundle represents those keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param <T>  the type of the convention to get
   * @param bundle  the external identifier bundle to search for, not null
   * @param type  the type of the convention to get, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  <T extends Convention> T getSingle(ExternalIdBundle bundle, Class<T> type);

  //-------------------------------------------------------------------------
  /**
   * Gets objects by external identifier bundle at the latest version-correction.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single object.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single object.
   * The default behavior in standard implementations should be to return any
   * element with <strong>any</strong> external identifier that matches <strong>any</strong>
   * identifier in the bundle. While specific implementations may modify this behavior,
   * this should be explicitly documented to avoid confusion.
   *
   * @param bundle  the external identifier bundle to search for, not null
   * @return all objects matching the bundle, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   * @deprecated Use {@link #get(ExternalIdBundle, VersionCorrection)}
   */
  @Deprecated
  @Override
  Collection<Convention> get(ExternalIdBundle bundle);

}
