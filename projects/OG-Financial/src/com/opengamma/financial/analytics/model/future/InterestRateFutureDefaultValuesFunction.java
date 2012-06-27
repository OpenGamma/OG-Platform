/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Dummy function for injecting default curve names into the dependency graph.
 */
public class InterestRateFutureDefaultValuesFunction extends DefaultPropertyFunction {
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_THETA};

  private final String[] _applicableCurrencyNames;
  private final String _curveCalculationConfig;

  public InterestRateFutureDefaultValuesFunction(final String curveCalculationConfig, final String... applicableCurrencyNames) {
    super(ComputationTargetType.TRADE, true);
    ArgumentChecker.notNull(curveCalculationConfig, "curve calculation config");
    ArgumentChecker.notNull(applicableCurrencyNames, "applicable currency names");
    _curveCalculationConfig = curveCalculationConfig;
    _applicableCurrencyNames = applicableCurrencyNames;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    if (!(target.getTrade().getSecurity() instanceof InterestRateFutureSecurity)) {
      return false;
    }
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    for (final String applicableCurrencyName : _applicableCurrencyNames) {
      if (applicableCurrencyName.equals(currency)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_curveCalculationConfig);
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return PriorityClass.ABOVE_NORMAL;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.INTEREST_RATE_FUTURE;
  }
}
