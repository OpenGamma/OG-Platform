/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.option.definition.MertonJumpDiffusionModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class MertonJumpDiffusionModel extends AnalyticOptionModel<OptionDefinition, MertonJumpDiffusionModelOptionDataBundle> {
  protected final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> _bsm = new BlackScholesMertonModel();
  protected final int N = 50;

  @Override
  public Function1D<MertonJumpDiffusionModelOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Definition was null");
    final Function1D<MertonJumpDiffusionModelOptionDataBundle, Double> pricingFunction = new Function1D<MertonJumpDiffusionModelOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final MertonJumpDiffusionModelOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
        final ZonedDateTime date = data.getDate();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(t, k);
        final double lambda = data.getLambda();
        final double gamma = data.getGamma();
        final double sigmaSq = sigma * sigma;
        if (lambda == 0)
          throw new IllegalArgumentException("Cannot have lambda of zero");
        final double delta = Math.sqrt(gamma * sigmaSq / lambda);
        final double z = Math.sqrt(sigmaSq - lambda * delta * delta);
        final double zSq = z * z;
        double sigmaAdjusted = z;
        final double lambdaT = lambda * t;
        double mult = Math.exp(-lambdaT);
        final StandardOptionDataBundle bsmData = new StandardOptionDataBundle(data.getDiscountCurve(), data.getCostOfCarry(), new ConstantVolatilitySurface(sigmaAdjusted), data
            .getSpot(), date);
        final Function1D<StandardOptionDataBundle, Double> bsmFunction = _bsm.getPricingFunction(definition);
        double price = mult * bsmFunction.evaluate(bsmData);
        for (int i = 1; i < N; i++) {
          sigmaAdjusted = Math.sqrt(zSq + delta * delta * i / t);
          mult *= lambdaT / i;
          price += mult * bsmFunction.evaluate(bsmData.withVolatilitySurface(new ConstantVolatilitySurface(sigmaAdjusted)));
        }
        return price;
      }
    };
    return pricingFunction;
  }
}
