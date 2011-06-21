/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A source of region information as accessed by the main application.
 * <p>
 * This interface provides a simple view of regions as used by most parts of the application.
 * This may be backed by a full-featured region master, or by a much simpler data structure.
 */
@PublicSPI
public interface RegionSource {

  /**
   * Finds a specific region by unique identifier.
   * 
   * @param uniqueId  the unique identifier, null returns null
   * @return the region, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Region getRegion(UniqueIdentifier uniqueId);

  /**
   * Get the region with a matching identifier that is highest up the
   * region hierarchy e.g. US will return USA rather than a dependency.
   * 
   * @param regionId a region identifier
   * @return the region, null if not found
   */
  Region getHighestLevelRegion(Identifier regionId);

  /**
   * Get the region with at least one identifier from the bundle that is highest up the
   * region hierarchy e.g. US will return USA rather than a dependency.  US + a more specific
   * identifier for a sub-region will return US also.
   * 
   * @param regionIdentifiers a bundle of region identifiers
   * @return the region, null if not found
   */
  Region getHighestLevelRegion(IdentifierBundle regionIdentifiers);

}
