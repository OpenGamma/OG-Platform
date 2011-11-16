/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of security information as accessed by the main application.
 * <p>
 * This interface provides a simple view of securities as needed by the engine.
 * This may be backed by a full-featured security master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface SecuritySource extends ChangeProvider {

  /**
   * Gets a security by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single security at a single version-correction.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched security, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the security could not be found
   * @throws RuntimeException if an error occurs
   */
  Security getSecurity(UniqueId uniqueId);
  
  /**
   * Potentially more efficient form of {@link #getSecurity} for multiple lookups.
   * 
   * @param uniqueIds the unique identifiers to query, not null
   * @return map of results. If there is no data for an identifier it will be missing from the map. 
   */
  Map<UniqueId, Security> getSecurity(Collection<UniqueId> uniqueIds);
  
  /**
   * Gets a security by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single security at a single version-correction.
   * 
   * @param objectId  the object identifier to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched security, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the security could not be found
   * @throws RuntimeException if an error occurs
   */
  Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets all securities at the given version-correction that match the specified
   * external identifier bundle.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single security.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single security.
   * This method returns all securities that may match for {@link SecurityResolver} to choose from.
   * 
   * @param bundle  the bundle keys to match, not null
   * @param versionCorrection  the version-correction, not null
   * @return all securities matching the bundle, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws RuntimeException if an error occurs
   */
  Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  // TODO: remove below here
  /**
   * Gets all securities at the latest version-correction that match the specified
   * bundle of keys.
   * <p>
   * The identifier bundle represents those keys associated with a single security.
   * In an ideal world, all the identifiers in a bundle would refer to the same security.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * <p>
   * The simplest implementation of this method will return a security if it matches one of the keys.
   * A more advanced implementation will choose using some form of priority order which
   * key or keys from the bundle to search for.
   * 
   * @param bundle  the bundle keys to match, not null
   * @return all securities matching the specified key, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid (e.g. empty)
   * @throws RuntimeException if an error occurs
   */
  Collection<Security> getSecurities(ExternalIdBundle bundle);

  /**
   * Gets the single best-fit security at the latest version-correction that matches the
   * specified bundle of keys.
   * <p>
   * The identifier bundle represents those keys associated with a single security.
   * In an ideal world, all the identifiers in a bundle would refer to the same security.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * <p>
   * An implementation will need some mechanism to decide what the best-fit match is.
   * 
   * @param bundle  the bundle keys to match, not null
   * @return the single security matching the bundle of keys, null if not found
   * @throws IllegalArgumentException if the identifier bundle is invalid (e.g. empty)
   * @throws RuntimeException if an error occurs
   */
  Security getSecurity(ExternalIdBundle bundle);

  /**
   * Gets the single best-fit security at the given version-correction that matches the
   * specified bundle of keys.
   * <p>
   * The identifier bundle represents those keys associated with a single security.
   * In an ideal world, all the identifiers in a bundle would refer to the same security.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * <p>
   * An implementation will need some mechanism to decide what the best-fit match is. 
   * 
   * @param bundle  the bundle keys to match, not null
   * @param versionCorrection  the version-correction, not null
   * @return the single security matching the bundle of keys, null if not found
   * @throws IllegalArgumentException if the identifier bundle is invalid (e.g. empty)
   * @throws RuntimeException if an error occurs
   */
  Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection);

}
