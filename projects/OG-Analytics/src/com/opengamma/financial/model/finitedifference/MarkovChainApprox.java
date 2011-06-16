/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.FunctionUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;

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
  private final Integrator1D<Double, Double> _integrator = new RungeKuttaIntegrator1D();
  private final BlackPriceFunction _black = new BlackPriceFunction();
  double[] _weights;
  double[] _vols;

  public MarkovChainApprox(final double vol1, final double vol2, final double lambda12, final double lambda21, final double probState1,
      final double expiry) {
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

    double wMin = _probState1 * Math.exp(-_lambda12 * expiry); //prob of always being in state 1
    double wMax = (1 - _probState1) * Math.exp(-_lambda21 * expiry); //prob of always being in state 1

    double mu = getM1(expiry);
    double modM1 = getModMoment(1, wMin, wMax, mu, mu);
    double modM2 = getModMoment(2, wMin, wMax, mu, getM2(expiry));
    double modM3 = getModMoment(3, wMin, wMax, mu, getM3(expiry));
    double skewOverVar = modM3 / modM2;
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

    _weights = new double[] {wMin, wMax, w * (1 - wMin - wMax), (1 - w) * (1 - wMin - wMax) };
    _vols = new double[] {_vol1, _vol2, Math.sqrt(modM1 - delta1), Math.sqrt(modM1 + delta2) };
  }

  public double price(final double forward, final double df, final double strike) {

    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _t, true);
    BlackPriceFunction func = new BlackPriceFunction();
    Function1D<BlackFunctionData, Double> priceFunc = func.getPriceFunction(option);
    double sum = 0;

    for (int i = 0; i < _weights.length; i++) {
      BlackFunctionData data = new BlackFunctionData(forward, df, _vols[i]);
      sum += _weights[i] * priceFunc.evaluate(data);
    }

    return sum;
  }

  public double priceCEV(final double forward, final double df, final double strike, final double beta) {

    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _t, true);
    CEVPriceFunction func = new CEVPriceFunction();
    Function1D<CEVFunctionData, Double> priceFunc = func.getPriceFunction(option);
    double sum = 0;

    for (int i = 0; i < _weights.length; i++) {
      CEVFunctionData data = new CEVFunctionData(forward, df, _vols[i], beta);
      sum += _weights[i] * priceFunc.evaluate(data);
    }

    return sum;
  }

  private double getModMoment(int n, double a, double b, double mu, double moment) {
    Validate.isTrue(n > 0);
    double denom = 1 - a - b;
    if (n == 1) {
      return (moment - a * _nu1 - b * _nu2) / denom;
    } else {
      return (moment - a * Math.pow(_nu1 - mu, n) - b * Math.pow(_nu2 - mu, n)) / denom;
    }
  }

  public void debug(final double expiry) {
    double m1 = getM1(expiry);
    double m2 = getM2(expiry);
    double m3 = getM3(expiry);

    System.out.println("cal m1: " + m1 + " m2: " + m2 + " m3: " + m3);
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
    double i1 = getI1(t);
    double i2 = getI2(t);
    double temp = i2 - i1 * i1;

    return FunctionUtils.square(_vol1 * _vol1 - _vol2 * _vol2) * temp;
  }

  private double getM3(final double t) {
    double i1 = getI1(t);
    double i2 = getI2(t);
    double i3 = getI3(t);
    double temp = i3 - 3 * i1 * i2 + 2 * FunctionUtils.cube(i1);
    return FunctionUtils.cube(_vol1 * _vol1 - _vol2 * _vol2) * temp;
  }

  private double getI1(final double t) {
    double lt = _lambda * t;
    if (lt < 1e-7) {
      return _probState1;
    }

    return _theta + (_probState1 - _theta) * (1 - Math.exp(-_lambda * t)) / lt;
  }

  private double getI2(final double t) {
    double t2 = t * t;

    double lt = _lambda * t;
    if (lt < 1e-7) {
      return _probState1;
    }

    double a = _theta;
    double a2 = a * a;
    double b = _lambda;
    double b2 = b * b;
    double p = _probState1;

    double temp1 = a2 * b2 + (2 * a * b * p - 4 * a2 * b + 2 * a * b) / t + (-4 * a * p + 2 * p + 6 * a2 - 4 * a) / t2;
    double temp2 = (2 * a * b * p - 2 * b * p - 2 * a2 * b + 2 * a * b) / t + (4 * a * p - 2 * p - 6 * a2 + 4 * a) / t2;
    return (temp1 + temp2 * Math.exp(-b * t)) / b2;

    //    double a = _theta;
    //    double b = _theta * (_probState1 - _theta);
    //    double c = (_probState1 - _theta) * (1 - _theta);
    //    double d = _theta * (1 - _theta);
    //    return (b + d) * t / _lambda + a * t * t / 2 + (c - b - d) / _lambda / _lambda + Math.exp(-_lambda * t) * (-c * t * _lambda - c + b + d) / _lambda / _lambda;
  }

  private double getI3(final double t) {
    double t2 = t * t;
    double t3 = t2 * t;

    double lt = _lambda * t;
    if (lt < 1e-7) {
      return _probState1;
    }

    double a = _theta;
    double a2 = a * a;
    double a3 = a2 * a;
    double b = _lambda;
    double b2 = b * b;
    double b3 = b2 * b;
    double p = _probState1;

    double temp1 = a3 * b3 + (3 * a2 * b2 * p - 9 * a3 * b2 + 6 * a2 * b2) / t + (-18 * a2 * b * p + 12 * a * b * p
        + 36 * a3 * b - 36 * a2 * b + 6 * a * b) / t2 + (36 * a2 * p - 36 * a * p + 6 * p - 60 * a3 + 72 * a2 - 18 * a) / t3;
    double temp2 = (-3 * a2 * b2 * p + 6 * a * b2 * p - 3 * b2 * p + 3 * a3 * b2 - 6 * a2 * b2 + 3 * a * b2) / t
        + (-18 * a2 * b * p + 24 * a * b * p - 6 * b * p + 24 * a3 * b - 36 * a2 * b + 12 * a * b) / t2 +
        (-36 * a2 * p + 36 * a * p - 6 * p + 60 * a3 - 72 * a2 + 18 * a) / t3;

    return (temp1 + temp2 * Math.exp(-b * t)) / b3;
  }
}
