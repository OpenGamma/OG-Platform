/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public abstract class LocalVolatilitySurfaceConverter {

  public static LocalVolatilitySurfaceMoneyness toMoneynessSurface(final LocalVolatilitySurfaceStrike from, final ForwardCurve fwdCurve) {

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double f = fwdCurve.getForward(t);
        double k = x * f;
        return from.getVolatility(t, k);
      }
    };

    return new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(surFunc), fwdCurve);
  }

  public static LocalVolatilitySurfaceStrike toStrikeSurface(final LocalVolatilitySurfaceMoneyness from) {

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];
        return from.getVolatility(t, k);
      }
    };

    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surFunc));
  }

  /**
   * Under the usual local volatility assumption, the expiry-strike parameterised surface is invariant to a change in the
   * forward curve of the underlying. This of course means that the expiry-moneyness parameterised surface (where moneyness
   * = strike/forward) is not invariant. This gives a new expiry-moneyness local volatility surface under a shifted forward curve.
   * @param from The original expiry-moneyness local volatility surface
   * @param newForwardCurve New Forward Curve
   * @return New expiry-moneyness local volatility surface, such that the expiry-strike surface would be unchanged
   */
  public static LocalVolatilitySurfaceMoneyness shiftForwardCurve(final LocalVolatilitySurfaceMoneyness from, final ForwardCurve newForwardCurve) {

    final ForwardCurve forwardCurve = from.getForwardCurve();

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double f = forwardCurve.getForward(t);
        double fPrime = newForwardCurve.getForward(t);
        double xPrime = x * fPrime / f;
        return from.getVolatilityForMoneyness(t, xPrime);
      }
    };

    return new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(surFunc), newForwardCurve);

  }

  /**
   * Under the usual local volatility assumption, the expiry-strike parameterised surface is invariant to a change in the
   * forward curve of the underlying. This of course means that the expiry-moneyness parameterised surface (where moneyness
   * = strike/forward) is not invariant. This gives a new expiry-moneyness local volatility surface under a shifted forward curve.
   * @param from The original expiry-moneyness local volatility surface
   * @param shift fraction shift amount, i.e. 0.1 with produce a forward curve 10% larger than the original
   * @return New expiry-moneyness local volatility surface, such that the expiry-strike surface would be unchanged
   */
  public static LocalVolatilitySurfaceMoneyness shiftForwardCurve(final LocalVolatilitySurfaceMoneyness from, final double shift) {
    Validate.isTrue(shift > -1, "shift must be > -1");
    final ForwardCurve newForwardCurve = from.getForwardCurve().withFractionalShift(shift);

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double xPrime = x * (1 + shift);
        return from.getVolatilityForMoneyness(t, xPrime);
      }
    };

    return new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(surFunc), newForwardCurve);

  }

}
