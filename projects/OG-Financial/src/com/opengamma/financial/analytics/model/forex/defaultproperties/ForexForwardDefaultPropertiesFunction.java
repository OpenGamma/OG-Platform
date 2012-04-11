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
import com.opengamma.financial.analytics.model.forex.forward.ForexForwardFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexForwardDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES
  };
  private final String _payCurveName;
  private final String _payForwardCurveName;
  private final String _payCurveCalculationMethod;
  private final String _receiveCurveName;
  private final String _receiveForwardCurveName;
  private final String _receiveCurveCalculationMethod;
  private final String _payCurrency;
  private final String _receiveCurrency;

  public ForexForwardDefaultPropertiesFunction(final String payCurveName, final String payForwardCurveName, final String payCurveCalculationMethod, final String receiveCurveName,
      final String receiveForwardCurveName, final String receiveCurveCalculationMethod, final String payCurrency, final String receiveCurrency) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(payCurveName, "pay curve name");
    ArgumentChecker.notNull(payForwardCurveName, "pay forward curve name");
    ArgumentChecker.notNull(payCurveCalculationMethod, "pay curve calculation method");
    ArgumentChecker.notNull(receiveCurveName, "receive curve name");
    ArgumentChecker.notNull(receiveForwardCurveName, "receive forward curve name");
    ArgumentChecker.notNull(receiveCurveCalculationMethod, "receive curve calculation method");
    ArgumentChecker.notNull(payCurrency, "pay currency");
    ArgumentChecker.notNull(receiveCurrency, "receive currency");
    _payCurveName = payCurveName;
    _payForwardCurveName = payForwardCurveName;
    _payCurveCalculationMethod = payCurveCalculationMethod;
    _receiveCurveName = receiveCurveName;
    _receiveForwardCurveName = receiveForwardCurveName;
    _receiveCurveCalculationMethod = receiveCurveCalculationMethod;
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
      defaults.addValuePropertyName(valueRequirement, ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.RECEIVE_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(_payCurveName);
    }
    if (ForexForwardFunction.PROPERTY_PAY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_payForwardCurveName);
    }
    if (ForexForwardFunction.PROPERTY_PAY_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_payCurveCalculationMethod);
    }
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(_receiveCurveName);
    }
    if (ForexForwardFunction.PROPERTY_RECEIVE_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_receiveForwardCurveName);
    }
    if (ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_receiveCurveCalculationMethod);
    }
    return null;
  }
}
