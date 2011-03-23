/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.BatesGeneralizedJumpDiffusionModelDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.ConstantDoublesSurface;

/**
 * The Bates generalized jump diffusion model is described by the jump-diffusion process
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * dS = (b - \\lambda \\overline{k})S dt + \\sigma S dz + k dq
 * \\end{align*}
 * }
 * with {@latex.inline $S$} the spot, {@latex.inline $b$} the cost-of-carry, {@latex.inline $\\sigma$} the volatility of the (relative) price
 * change based on no jumps, {@latex.inline $k$} a random percentage jump conditional on a Poisson-distributed event occurring, with ({@latex.inline $1+k$})
 * lognormally distributed, {@latex.inline $\\overline{k}$} the expected jump size, {@latex.inline $\\lambda$} the frequency of events (the average number
 * of events per year) and {@latex.inline $q$} a Poisson counter with intensity {@latex.inline $\\lambda$}.
 * <p>
 * The price of an option can be calculated using:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * c &= \\sum_{i=0}^{\\infty} \\frac{e^{-\\lambda T}(\\lambda T)^i}{i!}c_i(S, K, T, r, b_i, \\sigma_i)\\\\
 * p &= \\sum_{i=0}^{\\infty} \\frac{e^{-\\lambda T}(\\lambda T)^i}{i!}p_i(S, K, T, r, b_i, \\sigma_i)
 * \\end{align*}
 * }  
 * where
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * b_i &= b - \\lambda \\overline{k} + \\frac{i\\overline{\\gamma}}{T}\\\\
 * \\sigma_i &= \\sqrt{\\sigma^2 + \\delta^2\\frac{i}{T}}\\\\
 * \\overline{\\gamma} &= \\ln(1 + \\overline{k}) 
 * \\end{align*}
 * }
 * and {@latex.inline $\\delta$} is the standard deviation of log asset price jumps.
 */
public class BatesGeneralizedJumpDiffusionModel extends AnalyticOptionModel<OptionDefinition, BatesGeneralizedJumpDiffusionModelDataBundle> {
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final int N = 50;

  /**
   * {@inheritDoc}
   */
  @Override
  public Function1D<BatesGeneralizedJumpDiffusionModelDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<BatesGeneralizedJumpDiffusionModelDataBundle, Double> pricingFunction = new Function1D<BatesGeneralizedJumpDiffusionModelDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final BatesGeneralizedJumpDiffusionModelDataBundle data) {
        Validate.notNull(data);
        final double s = data.getSpot();
        final YieldAndDiscountCurve discountCurve = data.getInterestRateCurve();
        final VolatilitySurface volSurface = data.getVolatilitySurface();
        final ZonedDateTime date = data.getDate();
        final double t = definition.getTimeToExpiry(date);
        final double k = definition.getStrike();
        final double sigma = data.getVolatility(t, k);
        double b = data.getCostOfCarry();
        final double lambda = data.getLambda();
        final double expectedJumpSize = data.getExpectedJumpSize();
        final double delta = data.getDelta();
        final double gamma = Math.log(1 + expectedJumpSize);
        final double sigmaSq = sigma * sigma;
        double z;
        final double lambdaT = lambda * t;
        double mult = Math.exp(-lambdaT);
        b -= lambda * expectedJumpSize;
        StandardOptionDataBundle bsmData = new StandardOptionDataBundle(discountCurve, b, volSurface, s, date);
        final Function1D<StandardOptionDataBundle, Double> bsmFunction = BSM.getPricingFunction(definition);
        double price = mult * bsmFunction.evaluate(bsmData);
        for (int i = 1; i < N; i++) {
          z = Math.sqrt(sigmaSq + delta * delta * i / t);
          b += gamma / t;
          bsmData = bsmData.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(z))).withCostOfCarry(b);
          mult *= lambdaT / i;
          price += mult * bsmFunction.evaluate(bsmData);
        }
        return price;
      }
    };
    return pricingFunction;
  }
}
