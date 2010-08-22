/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.world;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Convenience interface to access a RegionMaster
 */
public interface RegionSource {
  /**
   * Get the region with a matching identifier that is highest up the
   * region hierarchy e.g. US will return USA rather than a dependency.
   * @param regionId a region identifier
   * @return the region
   */
  Region getHighestLevelRegion(Identifier regionId);
  /**
   * Get the region with at least one identifier from the bundle that is highest up the
   * region hierarchy e.g. US will return USA rather than a dependency.  US + a more specific
   * identifier for a sub-region will return US also.
   * @param regionIdentifiers a bundle of region identifiers
   * @return the region
   */
  Region getHighestLevelRegion(IdentifierBundle regionIdentifiers);
  /**
   * Get a region using a Unique Identifier
   * @param regionUniqueId the unique identifier for the region
   * @return the region
   */
  Region getRegion(UniqueIdentifier regionUniqueId);
}
