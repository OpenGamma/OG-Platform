/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class BlackPriceFunction implements OptionPriceFunction<BlackFunctionData> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<BlackFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    return new Function1D<BlackFunctionData, Double>() {

      @Override
      public Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double forward = data.getForward();
        final double sigma = data.getBlackVolatility();
        final double df = data.getDiscountFactor();
        return df * BlackFormulaRepository.price(forward, k, t, sigma, isCall);
      }
    };
  }

  /**
   * Return the Black price and its derivatives.
   * @param option The option.
   * @param data The Black data.
   * @return An array with [0] the price, [1] the derivative with respect to the forward, [2] the derivative with respect to the volatility and 
   * [3] the derivative with respect to the strike.
   */
  //TODO Refactor the method call to have the price as output and the derivatives as an array (like getPriceAdjoint2).
  // TODO: [PLAT-6343]Add the derivative to the time to expiry (theta)
  public double[] getPriceAdjoint(final EuropeanVanillaOption option, final BlackFunctionData data) {
    /**
     * The array storing the price and derivatives.
     */
    double[] priceAdjoint = new double[4];
    /**
     * The cut-off for small time and strike.
     */
    final double eps = 1E-16;

    final double strike = option.getStrike();
    final double timeToExpiry = option.getTimeToExpiry();
    final double vol = data.getBlackVolatility();
    final double forward = data.getForward();
    final boolean isCall = option.isCall();
    final double discountFactor = data.getDiscountFactor();
    double sqrttheta = Math.sqrt(timeToExpiry);
    double omega = isCall ? 1 : -1;
    // Implementation Note: Forward sweep.
    double volblack = 0, kappa = 0, d1 = 0, d2 = 0;
    double x = 0;
    if (strike < eps || sqrttheta < eps) {
      x = omega * (forward - strike);
      priceAdjoint[0] = (x > 0 ? discountFactor * x : 0.0);
    } else {
      volblack = vol * sqrttheta;
      kappa = Math.log(forward / strike) / volblack - 0.5 * volblack;
      d1 = NORMAL.getCDF(omega * (kappa + volblack));
      d2 = NORMAL.getCDF(omega * kappa);
      priceAdjoint[0] = discountFactor * omega * (forward * d1 - strike * d2);
    }
    // Implementation Note: Backward sweep.
    double pBar = 1.0;
    double forwardBar = 0, strikeBar = 0, volblackBar = 0, volatilityBar = 0;
    if (strike < eps || sqrttheta < eps) {
      forwardBar = (x > 0 ? discountFactor * omega : 0.0);
      strikeBar = (x > 0 ? -discountFactor * omega : 0.0);
    } else {
      double d1Bar = discountFactor * omega * forward * pBar;
      double density1 = NORMAL.getPDF(omega * (kappa + volblack));
      // Implementation Note: kappa_bar = 0; no need to implement it.
      // Methodology Note: kappa_bar is optimal exercise boundary. The
      // derivative at the optimal point is 0.
      forwardBar = discountFactor * omega * d1 * pBar;
      strikeBar = -discountFactor * omega * d2 * pBar;
      volblackBar = density1 * omega * d1Bar;
      volatilityBar = sqrttheta * volblackBar;
    }
    priceAdjoint[1] = forwardBar;
    priceAdjoint[2] = volatilityBar;
    priceAdjoint[3] = strikeBar;
    return priceAdjoint;
  }

  /**
   * Return the Black price and its first and second order derivatives.
   * @param option The option.
   * @param data The Black data.
   * @param bsD An array containing the price derivative [0] the derivative with respect to the forward, [1] the derivative with respect to the volatility and 
   * [2] the derivative with respect to the strike.
   * @param bsD2 An array of array containing the price second order derivatives. 
   * Second order derivatives with respect to: [0][0] forward-forward [0][1] forward-volatility [0][2] forward-strike [1][1]volatility-volatility, 
   * [1][2] volatility-strike, [2][2] strike-strike
   * @return The price.
   */
  public double getPriceAdjoint2(final EuropeanVanillaOption option, final BlackFunctionData data, double[] bsD, double[][] bsD2) {
    /**
     * The cut-off for small time and strike.
     */
    final double eps = 1E-16;

    double p;
    // Forward sweep
    final double strike = option.getStrike();
    final double timeToExpiry = option.getTimeToExpiry();
    final double vol = data.getBlackVolatility();
    final double forward = data.getForward();
    final boolean isCall = option.isCall();
    final double discountFactor = data.getDiscountFactor();
    double sqrttheta = Math.sqrt(timeToExpiry);
    double omega = isCall ? 1 : -1;
    // Implementation Note: Forward sweep.
    double volblack = 0, kappa = 0, d1 = 0, d2 = 0;
    double x = 0;
    if (strike < eps || sqrttheta < eps) {
      x = omega * (forward - strike);
      p = (x > 0 ? discountFactor * x : 0.0);
      volblack = sqrttheta < eps ? 0 : (vol * sqrttheta);
    } else {
      volblack = vol * sqrttheta;
      kappa = Math.log(forward / strike) / volblack - 0.5 * volblack;
      d1 = NORMAL.getCDF(omega * (kappa + volblack));
      d2 = NORMAL.getCDF(omega * kappa);
      p = discountFactor * omega * (forward * d1 - strike * d2);
    }
    // Implementation Note: Backward sweep.
    double pBar = 1.0;
    double density1 = 0.0;
    double d1Bar = 0.0;
    double forwardBar = 0, strikeBar = 0, volblackBar = 0, volatilityBar = 0;
    if (strike < eps || sqrttheta < eps) {
      forwardBar = (x > 0 ? discountFactor * omega : 0.0);
      strikeBar = (x > 0 ? -discountFactor * omega : 0.0);
    } else {
      d1Bar = discountFactor * omega * forward * pBar;
      density1 = NORMAL.getPDF(omega * (kappa + volblack));
      // Implementation Note: kappa_bar = 0; no need to implement it.
      // Methodology Note: kappa_bar is optimal exercise boundary. The
      // derivative at the optimal point is 0.
      forwardBar = discountFactor * omega * d1 * pBar;
      strikeBar = -discountFactor * omega * d2 * pBar;
      volblackBar = density1 * omega * d1Bar;
      volatilityBar = sqrttheta * volblackBar;
    }
    bsD[0] = forwardBar;
    bsD[1] = volatilityBar;
    bsD[2] = strikeBar;
    if (strike < eps || sqrttheta < eps) {
      return p;
    }
    // Backward sweep: second derivative
    double d2Bar = -discountFactor * omega * strike;
    double density2 = NORMAL.getPDF(omega * kappa);
    double d1Kappa = omega * density1;
    double d1KappaKappa = -(kappa + volblack) * d1Kappa;
    double d2Kappa = omega * density2;
    double d2KappaKappa = -kappa * d2Kappa;
    double kappaKappaBar2 = d1KappaKappa * d1Bar + d2KappaKappa * d2Bar;
    double kappaV = -Math.log(forward / strike) / (volblack * volblack) - 0.5;
    double kappaVV = 2 * Math.log(forward / strike) / (volblack * volblack * volblack);
    double d1TotVV = density1 * (-(kappa + volblack) * (kappaV + 1) * (kappaV + 1) + kappaVV);
    double d2TotVV = d2KappaKappa * kappaV * kappaV + d2Kappa * kappaVV;
    double vVbar2 = d1Bar * d1TotVV + d2Bar * d2TotVV;
    double volVolBar2 = vVbar2 * timeToExpiry;
    double kappaStrikeBar2 = -discountFactor * omega * d2Kappa;
    double kappaStrike = -1.0 / (strike * volblack);
    double strikeStrikeBar2 = (kappaKappaBar2 * kappaStrike + 2 * kappaStrikeBar2) * kappaStrike;
    double kappaStrikeV = 1.0 / strike / (volblack * volblack);
    double d1VK = -omega * (kappa + volblack) * density1 * (kappaV + 1) * kappaStrike + omega * density1 * kappaStrikeV;
    double d2V = d2Kappa * kappaV;
    double d2VK = -omega * kappa * density2 * kappaV * kappaStrike + omega * density2 * kappaStrikeV;
    double strikeD2Bar2 = -discountFactor * omega;
    double strikeVolblackBar2 = strikeD2Bar2 * d2V + d1Bar * d1VK + d2Bar * d2VK;
    double strikeVolBar2 = strikeVolblackBar2 * sqrttheta;
    double kappaForward = 1.0 / (forward * volblack);
    double forwardForwardBar2 = discountFactor * omega * d1Kappa * kappaForward;
    double strikeForwardBar2 = discountFactor * omega * d1Kappa * kappaStrike;
    double volForwardBar2 = discountFactor * omega * d1Kappa * (kappaV + 1) * sqrttheta;
    bsD2[0][0] = forwardForwardBar2;
    bsD2[0][1] = volForwardBar2;
    bsD2[1][0] = volForwardBar2;
    bsD2[0][2] = strikeForwardBar2;
    bsD2[2][0] = strikeForwardBar2;
    bsD2[1][1] = volVolBar2;
    bsD2[1][2] = strikeVolBar2;
    bsD2[2][1] = strikeVolBar2;
    bsD2[2][2] = strikeStrikeBar2;

    return p;
  }

  public Function1D<BlackFunctionData, Double> getVegaFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<BlackFunctionData, Double>() {

      @Override
      public Double evaluate(final BlackFunctionData data) {
        Validate.notNull(data, "data");
        final double sigma = data.getBlackVolatility();
        final double f = data.getForward();
        final double discountFactor = data.getDiscountFactor();
        return discountFactor * BlackFormulaRepository.vega(f, k, t, sigma);
      }
    };
  }

}
