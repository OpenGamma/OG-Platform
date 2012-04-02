/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
public class MarkovChainApprox {

  private final double _vol1;
  private final double _vol2;
  private final double _nu1;
  private final double _nu2;
  private final double _lambda12;
  private final double _lambda21;
  private final double _lambda;
  private final double _theta;
  private final double _probState1;
  private final double _t;
  //private final Integrator1D<Double, Double> _integrator = new RungeKuttaIntegrator1D();
  //private final BlackPriceFunction _black = new BlackPriceFunction();
  private final double[] _weights;
  private final double[] _vols;

  public MarkovChainApprox(final double vol1, final double vol2, final double lambda12, final double lambda21, final double probState1, final double expiry) {
    Validate.isTrue(vol1 >= 0);
    Validate.isTrue(vol2 >= 0);
    Validate.isTrue(lambda12 >= 0);
    Validate.isTrue(lambda21 >= 0);
    Validate.isTrue(probState1 >= 0 && probState1 <= 1.0);
    _vol1 = vol1;
    _vol2 = vol2;
    _nu1 = vol1 * vol1;
    _nu2 = vol2 * vol2;
    _lambda12 = lambda12;
    _lambda21 = lambda21;
    _lambda = lambda12 + lambda21;
    _probState1 = probState1;
    _theta = lambda21 / _lambda;
    _t = expiry;

    final double wMin = _probState1 * Math.exp(-_lambda12 * expiry); //prob of always being in state 1
    final double wMax = (1 - _probState1) * Math.exp(-_lambda21 * expiry); //prob of always being in state 1

    final double mu = getM1(expiry);
    final double modM1 = getModMoment(1, wMin, wMax, mu, mu);
    final double modM2 = getModMoment(2, wMin, wMax, mu, getM2(expiry));
    final double modM3 = getModMoment(3, wMin, wMax, mu, getM3(expiry));
    final double skewOverVar = modM3 / modM2;
    double delta2;
    double w;
    double delta1 = (-skewOverVar + Math.sqrt(FunctionUtils.square(skewOverVar) + 4 * modM2)) / 2.0;
    if (delta1 > modM1) {
      delta1 = (modM1 - _nu1) / 2;
      delta2 = modM2 / delta1;
    } else {
      delta2 = delta1 + skewOverVar;
    }
    w = delta2 / (delta1 + delta2);
    Validate.isTrue(w >= 0.0 && w <= 1.0, "weight not physical");

    _weights = new double[] {wMin, wMax, w * (1 - wMin - wMax), (1 - w) * (1 - wMin - wMax)};
    _vols = new double[] {_vol1, _vol2, Math.sqrt(modM1 - delta1), Math.sqrt(modM1 + delta2)};
  }

  public double price(final double forward, final double df, final double strike) {

    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _t, true);
    final BlackPriceFunction func = new BlackPriceFunction();
    final Function1D<BlackFunctionData, Double> priceFunc = func.getPriceFunction(option);
    double sum = 0;

    for (int i = 0; i < _weights.length; i++) {
      final BlackFunctionData data = new BlackFunctionData(forward, df, _vols[i]);
      sum += _weights[i] * priceFunc.evaluate(data);
    }

    return sum;
  }

  public double priceCEV(final double forward, final double df, final double strike, final double beta) {

    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _t, true);
    final CEVPriceFunction func = new CEVPriceFunction();
    final Function1D<CEVFunctionData, Double> priceFunc = func.getPriceFunction(option);
    double sum = 0;

    for (int i = 0; i < _weights.length; i++) {
      final CEVFunctionData data = new CEVFunctionData(forward, df, _vols[i], beta);
      sum += _weights[i] * priceFunc.evaluate(data);
    }

    return sum;
  }

  private double getModMoment(final int n, final double a, final double b, final double mu, final double moment) {
    Validate.isTrue(n > 0);
    final double denom = 1 - a - b;
    if (n == 1) {
      return (moment - a * _nu1 - b * _nu2) / denom;
    }
    return (moment - a * Math.pow(_nu1 - mu, n) - b * Math.pow(_nu2 - mu, n)) / denom;
  }

  public double[] getMoments(final double expiry) {
    final double m1 = getM1(expiry);
    final double m2 = getM2(expiry);
    final double m3 = getM3(expiry);

    //System.out.println("cal m1: " + m1 + " m2: " + m2 + " m3: " + m3);
    return new double[] {m1, m2, m3};
  }

  /**
   * The mean variance (sigma^2*t) of the markov chain
   * @param t
   * @return
   */
  private double getM1(final double t) {
    return _vol2 * _vol2 - (_vol2 * _vol2 - _vol1 * _vol1) * getI1(t);
  }

  private double getM2(final double t) {
    final double i1 = getI1(t);
    final double i2 = getI2(t);
    final double temp = i2 - i1 * i1;

    return FunctionUtils.square(_vol1 * _vol1 - _vol2 * _vol2) * temp;
  }

  private double getM3(final double t) {
    final double i1 = getI1(t);
    final double i2 = getI2(t);
    final double i3 = getI3(t);
    final double temp = i3 - 3 * i1 * i2 + 2 * FunctionUtils.cube(i1);
    return FunctionUtils.cube(_vol1 * _vol1 - _vol2 * _vol2) * temp;
  }

  private double getI1(final double t) {
    final double lt = _lambda * t;
    if (lt < 1e-7) {
      return _probState1;
    }

    return _theta + (_probState1 - _theta) * (1 - Math.exp(-_lambda * t)) / lt;
  }

  private double getI2(final double t) {
    final double t2 = t * t;

    final double lt = _lambda * t;
    if (lt < 1e-7) {
      return _probState1;
    }

    final double a = _theta;
    final double a2 = a * a;
    final double b = _lambda;
    final double b2 = b * b;
    final double p = _probState1;

    final double temp1 = a2 * b2 + (2 * a * b * p - 4 * a2 * b + 2 * a * b) / t + (-4 * a * p + 2 * p + 6 * a2 - 4 * a) / t2;
    final double temp2 = (2 * a * b * p - 2 * b * p - 2 * a2 * b + 2 * a * b) / t + (4 * a * p - 2 * p - 6 * a2 + 4 * a) / t2;
    return (temp1 + temp2 * Math.exp(-b * t)) / b2;

    //    double a = _theta;
    //    double b = _theta * (_probState1 - _theta);
    //    double c = (_probState1 - _theta) * (1 - _theta);
    //    double d = _theta * (1 - _theta);
    //    return (b + d) * t / _lambda + a * t * t / 2 + (c - b - d) / _lambda / _lambda + Math.exp(-_lambda * t) * (-c * t * _lambda - c + b + d) / _lambda / _lambda;
  }

  private double getI3(final double t) {
    final double t2 = t * t;
    final double t3 = t2 * t;

    final double lt = _lambda * t;
    if (lt < 1e-7) {
      return _probState1;
    }

    final double a = _theta;
    final double a2 = a * a;
    final double a3 = a2 * a;
    final double b = _lambda;
    final double b2 = b * b;
    final double b3 = b2 * b;
    final double p = _probState1;

    final double temp1 = a3 * b3 + (3 * a2 * b2 * p - 9 * a3 * b2 + 6 * a2 * b2) / t + (-18 * a2 * b * p + 12 * a * b * p + 36 * a3 * b - 36 * a2 * b + 6 * a * b) / t2
        + (36 * a2 * p - 36 * a * p + 6 * p - 60 * a3 + 72 * a2 - 18 * a) / t3;
    final double temp2 = (-3 * a2 * b2 * p + 6 * a * b2 * p - 3 * b2 * p + 3 * a3 * b2 - 6 * a2 * b2 + 3 * a * b2) / t
        + (-18 * a2 * b * p + 24 * a * b * p - 6 * b * p + 24 * a3 * b - 36 * a2 * b + 12 * a * b) / t2 + (-36 * a2 * p + 36 * a * p - 6 * p + 60 * a3 - 72 * a2 + 18 * a) / t3;

    return (temp1 + temp2 * Math.exp(-b * t)) / b3;
  }
}
