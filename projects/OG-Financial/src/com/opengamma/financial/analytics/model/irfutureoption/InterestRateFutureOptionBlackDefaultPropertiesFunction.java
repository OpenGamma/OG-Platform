/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

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
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateFutureOptionBlackDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] s_valueRequirements = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.IMPLIED_VOLATILITY,
    ValueRequirementNames.SECURITY_MODEL_PRICE,
    ValueRequirementNames.UNDERLYING_MODEL_PRICE,
    ValueRequirementNames.DAILY_PRICE,
    ValueRequirementNames.VALUE_THETA
  };
  private final String _surfaceName;
  private final String _curveCalculationConfig;
  private final String[] _applicableCurrencies;

  public InterestRateFutureOptionBlackDefaultPropertiesFunction(final String curveCalculationConfig, final String surfaceName, final String... applicableCurrencies) {
    super(ComputationTargetType.TRADE, true);
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(curveCalculationConfig, "curve calculation config");
    ArgumentChecker.notNull(applicableCurrencies, "applicable currencies");
    _surfaceName = surfaceName;
    _curveCalculationConfig = curveCalculationConfig;
    _applicableCurrencies = applicableCurrencies;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getTrade().getSecurity() instanceof IRFutureOptionSecurity)) {
      return false;
    }
    final IRFutureOptionSecurity irFutureOption = (IRFutureOptionSecurity) target.getTrade().getSecurity();
    final String currency = irFutureOption.getCurrency().getCode();
    for (final String applicableCurrency : _applicableCurrencies) {
      if (applicableCurrency.equals(currency)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : s_valueRequirements) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
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
    return OpenGammaFunctionExclusions.IR_FUTURE_OPTION_BLACK;
  }
}
