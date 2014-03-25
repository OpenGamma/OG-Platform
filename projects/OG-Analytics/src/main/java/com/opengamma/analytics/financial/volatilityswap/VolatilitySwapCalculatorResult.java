/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import java.util.Arrays;

/**
 * Result of a volatility swap calculator containing:
 * Replicating portfolio consisting of put options with price (_putPrices) and weight (_putWeights), call options with price (_callPrices) and weight (_callWeights),
 * straddle with price (_straddleWeight) and weight (_straddlePrice), and cash amount (_cash).
 * Fair value of the volatility swap (_fairValue) given by the sum of options (_optionTotal) plus the cash amount.
 */
public class VolatilitySwapCalculatorResult {

  private final double _fairValue;
  private final double _optionTotal;
  private final double[] _putWeights;
  private final double _straddleWeight;
  private final double[] _callWeights;
  private final double[] _putPrices;
  private final double _straddlePrice;
  private final double[] _callPrices;
  private final double _cash;

  /**
   * @param putWeights The weights of put options
   * @param straddleWeight The weight of straddle
   * @param callWeights The weights of call options
   * @param putPrices The put option prices
   * @param straddlePrice The straddle price
   * @param callPrices The call option prices
   * @param cash The cash amount
   */
  public VolatilitySwapCalculatorResult(final double[] putWeights, final double straddleWeight, final double[] callWeights, final double[] putPrices, final double straddlePrice,
      final double[] callPrices, final double cash) {
    final int nPuts = putWeights.length;
    final int nCalls = callWeights.length;

    _putWeights = new double[nPuts];
    _callWeights = new double[nCalls];
    _putPrices = new double[nPuts];
    _callPrices = new double[nCalls];

    System.arraycopy(putWeights, 0, _putWeights, 0, nPuts);
    _straddleWeight = straddleWeight;
    System.arraycopy(callWeights, 0, _callWeights, 0, nCalls);
    System.arraycopy(putPrices, 0, _putPrices, 0, nPuts);
    _straddlePrice = straddlePrice;
    System.arraycopy(callPrices, 0, _callPrices, 0, nCalls);
    _cash = cash;

    double sum = _straddleWeight * _straddlePrice;
    for (int i = 0; i < nPuts; ++i) {
      sum += _putWeights[i] * _putPrices[i];
    }
    for (int i = 0; i < nCalls; ++i) {
      sum += _callWeights[i] * _callPrices[i];
    }
    _optionTotal = sum;
    _fairValue = _optionTotal + _cash;
  }

  /**
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
  public VolatilitySwapCalculatorResult(final double[] putWeights, final double straddleWeight, final double[] callWeights, final double[] putPrices, final double straddlePrice,
      final double[] callPrices, final double cash, final double optionTotal, final double fairValue) {
    final int nPuts = putWeights.length;
    final int nCalls = callWeights.length;

    _putWeights = new double[nPuts];
    _callWeights = new double[nCalls];
    _putPrices = new double[nPuts];
    _callPrices = new double[nCalls];

    System.arraycopy(putWeights, 0, _putWeights, 0, nPuts);
    _straddleWeight = straddleWeight;
    System.arraycopy(callWeights, 0, _callWeights, 0, nCalls);
    System.arraycopy(putPrices, 0, _putPrices, 0, nPuts);
    _straddlePrice = straddlePrice;
    System.arraycopy(callPrices, 0, _callPrices, 0, nCalls);
    _cash = cash;

    _optionTotal = optionTotal;
    _fairValue = fairValue;
  }

  /**
   * Construct subclass with strikes
   * @param putStrikes The put option strikes
   * @param callStrikes The call option strike
   * @return {@link VolatilitySwapCalculatorResultWithStrikes}
   */
  public VolatilitySwapCalculatorResultWithStrikes withStrikes(final double[] putStrikes, final double[] callStrikes) {
    return new VolatilitySwapCalculatorResultWithStrikes(putStrikes, callStrikes, _putWeights, _straddleWeight, _callWeights, _putPrices, _straddlePrice, _callPrices, _cash, _optionTotal, _fairValue);
  }

  /**
   * Access _fairValue
   * @return fair value
   */
  public double getFairValue() {
    return _fairValue;
  }

  /**
   * Access _putWeights
   * @return put option weights
   */
  public double[] getPutWeights() {
    return _putWeights;
  }

  /**
   * Access _straddleWeight
   * @return straddle weight
   */
  public double getStraddleWeight() {
    return _straddleWeight;
  }

  /**
   * Access _callWeights
   * @return call option weights
   */
  public double[] getCallWeights() {
    return _callWeights;
  }

  /**
   * Access _putPrices
   * @return put option prices
   */
  public double[] getPutPrices() {
    return _putPrices;
  }

  /**
   * Access _straddlePrice
   * @return straddle price
   */
  public double getStraddlePrice() {
    return _straddlePrice;
  }

  /**
   * Access _callPrices
   * @return call option prices
   */
  public double[] getCallPrices() {
    return _callPrices;
  }

  /**
   * Access _cash
   * @return cash amount
   */
  public double getCash() {
    return _cash;
  }

  /**
   * Access _optionTotal
   * @return total option value
   */
  public double getOptionTotal() {
    return _optionTotal;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_callPrices);
    result = prime * result + Arrays.hashCode(_callWeights);
    long temp;
    temp = Double.doubleToLongBits(_cash);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_putPrices);
    result = prime * result + Arrays.hashCode(_putWeights);
    temp = Double.doubleToLongBits(_straddlePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_straddleWeight);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof VolatilitySwapCalculatorResult)) {
      return false;
    }
    VolatilitySwapCalculatorResult other = (VolatilitySwapCalculatorResult) obj;
    if (!Arrays.equals(_callPrices, other._callPrices)) {
      return false;
    }
    if (!Arrays.equals(_callWeights, other._callWeights)) {
      return false;
    }
    if (Double.doubleToLongBits(_cash) != Double.doubleToLongBits(other._cash)) {
      return false;
    }
    if (!Arrays.equals(_putPrices, other._putPrices)) {
      return false;
    }
    if (!Arrays.equals(_putWeights, other._putWeights)) {
      return false;
    }
    if (Double.doubleToLongBits(_straddlePrice) != Double.doubleToLongBits(other._straddlePrice)) {
      return false;
    }
    if (Double.doubleToLongBits(_straddleWeight) != Double.doubleToLongBits(other._straddleWeight)) {
      return false;
    }
    return true;
  }

}
