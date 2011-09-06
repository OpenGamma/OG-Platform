/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;

/**
 * Function to classify positions by Currency.
 *
 */
public class RegionAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Region";
  private static final String NO_REGION = "N/A";
  
  @Override
  public String classifyPosition(Position position) {
    try {
      ExternalId id = FinancialSecurityUtils.getRegion(position.getSecurity());
      return id != null ? id.getValue() : NO_REGION; 
    } catch (UnsupportedOperationException ex) {
      return "No or multiple regions";
    }
  }

  public String getName() {
    return NAME;
  }
}
