/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle2;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.Strike;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class VarianceSwapStaticReplicationStrike extends VarianceSwapStaticReplication2<Strike> {

  public VarianceSwapStaticReplicationStrike() {
    super(new Strike(EPS), new Strike(20.0));
  }

  public VarianceSwapStaticReplicationStrike(final Strike lowerBound, final Strike upperBound, final Integrator1D<Double, Double> integrator,
      final Strike cutoffLevel, final Strike cutoffSpread) {
    super(lowerBound, upperBound, integrator, cutoffLevel, cutoffSpread);
  }

  @Override
  protected Function1D<Double, Double> getMainIntegrand(final double expiry, final double fwd, final BlackVolatilitySurface<Strike> volSurf) {
    // 3. Define the hedging portfolio: The position to hold in each otmOption(k) = 2 / strike^2,
    //                                       where otmOption is a call if k > fwd and a put otherwise
    final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike) {
        final boolean isCall = strike > fwd;
        final double weight = 1.0 / (strike * strike);
        final double vol = volSurf.getVolatility(expiry, strike);
        final double otmPrice = BlackFormulaRepository.price(fwd, strike, expiry, vol, isCall);

        return otmPrice * weight;
      }
    };
    return otmOptionAndWeight;
  }

  @Override
  protected Pair<double[], double[]> getTailExtrapolationParameters(double fwd, double expiry, BlackVolatilitySurface<Strike> volSurf) {
    double[] ks = new double[2];
    double[] vols = new double[2];
    ks[0] = getCutoffLevel().value();
    ks[1] = ks[0] + getCutoffSpread().value();
    vols[0] = volSurf.getVolatility(expiry, ks[0]);
    vols[1] = volSurf.getVolatility(expiry, ks[1]);
    return new ObjectsPair<double[], double[]>(ks, vols);
  }

  //TODO this is v ugly
  @Override
  protected Pair<Strike, Strike> getIntegralLimits(double expiry, VarianceSwapDataBundle2<Strike> market) {
    final double fwd = market.getForwardCurve().getForward(expiry);
    if (!isCutoffProvided()) {
      return new ObjectsPair<Strike, Strike>(new Strike(getLowerBound().value() * fwd), new Strike(getUpperBound().value() * fwd));
    }
    double lower = Math.max(getLowerBound().value(), getCutoffLevel().value());
    return new ObjectsPair<Strike, Strike>(new Strike(lower * fwd), new Strike(getUpperBound().value() * fwd));
  }
}
