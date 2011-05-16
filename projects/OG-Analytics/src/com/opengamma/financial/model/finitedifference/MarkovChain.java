/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.Random;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class MarkovChain {

  private final double _vol1;
  private final double _vol2;
  private final double _lambda12;
  private final double _lambda21;

  private final Random _rand = new Random();

  public MarkovChain(final double vol1, final double vol2, final double lambda12, final double lambda21) {
    Validate.isTrue(vol1 >= 0);
    Validate.isTrue(vol2 >= 0);
    Validate.isTrue(lambda12 >= 0);
    Validate.isTrue(lambda21 >= 0);
    _vol1 = vol1;
    _vol2 = vol2;
    _lambda12 = lambda12;
    _lambda21 = lambda21;
  }

  public double price(final double forward, final double df, final double strike, final double timeToExiry, double[] sigmas) {
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, timeToExiry, true);
    BlackPriceFunction func = new BlackPriceFunction();
    Function1D<BlackFunctionData, Double> priceFunc = func.getPriceFunction(option);
    double sum = 0;
    for (double sigma : sigmas) {
      BlackFunctionData data = new BlackFunctionData(forward, df, sigma);
      sum += priceFunc.evaluate(data);
    }
    return sum / sigmas.length;
  }

  public double[] simulate(double timeToExpiry, double probState1, int n) {
    Validate.isTrue(probState1 >= 0 && probState1 <= 1.0);

    double vol, lambda, tau;
    double[] vols = new double[n];

    for (int i = 0; i < n; i++) {
      boolean state1 = probState1 > _rand.nextDouble();
      double t = 0;
      double var = 0.0;
      while (t < timeToExpiry) {
        if (state1) {
          vol = _vol1;
          lambda = _lambda12;
        } else {
          vol = _vol2;
          lambda = _lambda21;
        }
        tau = -Math.log(_rand.nextDouble()) / lambda;
        if (t + tau < timeToExpiry) {
          var += tau * vol * vol;
          state1 = !state1;
        } else {
          var += (timeToExpiry - t) * vol * vol;
        }
        t += tau;
      }
      vols[i] = Math.sqrt(var / timeToExpiry);
    }
    return vols;
  }

}
