/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.tuple.DoublesPair;

import org.apache.commons.lang.Validate;

/**
 * We construct a model independent method to price variance as a static replication
 * of an (in)finite sum of call and put option prices on the underlying.
 * Here, we assume the existence of a smooth function of these option prices / Implied volatilities.
 * Construction of this function is done exogenously.
 */
public class VarSwapStaticReplication {

  // ************************** Black *****************
  // * TODO Review where a Black(fwd,strike,variance,isCall) function might live
  // * I've rewritten the Black function instead of using BlackPriceFunction as that was based on an 'option'
  // * where my usage here is simply to create a Function1D:  k -> price. 
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final double Black(double fwd, double strike, double variance, boolean isCall) {

    final double small = 1.0E-16;

    if (strike < small) {
      return isCall ? fwd : 0.0;
    }
    final int sign = isCall ? 1 : -1;
    final double sigmaRootT = Math.sqrt(variance);
    if (Math.abs(fwd - strike) < small) {
      return fwd * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1);
    }
    if (sigmaRootT < small) {
      return Math.max(sign * (fwd - strike), 0.0);
    }
    final double d1 = (Math.log(fwd / strike) + 0.5 * variance) / sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    return sign * (fwd * NORMAL.getCDF(sign * d1) - strike * NORMAL.getCDF(sign * d2));
  }

  /**
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, spot underlying, and funding curve
   * @return presentValue of the variance *remaining* in the swap. 
   */
  public static double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    // 1. Compute the forward
    // TODO Review whether fwd=spot/df is sufficient, or whether the fwd itself should be in the VarianceSwapDataBundle
    final double expiry = deriv.getTimeToSettlement();
    final double df = market.getDiscountCurve().getDiscountFactor(expiry);
    final double spot = market.getSpotUnderlying();
    final double fwd = spot / df;

    // 2. Define integrand, the position to hold in each otmOption(k) = 2 / strike^2, where otmOption is a call if k>fwd and a put otherwise
    //    Note:  strike space is parameterized wrt the forward, moneyness := strike/fwd i.e. atm moneyness=1
    final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double moneyness) {

        double strike = moneyness * fwd;
        VolatilitySurface vsurf = market.getVolatilitySurface();
        DoublesPair coord = DoublesPair.of(expiry, strike);
        double vol = vsurf.getVolatility(coord);

        boolean isCall = moneyness > 1; // if strike > fwd, the call is out of the money..
        double otmPrice = Black(fwd, moneyness * fwd, vol * vol * expiry, isCall);
        double weight = 2 / (fwd * moneyness * moneyness);
        return otmPrice * weight;
      }
    };

    // 3. Compute variance hedge by integrating positions over all strikes
    // TODO How do I expose control params of pricing methods to user?
    final double lowerBound = 1e-9; // almost zero
    final double upperBound = 10.0; // multiple of the atm forward
    final Integrator1D<Double, Double> integrator = new RungeKuttaIntegrator1D();

    double variance = integrator.integrate(otmOptionAndWeight, lowerBound, upperBound);
    return variance;
  }
}
