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
 *
 * @deprecated Deprecated
 */
@Deprecated
public class LocalVolatilitySurfaceDefaultPropertiesFunction extends DefaultPropertyFunction {
  private final String _forwardCurveCalculationMethod;
  private final String _forwardCurveName;
  private final String _surfaceType;
  private final String _xAxis;
  private final String _yAxis;
  private final String _yAxisType;
  private final String _surfaceName;
  private final String _hName;

  public LocalVolatilitySurfaceDefaultPropertiesFunction(final String forwardCurveCalculationMethod, final String forwardCurveName, final String surfaceType, final String xAxis, final String yAxis,
      final String yAxisType, final String surfaceName, final String hName) {
    super(ComputationTargetType.LEGACY_PRIMITIVE, true); // // [PLAT-2286]: change to correct type
    ArgumentChecker.notNull(forwardCurveCalculationMethod, "forward curve calculation method");
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(surfaceType, "surface type");
    ArgumentChecker.notNull(xAxis, "x axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(yAxisType, "y axis type");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(hName, "h");
    _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
    _forwardCurveName = forwardCurveName;
    _surfaceType = surfaceType;
    _xAxis = xAxis;
    _yAxis = yAxis;
    _yAxisType = yAxisType;
    _surfaceName = surfaceName;
    _hName = hName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_SURFACE_TYPE);
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_X_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE);
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, ValuePropertyNames.SURFACE);
    defaults.addValuePropertyName(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, LocalVolatilityPDEValuePropertyNames.PROPERTY_H);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethod);
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
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
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_Y_AXIS_TYPE.equals(propertyName)) {
      return Collections.singleton(_yAxisType);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    if (LocalVolatilityPDEValuePropertyNames.PROPERTY_H.equals(propertyName)) {
      return Collections.singleton(_hName);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.LOCAL_VOLATILITY_SURFACE_DEFAULTS;
  }

}
