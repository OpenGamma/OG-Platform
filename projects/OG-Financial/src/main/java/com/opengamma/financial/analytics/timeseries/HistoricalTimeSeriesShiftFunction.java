/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;

/**
 * Function to shift historical market data values, implemented using properties and constraints.
 */
public class HistoricalTimeSeriesShiftFunction extends AbstractHistoricalTimeSeriesShiftFunction<HistoricalTimeSeries> {

  @Override
  protected ValueSpecification getResult(final ComputationTargetSpecification targetSpecification) {
    return new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, targetSpecification, createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY).get());
  }

  @Override
  protected HistoricalTimeSeries apply(final FunctionExecutionContext context, final OverrideOperation operation, final HistoricalTimeSeries value, final ValueSpecification valueSpec) {
    final HistoricalTimeSeriesSource htsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(context);
    return applyOverride(context, operation, valueSpec.getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY), htsSource.getExternalIdBundle(value.getUniqueId()), value);
  }

}
