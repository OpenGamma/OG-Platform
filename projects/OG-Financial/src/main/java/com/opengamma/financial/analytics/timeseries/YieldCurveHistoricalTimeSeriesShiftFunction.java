/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * Function to shift a bundle of historical market data values, implemented using properties and constraints.
 */
public class YieldCurveHistoricalTimeSeriesShiftFunction extends AbstractHistoricalTimeSeriesShiftFunction<HistoricalTimeSeriesBundle> {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!super.canApplyTo(context, target)) {
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  protected ValueSpecification getResult(final ComputationTargetSpecification targetSpecification) {
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, targetSpecification, createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY).get());
  }

  @Override
  protected HistoricalTimeSeriesBundle apply(final FunctionExecutionContext context, final OverrideOperation operation, final HistoricalTimeSeriesBundle value, final ValueSpecification valueSpec) {
    return applyOverride(context, operation, value);
  }

}
