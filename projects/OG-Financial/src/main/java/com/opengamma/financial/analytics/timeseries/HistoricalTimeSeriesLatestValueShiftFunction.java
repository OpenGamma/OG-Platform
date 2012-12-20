/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.id.ExternalIdBundle;

/**
 * Function to shift the latest historical market data values, implemented using properties and constraints.
 */
public class HistoricalTimeSeriesLatestValueShiftFunction extends AbstractHistoricalTimeSeriesShiftFunction<Double> {

  @Override
  protected ValueSpecification getResult(final ComputationTargetSpecification targetSpecification) {
    return new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, targetSpecification, createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY).get());
  }

  @Override
  protected Double apply(final FunctionExecutionContext context, final OverrideOperation operation, final Double value, final ValueSpecification valueSpec) {
    final HistoricalTimeSeriesSource htsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(context);
    final ExternalIdBundle ids = htsSource.getExternalIdBundle(valueSpec.getTargetSpecification().getUniqueId());
    return applyOverride(context, operation, valueSpec.getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY), ids, value);
  }

}
