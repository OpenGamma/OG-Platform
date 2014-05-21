/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.AdaptiveCompositeIntegrator1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * "Realized Volatility and Variance: Options via Swaps", 
 * Peter Carr and Roger Lee, Oct. 26, 2007
 */
public class CarrLeeSeasonedSyntheticVolatilitySwapCalculator {
  private static final Integrator1D<Double, Double> INTEGRATOR = new AdaptiveCompositeIntegrator1D(new RungeKuttaIntegrator1D());
  private static final double EPS = 1.e-12;

  /**
   * The respective strikes should be sorted in ascending order
   * @param spot The spot 
   * @param putStrikes The strikes for put options
   * @param callStrikes The strikes for call options
   * @param timeToExpiry The time to expiry
   * @param timeFromInception The time after the inception date
   * @param interestRate The interest rate
   * @param dividend The dividend
   * @param putVols The volatilities for put options
   * @param callVols The volatilities for call options
   * @param rvReturns The realized variance of log returns
   * @return  {@link VolatilitySwapCalculatorResult}
   */
  public VolatilitySwapCalculatorResult evaluate(final double spot, final double[] putStrikes, final double[] callStrikes, final double timeToExpiry, final double timeFromInception,
      final double interestRate, final double dividend, final double[] putVols, final double[] callVols, final double rvReturns) {
    ArgumentChecker.notNull(callStrikes, "callStrikes");
    ArgumentChecker.notNull(putStrikes, "putStrikes");
    ArgumentChecker.notNull(callVols, "callVols");
    ArgumentChecker.notNull(putVols, "putVols");

    final int nCalls = callStrikes.length;
    final int nPuts = putStrikes.length;

    ArgumentChecker.isTrue(callVols.length == nCalls, "callVols.length == callStrikes.length should hold");
    ArgumentChecker.isTrue(putVols.length == nPuts, "putVols.length == putStrikes.length should hold");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "spot should be finite");
    ArgumentChecker.isTrue(spot > 0., "spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(timeToExpiry), "timeToExpiry should be finite");
    ArgumentChecker.isTrue(timeToExpiry > 0., "timeToExpiry should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(timeFromInception), "timeFromInception should be finite");
    ArgumentChecker.isTrue(timeFromInception > 0., "timeFromInception should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend), "dividend should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(rvReturns), "rvReturns should be finite");
    ArgumentChecker.isTrue(rvReturns > 0., "rvReturns should be positive");

    final double deltaK = (callStrikes[nCalls - 1] - putStrikes[0]) / (nCalls + nPuts - 1);
    for (int i = 0; i < nCalls; ++i) {
      ArgumentChecker.isTrue(Doubles.isFinite(callStrikes[i]), "callStrikes should be finite");
      ArgumentChecker.isTrue(callStrikes[i] > 0., "callStrikes should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(callVols[i]), "callVols should be finite");
      ArgumentChecker.isTrue(callVols[i] > 0., "callVols should be positive");
      if (i < nCalls - 1) {
        ArgumentChecker.isTrue(Math.abs(callStrikes[i + 1] - callStrikes[i] - deltaK) < EPS, "All of the strikes  should be equally spaced");
      }
    }
    for (int i = 0; i < nPuts; ++i) {
      ArgumentChecker.isTrue(Doubles.isFinite(putStrikes[i]), "putStrikes should be finite");
      ArgumentChecker.isTrue(putStrikes[i] > 0., "putStrikes should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(putVols[i]), "putVols should be finite");
      ArgumentChecker.isTrue(putVols[i] > 0., "putVols should be positive");
      if (i < nPuts - 1) {
        ArgumentChecker.isTrue(Math.abs(putStrikes[i + 1] - putStrikes[i] - deltaK) < EPS, "All of the strikes  should be equally spaced");
      }
    }

    final double rate = interestRate - dividend;
    final double forward = spot * Math.exp(rate * timeToExpiry);
    ArgumentChecker.isTrue((callStrikes[0] >= forward && putStrikes[nPuts - 1] <= forward), "Max(putStrikes) <= forward <= Min(callStrikes) should hold");

    final double u = 100. / Math.sqrt(timeToExpiry + timeFromInception);
    final double us = 100. / Math.sqrt(timeFromInception);
    final double resRV = rvReturns * timeFromInception * 1.e-4;
    final double coef = u * Math.sqrt(2. / Math.PI);

    final double[] callWeights = getWeight(forward, callStrikes, deltaK, resRV, coef);
    final double[] putWeights = getWeight(forward, putStrikes, deltaK, resRV, coef);

    final double distance = callStrikes[0] + putStrikes[nPuts - 1] - 2. * forward;
    if (distance < -EPS) {
      callWeights[0] = getNearestWeight(forward, deltaK, callStrikes[0], resRV, coef);
    } else if (distance > EPS) {
      putWeights[nPuts - 1] = getNearestWeight(forward, deltaK, putStrikes[nPuts - 1], resRV, coef);
    }

    final double[] putPrices = new double[nPuts];
    final double[] callPrices = new double[nCalls];
    for (int i = 0; i < nCalls; ++i) {
      callPrices[i] = BlackScholesFormulaRepository.price(spot, callStrikes[i], timeToExpiry, callVols[i], interestRate, rate, true);
    }
    for (int i = 0; i < nPuts; ++i) {
      putPrices[i] = BlackScholesFormulaRepository.price(spot, putStrikes[i], timeToExpiry, putVols[i], interestRate, rate, false);
    }
    final double cash = Math.exp(-interestRate * timeToExpiry) * Math.sqrt(rvReturns) * u / us;

    return new VolatilitySwapCalculatorResult(putWeights, 0., callWeights, putPrices, 0., callPrices, cash);
  }

  private double[] getWeight(final double forward, final double[] strikes, final double deltaK, final double resRV, final double coef) {
    final int nOptions = strikes.length;
    final double[] res = new double[nOptions];
    final double reFac = 0.5 * deltaK * coef;

    for (int i = 0; i < nOptions; ++i) {
      final double logKF = Math.log(strikes[i] / forward);
      if (Math.abs(logKF) < EPS) {
        res[i] = reFac * Math.sqrt(2. * Math.PI / resRV) / strikes[i] / strikes[i];
      } else {
        final double bound = 50. / Math.sqrt(resRV);
        final Function1D<Double, Double> funcFin = integrandFin(logKF, resRV);
        final Function1D<Double, Double> funcInf = integrandInf(logKF, resRV);
        res[i] = reFac * Math.exp(0.5 * logKF) * (INTEGRATOR.integrate(funcFin, 0., 0.5 * Math.PI) + INTEGRATOR.integrate(funcInf, 0., bound)) / strikes[i] / strikes[i];
      }
    }

    return res;
  }

  private double getNearestWeight(final double forward, final double deltaK, final double kStar, final double resRV, final double coef) {

    final double hDeltaK = 0.5 * deltaK;
    final double kp = kStar + hDeltaK;
    final double km = kStar - hDeltaK;
    final double logKpF = Math.log(kp / forward);
    final double logKmF = Math.log(km / forward);

    return coef * (getCloseIntegrals(resRV, logKpF) / kp - getCloseIntegrals(resRV, logKmF) / km);
  }

  private double getCloseIntegrals(final double resRV, final double logKF) {
    final Function1D<Double, Double> funcFin = closeIntegrandFin(logKF, resRV);
    final Function1D<Double, Double> funcInf = closeIntegrandInf(logKF, resRV);
    final double bound = 50. / Math.sqrt(resRV);
    return Math.exp(0.5 * logKF) * (INTEGRATOR.integrate(funcFin, 0., 0.5 * Math.PI) + INTEGRATOR.integrate(funcInf, 0., bound));
  }

  private Function1D<Double, Double> integrandFin(final double logKF, final double resRV) {

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final double cosx = Math.cos(x);
        return Math.exp(-0.125 * resRV * Math.pow(Math.sin(x), 2.)) * (cosx * Math.cosh(0.5 * logKF * cosx) - Math.sinh(0.5 * logKF * cosx));
      }
    };
  }

  private Function1D<Double, Double> integrandInf(final double logKF, final double resRV) {

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final double tmp = 1. + x * x;
        final double half = 0.5 * logKF;
        return Math.exp(-0.125 * resRV * tmp) * ((0.25 * resRV * x * x - half) * tmp - 1.) * Math.sin(half * x) / Math.pow(tmp, 1.5) / half;
      }
    };
  }

  private Function1D<Double, Double> closeIntegrandFin(final double logKF, final double resRV) {

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        return Math.exp(-0.125 * resRV * Math.pow(Math.sin(x), 2.)) * Math.sinh(0.5 * logKF * Math.cos(x));
      }
    };
  }

  private Function1D<Double, Double> closeIntegrandInf(final double logKF, final double resRV) {

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final double tmp = 1. + x * x;
        return Math.exp(-0.125 * resRV * tmp) * Math.sin(0.5 * logKF * x) / Math.sqrt(tmp);
      }
    };
  }

}
