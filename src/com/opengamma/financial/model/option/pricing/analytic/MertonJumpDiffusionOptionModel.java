/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.MertonJumpDiffusionModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 * 
 */

public class MertonJumpDiffusionOptionModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, MertonJumpDiffusionModelOptionDataBundle> {
  protected BlackScholesMertonModel _bsm = new BlackScholesMertonModel();

  @Override
  public Function1D<MertonJumpDiffusionModelOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    final Function1D<MertonJumpDiffusionModelOptionDataBundle, Double> pricingFunction = new Function1D<MertonJumpDiffusionModelOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final MertonJumpDiffusionModelOptionDataBundle data) {
        try {
          final ZonedDateTime date = data.getDate();
          final double k = definition.getStrike();
          final double t = definition.getTimeToExpiry(date);
          final double sigma = data.getVolatility(t, k);
          final double lambda = data.getLambda();
          final double gamma = data.getGamma();
          final double sigmaSq = sigma * sigma;
          final double delta = gamma * sigmaSq / lambda;
          double sigmaAdjusted = Math.sqrt(sigmaSq - lambda * delta);
          final double lambdaT = lambda * t;
          double mult = Math.exp(-lambdaT);
          // TODO this is not right
          final StandardOptionDataBundle bsmData = new StandardOptionDataBundle(data.getDiscountCurve(), data.getCostOfCarry(), data.getVolatilitySurface(), data.getSpot(), date);
          final Function1D<StandardOptionDataBundle, Double> bsmFunction = _bsm.getPricingFunction(definition);
          double price = mult * bsmFunction.evaluate(bsmData);
          for (int i = 1; i < 50; i++) {
            sigmaAdjusted = Math.sqrt(sigmaAdjusted * sigmaAdjusted + delta * i / t);
            mult *= lambdaT / i;
            // TODO have to change volatility
            price += mult * bsmFunction.evaluate(bsmData);
          }
          return price;
        } catch (final InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }

    };
    return pricingFunction;
  }
}
