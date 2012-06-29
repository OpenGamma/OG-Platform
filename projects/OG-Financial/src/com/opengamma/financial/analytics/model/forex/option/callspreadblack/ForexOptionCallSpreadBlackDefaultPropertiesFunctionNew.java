/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunctionNew;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated.ForexDigitalOptionCallSpreadBlackFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexOptionCallSpreadBlackDefaultPropertiesFunctionNew extends DefaultPropertyFunction {
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
    ValueRequirementNames.CALL_SPREAD_VALUE_VEGA,
    ValueRequirementNames.VALUE_THETA
  };
  private final String _putCurveName;
  private final String _callCurveName;
  private final String _putCurveConfig;
  private final String _callCurveConfig;
  private final String _surfaceName;
  private final String _interpolatorName;
  private final String _leftExtrapolatorName;
  private final String _rightExtrapolatorName;
  private final String _putCurrency;
  private final String _callCurrency;
  private final String _spread;

  public ForexOptionCallSpreadBlackDefaultPropertiesFunctionNew(final String putCurveName, final String callCurveName, final String putCurveConfig, final String callCurveConfig,
      final String surfaceName, final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName, final String putCurrency, final String callCurrency, final String spread) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(putCurveName, "put curve name");
    ArgumentChecker.notNull(callCurveName, "call curve name");
    ArgumentChecker.notNull(putCurveConfig, "put curve config");
    ArgumentChecker.notNull(callCurveConfig, "call curve config");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(putCurrency, "put currency");
    ArgumentChecker.notNull(callCurrency, "call currency");
    ArgumentChecker.notNull(spread, "spread");
    _putCurveName = putCurveName;
    _callCurveName = callCurveName;
    _putCurveConfig = putCurveConfig;
    _callCurveConfig = callCurveConfig;
    _surfaceName = surfaceName;
    _interpolatorName = interpolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
    _putCurrency = putCurrency;
    _callCurrency = callCurrency;
    _spread = spread;
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
    if (!(security instanceof FXDigitalOptionSecurity)) {
      return false;
    }
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    return callCurrency.getCode().equals(_callCurrency) && putCurrency.getCode().equals(_putCurrency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunctionNew.PUT_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunctionNew.CALL_CURVE);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunctionNew.PUT_CURVE_CALC_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ForexOptionBlackFunctionNew.CALL_CURVE_CALC_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, ForexDigitalOptionCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ForexOptionBlackFunctionNew.CALL_CURVE.equals(propertyName)) {
      return Collections.singleton(_callCurveName);
    }
    if (ForexOptionBlackFunctionNew.PUT_CURVE.equals(propertyName)) {
      return Collections.singleton(_putCurveName);
    }
    if (ForexOptionBlackFunctionNew.CALL_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(_callCurveConfig);
    }
    if (ForexOptionBlackFunctionNew.PUT_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(_putCurveConfig);
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
    if (ForexDigitalOptionCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE.equals(propertyName)) {
      return Collections.singleton(_spread);
    }
    return null;
  }
}
