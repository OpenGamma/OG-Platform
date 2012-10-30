/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.engine.ComputationTargetType;

/**
 * Function which exposes the latest portfolio node value of an historical time-series for a given data field under a
 * different value name.
 */
public class AliasedHistoricalTimeSeriesLatestPortfolioNodeValueFunction extends AliasedHistoricalTimeSeriesLatestValueFunction {

  /**
   * Constructs an instance
   * 
   * @param htsDataField  the historical time-series data field, not null
   * @param aliasedValueRequirementName  the value requirement name under which to expose the output, not null
   */
  public AliasedHistoricalTimeSeriesLatestPortfolioNodeValueFunction(String htsDataField, String aliasedValueRequirementName) {
    super(htsDataField, aliasedValueRequirementName, ComputationTargetType.PORTFOLIO_NODE);
  }

}
