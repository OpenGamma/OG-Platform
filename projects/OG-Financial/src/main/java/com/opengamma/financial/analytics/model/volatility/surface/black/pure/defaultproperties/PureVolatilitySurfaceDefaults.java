/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.pure.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class PureVolatilitySurfaceDefaults extends DefaultPropertyFunction {
  private final String _timeAxis;
  private final String _yAxis;
  private final String _volatilityTransform;
  private final String _timeInterpolator;
  private final String _timeLeftExtrapolator;
  private final String _timeRightExtrapolator;
  private final String _discountingCurveName;
  private final String _discountingCurveCalculationConfig;
  private final String _surfaceName;
  private final String _curveCurrency;

  public PureVolatilitySurfaceDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String discountingCurveName, final String discountingCurveCalculationConfig, final String surfaceName,
      final String curveCurrency) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(timeAxis, "time axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(volatilityTransform, "volatility transform");
    ArgumentChecker.notNull(timeInterpolator, "time interpolator");
    ArgumentChecker.notNull(timeLeftExtrapolator, "time left extrapolator");
    ArgumentChecker.notNull(timeRightExtrapolator, "time right extrapolator");
    ArgumentChecker.notNull(discountingCurveName, "discounting curve name");
    ArgumentChecker.notNull(discountingCurveCalculationConfig, "discounting curve calculation method");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(curveCurrency, "curve currency");
    _discountingCurveName = discountingCurveName;
    _discountingCurveCalculationConfig = discountingCurveCalculationConfig;
    _surfaceName = surfaceName;
    _timeAxis = timeAxis;
    _yAxis = yAxis;
    _volatilityTransform = volatilityTransform;
    _timeInterpolator = timeInterpolator;
    _timeLeftExtrapolator = timeLeftExtrapolator;
    _timeRightExtrapolator = timeRightExtrapolator;
    _curveCurrency = curveCurrency;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, ValuePropertyNames.SURFACE);
    defaults.addValuePropertyName(ValueRequirementNames.PURE_VOLATILITY_SURFACE, ValuePropertyNames.CURVE_CURRENCY);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS.equals(propertyName)) {
      return Collections.singleton(_timeAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS.equals(propertyName)) {
      return Collections.singleton(_yAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM.equals(propertyName)) {
      return Collections.singleton(_volatilityTransform);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeInterpolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeLeftExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeRightExtrapolator);
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_discountingCurveName);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_discountingCurveCalculationConfig);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(_curveCurrency);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PURE_VOLATILITY_SURFACE_DEFAULTS;
  }

}
