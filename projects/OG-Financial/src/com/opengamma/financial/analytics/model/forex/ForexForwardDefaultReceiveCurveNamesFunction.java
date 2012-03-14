/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
//TODO don't like the split between pay and receive
public class ForexForwardDefaultReceiveCurveNamesFunction extends DefaultPropertyFunction {
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES};
  private final String _curveName;
  private final String _curveCalculationMethod;
  private final String[] _applicableCurrencyNames;

  public ForexForwardDefaultReceiveCurveNamesFunction(final String curveName, final String curveCalculationMethod, final String... applicableCurrencyNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(curveName, "curve name");
    ArgumentChecker.notNull(curveCalculationMethod, "curve calculation method");
    ArgumentChecker.notNull(applicableCurrencyNames, "currency names");
    _curveName = curveName;
    _curveCalculationMethod = curveCalculationMethod;
    _applicableCurrencyNames = applicableCurrencyNames;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    if (!(security instanceof FXForwardSecurity)) {
      return false;
    }
    final FXForwardSecurity fxForward = (FXForwardSecurity) security;
    for (final String currencyName : _applicableCurrencyNames) {
      if (currencyName.equals(fxForward.getReceiveCurrency().getCode())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.RECEIVE_CURVE);
      defaults.addValuePropertyName(valueName, ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(_curveName);
    }
    if (ForexForwardFunction.PROPERTY_RECEIVE_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_curveCalculationMethod);
    }
    return null;
  }
}
