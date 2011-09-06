/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;

/**
 * Function to classify positions by Currency.
 *
 */
public class RegionAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Region";
  private static final String NO_REGION = "N/A";
  private RegionSource _regionSource;
  
  public RegionAggregationFunction(RegionSource regionSource) {
    _regionSource = regionSource;
  }
  
  /**
   * Can use this when no RegionSource available and will get the ISO code instead of the pretty string.
   */
  public RegionAggregationFunction() {
    _regionSource = null;
  }
  
  @Override
  public String classifyPosition(Position position) {
    try {
      ExternalId id = FinancialSecurityUtils.getRegion(position.getSecurity());
      if (_regionSource != null) {
        if (id != null) {
          Region highestLevelRegion = _regionSource.getHighestLevelRegion(id);
          if (highestLevelRegion != null) {
            return highestLevelRegion.getName();
          } else {
            return id.getValue();
          }
        } else {
          return NO_REGION;
        }
      } else {
        if (id != null) {
          return id.getValue();
        } else {
          return NO_REGION;
        }
      }    
    } catch (UnsupportedOperationException ex) {
      return NO_REGION;
    }
  }

  public String getName() {
    return NAME;
  }
}
