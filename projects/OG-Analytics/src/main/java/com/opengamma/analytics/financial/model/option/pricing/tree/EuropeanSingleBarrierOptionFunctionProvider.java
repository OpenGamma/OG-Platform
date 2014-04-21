/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * European type of single barrier option
 */
public class EuropeanSingleBarrierOptionFunctionProvider extends BarrierOptionFunctionProvider {

  private final Calculator _calc;

  /**
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param barrier Barrier price
   * @param typeName {@link com.opengamma.analytics.financial.model.option.pricing.tree.BarrierOptionFunctionProvider.BarrierTypes}, DownAndOut or UpAndOut
   */
  public EuropeanSingleBarrierOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double barrier, final BarrierTypes typeName) {
    super(strike, timeToExpiry, steps, isCall, barrier, typeName);
    _calc = new NormalCalculator();
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nStepsP = nSteps + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = getChecker().checkOut(priceTmp) ? 0. : Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double sumCashDiv,
      final double downFactor, final double upOverDown, final int steps) {
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = getChecker().checkOut(assetPrice + sumCashDiv) ? 0. : discount * (upProbability * values[j + 1] + downProbability * values[j]);
      assetPrice *= upOverDown;
    }
    return res;
  }

  @Override
  public double[] getPayoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown) {
    return _calc.payoffAtExpiryTrinomial(assetPrice, downFactor, middleOverDown);
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double middleProbability, final double downProbability, final double[] values,
      final double baseAssetPrice, final double sumCashDiv, final double downFactor, final double middleOverDown, final int steps) {
    return _calc.nextOptionValues(discount, upProbability, middleProbability, downProbability, values, baseAssetPrice, sumCashDiv, downFactor, middleOverDown, steps);
  }

  /*
   * 
   * 
   * 
   * 
   * Private class defines calculation method
   * 
   * 
   * 
   * 
   */
  private abstract class Calculator {
    abstract double[] payoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown);

    abstract double[] nextOptionValues(final double discount, final double upProbability, final double middleProbability, final double downProbability, final double[] values,
        final double baseAssetPrice, final double sumCashDiv, final double downFactor, final double middleOverDown, final int steps);
  }

  private class NormalCalculator extends Calculator {

    @Override
    public double[] payoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown) {
      final double strike = getStrike();
      final int nSteps = getNumberOfSteps();
      final int nNodes = 2 * getNumberOfSteps() + 1;
      final double sign = getSign();

      final double[] values = new double[nNodes];
      double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
      for (int i = 0; i < nNodes; ++i) {
        values[i] = getChecker().checkOut(priceTmp) ? 0. : Math.max(sign * (priceTmp - strike), 0.);
        priceTmp *= middleOverDown;
      }
      return values;
    }

    @Override
    public double[] nextOptionValues(final double discount, final double upProbability, final double middleProbability, final double downProbability, final double[] values,
        final double baseAssetPrice, final double sumCashDiv, final double downFactor, final double middleOverDown, final int steps) {
      final int nNodes = 2 * steps + 1;

      final double[] res = new double[nNodes];
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < nNodes; ++j) {
        res[j] = getChecker().checkOut(assetPrice + sumCashDiv) ? 0. : discount * (upProbability * values[j + 2] + middleProbability * values[j + 1] + downProbability * values[j]);
        assetPrice *= middleOverDown;
      }
      return res;
    }

  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof EuropeanSingleBarrierOptionFunctionProvider)) {
      return false;
    }
    return super.equals(obj);
  }
}
