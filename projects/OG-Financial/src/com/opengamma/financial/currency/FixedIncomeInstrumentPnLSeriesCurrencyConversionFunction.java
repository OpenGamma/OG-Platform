/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.pnl.YieldCurveNodePnLFunction;

/**
 * 
 */
public class FixedIncomeInstrumentPnLSeriesCurrencyConversionFunction extends PnlSeriesCurrencyConversionFunction {

  public FixedIncomeInstrumentPnLSeriesCurrencyConversionFunction(final String currencyMatrixName) {
    super(currencyMatrixName);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  protected ValueSpecification getValueSpec(final ValueSpecification inputSpec, final String currencyCode) {
    final ValueProperties properties = inputSpec.getProperties().copy()
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId())
        .withoutAny(ValuePropertyNames.CURRENCY).with(ValuePropertyNames.CURRENCY, currencyCode).get();
    return new ValueSpecification(ValueRequirementNames.PNL_SERIES, inputSpec.getTargetSpecification(), properties);
  }
}
