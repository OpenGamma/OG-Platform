/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BlackVolatilitySurfaceSABRInterpolatorDefaults extends BlackVolatilitySurfaceInterpolatorDefaults {
  private final String _sabrModel;
  private final String _weightingFunction;
  private final String _useExternalBeta;
  private final String _externalBeta;

  public BlackVolatilitySurfaceSABRInterpolatorDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String sabrModel, final String weightingFunction, final String useExternalBeta, final String externalBeta) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator);
    ArgumentChecker.notNull(sabrModel, "SARB model");
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
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_MODEL);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_WEIGHTING_FUNCTION);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_USE_EXTERNAL_BETA);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_EXTERNAL_BETA);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> commonProperties = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (commonProperties != null) {
      return commonProperties;
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
