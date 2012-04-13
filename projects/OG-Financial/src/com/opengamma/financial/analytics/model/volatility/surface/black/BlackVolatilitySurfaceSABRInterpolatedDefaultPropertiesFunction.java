/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BlackVolatilitySurfaceSABRInterpolatedDefaultPropertiesFunction extends DefaultPropertyFunction {
  private final String _timeAxis;
  private final String _yAxis;
  private final String _volatilityTransform;
  private final String _timeInterpolator;
  private final String _timeLeftExtrapolator;
  private final String _timeRightExtrapolator;
  private final String _sabrModel;
  private final String _weightingFunction;
  private final String _useExternalBeta;
  private final String _externalBeta;

  public BlackVolatilitySurfaceSABRInterpolatedDefaultPropertiesFunction(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String sabrModel, final String weightingFunction, final String useExternalBeta, final String externalBeta) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(timeAxis, "time axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(volatilityTransform, "volatility transform");
    ArgumentChecker.notNull(timeInterpolator, "time interpolator");
    ArgumentChecker.notNull(timeLeftExtrapolator, "time left extrapolator");
    ArgumentChecker.notNull(timeRightExtrapolator, "time right extrapolator");
    ArgumentChecker.notNull(sabrModel, "SARB model");
    ArgumentChecker.notNull(weightingFunction, "weighting function");
    ArgumentChecker.notNull(useExternalBeta, "use external beta");
    ArgumentChecker.notNull(externalBeta, "external beta");
    _timeAxis = timeAxis;
    _yAxis = yAxis;
    _volatilityTransform = volatilityTransform;
    _timeInterpolator = timeInterpolator;
    _timeLeftExtrapolator = timeLeftExtrapolator;
    _timeRightExtrapolator = timeRightExtrapolator;
    _sabrModel = sabrModel;
    _weightingFunction = weightingFunction;
    _useExternalBeta = useExternalBeta;
    _externalBeta = externalBeta;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_MODEL);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_WEIGHTING_FUNCTION);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_USE_EXTERNAL_BETA);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_EXTERNAL_BETA);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS.equals(propertyName)) {
      return Collections.singleton(_timeAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS.equals(propertyName)) {
      return Collections.singleton(_yAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM.equals(propertyName)) {
      return Collections.singleton(_volatilityTransform);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeInterpolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeLeftExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeRightExtrapolator);
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
