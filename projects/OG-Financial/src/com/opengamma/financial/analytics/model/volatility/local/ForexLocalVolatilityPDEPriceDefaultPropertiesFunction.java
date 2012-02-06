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
public class ForexLocalVolatilityPDEPriceDefaultPropertiesFunction extends DefaultPropertyFunction {
  private final String _forwardCurveCalculationMethod;
  private final String _forwardCurveInterpolator;
  private final String _forwardCurveLeftExtrapolator;
  private final String _forwardCurveRightExtrapolator;
  private final String _surfaceType;
  private final String _xAxis;
  private final String _yAxis;
  private final String _lambda;
  private final String _surfaceName;
  private final String _h;
  private final String _pdeDirection;
  private final String _theta;
  private final String _timeSteps;
  private final String _spaceSteps;
  private final String _timeGridBunching;
  private final String _spaceGridBunching;
  private final String _maxMoneyness;
  private final String _strikeInterpolatorName;
  private final String _timeInterpolatorName;

  public ForexLocalVolatilityPDEPriceDefaultPropertiesFunction(final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator, final String surfaceType, final String xAxis, final String yAxis, final String lambda, final String surfaceName,
      final String h, final String pdeDirection, final String theta, final String timeSteps, final String spaceSteps,
      final String timeGridBunching, final String spaceGridBunching, final String maxMoneyness, final String strikeInterpolatorName, final String timeInterpolatorName) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(forwardCurveCalculationMethod, "forward curve calculation method");
    ArgumentChecker.notNull(forwardCurveInterpolator, "forward curve interpolator");
    ArgumentChecker.notNull(forwardCurveLeftExtrapolator, "forward curve left extrapolator");
    ArgumentChecker.notNull(forwardCurveRightExtrapolator, "forward curve right extrapolator");
    ArgumentChecker.notNull(surfaceType, "surface type");
    ArgumentChecker.notNull(xAxis, "x axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(lambda, "lambda");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(h, "h");
    ArgumentChecker.notNull(pdeDirection, "PDE direction");
    ArgumentChecker.notNull(theta, "theta");
    ArgumentChecker.notNull(timeSteps, "time steps");
    ArgumentChecker.notNull(spaceSteps, "space steps");
    ArgumentChecker.notNull(timeGridBunching, "time grid bunching");
    ArgumentChecker.notNull(spaceGridBunching, "space grid bunching");
    ArgumentChecker.notNull(maxMoneyness, "maximum moneyness");
    ArgumentChecker.notNull(strikeInterpolatorName, "strike interpolator name");
    ArgumentChecker.notNull(timeInterpolatorName, "time interpolator name");
    _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
    _forwardCurveInterpolator = forwardCurveInterpolator;
    _forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolator;
    _forwardCurveRightExtrapolator = forwardCurveRightExtrapolator;
    _surfaceType = surfaceType;
    _xAxis = xAxis;
    _yAxis = yAxis;
    _lambda = lambda;
    _surfaceName = surfaceName;
    _h = h;
    _pdeDirection = pdeDirection;
    _theta = theta;
    _timeSteps = timeSteps;
    _spaceSteps = spaceSteps;
    _timeGridBunching = timeGridBunching;
    _spaceGridBunching = spaceGridBunching;
    _maxMoneyness = maxMoneyness;
    _strikeInterpolatorName = strikeInterpolatorName;
    _timeInterpolatorName = timeInterpolatorName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, InterpolatedForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_H);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_LAMBDA);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_MAX_MONEYNESS);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_PDE_DIRECTION);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_GRID_BUNCHING);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_STEPS);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, ValuePropertyNames.SURFACE);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_THETA);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_GRID_BUNCHING);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_STEPS);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_STRIKE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PRESENT_VALUE, LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_TIME_INTERPOLATOR);
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
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_H.equals(propertyName)) {
      return Collections.singleton(_h);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_LAMBDA.equals(propertyName)) {
      return Collections.singleton(_lambda);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_MAX_MONEYNESS.equals(propertyName)) {
      return Collections.singleton(_maxMoneyness);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_PDE_DIRECTION.equals(propertyName)) {
      return Collections.singleton(_pdeDirection);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_GRID_BUNCHING.equals(propertyName)) {
      return Collections.singleton(_spaceGridBunching);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_STEPS.equals(propertyName)) {
      return Collections.singleton(_spaceSteps);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE.equals(propertyName)) {
      return Collections.singleton(_surfaceType);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_THETA.equals(propertyName)) {
      return Collections.singleton(_theta);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_GRID_BUNCHING.equals(propertyName)) {
      return Collections.singleton(_timeGridBunching);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_STEPS.equals(propertyName)) {
      return Collections.singleton(_timeSteps);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS.equals(propertyName)) {
      return Collections.singleton(_xAxis);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_STRIKE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_strikeInterpolatorName);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS.equals(propertyName)) {
      return Collections.singleton(_yAxis);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_TIME_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeInterpolatorName);
    }
    return null;
  }
}
