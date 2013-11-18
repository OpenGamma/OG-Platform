/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;

/**
 * Function to inject default yield curve market data shifts into the dependency graph. Shifts are taken from the default properties in the following order:
 * <ul>
 * <li><em>curve name</em>_<em>currency</em>_YIELD_CURVE_MARKET_DATA_SHIFT
 * <li><em>curve_name</em>_YIELD_CURVE_MARKET_DATA_SHIFT
 * <li><em>currency</em>_YIELD_CURVE_MARKET_DATA_SHIFT
 * <li>YIELD_CURVE_MARKET_DATA_SHIFT
 * </ul>
 * This should allow either specific curves to be adjusted or more global changes.
 */
public class DefaultYieldCurveMarketDataShiftFunction extends StaticDefaultPropertyFunction {

  /** Property to shift all yield curve market data. */
  protected static final String YIELD_CURVE_MARKET_DATA_SHIFT = "YIELD_CURVE_MARKET_DATA_" + YieldCurveShiftFunction.SHIFT;

  public DefaultYieldCurveMarketDataShiftFunction() {
    super(ComputationTargetType.CURRENCY, YieldCurveMarketDataShiftFunction.SHIFT, false, ValueRequirementNames.YIELD_CURVE_MARKET_DATA);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String currency = target.getUniqueId().getValue();
    final ValueProperties defaults = context.getViewCalculationConfiguration().getDefaultProperties();
    final String name = desiredValue.getConstraints().getStrictValue(ValuePropertyNames.CURVE);
    Set<String> values;
    if (name != null) {
      values = defaults.getValues(name + "_" + currency + "_" + YIELD_CURVE_MARKET_DATA_SHIFT);
      if (values != null) {
        return values;
      }
      values = defaults.getValues(name + "_" + YIELD_CURVE_MARKET_DATA_SHIFT);
      if (values != null) {
        return values;
      }
    }
    values = defaults.getValues(currency + "_" + YIELD_CURVE_MARKET_DATA_SHIFT);
    if (values != null) {
      return values;
    }
    return defaults.getValues(YIELD_CURVE_MARKET_DATA_SHIFT);
  }

}
