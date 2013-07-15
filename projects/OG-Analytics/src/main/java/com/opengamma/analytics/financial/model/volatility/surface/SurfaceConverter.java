/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class SurfaceConverter {
  /** The tolerance */
  private static final double EPS = 1e-12;
  /** The root bracketer */
  private static final BracketRoot BRACKETER = new BracketRoot();
  /** The root-finder */
  private static final BisectionSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder(EPS);
  /** A normal distribution */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  /** A static instance of this class */
  private static final SurfaceConverter INSTANCE = new SurfaceConverter();

  private SurfaceConverter() {
  }

  /**
   * Gets the static instance of this class
   * @return The static instance
   */
  public static SurfaceConverter getInstance() {
    return INSTANCE;
  }

  Surface<Double, Double, Double> deltaToLogMoneyness(final Surface<Double, Double, Double> deltaSurf) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");

        //find the delta that gives the required log-moneyness (x) at time t
        final double delta = getDeltaForLogMoneyness(x, deltaSurf, t);
        return deltaSurf.getZValue(t, delta);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  Surface<Double, Double, Double> deltaToMoneyness(final Surface<Double, Double, Double> deltaSurf) {
    final Surface<Double, Double, Double> logMoneynessSurf = deltaToLogMoneyness(deltaSurf);
    return logMoneynessToMoneyness(logMoneynessSurf);
  }

  Surface<Double, Double, Double> deltaToStrike(final Surface<Double, Double, Double> deltaSurf, final ForwardCurve forwardCurve) {
    final Surface<Double, Double, Double> moneynessSurf = deltaToMoneyness(deltaSurf);
    return moneynessToStrike(moneynessSurf, forwardCurve);
  }

  Surface<Double, Double, Double> logMoneynessToDelta(final Surface<Double, Double, Double> logMoneynessSurf) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... td) {
        final double t = td[0];
        final double delta = td[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");
        ArgumentChecker.isTrue(delta > 0 && delta < 1.0, "Delta not in range (0,1)");

        //find the log-moneyness that gives the required Black delta at the given time
        final double x = getlogMoneynessForDelta(delta, logMoneynessSurf, t);
        return logMoneynessSurf.getZValue(t, x);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  Surface<Double, Double, Double> logMoneynessToMoneyness(final Surface<Double, Double, Double> logMoneynessSurf) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double m = tx[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");
        ArgumentChecker.isTrue(m > 0, "Must have moneyness > 0");
        final double logM = Math.log(m);
        return logMoneynessSurf.getZValue(t, logM);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  Surface<Double, Double, Double> logMoneynessToStrike(final Surface<Double, Double, Double> logMoneynessSurf, final ForwardCurve forwardCurve) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");
        ArgumentChecker.isTrue(k > 0, "Must have strike > 0");
        final double f = forwardCurve.getForward(t);
        final double logM = Math.log(k / f);
        return logMoneynessSurf.getZValue(t, logM);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  Surface<Double, Double, Double> moneynessToDelta(final Surface<Double, Double, Double> moneynessSurf) {
    final Surface<Double, Double, Double> logMoneynessSurf = moneynessToLogMoneyness(moneynessSurf);
    return logMoneynessToDelta(logMoneynessSurf);
  }

  Surface<Double, Double, Double> moneynessToLogMoneyness(final Surface<Double, Double, Double> moneynessSurf) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double lm = tx[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");
        final double m = Math.exp(lm);
        return moneynessSurf.getZValue(t, m);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  /**
   * Convert a volatility surface parameterised by moneyness (strike/forward)  to one parameterised by strike
   * @param  strikeSurf volatility surface parameterised by  moneyness
   * @param forwardCurve The forward Curve
   * @return volatility surface parameterised by strike
   */
  Surface<Double, Double, Double> moneynessToStrike(final Surface<Double, Double, Double> moneynessSurf, final ForwardCurve forwardCurve) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");
        ArgumentChecker.isTrue(k > 0, "Must have strike > 0");
        final double m = k / forwardCurve.getForward(t);
        return moneynessSurf.getZValue(t, m);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  Surface<Double, Double, Double> strikeToDelta(final Surface<Double, Double, Double> strikeSurf, final ForwardCurve forwardCurve) {
    final Surface<Double, Double, Double> logMoneynessSurf = strikeToLogMoneyness(strikeSurf, forwardCurve);
    return logMoneynessToDelta(logMoneynessSurf);
  }

  /**
   * Convert a volatility surface parameterised by strike to one parameterised by moneyness (strike/forward)
   * @param  strikeSurf volatility surface parameterised by strike
   * @param forwardCurve The forward Curve
   * @return volatility surface parameterised by moneyness
   */
  Surface<Double, Double, Double> strikeToLogMoneyness(final Surface<Double, Double, Double> strikeSurf, final ForwardCurve forwardCurve) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");
        final double k = Math.exp(x) * forwardCurve.getForward(t);
        return strikeSurf.getZValue(t, k);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  /**
   * Convert a volatility surface parameterised by strike to one parameterised by moneyness (strike/forward)
   * @param  strikeSurf volatility surface parameterised by strike
   * @param forwardCurve The forward Curve
   * @return volatility surface parameterised by moneyness
   */
  Surface<Double, Double, Double> strikeToMoneyness(final Surface<Double, Double, Double> strikeSurf, final ForwardCurve forwardCurve) {
    final Function<Double, Double> surFunc = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];
        ArgumentChecker.isTrue(t >= 0, "Must have t >= 0.0");
        ArgumentChecker.isTrue(m > 0, "Must have moneyness > 0");
        final double k = m * forwardCurve.getForward(t);
        return strikeSurf.getZValue(t, k);
      }
    };
    return FunctionalDoublesSurface.from(surFunc);
  }

  double getlogMoneynessForDelta(final double delta, final Surface<Double, Double, Double> logMoneynessSurface, final double t) {

    ArgumentChecker.isInRangeExclusive(0.0, 1.0, delta);
    ArgumentChecker.notNull(logMoneynessSurface, "null surface");
    ArgumentChecker.isTrue(t > 0, "t must be possitive");

    final double rootT = Math.sqrt(t);
    final double inDelta = NORMAL.getInverseCDF(delta);

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final double sigma = logMoneynessSurface.getZValue(t, x);
        return sigma * sigma * t / 2 - inDelta * sigma * rootT - x;
      }
    };

    final double xEst = func.evaluate(0.0);

    double l, u;
    double xMin, xMax;
    if (xEst < 0.0) {
      xMin = -100.0;
      xMax = 1.0;
      l = Math.max(xMin, 1.25 * xEst);
      u = 0.75 * xEst;
    } else {
      xMin = -1.0;
      xMax = 100.0;
      l = 0.75 * xEst;
      u = Math.min(xMax, 1.25 * xEst);
    }

    try {
      final double[] bracket = BRACKETER.getBracketedPoints(func, l, u, xMin, xMax);
      final double x = ROOT_FINDER.getRoot(func, bracket[0], bracket[1]);
      return x;
    } catch (final MathException e) {
      String error = "Cannot find a log-moneyness corresponding to a delta of " + delta;
      if (delta < 0.05) {
        error += " It is possible that the smile exhibits arbitrable for very high strikes. Check that the call price is always decreasing in strike. ";
      } else if (delta > 0.95) {
        error += " It is possible that the smile exhibits arbitrable for very low strikes. Check that the put price is always increasing in strike. ";
      } else {
        error += " It is likely that the smile exhibits arbitrage";
      }
      throw new MathException(error + " " + e.getMessage());
    }

  }

  double getDeltaForLogMoneyness(final double x, final Surface<Double, Double, Double> deltaSurface, final double t) {

    final double rootT = Math.sqrt(t);

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double d1) {
        @SuppressWarnings("synthetic-access")
        final double delta = NORMAL.getCDF(d1);
        if (delta == 1.0 || delta == 0.0) {
          return -d1;
        }
        try {
          final double sigma = deltaSurface.getZValue(t, delta);
          return (-x + sigma * sigma * t / 2) / sigma / rootT - d1;
        } catch (final MathException e) {
          return -d1;
        }
      }
    };

    final double dEst = func.evaluate(0.0);
    double l, u;
    if (dEst < 0.0) {
      l = 1.25 * dEst;
      u = 0.75 * dEst;
    } else {
      l = 0.75 * dEst;
      u = 1.25 * dEst;
    }

    final double[] bracket = BRACKETER.getBracketedPoints(func, l, u);
    final Double d1 = ROOT_FINDER.getRoot(func, bracket[0], bracket[1]);

    return NORMAL.getCDF(d1);
  }

}
