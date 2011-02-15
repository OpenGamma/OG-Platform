/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFormula;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class FourierPricer {

  private static Logger s_logger = LoggerFactory.getLogger(FourierPricer.class);

  //private final double _alpha;
  private final Integrator1D<Double, Function1D<Double, Double>, Double> _integrator;
  //private final double _upperLimit = 1e5; // TODO set the limit algorithmically
  private static final IntegralLimitCalculator s_limitCal = new IntegralLimitCalculator();

  public FourierPricer() {
    _integrator = new RungeKuttaIntegrator1D();
  }

  public FourierPricer(final double tol, final int minSteps) {
    _integrator = new RungeKuttaIntegrator1D(tol, minSteps);
  }

  public FourierPricer(final Integrator1D<Double, Function1D<Double, Double>, Double> integrator) {
    Validate.notNull(integrator, "null integrator");
    _integrator = integrator;
  }

  double price(final double forward, final double strike, final double discountFactor, final boolean isCall, final CharacteristicExponent ce,
      final double alpha, final double tol) {

    if (alpha >= ce.getLargestAlpha() || alpha <= ce.getSmallestAlpha()) {
      s_logger.warn("The value of alpha is not valid for the Characteristic Exponent and will most likely lead to mispricing. Choose a value between "
          + ce.getSmallestAlpha() + " and " + ce.getLargestAlpha());
    }

    Function1D<Double, Double> func = new EuropeanPriceIntegrand(ce, alpha, forward, strike, false, 0.0);

    Function1D<ComplexNumber, ComplexNumber> psi = new EuropeanCallFT(ce);
    double xMax = s_limitCal.solve(psi, alpha, tol);

    double integral = Math.exp(-alpha * Math.log(strike / forward)) * _integrator.integrate(func, 0.0, xMax) / Math.PI;

    double res = discountFactor * forward * integral;

    if (isCall) {
      if (alpha > 0.0) {
        res = discountFactor * forward * integral;
      } else if (alpha < -1.0) {
        res = discountFactor * (forward * (1 + integral) - strike);
      } else {
        res = discountFactor * forward * (integral + 1);
      }
    } else {
      if (alpha > 0.0) {
        res = discountFactor * (forward * (integral - 1) + strike);
      } else if (alpha < -1.0) {
        res = discountFactor * forward * integral;
      } else {
        res = discountFactor * (forward * integral + strike);
      }
    }
    return res;

  }

  double price(final double forward, final double strike, final double discountFactor, final boolean isCall, final CharacteristicExponent ce, final double alpha,
      final double tol,
      final double blackVol) {

    Function1D<Double, Double> func = new EuropeanPriceIntegrand(ce, alpha, forward, strike, true, blackVol);

    Function1D<ComplexNumber, ComplexNumber> psi = new EuropeanCallFT(ce);
    double xMax = s_limitCal.solve(psi, alpha, tol);

    double integral = Math.exp(-alpha * Math.log(strike / forward)) * _integrator.integrate(func, 0.0, xMax) / Math.PI;

    double black = BlackFormula.optionPrice(forward, strike, discountFactor, blackVol, ce.getTime(), isCall);
    double diff = discountFactor * forward * integral;

    return diff + black;
  }
}
