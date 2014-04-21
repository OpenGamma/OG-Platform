/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import java.util.Arrays;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AmericanVanillaOptionFunctionProvider extends OptionFunctionProvider1D {
  private final Calculator _calc;

  /**
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   */
  public AmericanVanillaOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall) {
    super(strike, timeToExpiry, steps, isCall);
    _calc = new NormalCalculator();
  }

  /**
   * American vanilla option function with acceleration technique in binomial model
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param dividend Dividend
   */
  public AmericanVanillaOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double volatility, final double interestRate,
      final double dividend) {
    super(strike, timeToExpiry, steps, isCall);
    _calc = new AccelerationCalculator(volatility, interestRate, dividend);
  }

  /**
   * American vanilla option function with truncation technique in binomial model
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @param stdDev Truncation parameter
   */
  public AmericanVanillaOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double volatility, final double interestRate,
      final double dividend, final double stdDev) {
    super(strike, timeToExpiry, steps, isCall);
    _calc = new TruncationCalculator(volatility, interestRate, dividend, stdDev);
  }

  /**
   * American vanilla option function with truncation and acceleration techniques in binomial model
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @param stdDev Truncation parameter
   * @param acc True if acceleration is used
   */
  public AmericanVanillaOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double volatility, final double interestRate,
      final double dividend, final double stdDev, final boolean acc) {
    super(strike, timeToExpiry, steps, isCall);
    _calc = acc ? new AcceleratedTruncationCalculator(volatility, interestRate, dividend, stdDev) : new TruncationCalculator(volatility, interestRate, dividend, stdDev);
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
    return _calc.payoffsAtExpiry(assetPrice, downFactor, upOverDown);
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double sumCashDiv,
      final double downFactor, final double upOverDown, final int steps) {
    return _calc.nextValues(discount, upProbability, downProbability, values, sumCashDiv, baseAssetPrice, downFactor, upOverDown, steps);
  }

  @Override
  public double[] getPayoffAtExpiryTrinomial(double assetPrice, final double downFactor, double middleOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nNodes = 2 * getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nNodes];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nNodes; ++i) {
      values[i] = Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= middleOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double middleProbability, final double downProbability, final double[] values,
      final double baseAssetPrice, final double sumCashDiv, final double downFactor, final double middleOverDown, final int steps) {
    final double strike = getStrike();
    final double sign = getSign();
    final int nNodes = 2 * steps + 1;

    final double[] res = new double[nNodes];
    double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
    for (int j = 0; j < nNodes; ++j) {
      res[j] = Math.max(discount * (upProbability * values[j + 2] + middleProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice + sumCashDiv - strike));
      assetPrice *= middleOverDown;
    }
    return res;
  }

  /**
   * 
   * 
   * 
   * Private class defines calculation method
   * 
   * 
   * 
   */
  private abstract class Calculator {
    abstract double[] payoffsAtExpiry(final double assetPrice, final double downFactor, final double upOverDown);

    public double[] nextValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double sumCashDiv, final double baseAssetPrice,
        final double downFactor, final double upOverDown, final int steps) {
      final double strike = getStrike();
      final double sign = getSign();
      final int nStepsP = steps + 1;
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      final double[] res = new double[nStepsP];
      for (int j = 0; j < nStepsP; ++j) {
        res[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice + sumCashDiv - strike));
        assetPrice *= upOverDown;
      }
      return res;
    }
  }

  private class NormalCalculator extends Calculator {

    @Override
    public double[] payoffsAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
      final int nSteps = getNumberOfSteps();
      final int nStepsP = nSteps + 1;
      final double strike = getStrike();
      final double sign = getSign();

      double assetPriceTerminal = assetPrice * Math.pow(downFactor, nSteps);
      final double[] values = new double[nStepsP];
      for (int i = 0; i < nStepsP; ++i) {
        values[i] = Math.max(sign * (assetPriceTerminal - strike), 0.);
        assetPriceTerminal *= upOverDown;
      }
      return values;
    }

  }

  private class AccelerationCalculator extends Calculator {
    private final double _volatility;
    private final double _interestRate;
    private final double _dividend;

    public AccelerationCalculator(final double volatility, final double interestRate, final double dividend) {
      ArgumentChecker.isTrue(volatility > 0., "volatility should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(volatility), "volatility should be finite");
      _volatility = volatility;
      _interestRate = interestRate;
      _dividend = dividend;
    }

    @Override
    public double[] payoffsAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
      final double[] values = new double[getNumberOfSteps() + 1];
      Arrays.fill(values, 0.);
      return values;
    }

    @Override
    public double[] nextValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double sumCashDiv, final double baseAssetPrice,
        final double downFactor, final double upOverDown, final int steps) {

      if (getNumberOfSteps() - 1 == steps) {
        final double strike = getStrike();
        final double sign = getSign();
        final int nStepsP = steps + 1;
        final double[] res = new double[nStepsP];

        double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
        final double volatility = _volatility;
        final double dt = getTimeToExpiry() / getNumberOfSteps();
        final double interestRate = _interestRate;
        final double dividend = _dividend;
        final boolean isCall = sign == 1.;

        for (int j = 0; j < nStepsP; ++j) {
          res[j] = BlackScholesFormulaRepository.price(assetPrice, strike, dt, volatility, interestRate, interestRate - dividend, isCall);
          assetPrice *= upOverDown;
        }
        return res;
      }

      return super.nextValues(discount, upProbability, downProbability, values, sumCashDiv, baseAssetPrice, downFactor, upOverDown, steps);
    }
  }

  private class TruncationCalculator extends Calculator {
    private final double _volatility;
    private final double _interestRate;
    private final double _dividend;
    private final double _stdDiv;

    public TruncationCalculator(final double volatility, final double interestRate, final double dividend, final double stdDiv) {
      ArgumentChecker.isTrue(volatility > 0., "volatility should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(volatility), "volatility should be finite");
      ArgumentChecker.isTrue(stdDiv > 0., "stdDiv should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(stdDiv), "stdDiv should be finite");
      _volatility = volatility;
      _interestRate = interestRate;
      _dividend = dividend;
      _stdDiv = stdDiv;
    }

    @Override
    public double[] payoffsAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
      final int nSteps = getNumberOfSteps();
      final int nStepsP = nSteps + 1;
      final double strike = getStrike();
      final double sign = getSign();

      double assetPriceLowest = assetPrice * Math.pow(downFactor, nSteps);
      final int ref = (int) (Math.log(strike / assetPriceLowest) / Math.log(upOverDown));
      final double[] values = new double[nStepsP];

      Arrays.fill(values, 0.);
      double tmpValue = assetPriceLowest * Math.pow(upOverDown, ref - 3);
      for (int i = 0; i < 6; ++i) {
        values[ref - 3 + i] = Math.max(sign * (tmpValue - strike), 0.);
        tmpValue *= upOverDown;
      }
      return values;
    }

    @Override
    public double[] nextValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double sumCashDiv, final double baseAssetPrice,
        final double downFactor, final double upOverDown, final int steps) {

      final double strike = getStrike();
      final double sign = getSign();
      final int nStepsP = steps + 1;
      final double assetPriceLowest = baseAssetPrice * Math.pow(downFactor, steps);
      final double[] res = new double[nStepsP];
      Arrays.fill(res, 0.);

      final double time = getTimeToExpiry() - steps * getTimeToExpiry() / getNumberOfSteps();
      final double part1 = Math.log(strike / assetPriceLowest) - (_interestRate - _dividend) * time;
      final double part2 = _stdDiv * _volatility * Math.sqrt(time);
      int jmax = (int) ((part1 + part2) / Math.log(upOverDown));
      int jmin = (int) ((part1 - part2) / Math.log(upOverDown)) + 1;

      jmax = jmax > steps ? steps : jmax;
      jmin = jmin < 0 ? 0 : jmin;
      final boolean isCall = sign == 1.;
      double tmpValue = assetPriceLowest * Math.pow(upOverDown, jmin - 3);
      if (jmin > 2) {
        res[jmin - 3] = BlackScholesFormulaRepository.price(tmpValue, strike, time, _volatility, _interestRate, _interestRate - _dividend, isCall);
      }
      tmpValue *= upOverDown;
      if (jmin > 1) {
        res[jmin - 2] = BlackScholesFormulaRepository.price(tmpValue, strike, time, _volatility, _interestRate, _interestRate - _dividend, isCall);
      }
      tmpValue *= upOverDown;
      if (jmin > 0) {
        res[jmin - 1] = BlackScholesFormulaRepository.price(tmpValue, strike, time, _volatility, _interestRate, _interestRate - _dividend, isCall);
      }
      tmpValue *= upOverDown;
      for (int j = jmin; j < jmax + 1; ++j) {
        res[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (tmpValue + sumCashDiv - strike));
        tmpValue *= upOverDown;
      }
      if (jmax < steps) {
        res[jmax + 1] = BlackScholesFormulaRepository.price(tmpValue, strike, time, _volatility, _interestRate, _interestRate - _dividend, isCall);
      }
      tmpValue *= upOverDown;
      if (jmax < steps - 1) {
        res[jmax + 2] = BlackScholesFormulaRepository.price(tmpValue, strike, time, _volatility, _interestRate, _interestRate - _dividend, isCall);
      }
      return res;
    }

  }

  private class AcceleratedTruncationCalculator extends Calculator {
    private final double _volatility;
    private final double _interestRate;
    private final double _dividend;
    private final double _stdDiv;
    private final double _dt;
    private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);

    public AcceleratedTruncationCalculator(final double volatility, final double interestRate, final double dividend, final double stdDiv) {
      ArgumentChecker.isTrue(volatility > 0., "volatility should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(volatility), "volatility should be finite");
      ArgumentChecker.isTrue(stdDiv > 0., "stdDiv should be positive");
      ArgumentChecker.isTrue(Doubles.isFinite(stdDiv), "stdDiv should be finite");
      _volatility = volatility;
      _interestRate = interestRate;
      _dividend = dividend;
      _stdDiv = stdDiv;
      _dt = getTimeToExpiry() / getNumberOfSteps();
    }

    @Override
    public double[] payoffsAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
      final int nStepsP = getNumberOfSteps() + 1;
      final double[] values = new double[nStepsP];
      Arrays.fill(values, 0.);
      return values;
    }

    @Override
    public double[] nextValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double sumCashDiv, final double baseAssetPrice,
        final double downFactor, final double upOverDown, final int steps) {

      final double strike = getStrike();
      final double sign = getSign();
      final int nStepsP = steps + 1;
      final double assetPriceLowest = baseAssetPrice * Math.pow(downFactor, steps);
      final double[] res = new double[nStepsP];

      final double time = getTimeToExpiry() - steps * _dt;
      final double part1 = Math.log(strike / assetPriceLowest) - (_interestRate - _dividend) * time;
      final double part2 = _stdDiv * _volatility * Math.sqrt(time);
      final double logFactorInv = 1. / Math.log(upOverDown);
      int jmax = (int) ((part1 + part2) * logFactorInv);
      int jmin = (int) ((part1 - part2) * logFactorInv) + 1;

      jmax = Math.min(jmax, steps);
      jmin = Math.max(jmin, 0);
      if (jmax == steps && jmin == 0) {
        double assetPrice = assetPriceLowest;
        for (int j = 0; j < nStepsP; ++j) {
          res[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice + sumCashDiv - strike));
          assetPrice *= upOverDown;
        }
        return res;
      }

      Arrays.fill(res, 0.);
      jmax = jmax < steps - 1 ? jmax + 2 : (jmax < steps ? jmax - 1 : jmax);
      jmin = jmin > 1 ? jmin - 2 : (jmin > 0 ? jmin - 1 : jmin);
      double tmpValue = assetPriceLowest * Math.pow(upOverDown, jmin);
      for (int j = jmin; j < jmax + 1; ++j) {
        if (getNumberOfSteps() - 1 == steps) {
          res[j] = blackScholesPrice(tmpValue, strike, _dt, _volatility, _interestRate, _dividend, sign);
        } else {
          res[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (tmpValue + sumCashDiv - strike));
        }
        tmpValue *= upOverDown;
      }

      return res;
    }

    private double blackScholesPrice(final double spot, final double strike, final double time, final double vol, final double interestRate, final double dividend, final double sign) {
      final double factor1 = Math.exp(-dividend * time);
      final double factor2 = Math.exp(-interestRate * time);
      final double sigRootT = vol * Math.sqrt(time);
      final double part = (Math.log(spot / strike) + (interestRate - dividend) * time) / sigRootT;
      final double d1 = part + 0.5 * sigRootT;
      final double d2 = part - 0.5 * sigRootT;

      return sign * (spot * factor1 * _normal.getCDF(sign * d1) - factor2 * strike * _normal.getCDF(sign * d2));
    }
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AmericanVanillaOptionFunctionProvider)) {
      return false;
    }
    return super.equals(obj);
  }

}
