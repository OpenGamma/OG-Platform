/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
/**
 * 
 */
public class DefaultRegionSource implements RegionSource {
  private RegionMaster _regionMaster;
  
  public DefaultRegionSource(RegionMaster regionMaster) {
    _regionMaster = regionMaster;
  }
  
  @Override
  public Region getHighestLevelRegion(Identifier regionId) {
    return _regionMaster.searchRegions(new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME, regionId)).getBestResult();
  }

  @Override
  public Region getHighestLevelRegion(IdentifierBundle regionIdentifiers) {
    return _regionMaster.searchRegions(new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME, regionIdentifiers)).getBestResult();
  }

  @Override
  public Region getRegion(UniqueIdentifier regionUniqueId) {
    return _regionMaster.getRegion(regionUniqueId);
  }

}
