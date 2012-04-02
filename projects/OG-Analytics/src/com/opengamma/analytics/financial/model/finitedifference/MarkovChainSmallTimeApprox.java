/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;

/**
 * 
 */
public class MarkovChainSmallTimeApprox {

  private final double _vol1;
  private final double _vol2;
  private final double _lambda12;
  private final double _lambda21;
  private final double _probState1;
  private final Integrator1D<Double, Double> _integrator = new RungeKuttaIntegrator1D();
  private final BlackPriceFunction _black = new BlackPriceFunction();

  public MarkovChainSmallTimeApprox(final double vol1, final double vol2, final double lambda12, final double lambda21, final double probState1) {
    Validate.isTrue(vol1 >= 0);
    Validate.isTrue(vol2 >= 0);
    Validate.isTrue(lambda12 >= 0);
    Validate.isTrue(lambda21 >= 0);
    Validate.isTrue(probState1 >= 0 && probState1 <= 1.0);
    _vol1 = vol1;
    _vol2 = vol2;
    _lambda12 = lambda12;
    _lambda21 = lambda21;
    _probState1 = probState1;
  }

  public double price(final double forward, final double df, final double strike, final double expiry) {

    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
    final Function1D<BlackFunctionData, Double> priceFunc = _black.getPriceFunction(option);

    final Function1D<Double, Double> fun1 = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double tau) {
        double sigma = _vol1 * _vol1 * tau / expiry + _vol2 * _vol2 * (1 - tau / expiry);
        sigma = Math.sqrt(sigma);
        final BlackFunctionData data = new BlackFunctionData(forward, df, sigma);
        return _lambda12 * priceFunc.evaluate(data) * Math.exp(-_lambda12 * tau);
      }
    };

    final Function1D<Double, Double> fun2 = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double tau) {
        double sigma = _vol1 * _vol1 * (1 - tau / expiry) + _vol2 * _vol2 * tau / expiry;
        sigma = Math.sqrt(sigma);
        final BlackFunctionData data = new BlackFunctionData(forward, df, sigma);
        return _lambda21 * priceFunc.evaluate(data) * Math.exp(-_lambda21 * tau);
      }
    };

    double p1 = 0;
    double p2 = 0;

    if (_probState1 > 0.0) {
      final BlackFunctionData data = new BlackFunctionData(forward, df, _vol1);
      p1 = _integrator.integrate(fun1, 0.0, expiry) + priceFunc.evaluate(data) * Math.exp(-_lambda12 * expiry);
    }

    if (_probState1 < 1.0) {
      final BlackFunctionData data = new BlackFunctionData(forward, df, _vol2);
      p2 = _integrator.integrate(fun2, 0.0, expiry) + priceFunc.evaluate(data) * Math.exp(-_lambda21 * expiry);
    }
    return _probState1 * p1 + (1 - _probState1) * p2;
  }
}
