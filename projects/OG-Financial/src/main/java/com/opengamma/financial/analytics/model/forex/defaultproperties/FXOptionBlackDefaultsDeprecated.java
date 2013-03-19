/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXOptionBlackDefaults
 */
@Deprecated
public class FXOptionBlackDefaultsDeprecated extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_GAMMA_P,
    ValueRequirementNames.VEGA_MATRIX,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY
  };
  private final String _putCurveName;
  private final String _putForwardCurveName;
  private final String _putCurveCalculationMethod;
  private final String _callCurveName;
  private final String _callForwardCurveName;
  private final String _callCurveCalculationMethod;
  private final String _surfaceName;
  private final String _interpolatorName;
  private final String _leftExtrapolatorName;
  private final String _rightExtrapolatorName;
  private final String _putCurrency;
  private final String _callCurrency;

  public FXOptionBlackDefaultsDeprecated(final String putCurveName, final String putForwardCurveName, final String putCurveCalculationMethod, final String callCurveName,
      final String callForwardCurveName, final String callCurveCalculationMethod, final String surfaceName, final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName, final String putCurrency, final String callCurrency) {
    super(FinancialSecurityTypes.FX_OPTION_SECURITY.or(FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY).or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY), true);
    ArgumentChecker.notNull(putCurveName, "put curve name");
    ArgumentChecker.notNull(putForwardCurveName, "put forward curve name");
    ArgumentChecker.notNull(putCurveCalculationMethod, "put curve calculation method");
    ArgumentChecker.notNull(putCurveName, "call curve name");
    ArgumentChecker.notNull(putForwardCurveName, "call forward curve name");
    ArgumentChecker.notNull(callCurveCalculationMethod, "call curve calculation method");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(putCurrency, "put currency");
    ArgumentChecker.notNull(callCurrency, "call currency");
    _putCurveName = putCurveName;
    _putForwardCurveName = putForwardCurveName;
    _putCurveCalculationMethod = putCurveCalculationMethod;
    _callCurveName = callCurveName;
    _callForwardCurveName = callForwardCurveName;
    _callCurveCalculationMethod = callCurveCalculationMethod;
    _surfaceName = surfaceName;
    _interpolatorName = interpolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
    _putCurrency = putCurrency;
    _callCurrency = callCurrency;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    return callCurrency.getCode().equals(_callCurrency) && putCurrency.getCode().equals(_putCurrency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunctionDeprecated.PROPERTY_PUT_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunctionDeprecated.PROPERTY_PUT_FORWARD_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunctionDeprecated.PROPERTY_PUT_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunctionDeprecated.PROPERTY_CALL_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunctionDeprecated.PROPERTY_CALL_FORWARD_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunctionDeprecated.PROPERTY_CALL_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (FXOptionBlackFunctionDeprecated.PROPERTY_CALL_CURVE.equals(propertyName)) {
      return Collections.singleton(_callCurveName);
    }
    if (FXOptionBlackFunctionDeprecated.PROPERTY_CALL_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_callForwardCurveName);
    }
    if (FXOptionBlackFunctionDeprecated.PROPERTY_CALL_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_callCurveCalculationMethod);
    }
    if (FXOptionBlackFunctionDeprecated.PROPERTY_PUT_CURVE.equals(propertyName)) {
      return Collections.singleton(_putCurveName);
    }
    if (FXOptionBlackFunctionDeprecated.PROPERTY_PUT_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_putForwardCurveName);
    }
    if (FXOptionBlackFunctionDeprecated.PROPERTY_PUT_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_putCurveCalculationMethod);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    if (InterpolatedDataProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_interpolatorName);
    }
    if (InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftExtrapolatorName);
    }
    if (InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightExtrapolatorName);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SURFACE_DEFAULTS;
  }

}
