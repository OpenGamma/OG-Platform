/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFormula;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;

/**
 * 
 */
public class FourierPricer {

  private final double _alpha;
  private final Integrator1D<Double, Function1D<Double, Double>, Double> _integrator;
  private final double _upperLimit = 1e5; // TODO set the limit algorithmically

  public FourierPricer(final double alpha) {
    _alpha = alpha;
    _integrator = new RungeKuttaIntegrator1D(1e-13, 30);
  }

  double price(final double forward, final double strike, final double maturity, final double discountFactor, final boolean isCall, final CharacteristicExponent ce) {
    Function1D<Double, Double> func = new EuropeanPriceIntegrand(ce, _alpha, forward, strike, maturity, false, 0.0);

    double integral = Math.exp(-_alpha * Math.log(strike / forward)) * _integrator.integrate(func, 0.0, _upperLimit) / Math.PI;

    double res = discountFactor * forward * integral;

    if (isCall) {
      if (_alpha > 0.0) {
        res = discountFactor * forward * integral;
      } else if (_alpha < -1.0) {
        res = discountFactor * (forward * (1 + integral) - strike);
      } else {
        res = discountFactor * forward * (integral + 1);
      }
    } else {
      if (_alpha > 0.0) {
        res = discountFactor * (forward * (integral - 1) + strike);
      } else if (_alpha < -1.0) {
        res = discountFactor * forward * integral;
      } else {
        res = discountFactor * (forward * integral + strike);
      }
    }
    return res;

  }

  double price(final double forward, final double strike, final double maturity, final double discountFactor, final boolean isCall, final CharacteristicExponent ce, final double blackVol) {

    Function1D<Double, Double> func = new EuropeanPriceIntegrand(ce, _alpha, forward, strike, maturity, true, blackVol);

    double integral = Math.exp(-_alpha * Math.log(strike / forward)) * _integrator.integrate(func, 0.0, _upperLimit) / Math.PI;

    double black = BlackFormula.optionPrice(forward, strike, discountFactor, blackVol, maturity, isCall);
    double diff = discountFactor * forward * integral;

    return diff + black;
  }
}
