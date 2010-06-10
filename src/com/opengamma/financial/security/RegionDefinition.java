/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Set;

/*package*/ class RegionDefinition {
  /**
   * @return the region
   */
  public RegionNode getRegion() {
    return _region;
  }

  /**
   * @return the subRegionNames
   */
  public Set<String> getSubRegionNames() {
    return _subRegionNames;
  }

  private RegionNode _region;
  private Set<String> _subRegionNames;

  /**
   * @param region
   * @param superRegionName
   * @param subRegionNames
   */
  public RegionDefinition(RegionNode region, Set<String> subRegionNames) {
    _region = region;
    _subRegionNames = subRegionNames;
  }
}
