package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Date;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.BatesGeneralizedJumpDiffusionModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 * 
 */

public class BatesGeneralizedJumpDiffusionOptionModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, BatesGeneralizedJumpDiffusionModelOptionDataBundle> {
  protected BlackScholesMertonModel _bsm = new BlackScholesMertonModel();

  @Override
  public Function1D<BatesGeneralizedJumpDiffusionModelOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    Function1D<BatesGeneralizedJumpDiffusionModelOptionDataBundle, Double> pricingFunction = new Function1D<BatesGeneralizedJumpDiffusionModelOptionDataBundle, Double>() {

      @Override
      public Double evaluate(BatesGeneralizedJumpDiffusionModelOptionDataBundle data) {
        try {
          double s = data.getSpot();
          DiscountCurve discountCurve = data.getDiscountCurve();
          VolatilitySurface volSurface = data.getVolatilitySurface();
          Date date = data.getDate();
          double t = definition.getTimeToExpiry(date);
          double k = definition.getStrike();
          double sigma = data.getVolatility(t, k);
          double b = data.getCostOfCarry();
          double lambda = data.getLambda();
          double expectedJumpSize = data.getExpectedJumpSize();
          double delta = data.getDelta();
          double gamma = Math.log(1 + expectedJumpSize);
          double sigmaSq = sigma * sigma;
          double z = Math.sqrt(sigmaSq - lambda * delta);
          double lambdaT = lambda * t;
          double mult = Math.exp(-lambdaT);
          b -= lambda * expectedJumpSize;
          StandardOptionDataBundle bsmData = new StandardOptionDataBundle(discountCurve, b, volSurface, s, date);
          Function1D<StandardOptionDataBundle, Double> bsmFunction = _bsm.getPricingFunction(definition);
          double price = mult * bsmFunction.evaluate(bsmData);
          for (int i = 1; i < 50; i++) {
            sigma = Math.sqrt(z * z + delta * i / t);
            b += gamma / t;
            // TODO this is wrong - need to change the surface
            bsmData = new StandardOptionDataBundle(discountCurve, b, volSurface, s, date);
            mult *= lambdaT / i;
            price += mult * bsmFunction.evaluate(bsmData);
          }
          return price;
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }
}
