/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of region information as accessed by the main application.
 * <p>
 * This interface provides a simple view of regions as used by most parts of the application.
 * This may be backed by a full-featured region master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface RegionSource {

  /**
   * Gets a region by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single region at a single version-correction.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched region, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the region could not be found
   * @throws RuntimeException if an error occurs
   */
  Region getRegion(UniqueId uniqueId);

  /**
   * Gets a region by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single region at a single version-correction.
   * 
   * @param objectId  the object identifier to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched region, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the region could not be found
   * @throws RuntimeException if an error occurs
   */
  Region getRegion(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets all regions at the given version-correction that match the specified
   * external identifier bundle.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single region.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single region.
   * This method returns all regions that may match for {@link RegionResolver} to choose from.
   * 
   * @param bundle  the external identifier bundle to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return all regions matching the bundle, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws DataNotFoundException if the region could not be found
   * @throws RuntimeException if an error occurs
   */
  Collection<? extends Region> getRegions(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  // TODO: remove below here
  /**
   * Get the region with a matching identifier that is highest up the
   * region hierarchy, for example US will return USA rather than a dependency.
   * 
   * @param externalId  the region identifier to find, not null
   * @return the region, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Region getHighestLevelRegion(ExternalId externalId);

  /**
   * Get the region with at least one identifier from the bundle that is highest up the
   * region hierarchy, for example US will return USA rather than a dependency.
   * US + a more specific identifier for a sub-region will also return US.
   * 
   * @param bundle  the bundle of region identifiers to find, not null
   * @return the region, null if not found
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws RuntimeException if an error occurs
   */
  Region getHighestLevelRegion(ExternalIdBundle bundle);

}
