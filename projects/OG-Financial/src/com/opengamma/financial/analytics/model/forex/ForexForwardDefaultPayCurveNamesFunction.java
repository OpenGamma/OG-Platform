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
public class ForexForwardDefaultPayCurveNamesFunction extends DefaultPropertyFunction {
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES};
  private final String _curveName;
  private final String[] _applicableCurrencyNames;

  public ForexForwardDefaultPayCurveNamesFunction(final String curveName, final String... applicableCurrencyNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(curveName, "curve name");
    ArgumentChecker.notNull(applicableCurrencyNames, "currency names");
    _curveName = curveName;
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
      if (currencyName.equals(fxForward.getPayCurrency().getCode())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.PAY_CURVE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(_curveName);
    }
    return null;
  }
}
