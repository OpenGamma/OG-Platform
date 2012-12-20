/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForwardPDEMixedLogNormalDefaults extends ForwardPDEDefaults {
  private final String _weightingFunctionName;

  public ForwardPDEMixedLogNormalDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String forwardCurveName, final String forwardCurveCalculationMethod, final String surfaceName,
      final String eps, final String theta, final String nTimeSteps, final String nSpaceSteps, final String timeStepBunching, final String spaceStepBunching,
      final String maxProxyDelta, final String centreMoneyness, final String spaceDirectionInterpolator, final String discountingCurveName,
      final String weightingFunctionName) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, forwardCurveName, forwardCurveCalculationMethod,
        surfaceName, eps, theta, nTimeSteps, nSpaceSteps, timeStepBunching, spaceStepBunching, maxProxyDelta, centreMoneyness, spaceDirectionInterpolator,
        discountingCurveName);
    ArgumentChecker.notNull(weightingFunctionName, "weighting function name");
    _weightingFunctionName = weightingFunctionName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    for (final String valueRequirement : getValueRequirementNames()) {
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> surfaceDefaults = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (surfaceDefaults != null) {
      return surfaceDefaults;
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_weightingFunctionName);
    }
    return null;
  }
}
