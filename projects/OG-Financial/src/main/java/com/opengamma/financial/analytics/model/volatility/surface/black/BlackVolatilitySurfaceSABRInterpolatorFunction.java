/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_EXTERNAL_BETA;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_MODEL;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_USE_EXTERNAL_BETA;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_WEIGHTING_FUNCTION;

import java.util.Set;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 */
public class BlackVolatilitySurfaceSABRInterpolatorFunction extends BlackVolatilitySurfaceInterpolatorFunction {

  @Override
  protected Set<ValueRequirement> getSpecificRequirements(final ValueProperties constraints) {
    return BlackVolatilitySurfacePropertyUtils.ensureSABRVolatilityInterpolatorProperties(constraints);
  }

  @Override
  protected GeneralSmileInterpolator getSmileInterpolator(final ValueRequirement desiredValue) {
    final String modelName = desiredValue.getConstraint(PROPERTY_SABR_MODEL);
    final String weightingFunctionName = desiredValue.getConstraint(PROPERTY_SABR_WEIGHTING_FUNCTION);
    final VolatilityFunctionProvider<SABRFormulaData> model = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory.getCalculator(modelName);
    final WeightingFunction weightingFunction = WeightingFunctionFactory.getWeightingFunction(weightingFunctionName);
    final boolean useExternalBeta = Boolean.parseBoolean(desiredValue.getConstraint(PROPERTY_SABR_USE_EXTERNAL_BETA));
    if (useExternalBeta) {
      final double beta = Double.parseDouble(desiredValue.getConstraint(PROPERTY_SABR_EXTERNAL_BETA));
      return new SmileInterpolatorSABR(model, beta, weightingFunction);
    }
    return new SmileInterpolatorSABR(model, weightingFunction);
  }

  @Override
  protected ValueProperties getResultProperties() {
    return BlackVolatilitySurfacePropertyUtils.addSABRVolatilityInterpolatorProperties(createValueProperties().get()).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    return BlackVolatilitySurfacePropertyUtils.addSABRVolatilityInterpolatorProperties(createValueProperties().get(), desiredValue).get();
  }
}
