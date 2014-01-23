/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import java.util.ArrayList;
import java.util.List;

import cern.jet.math.Bessel;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * "Realized Volatility and Variance: Options via Swaps", 
 * Peter Carr and Roger Lee, Oct. 26, 2007
 */
public class CarrLeeSyntheticVolatilitySwapValuationModel {

  /**
   * Compute the present value of volatility swap which pays R_{0,T} where R_{0,T}^2 is the realized variance of returns over [0,T]
   * The respective strikes should be sorted in ascending order
   * @param spot The spot of underlying
   * @param putStrikes Strikes of put options
   * @param callStrikes Strikes of call options
   * @param timeToExpiry Time to expiry
   * @param interestRate Interest rate
   * @param putVols Volatilities of put options
   * @param strdVol Volatility of straddle
   * @param callVols Volatilities of call options
   * @return value of volatility swap
   */
  public double getVolatilitySwapValue(final double spot, final double[] putStrikes, final double[] callStrikes, final double timeToExpiry, final double interestRate, final double[] putVols,
      final double strdVol, final double[] callVols) {
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
    ArgumentChecker.isTrue(Doubles.isFinite(strdVol), "strdVol should be finite");
    ArgumentChecker.isTrue(strdVol > 0., "strdVol should be positive");
    for (int i = 0; i < nCalls; ++i) {
      ArgumentChecker.isTrue(Doubles.isFinite(callStrikes[i]), "callStrikes should be finite");
      ArgumentChecker.isTrue(callStrikes[i] > 0., "callStrikes should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(callVols[i]), "callVols should be finite");
      ArgumentChecker.isTrue(callVols[i] > 0., "callVols should be positive");
    }
    for (int i = 0; i < nPuts; ++i) {
      ArgumentChecker.isTrue(Doubles.isFinite(putStrikes[i]), "putStrikes should be finite");
      ArgumentChecker.isTrue(putStrikes[i] > 0., "putStrikes should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(putVols[i]), "putVols should be finite");
      ArgumentChecker.isTrue(putVols[i] > 0., "putVols should be positive");
    }

    final double discount = Math.exp(-interestRate * timeToExpiry);
    final double forward = spot / discount;
    ArgumentChecker.isTrue((callStrikes[0] > forward && putStrikes[nPuts - 1] < forward), "Max(putStrikes) < forward < Min(callStrikes) should hold");

    final double deltaK = callStrikes[0] - putStrikes[nPuts - 1];
    final boolean nearestIsCall = (callStrikes[0] - forward <= forward - putStrikes[nPuts - 1]);
    final double factor = 100. / Math.sqrt(timeToExpiry) * Math.sqrt(Math.PI * 0.5);

    double res = factor / forward;
    final double[] callWeights = getWeight(forward, callStrikes, deltaK, res);
    final double[] putWeights = getWeight(forward, putStrikes, deltaK, res);
    if (nearestIsCall) {
      callWeights[0] = getNearestWeight(forward, deltaK, callStrikes[0], res);
    } else {
      putWeights[nPuts - 1] = getNearestWeight(forward, deltaK, putStrikes[nPuts - 1], res);
    }

    res *= (BlackScholesFormulaRepository.price(spot, forward, timeToExpiry, strdVol, interestRate, interestRate, true) + BlackScholesFormulaRepository.price(spot, forward, timeToExpiry, strdVol,
        interestRate, interestRate, false));
    for (int i = 0; i < nCalls; ++i) {
      res += callWeights[i] * BlackScholesFormulaRepository.price(spot, callStrikes[i], timeToExpiry, callVols[i], interestRate, interestRate, true);
    }
    for (int i = 0; i < nPuts; ++i) {
      res += putWeights[i] * BlackScholesFormulaRepository.price(spot, putStrikes[i], timeToExpiry, putVols[i], interestRate, interestRate, false);
    }
    res += getCashAmount(callStrikes[0], putStrikes[nPuts - 1], forward, discount, factor);

    return res;
  }

  /**
   * Compute weights for market values of vanilla options replicating the volatility swap
   * The respective strikes should be sorted in ascending order
   * @param spot The spot of underlying
   * @param putStrikes Strikes of put options
   * @param callStrikes Strikes of call options
   * @param timeToExpiry Time to expiry
   * @param interestRate Interest rate
   * @param putVols Volatilities of put options
   * @param strdVol Volatility of straddle
   * @param callVols Volatilities of call options
   * @return List of weights with size 4, [{Put Weights}, {Straddle Weight}, {Call Weights}, {cash}]
   */
  public List<double[]> getWeights(final double spot, final double[] putStrikes, final double[] callStrikes, final double timeToExpiry, final double interestRate,
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
    ArgumentChecker.isTrue(Doubles.isFinite(timeToExpiry), "timeToExpiry should be finite");
    ArgumentChecker.isTrue(timeToExpiry > 0., "timeToExpiry should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(interestRate), "interestRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(strdVol), "strdVol should be finite");
    ArgumentChecker.isTrue(strdVol > 0., "strdVol should be positive");
    for (int i = 0; i < nCalls; ++i) {
      ArgumentChecker.isTrue(Doubles.isFinite(callStrikes[i]), "callStrikes should be finite");
      ArgumentChecker.isTrue(callStrikes[i] > 0., "callStrikes should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(callVols[i]), "callVols should be finite");
      ArgumentChecker.isTrue(callVols[i] > 0., "callVols should be positive");
    }
    for (int i = 0; i < nPuts; ++i) {
      ArgumentChecker.isTrue(Doubles.isFinite(putStrikes[i]), "putStrikes should be finite");
      ArgumentChecker.isTrue(putStrikes[i] > 0., "putStrikes should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(putVols[i]), "putVols should be finite");
      ArgumentChecker.isTrue(putVols[i] > 0., "putVols should be positive");
    }

    final double discount = Math.exp(-interestRate * timeToExpiry);
    final double forward = spot / discount;
    ArgumentChecker.isTrue((callStrikes[0] > forward && putStrikes[nPuts - 1] < forward), "Max(putStrikes) < forward < Min(callStrikes) should hold");

    final double deltaK = callStrikes[0] - putStrikes[nPuts - 1];
    final boolean nearestIsCall = (callStrikes[0] - forward <= forward - putStrikes[nPuts - 1]);
    final double factor = 100. / Math.sqrt(timeToExpiry) * Math.sqrt(Math.PI * 0.5);

    double res = factor / forward;
    final double[] callWeights = getWeight(forward, callStrikes, deltaK, res);
    final double[] putWeights = getWeight(forward, putStrikes, deltaK, res);
    if (nearestIsCall) {
      callWeights[0] = getNearestWeight(forward, deltaK, callStrikes[0], res);
    } else {
      putWeights[nPuts - 1] = getNearestWeight(forward, deltaK, putStrikes[nPuts - 1], res);
    }

    final double[] callRatio = new double[nCalls];
    final double[] putRatio = new double[nPuts];
    res *= (BlackScholesFormulaRepository.price(spot, forward, timeToExpiry, strdVol, interestRate, interestRate, true) + BlackScholesFormulaRepository.price(spot, forward, timeToExpiry, strdVol,
        interestRate, interestRate, false));
    double strdRatio = res;
    for (int i = 0; i < nCalls; ++i) {
      callRatio[i] = callWeights[i] * BlackScholesFormulaRepository.price(spot, callStrikes[i], timeToExpiry, callVols[i], interestRate, interestRate, true);
      res += callRatio[i];
    }
    for (int i = 0; i < nPuts; ++i) {
      putRatio[i] = putWeights[i] * BlackScholesFormulaRepository.price(spot, putStrikes[i], timeToExpiry, putVols[i], interestRate, interestRate, false);
      res += putRatio[i];
    }

    for (int i = 0; i < nCalls; ++i) {
      callRatio[i] /= res;
    }
    for (int i = 0; i < nPuts; ++i) {
      putRatio[i] /= res;
    }
    strdRatio /= res;

    List<double[]> resList = new ArrayList<>(4);
    resList.add(putRatio);
    resList.add(new double[] {strdRatio });
    resList.add(callRatio);
    resList.add(new double[] {getCashAmount(callStrikes[0], putStrikes[nPuts - 1], forward, discount, factor) });

    return resList;
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
