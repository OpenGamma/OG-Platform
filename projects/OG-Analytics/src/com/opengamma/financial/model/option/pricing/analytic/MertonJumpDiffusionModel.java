/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.MertonJumpDiffusionModelDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.ConstantDoublesSurface;

/**
 * 
 * 
 */

public class MertonJumpDiffusionModel extends AnalyticOptionModel<OptionDefinition, MertonJumpDiffusionModelDataBundle> {
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final int N = 50;

  @Override
  public Function1D<MertonJumpDiffusionModelDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<MertonJumpDiffusionModelDataBundle, Double> pricingFunction = new Function1D<MertonJumpDiffusionModelDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final MertonJumpDiffusionModelDataBundle data) {
        Validate.notNull(data);
        final ZonedDateTime date = data.getDate();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(t, k);
        final double lambda = data.getLambda();
        final double gamma = data.getGamma();
        final double sigmaSq = sigma * sigma;
        final double delta = Math.sqrt(gamma * sigmaSq / lambda);
        final double z = Math.sqrt(sigmaSq - lambda * delta * delta);
        final double zSq = z * z;
        double sigmaAdjusted = z;
        final double lambdaT = lambda * t;
        double mult = Math.exp(-lambdaT);
        final StandardOptionDataBundle bsmData = new StandardOptionDataBundle(data.getInterestRateCurve(), data.getCostOfCarry(), new VolatilitySurface(ConstantDoublesSurface.from(sigmaAdjusted)),
            data.getSpot(), date);
        final Function1D<StandardOptionDataBundle, Double> bsmFunction = BSM.getPricingFunction(definition);
        double price = mult * bsmFunction.evaluate(bsmData);
        for (int i = 1; i < N; i++) {
          sigmaAdjusted = Math.sqrt(zSq + delta * delta * i / t);
          mult *= lambdaT / i;
          price += mult * bsmFunction.evaluate(bsmData.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(sigmaAdjusted))));
        }
        return price;
      }
    };
    return pricingFunction;
  }
}
