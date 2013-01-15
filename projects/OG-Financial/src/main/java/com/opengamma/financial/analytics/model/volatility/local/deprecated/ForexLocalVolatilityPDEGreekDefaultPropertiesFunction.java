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
public class ForexLocalVolatilityPDEGreekDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] GREEK_NAMES = new String[] {
    ValueRequirementNames.LOCAL_VOLATILITY_DELTA,
    ValueRequirementNames.LOCAL_VOLATILITY_DUAL_DELTA,
    ValueRequirementNames.LOCAL_VOLATILITY_DUAL_GAMMA,
    ValueRequirementNames.LOCAL_VOLATILITY_GAMMA,
    ValueRequirementNames.LOCAL_VOLATILITY_VANNA,
    ValueRequirementNames.LOCAL_VOLATILITY_VEGA,
    ValueRequirementNames.LOCAL_VOLATILITY_VOMMA,
    ValueRequirementNames.LOCAL_VOLATILITY_GRID_PRICE,
    ValueRequirementNames.BLACK_VOLATILITY_GRID_PRICE,
    ValueRequirementNames.LOCAL_VOLATILITY_GRID_IMPLIED_VOL,
    ValueRequirementNames.LOCAL_VOLATILITY_DOMESTIC_PRICE };
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
  private final String _strikeInterpolatorName;

  public ForexLocalVolatilityPDEGreekDefaultPropertiesFunction(final String forwardCurveCalculationMethod, final String forwardCurveName, final String surfaceType,
      final String xAxis, final String yAxis, final String yAxisType, final String surfaceName, final String h, final String pdeDirection, final String theta,
      final String timeSteps, final String spaceSteps, final String timeGridBunching, final String spaceGridBunching, final String maxMoneyness,
      final String strikeInterpolatorName) {
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
    ArgumentChecker.notNull(strikeInterpolatorName, "strike interpolator name");
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
    _strikeInterpolatorName = strikeInterpolatorName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String greek : GREEK_NAMES) {
      defaults.addValuePropertyName(greek, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(greek, ValuePropertyNames.CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_H);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_MAX_MONEYNESS);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_PDE_DIRECTION);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_GRID_BUNCHING);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_SPACE_STEPS);
      defaults.addValuePropertyName(greek, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_THETA);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_GRID_BUNCHING);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_TIME_STEPS);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE);
      defaults.addValuePropertyName(greek, LocalVolatilityPDEValuePropertyNames.PROPERTY_RESULT_STRIKE_INTERPOLATOR);
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
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE.equals(propertyName)) {
      return Collections.singleton(_yAxisType);
    }
    return null;
  }
}
