/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.definition.ConstantElasticityOfVarianceModelDataBundle;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConstantElasticityOfVarianceBlackEquivalentVolatilitySurfaceModel implements VolatilitySurfaceModel<Map<OptionDefinition, Double>, ConstantElasticityOfVarianceModelDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(ConstantElasticityOfVarianceBlackEquivalentVolatilitySurfaceModel.class);

  @Override
  public VolatilitySurface getSurface(final Map<OptionDefinition, Double> optionData, final ConstantElasticityOfVarianceModelDataBundle data) {
    Validate.notNull(optionData, "option data");
    ArgumentChecker.notEmpty(optionData, "option data");
    Validate.notNull(data, "data");
    if (optionData.size() > 1) {
      s_logger.warn("Have more than one option: only using the first");
    }
    final OptionDefinition option = optionData.keySet().iterator().next();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry(data.getDate());
    final double sigma = data.getVolatility(t, k);
    final double beta = data.getElasticity();
    final double forward = data.getSpot();
    final double f = 0.5 * (forward + k);
    final double beta1 = 1 - beta;
    final double sigmaAdjusted = sigma * (1 + beta1 * (2 + beta) * (f - k) * (f - k) / 24 / f / f + beta1 * beta1 * sigma * sigma * t / 24 / Math.pow(f, 2 * beta1)) / Math.pow(f, beta1);
    return new VolatilitySurface(ConstantDoublesSurface.from(sigmaAdjusted));
  }

}
