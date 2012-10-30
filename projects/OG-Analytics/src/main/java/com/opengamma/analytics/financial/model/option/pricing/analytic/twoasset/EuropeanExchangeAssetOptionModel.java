/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.twoasset;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.twoasset.EuropeanExchangeAssetOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * The value of a European-style exchange-asset option is given by:
 * $$
 * \begin{eqnarray*}
 * Q_1 S_1 e^{(b_1 - r)T} N(d_1) - Q_2 S_2 e^{(b_2 - r)T} N(d_2)
 * \end{eqnarray*}
 * $$
 * where
 * $$
 * \begin{eqnarray*}
 * \hat{\sigma} &=& \sqrt{\sigma_1 ^2 + \sigma_2 ^2 - 2 \rho\sigma_1\sigma_2}\\
 * d_1 &=& \frac{\ln{\frac{Q_1 S_1}{Q_2 S_2}} + \left(b_1 - b_2 + \frac{\hat{\sigma}^2}{2}\right) T }{\hat{\sigma} \sqrt{T}}\\
 * d_2 &=& d_1 - \hat{\sigma}\sqrt{T}
 * \end{eqnarray*}
 * $$
 * and
 * <ul>
 * <li>$Q_1$ is the quantity of the first asset</li>
 * <li>$Q_2$ is the quantity of the second asset</li>
 * <li>$S_1$ is the spot value of the first asset</li>
 * <li>$S_2$ is the spot value of the second asset</li>
 * <li>$b_1$ is the cost-of-carry of the first asset</li>
 * <li>$b_2$ is the cost-of-carry of the second asset</li>
 * <li>$T$ is the time to expiry of the option</li>
 * <li>$r$ is the spot interest rate for time $T$</li>
 * <li>$\sigma_1$ is the annualized volatility of the first asset</li>
 * <li>$\sigma_2$ is the annualized volatility of the second asset</li>
 * <li>$N(x)$ is the CDF of the normal distribution $N(0, 1)$  </li>
 * </ul>
 */
public class EuropeanExchangeAssetOptionModel extends TwoAssetAnalyticOptionModel<EuropeanExchangeAssetOptionDefinition, StandardTwoAssetOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Gets the pricing function for a European-style exchange asset option 
   * @param definition The option definition
   * @return The pricing function
   * @throws IllegalArgumentException If the definition is null
   */
  @Override
  public Function1D<StandardTwoAssetOptionDataBundle, Double> getPricingFunction(final EuropeanExchangeAssetOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardTwoAssetOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardTwoAssetOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s1 = data.getFirstSpot();
        final double s2 = data.getSecondSpot();
        final double b1 = data.getFirstCostOfCarry();
        final double b2 = data.getSecondCostOfCarry();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double sigma1 = data.getFirstVolatility(t, s1);
        final double sigma2 = data.getSecondVolatility(t, s2);
        final double rho = data.getCorrelation();
        final double q1 = definition.getFirstQuantity();
        final double q2 = definition.getSecondQuantity();
        final double sigma = Math.sqrt(sigma1 * sigma1 + sigma2 * sigma2 - 2 * rho * sigma1 * sigma2);
        final double sigmaT = sigma * Math.sqrt(t);
        final double d1 = (Math.log(q1 * s1 / q2 / s2) + t * (b1 - b2 + sigma * sigma / 2)) / sigmaT;
        final double d2 = d1 - sigmaT;
        return q1 * s1 * Math.exp(t * (b1 - r)) * NORMAL.getCDF(d1) - q2 * s2 * Math.exp(t * (b2 - r)) * NORMAL.getCDF(d2);
      }

    };
  }

}
