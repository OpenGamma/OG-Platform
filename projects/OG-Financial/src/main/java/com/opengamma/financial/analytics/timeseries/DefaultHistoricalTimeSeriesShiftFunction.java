/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;

/**
 * Function to inject default historical time series shifts into the dependency graph. Shifts are taken from the default property "MARKET_DATA_SHIFT" in the same manner that market data is adjusted at
 * the start of a view cycle.
 */
public class DefaultHistoricalTimeSeriesShiftFunction extends StaticDefaultPropertyFunction {

  public DefaultHistoricalTimeSeriesShiftFunction() {
    super(ComputationTargetType.PRIMITIVE, AbstractHistoricalTimeSeriesShiftFunction.SHIFT_PROPERTY, false, ValueRequirementNames.HISTORICAL_TIME_SERIES,
        ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return context.getViewCalculationConfiguration().getDefaultProperties().getValues(SingleComputationCycle.MARKET_DATA_SHIFT_PROPERTY);
  }

}
