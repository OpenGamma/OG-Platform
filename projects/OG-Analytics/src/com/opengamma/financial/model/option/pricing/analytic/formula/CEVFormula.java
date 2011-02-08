/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.math.statistics.distribution.NonCentralChiSquareDistribution;

/**
 * CEV formula modified from Hull 
 */
public class CEVFormula {

  /**
   * European option price when the forward value follows a CEV dynamic df = sigma*f^beta dw
   * <b>Note:</b> the formula assumed that f = 0 is an absorbing barrier, which is obtainable for beta < 1/2 (hence the PDF will have a Dirac delta at f=0).
   *  At beta = 0.0 we switch to the normal pricer, which allows f < 0 (the PDF is now a Gaussian) - this gives a price jump since keeping the CEV formula means folding
   *  the f < 0 probability density into f = 0
   * @param f The forward value at maturity. This can be expressed in terms of stop s_0 by  f_t = s_0*exp((r-q)t) where r is the risk free rate,
   *  q is dividend yield (or other cost of carry) a t is time to maturity 
   * @param k The strike
   * @param beta The CEV parameter beta = 1 is log normal, while beta = 0 is normal
   * @param discountFactor The value of a zero coupon bond maturity at the expiry of the option 
   * @param sigma The CEV volatility 
   * @param t The time to maturity 
   * @param isCall True for call 
   * @return The option price. 
   */
  public static double optionPrice(final double f, final double k, final double beta, final double discountFactor, final double sigma, final double t, boolean isCall) {

    Validate.isTrue(beta >= 0.0, "beta less than zero not supported");

    if (beta == 1.0) {
      return BlackFormula.optionPrice(f, k, discountFactor, sigma, t, isCall);
    }

    if (beta == 0.0) {
      return NormalFormula.optionPrice(f, k, discountFactor, sigma, t, isCall);
    }

    final double b = 1.0 / (1 - beta);
    final double x = b * b / sigma / sigma / t;
    final double a = Math.pow(k, 2 * (1 - beta)) * x;
    final double c = Math.pow(f, 2 * (1 - beta)) * x;

    if (beta < 1) {

      NonCentralChiSquareDistribution chiSq1 = new NonCentralChiSquareDistribution(b + 2, c);
      NonCentralChiSquareDistribution chiSq2 = new NonCentralChiSquareDistribution(b, a);
      if (isCall) {
        return discountFactor * (f * (1 - chiSq1.getCDF(a)) - k * chiSq2.getCDF(c));
      }
      return discountFactor * (k * (1 - chiSq2.getCDF(c)) - f * chiSq1.getCDF(a));
    } 
    NonCentralChiSquareDistribution chiSq1 = new NonCentralChiSquareDistribution(-b, a);
    NonCentralChiSquareDistribution chiSq2 = new NonCentralChiSquareDistribution(2 - b, c);
    if (isCall) {
      return discountFactor * (f * (1 - chiSq1.getCDF(c)) - k * chiSq2.getCDF(a));
    }
    return discountFactor * (k * (1 - chiSq2.getCDF(a)) - f * chiSq1.getCDF(c));    
  }

}
