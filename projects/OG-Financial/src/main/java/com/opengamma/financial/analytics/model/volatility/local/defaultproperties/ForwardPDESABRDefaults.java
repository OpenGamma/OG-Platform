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
public class ForwardPDESABRDefaults extends ForwardPDEDefaults {
  private final String _sabrModel;
  private final String _weightingFunction;
  private final String _useExternalBeta;
  private final String _externalBeta;

  public ForwardPDESABRDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator, //CSIGNORE
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String forwardCurveName, final String forwardCurveCalculationMethod, final String surfaceName,
      final String eps, final String theta, final String nTimeSteps, final String nSpaceSteps, final String timeStepBunching, final String spaceStepBunching,
      final String maxProxyDelta, final String centreMoneyness, final String spaceDirectionInterpolator, final String discountingCurveName,
      final String sabrModel, final String weightingFunction, final String useExternalBeta, final String externalBeta) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, forwardCurveName, forwardCurveCalculationMethod,
        surfaceName, eps, theta, nTimeSteps, nSpaceSteps, timeStepBunching, spaceStepBunching, maxProxyDelta, centreMoneyness, spaceDirectionInterpolator,
        discountingCurveName);
    ArgumentChecker.notNull(sabrModel, "SABR model");
    ArgumentChecker.notNull(weightingFunction, "weighting function");
    ArgumentChecker.notNull(useExternalBeta, "use external beta");
    ArgumentChecker.notNull(externalBeta, "external beta");
    _sabrModel = sabrModel;
    _weightingFunction = weightingFunction;
    _useExternalBeta = useExternalBeta;
    _externalBeta = externalBeta;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    for (final String valueRequirement : getValueRequirementNames()) {
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_MODEL);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_WEIGHTING_FUNCTION);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_USE_EXTERNAL_BETA);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_EXTERNAL_BETA);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> surfaceDefaults = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (surfaceDefaults != null) {
      return surfaceDefaults;
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_MODEL.equals(propertyName)) {
      return Collections.singleton(_sabrModel);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_WEIGHTING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_weightingFunction);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_USE_EXTERNAL_BETA.equals(propertyName)) {
      return Collections.singleton(_useExternalBeta);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_EXTERNAL_BETA.equals(propertyName)) {
      return Collections.singleton(_externalBeta);
    }
    return null;
  }

}
