/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InterpolatedCurveAndSurfaceProperties;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterpolatedVolatilitySurfaceDefaultPropertiesFunction extends DefaultPropertyFunction {
  private final String _leftXExtrapolatorName;
  private final String _rightXExtrapolatorName;
  private final String _xInterpolatorName;
  private final String _leftYExtrapolatorName;
  private final String _rightYExtrapolatorName;
  private final String _yInterpolatorName;

  public InterpolatedVolatilitySurfaceDefaultPropertiesFunction(final String leftXExtrapolatorName, final String rightXExtrapolatorName, final String xInterpolatorName,
      final String leftYExtrapolatorName, final String rightYExtrapolatorName, final String yInterpolatorName) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(leftXExtrapolatorName, "left x extrapolator name");
    ArgumentChecker.notNull(rightXExtrapolatorName, "right x extrapolator name");
    ArgumentChecker.notNull(xInterpolatorName, "x interpolator name");
    ArgumentChecker.notNull(leftYExtrapolatorName, "left y extrapolator name");
    ArgumentChecker.notNull(rightYExtrapolatorName, "right y extrapolator name");
    ArgumentChecker.notNull(yInterpolatorName, "y interpolator name");
    _leftXExtrapolatorName = leftXExtrapolatorName;
    _rightXExtrapolatorName = rightXExtrapolatorName;
    _xInterpolatorName = xInterpolatorName;
    _leftYExtrapolatorName = leftYExtrapolatorName;
    _rightYExtrapolatorName = rightYExtrapolatorName;
    _yInterpolatorName = yInterpolatorName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, InterpolatedCurveAndSurfaceProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, InterpolatedCurveAndSurfaceProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, InterpolatedCurveAndSurfaceProperties.Y_INTERPOLATOR_NAME);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget taget, final ValueRequirement desiredValue, final String propertyName) {
    if (InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftXExtrapolatorName);
    }
    if (InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightXExtrapolatorName);
    }
    if (InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_xInterpolatorName);
    }
    if (InterpolatedCurveAndSurfaceProperties.LEFT_Y_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftYExtrapolatorName);
    }
    if (InterpolatedCurveAndSurfaceProperties.RIGHT_Y_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightYExtrapolatorName);
    }
    if (InterpolatedCurveAndSurfaceProperties.Y_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_yInterpolatorName);
    }
    return null;
  }
}
