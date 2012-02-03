/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForexOptionDefaultCurveNamesFunction extends DefaultPropertyFunction {
  private final String[] _valueNames;
  private final String _putFundingCurve;
  private final String _putForwardCurve;
  private final String _callFundingCurve;
  private final String _callForwardCurve;
  private final String _surface;

  public ForexOptionDefaultCurveNamesFunction(final String putFundingCurve, final String putForwardCurve, final String callFundingCurve,
      final String callForwardCurve, final String surface, final String... valueNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(putFundingCurve, "put funding curve name");
    ArgumentChecker.notNull(putForwardCurve, "put forward curve name");
    ArgumentChecker.notNull(callFundingCurve, "call funding curve name");
    ArgumentChecker.notNull(callForwardCurve, "call forward curve name");
    ArgumentChecker.notNull(surface, "surface name");
    ArgumentChecker.notNull(valueNames, "value names");
    _putFundingCurve = putFundingCurve;
    _putForwardCurve = putForwardCurve;
    _callFundingCurve = callFundingCurve;
    _callForwardCurve = callForwardCurve;
    _surface = surface;
    _valueNames = valueNames;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity || target.getSecurity() instanceof FXBarrierOptionSecurity;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : _valueNames) {
      defaults.addValuePropertyName(valueName, ForexOptionFunction.PROPERTY_PUT_FUNDING_CURVE_NAME);
      defaults.addValuePropertyName(valueName, ForexOptionFunction.PROPERTY_PUT_FORWARD_CURVE_NAME);
      defaults.addValuePropertyName(valueName, ForexOptionFunction.PROPERTY_CALL_FUNDING_CURVE_NAME);
      defaults.addValuePropertyName(valueName, ForexOptionFunction.PROPERTY_CALL_FORWARD_CURVE_NAME);
      defaults.addValuePropertyName(valueName, ForexOptionFunction.PROPERTY_FX_VOLATILITY_SURFACE_NAME);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ForexOptionFunction.PROPERTY_PUT_FUNDING_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_putFundingCurve);
    }
    if (ForexOptionFunction.PROPERTY_PUT_FORWARD_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_putForwardCurve);
    }
    if (ForexOptionFunction.PROPERTY_CALL_FUNDING_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_callFundingCurve);
    }
    if (ForexOptionFunction.PROPERTY_CALL_FORWARD_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_callForwardCurve);
    }
    if (ForexOptionFunction.PROPERTY_FX_VOLATILITY_SURFACE_NAME.equals(propertyName)) {
      return Collections.singleton(_surface);
    }
    return null;
  }
}
