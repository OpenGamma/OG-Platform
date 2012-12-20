/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.twoasset;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.twoasset.TwoAssetCorrelationOptionDefinition;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * The value of a two-asset correlation call option is:
 * $$
 * \begin{eqnarray*}
 * c = S_2 e ^ {(b_2 - r)T} M(y_2 + \sigma_2\sqrt{T}, y_1 + \rho\sigma_2\sqrt{T}; \rho) - P e^{-rT} M(y_2, y_1; \rho)
 * \end{eqnarray*}
 * $$
 * and that of a put is:
 * $$
 * \begin{eqnarray*}
 * p = P e^{-rT} M(-y_2, -y_1; \rho) - S_2 e^{(b_2 - r)T} M(-y_2 - \sigma_2\sqrt{T}, -y_1 - \rho\sigma_2\sqrt{T}; \rho)
 * \end{eqnarray*}
 * $$
 * where
 * $$
 * \begin{eqnarray*}
 * y_1 &=& \frac{\ln{\frac{S_1}{K_1}} + (b_1 - \frac{\sigma_1^2}{2})T}{\sigma_1\sqrt{T}}\\
 * y_2 &=& \frac{\ln{\frac{S_2}{P}} + (b_2 - \frac{\sigma_2^2}{2})T}{\sigma_2\sqrt{T}}
 * \end{eqnarray*}
 * $$
 * and
 * $$
 * <ul>
 * <li>$K$ is the strike</li>
 * <li>$P$ is the payoff</li>
 * <li>$S_1$ is the spot value of the first asset</li>
 * <li>$S_2$ is the spot value of the second asset</li>
 * <li>$b_1$ is the cost-of-carry of the first asset</li>
 * <li>$b_2$ is the cost-of-carry of the second asset</li>
 * <li>$T$ is the time to expiry of the option</li>
 * <li>$r$ is the spot interest rate for time $T$</li>
 * <li>$\sigma_1$ is the annualized volatility of the first asset</li>
 * <li>$\sigma_2$ is the annualized volatility of the second asset</li>
 * <li>$\rho$ is the correlation between the returns of the two assets</li>
 * <li>$M(x, y; \rho)$ is the CDF of the bivariate normal distribution   </li>
 * </ul>

 */
public class TwoAssetCorrelationOptionModel extends TwoAssetAnalyticOptionModel<TwoAssetCorrelationOptionDefinition, StandardTwoAssetOptionDataBundle> {
  private static final ProbabilityDistribution<double[]> BIVARIATE = new BivariateNormalDistribution();

  /**
   * Gets the pricing function for a European-style two-asset correlation option
   * @param definition The option definition
   * @return The pricing function
   * @throws IllegalArgumentException If the definition is null
   */
  @Override
  public Function1D<StandardTwoAssetOptionDataBundle, Double> getPricingFunction(final TwoAssetCorrelationOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardTwoAssetOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardTwoAssetOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s1 = data.getFirstSpot();
        final double s2 = data.getSecondSpot();
        final double k = definition.getStrike();
        final double payout = definition.getPayoutLevel();
        final double b1 = data.getFirstCostOfCarry();
        final double b2 = data.getSecondCostOfCarry();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double sigma1 = data.getFirstVolatility(t, k);
        final double sigma2 = data.getSecondVolatility(t, k);
        final double rho = data.getCorrelation();
        final double tSqrt = Math.sqrt(t);
        final double sigmaT1 = sigma1 * tSqrt;
        final double sigmaT2 = sigma2 * tSqrt;
        final double d1 = (Math.log(s1 / k) + t * (b1 - sigma1 * sigma1 / 2)) / sigmaT1;
        final double d2 = (Math.log(s2 / payout) + t * (b2 - sigma2 * sigma2 / 2)) / sigmaT2;
        final double df1 = Math.exp(t * (b2 - r));
        final double df2 = Math.exp(-r * t);
        final int sign = definition.isCall() ? 1 : -1;
        return sign * (s2 * df1 * BIVARIATE.getCDF(new double[] {sign * (d2 + sigmaT2), sign * (d1 + rho * sigmaT2), rho}) - payout * df2 * BIVARIATE.getCDF(new double[] {sign * d2, sign * d1, rho}));

      }

    };
  }

}
