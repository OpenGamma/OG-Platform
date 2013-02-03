/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;

/**
 * Functions to shift historical market data values used for yield curve construction, implemented using properties and constraints.
 * 
 * @deprecated see {@link YieldCurveInstrumentConversionHistoricalTimeSeriesFunctionDeprecated}
 */
@Deprecated
public class YieldCurveInstrumentConversionHistoricalTimeSeriesShiftFunctionDeprecated extends AbstractHistoricalTimeSeriesShiftFunction<HistoricalTimeSeriesBundle> {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  protected ValueSpecification getResult(final ComputationTargetSpecification targetSpecification) {
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpecification, createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get());
  }

  @Override
  protected HistoricalTimeSeriesBundle apply(final FunctionExecutionContext context, final OverrideOperation operation, final HistoricalTimeSeriesBundle value, final ValueSpecification valueSpec) {
    return applyOverride(context, operation, value);
  }

}
