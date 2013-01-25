/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.MertonJumpDiffusionModelDataBundle;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;

/**
 * The Merton jump-diffusion model prices options with an underlying process
 * $$
 * \begin{align*}
 * dS = (b - \lambda k)S dt + \sigma S dz + k dq
 * \end{align*}
 * $$
 * with $S$ the spot, $b$ the cost-of-carry, $\sigma$ the volatility of the
 * (relative) price change based on no jumps, $dz$ a Brownian motion, $dq$ a
 * jump component and $\lambda$ the expected number of jumps per year. $dz$ and
 * $dq$ are assumed to be uncorrelated.
 * <p>
 * The price of an option can be calculated using:
 * $$
 * \begin{align*}
 * c &= \sum_{i=0}^{\infty} \frac{e^{-\lambda T}(\lambda T)^i}{i!}c_i(S, K, T, r, \sigma_i)\\
 * p &= \sum_{i=0}^{\infty} \frac{e^{-\lambda T}(\lambda T)^i}{i!}p_i(S, K, T, r, \sigma_i)
 * \end{align*}
 * $$
 * where
 * $$
 * \begin{align*}
 * \sigma_i &= \sqrt{z^2 + \frac{\delta^2 i}{T}}\\
 * \delta &= \sqrt{\frac{\gamma v^2}{\lambda}}\\
 * z &= \sqrt{v^2 - \lambda \delta^2}
 * \end{align*}
 * $$
 * and $v$ is the total volatility (including jumps) and $\gamma$ is the
 * percentage of the total volatility explained by the jumps. 
 */
public class MertonJumpDiffusionModel extends AnalyticOptionModel<OptionDefinition, MertonJumpDiffusionModelDataBundle> {
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final int N = 50;

  /**
   * {@inheritDoc}
   */
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
