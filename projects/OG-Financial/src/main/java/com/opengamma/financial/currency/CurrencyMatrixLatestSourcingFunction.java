/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;

/**
 * Injects the last point from a time series implied from a value from a {@link CurrencyMatrix} into a dependency graph.
 */
public class CurrencyMatrixLatestSourcingFunction extends CurrencyMatrixSeriesSourcingFunction {

  public CurrencyMatrixLatestSourcingFunction() {
    super(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
  }

  @Override
  protected Object getRate(CurrencyMatrix matrix, ValueRequirement desiredValue, FunctionExecutionContext executionContext, FunctionInputs inputs, Currency source, Currency target) {
    final Object fxSeries = super.getRate(matrix, desiredValue, executionContext, inputs, source, target);
    if (fxSeries == null) {
      return null;
    }
    if (fxSeries instanceof DoubleTimeSeries) {
      final DoubleTimeSeries<?> ts = (DoubleTimeSeries<?>) fxSeries;
      return ts.getLatestValue();
    }
    throw new IllegalArgumentException("Expected timeseries, got " + fxSeries);
  }

  public static ValueRequirement getConversionRequirement(final Currency source, final Currency target) {
    return new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, CurrencyPair.TYPE.specification(CurrencyPair.of(target, source)));
  }

  public static ValueRequirement getConversionRequirement(final String source, final String target) {
    return getConversionRequirement(Currency.of(source), Currency.of(target));
  }

}
