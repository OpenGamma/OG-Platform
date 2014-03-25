/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import java.util.Arrays;

/**
 * 
 */
public class VolatilitySwapCalculatorResultWithStrikes extends VolatilitySwapCalculatorResult {
  private final double[] _putStrikes;
  private final double[] _callStrikes;

  /**
   * @param putStrikes The put option strikes
   * @param callStrikes The call option strikes
   * @param putWeights The weights of put options
   * @param straddleWeight The weight of straddle
   * @param callWeights The weights of call options
   * @param putPrices The put option prices
   * @param straddlePrice The straddle price
   * @param callPrices The call option prices
   * @param cash The cash amount
   */
  public VolatilitySwapCalculatorResultWithStrikes(final double[] putStrikes, final double[] callStrikes, final double[] putWeights, final double straddleWeight,
      final double[] callWeights, final double[] putPrices, final double straddlePrice, final double[] callPrices, final double cash) {
    super(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash);

    final int nPuts = putStrikes.length;
    final int nCalls = callStrikes.length;
    _putStrikes = new double[nPuts];
    _callStrikes = new double[nCalls];

    System.arraycopy(putStrikes, 0, _putStrikes, 0, nPuts);
    System.arraycopy(callStrikes, 0, _callStrikes, 0, nCalls);
  }

  /**
   * @param putStrikes The put option strikes
   * @param callStrikes The call option strikes
   * @param putWeights The weights of put options
   * @param straddleWeight The weight of straddle
   * @param callWeights The weights of call options
   * @param putPrices The put option prices
   * @param straddlePrice The straddle price
   * @param callPrices The call option prices
   * @param cash The cash amount
   * @param optionTotal The total option value
   * @param fairValue The fair value
   */
  public VolatilitySwapCalculatorResultWithStrikes(final double[] putStrikes, final double[] callStrikes, final double[] putWeights, final double straddleWeight,
      final double[] callWeights, final double[] putPrices, final double straddlePrice, final double[] callPrices, final double cash, final double optionTotal, final double fairValue) {
    super(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash, optionTotal, fairValue);

    final int nPuts = putStrikes.length;
    final int nCalls = callStrikes.length;
    _putStrikes = new double[nPuts];
    _callStrikes = new double[nCalls];

    System.arraycopy(putStrikes, 0, _putStrikes, 0, nPuts);
    System.arraycopy(callStrikes, 0, _callStrikes, 0, nCalls);
  }

  /**
   * Access _putStrikes
   * @return put option strikes
   */
  public double[] getPutStrikes() {
    return _putStrikes;
  }

  /**
   * Access _callStrikes
   * @return call option strikes
   */
  public double[] getCallStrikes() {
    return _callStrikes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_callStrikes);
    result = prime * result + Arrays.hashCode(_putStrikes);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof VolatilitySwapCalculatorResultWithStrikes)) {
      return false;
    }
    VolatilitySwapCalculatorResultWithStrikes other = (VolatilitySwapCalculatorResultWithStrikes) obj;
    if (!Arrays.equals(_callStrikes, other._callStrikes)) {
      return false;
    }
    if (!Arrays.equals(_putStrikes, other._putStrikes)) {
      return false;
    }
    return true;
  }

}
