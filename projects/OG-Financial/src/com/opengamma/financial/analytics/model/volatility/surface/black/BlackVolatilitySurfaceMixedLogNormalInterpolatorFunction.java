/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION;

import java.util.Set;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorMixedLogNormal;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 */
public class BlackVolatilitySurfaceMixedLogNormalInterpolatorFunction extends BlackVolatilitySurfaceInterpolatorFunction {

  @Override
  protected Set<ValueRequirement> getSpecificRequirements(final ValueProperties constraints) {
    return BlackVolatilitySurfacePropertyUtils.ensureMixedLogNormalVolatilityInterpolatorProperties(constraints);
  }

  @Override
  protected GeneralSmileInterpolator getSmileInterpolator(final ValueRequirement desiredValue) {
    final String weightingFunctionName = desiredValue.getConstraint(PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION);
    final WeightingFunction weightingFunction = WeightingFunctionFactory.getWeightingFunction(weightingFunctionName);
    return new SmileInterpolatorMixedLogNormal(weightingFunction);
  }

  @Override
  protected ValueProperties getResultProperties() {
    return BlackVolatilitySurfacePropertyUtils.addMixedLogNormalVolatilityInterpolatorProperties(createValueProperties().get()).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    return BlackVolatilitySurfacePropertyUtils.addMixedLogNormalVolatilityInterpolatorProperties(createValueProperties().get(), desiredValue).get();
  }
}
