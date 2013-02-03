/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
public class LocalVolatilityPDEDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] REQUIREMENTS = new String[] {
    ValueRequirementNames.LOCAL_VOLATILITY_FULL_PDE_GRID,
    ValueRequirementNames.LOCAL_VOLATILITY_PDE_GREEKS,
    ValueRequirementNames.LOCAL_VOLATILITY_PDE_BUCKETED_VEGA,
    ValueRequirementNames.LOCAL_VOLATILITY_FOREX_PV_QUOTES};
  private final String _forwardCurveCalculationMethod;
  private final String _forwardCurveName;
  private final String _surfaceType;
  private final String _xAxis;
  private final String _yAxis;
  private final String _yAxisType;
  private final String _surfaceName;
  private final String _h;
  private final String _pdeDirection;
  private final String _theta;
  private final String _timeSteps;
  private final String _spaceSteps;
  private final String _timeGridBunching;
  private final String _spaceGridBunching;
  private final String _maxMoneyness;

  public LocalVolatilityPDEDefaultPropertiesFunction(final String forwardCurveCalculationMethod, final String forwardCurveName, final String surfaceType, final String xAxis,
      final String yAxis, final String yAxisType, final String surfaceName, final String h, final String pdeDirection, final String theta, final String timeSteps, final String spaceSteps,
      final String timeGridBunching, final String spaceGridBunching, final String maxMoneyness) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(forwardCurveCalculationMethod, "forward curve calculation method");
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(surfaceType, "surface type");
    ArgumentChecker.notNull(xAxis, "x axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(yAxisType, "y axis type");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(h, "h");
    ArgumentChecker.notNull(pdeDirection, "PDE direction");
    ArgumentChecker.notNull(theta, "theta");
    ArgumentChecker.notNull(timeSteps, "time steps");
    ArgumentChecker.notNull(spaceSteps, "space steps");
    ArgumentChecker.notNull(timeGridBunching, "time grid bunching");
    ArgumentChecker.notNull(spaceGridBunching, "space grid bunching");
    ArgumentChecker.notNull(maxMoneyness, "maximum moneyness");
    _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
    _forwardCurveName = forwardCurveName;
    _surfaceType = surfaceType;
    _xAxis = xAxis;
    _yAxis = yAxis;
    _yAxisType = yAxisType;
    _surfaceName = surfaceName;
    _h = h;
    _pdeDirection = pdeDirection;
    _theta = theta;
    _timeSteps = timeSteps;
    _spaceSteps = spaceSteps;
    _timeGridBunching = timeGridBunching;
    _spaceGridBunching = spaceGridBunching;
    _maxMoneyness = maxMoneyness;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String requirement : REQUIREMENTS) {
      defaults.addValuePropertyName(requirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(requirement, ValuePropertyNames.CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_H);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_MAX_MONEYNESS);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_PDE_DIRECTION);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_GRID_BUNCHING);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_STEPS);
      defaults.addValuePropertyName(requirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_THETA);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_GRID_BUNCHING);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_STEPS);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS);
      defaults.addValuePropertyName(requirement, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethod);
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_H.equals(propertyName)) {
      return Collections.singleton(_h);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE.equals(propertyName)) {
      return Collections.singleton(_yAxisType);
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
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS.equals(propertyName)) {
      return Collections.singleton(_yAxis);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE.equals(propertyName)) {
      return Collections.singleton(_yAxisType);
    }
    return null;
  }
}
