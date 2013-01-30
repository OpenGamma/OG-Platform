/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.target.ComputationTargetType;
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
   */
  public AliasedHistoricalTimeSeriesLatestValueFunction(final String htsDataField, final String aliasedValueRequirementName) {
    super(aliasedValueRequirementName,
        ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST,
        ValueProperties.with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, htsDataField).get(),
        ImmutableSet.of(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY),
        ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION));
  }

}
