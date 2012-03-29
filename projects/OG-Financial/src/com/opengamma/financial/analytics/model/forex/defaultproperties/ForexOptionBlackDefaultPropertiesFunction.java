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
import com.opengamma.financial.analytics.model.forex.ForexOptionBlackFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexOptionBlackDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VEGA_MATRIX,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES
  };
  private final String _putCurveName;
  private final String _putForwardCurveName;
  private final String _putCurveCalculationMethod;
  private final String _callCurveName;
  private final String _callForwardCurveName;
  private final String _callCurveCalculationMethod;
  private final String _surfaceName;
  private final String _putCurrency;
  private final String _callCurrency;

  public ForexOptionBlackDefaultPropertiesFunction(final String putCurveName, final String putForwardCurveName, final String putCurveCalculationMethod, final String callCurveName,
      final String callForwardCurveName, final String callCurveCalculationMethod, final String surfaceName, final String putCurrency, final String callCurrency) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(putCurveName, "put curve name");
    ArgumentChecker.notNull(putForwardCurveName, "put forward curve name");
    ArgumentChecker.notNull(putCurveCalculationMethod, "put curve calculation method");
    ArgumentChecker.notNull(putCurveName, "call curve name");
    ArgumentChecker.notNull(putForwardCurveName, "call forward curve name");
    ArgumentChecker.notNull(callCurveCalculationMethod, "call curve calculation method");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(putCurrency, "put currency");
    ArgumentChecker.notNull(callCurrency, "call currency");
    _putCurveName = putCurveName;
    _putForwardCurveName = putForwardCurveName;
    _putCurveCalculationMethod = putCurveCalculationMethod;
    _callCurveName = callCurveName;
    _callForwardCurveName = callForwardCurveName;
    _callCurveCalculationMethod = callCurveCalculationMethod;
    _surfaceName = surfaceName;
    _putCurrency = putCurrency;
    _callCurrency = callCurrency;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    if (!(security instanceof FXOptionSecurity || security instanceof FXBarrierOptionSecurity || security instanceof FXDigitalOptionSecurity)) {
      return false;
    }
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    return callCurrency.getCode().equals(_callCurrency) && putCurrency.getCode().equals(_putCurrency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunction.PROPERTY_PUT_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunction.PROPERTY_CALL_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ForexOptionBlackFunction.PROPERTY_CALL_CURVE.equals(propertyName)) {
      return Collections.singleton(_callCurveName);
    }
    if (ForexOptionBlackFunction.PROPERTY_CALL_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_callForwardCurveName);
    }
    if (ForexOptionBlackFunction.PROPERTY_CALL_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_callCurveCalculationMethod);
    }
    if (ForexOptionBlackFunction.PROPERTY_PUT_CURVE.equals(propertyName)) {
      return Collections.singleton(_putCurveName);
    }
    if (ForexOptionBlackFunction.PROPERTY_PUT_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_putForwardCurveName);
    }
    if (ForexOptionBlackFunction.PROPERTY_PUT_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_putCurveCalculationMethod);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    return null;
  }
}
