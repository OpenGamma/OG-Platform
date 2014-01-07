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
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated Deprecated
 */
@Deprecated
public class BlackVolatilitySurfaceDefaultPropertiesFunction extends DefaultPropertyFunction {

  private final Set<String> _forwardCurveCalculationMethod;
  private final Set<String> _forwardCurveName;
  private final Set<String> _surfaceType;
  private final Set<String> _xAxis;
  private final Set<String> _yAxis;
  private final Set<String> _yAxisType;
  private final Set<String> _surfaceName;

  public BlackVolatilitySurfaceDefaultPropertiesFunction(final String forwardCurveCalculationMethod, final String forwardCurveName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String surfaceName) {
    super(ComputationTargetType.LEGACY_PRIMITIVE, true); // // [PLAT-2286]: change to correct type
    ArgumentChecker.notNull(forwardCurveCalculationMethod, "forward curve calculation method");
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(surfaceType, "surface type");
    ArgumentChecker.notNull(xAxis, "x axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(yAxisType, "y axis type");
    ArgumentChecker.notNull(surfaceName, "surface name");
    _forwardCurveCalculationMethod = Collections.singleton(forwardCurveCalculationMethod);
    _forwardCurveName = Collections.singleton(forwardCurveName);
    _surfaceType = Collections.singleton(surfaceType);
    _xAxis = Collections.singleton(xAxis);
    _yAxis = Collections.singleton(yAxis);
    _yAxisType = Collections.singleton(yAxisType);
    _surfaceName = Collections.singleton(surfaceName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE);
    defaults.addValuePropertyName(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE);
    defaults.addValuePropertyName(ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE, ValuePropertyNames.SURFACE);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    switch (propertyName) {
      case ValuePropertyNames.CURVE_CALCULATION_METHOD:
        return _forwardCurveCalculationMethod;
      case ValuePropertyNames.CURVE:
        return _forwardCurveName;
      case LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE:
        return _surfaceType;
      case LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS:
        return _xAxis;
      case LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS:
        return _yAxis;
      case LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE:
        return _yAxisType;
      case ValuePropertyNames.SURFACE:
        return _surfaceName;
      default:
        return null;
    }
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.BLACK_VOLATILITY_SURFACE_DEFAULTS;
  }

}
