/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.forward.ForexForwardFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexForwardDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_THETA
  };
  private final String _payCurveName;
  private final String _receiveCurveName;
  private final String _payCurveConfig;
  private final String _receiveCurveConfig;
  private final String _payCurrency;
  private final String _receiveCurrency;

  public ForexForwardDefaults(final String payCurveName, final String receiveCurveName, final String payCurveConfig, final String receiveCurveConfig,
      final String payCurrency, final String receiveCurrency) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(payCurveName, "pay curve name");
    ArgumentChecker.notNull(receiveCurveName, "receive curve name");
    ArgumentChecker.notNull(payCurveConfig, "pay curve config");
    ArgumentChecker.notNull(receiveCurveConfig, "receive curve config");
    ArgumentChecker.notNull(payCurrency, "pay currency");
    ArgumentChecker.notNull(receiveCurrency, "receive currency");
    _payCurveName = payCurveName;
    _receiveCurveName = receiveCurveName;
    _payCurveConfig = payCurveConfig;
    _receiveCurveConfig = receiveCurveConfig;
    _payCurrency = payCurrency;
    _receiveCurrency = receiveCurrency;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (!(target.getSecurity() instanceof FXForwardSecurity)) {
      return false;
    }
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    return payCurrency.getCode().equals(_payCurrency) && receiveCurrency.getCode().equals(_receiveCurrency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.PAY_CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.RECEIVE_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexForwardFunction.PAY_CURVE_CALC_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ForexForwardFunction.RECEIVE_CURVE_CALC_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(_payCurveName);
    }
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(_receiveCurveName);
    }
    if (ForexForwardFunction.PAY_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(_payCurveConfig);
    }
    if (ForexForwardFunction.RECEIVE_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(_receiveCurveConfig);
    }
    return null;
  }
  
  @Override
  public PriorityClass getPriority() {
    return PriorityClass.ABOVE_NORMAL;
  }
  
  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.FX_FORWARD_DEFAULTS;
  }
}
