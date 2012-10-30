/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;

/**
 * 
 */
public class SkewnessKurtosisBlackScholesMertonEquivalentVolatilitySurfaceModel implements VolatilitySurfaceModel<OptionDefinition, SkewKurtosisOptionDataBundle> {

  @Override
  public VolatilitySurface getSurface(final OptionDefinition option, final SkewKurtosisOptionDataBundle data) {
    Validate.notNull(option, "option");
    Validate.notNull(data, "data");
    final double s = data.getSpot();
    final double t = option.getTimeToExpiry(data.getDate());
    final double k = option.getStrike();
    final double sigma = data.getVolatility(t, k);
    final double b = data.getCostOfCarry();
    final double skew = data.getAnnualizedSkew();
    final double kurtosis = data.getAnnualizedFisherKurtosis();
    final double d1 = (Math.log(s / k) + t * (b + sigma * sigma * 0.5)) / sigma / Math.sqrt(t);
    return new VolatilitySurface(ConstantDoublesSurface.from(sigma * (1 - skew * d1 / 6 - kurtosis * (1 - d1 * d1) / 24)));
  }

}
