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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForexForwardDefaultPayCurveNamesYieldCurveNodeSensitivitiesFunction extends DefaultPropertyFunction {
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES};
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final String[] _applicableCurrencyNames;

  public ForexForwardDefaultPayCurveNamesYieldCurveNodeSensitivitiesFunction(final String forwardCurveName, final String fundingCurveName, final String... applicableCurrencyNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(fundingCurveName, "funding curve name");
    ArgumentChecker.notNull(applicableCurrencyNames, "currency names");
    _forwardCurveName = forwardCurveName;
    _fundingCurveName = fundingCurveName;
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
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (YieldCurveFunction.PROPERTY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
    }
    if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurveName);
    }
    return null;
  }
}
