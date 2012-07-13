/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.value.ValueRequirementAliasFunction;

/**
 * Function which exposes the latest value of an historical time-series for a given data field under a different value
 * name.
 */
public class AliasedHistoricalTimeSeriesLatestValueFunction extends ValueRequirementAliasFunction {

  /**
   * Constructs an instance
   * 
   * @param htsDataField  the historical time-series data field, not null
   * @param aliasedValueRequirementName  the value requirement name under which to expose the output, not null
   * @param targetType  the function target type, not null
   */
  public AliasedHistoricalTimeSeriesLatestValueFunction(String htsDataField, String aliasedValueRequirementName, ComputationTargetType targetType) {
    super(aliasedValueRequirementName,
        ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST,
        ValueProperties.with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, htsDataField).get(),
        targetType);
  }

}
