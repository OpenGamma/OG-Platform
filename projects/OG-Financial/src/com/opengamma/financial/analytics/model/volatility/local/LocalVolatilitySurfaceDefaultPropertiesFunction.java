/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LocalVolatilitySurfaceDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] REQUIREMENTS = new String[] {ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, ValueRequirementNames.LOCAL_VOLATILITY_SURFACE};
  private final String _forwardCurveCalculationMethod;
  private final String _forwardCurveInterpolator;
  private final String _forwardCurveLeftExtrapolator;
  private final String _forwardCurveRightExtrapolator;
  private final String _surfaceType;
  private final String _xAxis;
  private final String _yAxis;
  private final String _lambda;
  private final String _surfaceName;
  private final String _hName;

  public LocalVolatilitySurfaceDefaultPropertiesFunction(final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator, final String surfaceType, final String xAxis, final String yAxis, final String lambda,
      final String surfaceName, final String hName) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(forwardCurveCalculationMethod, "forward curve calculation method");
    ArgumentChecker.notNull(forwardCurveInterpolator, "forward curve interpolator");
    ArgumentChecker.notNull(forwardCurveLeftExtrapolator, "forward curve left extrapolator");
    ArgumentChecker.notNull(forwardCurveRightExtrapolator, "forward curve right extrapolator");
    ArgumentChecker.notNull(surfaceType, "surface type");
    ArgumentChecker.notNull(xAxis, "x axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(lambda, "lambda");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(hName, "h");
    _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
    _forwardCurveInterpolator = forwardCurveInterpolator;
    _forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolator;
    _forwardCurveRightExtrapolator = forwardCurveRightExtrapolator;
    _surfaceType = surfaceType;
    _xAxis = xAxis;
    _yAxis = yAxis;
    _lambda = lambda;
    _surfaceName = surfaceName;
    _hName = hName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String requirement : REQUIREMENTS) {
      defaults.addValuePropertyName(requirement, ValuePropertyNames.CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(requirement, InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
      defaults.addValuePropertyName(requirement, InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
      defaults.addValuePropertyName(requirement, InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_LAMBDA);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS);
      defaults.addValuePropertyName(requirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_H);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethod);
    }
    if (InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveInterpolator);
    }
    if (InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveLeftExtrapolator);
    }
    if (InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_forwardCurveRightExtrapolator);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_LAMBDA.equals(propertyName)) {
      return Collections.singleton(_lambda);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE.equals(propertyName)) {
      return Collections.singleton(_surfaceType);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS.equals(propertyName)) {
      return Collections.singleton(_xAxis);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS.equals(propertyName)) {
      return Collections.singleton(_yAxis);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_H.equals(propertyName)) {
      return Collections.singleton(_hName);
    }
    return null;
  }
}
