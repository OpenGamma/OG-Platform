/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import com.opengamma.core.SourceWithExternalBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;

/**
 * A source of region information as accessed by the main application.
 * <p>
 * This interface provides a simple view of regions as used by most parts of the application. This may be backed by a full-featured region master, or by a much simpler data structure.
 * <p>
 * This interface is read-only. Implementations must be thread-safe.
 */
@PublicSPI
public interface RegionSource extends SourceWithExternalBundle<Region> {

  //-------------------------------------------------------------------------
  // TODO: remove below here
  /**
   * Get the region with a matching identifier that is highest up the region hierarchy, for example US will return USA rather than a dependency.
   * 
   * @param externalId the region identifier to find, not null
   * @return the region, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Region getHighestLevelRegion(ExternalId externalId);

  /**
   * Get the region with at least one identifier from the bundle that is highest up the region hierarchy, for example US will return USA rather than a dependency. US + a more specific identifier for a
   * sub-region will also return US.
   * 
   * @param bundle the bundle of region identifiers to find, not null
   * @return the region, null if not found
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws RuntimeException if an error occurs
   */
  Region getHighestLevelRegion(ExternalIdBundle bundle);

}
