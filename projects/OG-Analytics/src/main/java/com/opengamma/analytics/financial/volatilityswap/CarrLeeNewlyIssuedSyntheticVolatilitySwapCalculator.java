/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import cern.jet.math.Bessel;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * "Realized Volatility and Variance: Options via Swaps", 
 * Peter Carr and Roger Lee, Oct. 26, 2007
 */
public class CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator {
  private static final double EPS = 1.e-12;

  /**
   * The respective strikes should be sorted in ascending order
   * @param spot The spot of underlying
   * @param putStrikes Strikes of put options
   * @param callStrikes Strikes of call options
   * @param timeToExpiry Time to expiry
   * @param interestRate Interest rate
   * @param dividend The dividend
   * @param putVols Volatilities of put options
   * @param strdVol Volatility of straddle
   * @param callVols Volatilities of call options
   * @return {@link VolatilitySwapCalculatorResult}
   */
  public VolatilitySwapCalculatorResult evaluate(final double spot, final double[] putStrikes, final double[] callStrikes, final double timeToExpiry, final double interestRate, final double dividend,
      final double[] putVols, final double strdVol, final double[] callVols) {
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
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(dividend), "dividend should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(strdVol), "strdVol should be finite");
    ArgumentChecker.isTrue(strdVol > 0., "strdVol should be positive");

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
    final double discount = Math.exp(-interestRate * timeToExpiry);
    final double forward = spot * Math.exp(rate * timeToExpiry);
    ArgumentChecker.isTrue((callStrikes[0] > forward && putStrikes[nPuts - 1] < forward), "Max(putStrikes) < forward < Min(callStrikes) should hold");

    final double factor = 100. / Math.sqrt(timeToExpiry) * Math.sqrt(Math.PI * 0.5);

    final double straddleWeight = factor / forward;
    final double[] callWeights = getWeight(forward, callStrikes, deltaK, straddleWeight);
    final double[] putWeights = getWeight(forward, putStrikes, deltaK, straddleWeight);

    final double distance = callStrikes[0] + putStrikes[nPuts - 1] - 2. * forward;
    if (distance < -EPS) {
      callWeights[0] = getNearestWeight(forward, deltaK, callStrikes[0], straddleWeight);
    } else if (distance > EPS) {
      putWeights[nPuts - 1] = getNearestWeight(forward, deltaK, putStrikes[nPuts - 1], straddleWeight);
    }

    final double[] putPrices = new double[nPuts];
    final double[] callPrices = new double[nCalls];
    final double straddlePrice = BlackScholesFormulaRepository.price(spot, forward, timeToExpiry, strdVol, interestRate, rate, true) + BlackScholesFormulaRepository.price(spot, forward,
        timeToExpiry, strdVol, interestRate, rate, false);
    for (int i = 0; i < nCalls; ++i) {
      callPrices[i] = BlackScholesFormulaRepository.price(spot, callStrikes[i], timeToExpiry, callVols[i], interestRate, rate, true);
    }
    for (int i = 0; i < nPuts; ++i) {
      putPrices[i] = BlackScholesFormulaRepository.price(spot, putStrikes[i], timeToExpiry, putVols[i], interestRate, rate, false);
    }
    final double cash = getCashAmount(callStrikes[0], putStrikes[nPuts - 1], forward, discount, factor);

    return new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash);
  }

  private double[] getWeight(final double forward, final double[] strikes, final double deltaK, final double strdWeight) {
    final int nStrikes = strikes.length;
    final double[] res = new double[nStrikes];

    final double comFactor = 0.5 * strdWeight * deltaK / forward;
    for (int i = 0; i < nStrikes; ++i) {
      final double mLocal = 0.5 * Math.log(strikes[i] / forward);
      res[i] = comFactor * Math.signum(mLocal) * Math.exp(-3. * mLocal) * (Bessel.i1(mLocal) - Bessel.i0(mLocal));
    }
    return res;
  }

  private double getNearestWeight(final double forward, final double deltaK, final double kStar, final double strdWeight) {
    final double mVal1 = 0.5 * Math.log((kStar + 0.5 * deltaK) / forward);
    final double mVal2 = 0.5 * Math.log((kStar - 0.5 * deltaK) / forward);
    return strdWeight * (Math.signum(mVal1) * Math.exp(-mVal1) * Bessel.i0(mVal1) - Math.signum(mVal2) * Math.exp(-mVal2) * Bessel.i0(mVal2) - 2.);
  }

  private double getCashAmount(final double callStrike, final double putStrike, final double forward, final double discount, final double factor) {
    final double m1 = Math.log(callStrike / forward);
    final double m2 = Math.log(putStrike / forward);
    final double first = (callStrike - forward) * (Math.abs(Math.exp(0.5 * m2) * m2 * (Bessel.i0(0.5 * m2) - Bessel.i1(0.5 * m2))) - Math.abs(putStrike / forward - 1.));
    final double second = (forward - putStrike) * (Math.abs(Math.exp(0.5 * m1) * m1 * (Bessel.i0(0.5 * m1) - Bessel.i1(0.5 * m1))) - Math.abs(callStrike / forward - 1.));
    return discount * factor * (first + second) / (callStrike - putStrike);
  }
}
