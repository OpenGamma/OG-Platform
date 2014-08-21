/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.PublicAPI;


/**
 * Utilities and constants for {@code Region}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class RegionUtils {
  
  /**
   * Restricted constructor.
   */
  protected RegionUtils() {
  }


  /**
   * Creates a set of regions from a region id.
   * This is useful in the case where the region is compound (e.g. NY+LON).
   * 
   * @param regionSource The region source, not null
   * @param regionId The region id, not null
   * @return a set of the region(s)
   */
  public static Set<Region> getRegions(RegionSource regionSource, final ExternalId regionId) {
    Validate.notNull(regionSource, "region source");
    Validate.notNull(regionId, "region id");
    if (regionId.isScheme(ExternalSchemes.FINANCIAL) && regionId.getValue().contains("+")) {
      final String[] regions = regionId.getValue().split("\\+");
      final Set<Region> resultRegions = new HashSet<Region>();
      for (final String region : regions) {
        resultRegions.add(regionSource.getHighestLevelRegion(ExternalSchemes.financialRegionId(region)));
      }
      return resultRegions;
    }
    return Collections.singleton(regionSource.getHighestLevelRegion(regionId));
  }

}
