/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public abstract class BlackVolatilitySurfaceConverter {

  private static final double EPS = 1e-6;
  private static final BracketRoot BRACKETER = new BracketRoot();
  private static final BisectionSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder(EPS);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public static BlackVolatilitySurfaceMoneyness toMoneynessSurface(final BlackVolatilitySurfaceStrike from, final ForwardCurve fwdCurve) {

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tm) {
        double t = tm[0];
        double m = tm[1];
        double f = fwdCurve.getForward(t);
        double k = m * f;
        return from.getVolatility(t, k);
      }
    };
    return new BlackVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(surFunc), fwdCurve);
  }

  public static BlackVolatilitySurfaceStrike toStrikeSurface(final BlackVolatilitySurfaceMoneyness from) {

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];
        return from.getVolatility(t, k);
      }
    };
    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surFunc));
  }

  public static BlackVolatilitySurfaceDelta toDeltaSurface(final BlackVolatilitySurfaceStrike from, final ForwardCurve forwardCurve) {

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... td) {
        final double t = td[0];
        double delta = td[1];
        final double fwd = forwardCurve.getForward(t);
        final double rootT = Math.sqrt(t);
        final double inDelta = NORMAL.getInverseCDF(delta);

        Function1D<Double, Double> func = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(Double k) {
            final double sigma = from.getVolatility(t, k);
            return fwd * Math.exp(-sigma * rootT * inDelta + sigma * sigma * t / 2) - k;
          }
        };

        double sigma = from.getVolatility(t, fwd);
        final double strikeApprox = fwd * Math.exp(-sigma * rootT * inDelta + sigma * sigma * t / 2);
        double l = 0.8 * strikeApprox;
        double u = 1.2 * strikeApprox;
        double minStrike = 0.0;
        double maxStrike = 1000 * fwd;

        final double[] range = BRACKETER.getBracketedPoints(func, l, u, minStrike, maxStrike);
        double strike = ROOT_FINDER.getRoot(func, range[0], range[1]);
        return from.getVolatility(t, strike);

        //        double sigmaOld = from.getVolatility(t, fwd);
        //        double k = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, true, fwd, t, sigmaOld);
        //        double sigmaNew = from.getVolatility(t, k);
        //        int iterations = 0;
        //        while (Math.abs(sigmaNew - sigmaOld) > EPS) {
        //          sigmaOld = sigmaNew;
        //          k = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, true, fwd, t, sigmaOld);
        //          sigmaNew = from.getVolatility(t, k);
        //          iterations++;
        //          if (iterations > ITERATIONS_MAX) {
        //            throw new MathException("Failed to find volatility for delta of " + delta);
        //          }
        //        }
        //        return sigmaNew;

      }
    };
    return new BlackVolatilitySurfaceDelta(FunctionalDoublesSurface.from(surFunc), forwardCurve);
  }

  public static BlackVolatilitySurfaceStrike toStrikeSurface(final BlackVolatilitySurfaceDelta from) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];
        return from.getVolatility(t, k);
      }
    };
    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surFunc));
  }

}
