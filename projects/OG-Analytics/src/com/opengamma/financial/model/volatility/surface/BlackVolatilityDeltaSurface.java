/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication.StrikeParameterization;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.math.surface.Surface;

/**
 * Like BlackVolatilitySurface, a surface that contains the Black (aka implied, aka lognormal) volatility  as a function of time to maturity and strike.
 * Here, however, the strike is parameterised in terms of the Delta (dPrice/dUnderlying) of a Call.
 * The range of strikes is thus [0,1]. At 0.5, the delta of a call is equal to that of a put at same strike, but with opposite sign. Hence a straddle here is delta-neutral.
 * Note: Though based on call deltas, in practice, the volatilities for strikes below 0.5 will often be based on put prices.
 * This is because out-of-the-money options are more liquid than in-the-money
 */
public class BlackVolatilityDeltaSurface extends BlackVolatilitySurface {

  private final boolean _strikeAxisRepresentsCalls;

  /**
   * @param surface Surface<Expiry, Delta, LognormalVol>
   */
  public BlackVolatilityDeltaSurface(Surface<Double, Double, Double> surface) {
    this(surface, true);
  }

  public BlackVolatilityDeltaSurface(Surface<Double, Double, Double> surface, final boolean strikeAxisRepresentsCalls) {
    super(surface, strikeAxisRepresentsCalls ? StrikeParameterization.CALLDELTA : StrikeParameterization.PUTDELTA);
    _strikeAxisRepresentsCalls = strikeAxisRepresentsCalls;

  }

  public BlackVolatilityDeltaSurface(Surface<Double, Double, Double> surface, final StrikeParameterization strikeType) {
    super(surface, strikeType);
    _strikeAxisRepresentsCalls = strikeType == StrikeParameterization.CALLDELTA;

  }

  //  public BlackVolatilityDeltaSurface(final BlackVolatilitySurface surface) {
  //    this(strikeToDeltaSurface(surface).getSurface(),true);
  //  }

  //  public static BlackVolatilityDeltaSurface strikeToDeltaSurface(final BlackVolatilitySurface surface) {
  //
  //    Function<Double, Double> func = new Function<Double, Double>() {
  //
  //      @Override
  //      public Double evaluate(Double... deltaT) {
  //        double delta = deltaT[0];
  //        double t = deltaT[1];
  //        BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, false, delta, t, t)
  //        return null;
  //      }
  //    };
  //
  //    return null;
  //  }

  @Override
  public double getVolatility(final double expiry, final double delta) {
    return super.getVolatility(expiry, delta);
  }

  public double getStrike(final double expiry, final double delta, final double forward) {
    double vol = getVolatility(expiry, delta);

    BlackFormula black = new BlackFormula(forward, forward, expiry, vol, null, true);
    return black.computeStrikeImpliedByForwardDelta(delta, _strikeAxisRepresentsCalls);
  }

  @Override
  public double getForwardPrice(final double expiry, final double delta, final double forward, boolean isCall) {
    Validate.isTrue(0.0 <= delta && delta <= 1.0, "delta provided was not in 0..1. Perhaps a strike price was provided.");
    double vol = getVolatility(expiry, delta);
    BlackFormula black = new BlackFormula(forward, forward, expiry, vol, null, isCall);
    black.setStrike(black.computeStrikeImpliedByForwardDelta(delta, _strikeAxisRepresentsCalls));
    return black.computePrice();
  }

  /**
   * @return true if strike axis represents call deltas ([1.0, 0.9, 0.75, ..., 0]. false if it represents put deltas [0, .., 1]
   */
  public final boolean strikeAxisRepresentsCalls() {
    return _strikeAxisRepresentsCalls;
  }
}
