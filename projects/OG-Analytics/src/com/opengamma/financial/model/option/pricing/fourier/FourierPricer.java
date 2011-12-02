/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class FourierPricer {
  private static final IntegralLimitCalculator LIMIT_CALCULATOR = new IntegralLimitCalculator();
  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private final Integrator1D<Double, Double> _integrator;

  public FourierPricer() {
    this(new RungeKuttaIntegrator1D());
  }

  public FourierPricer(final Integrator1D<Double, Double> integrator) {
    Validate.notNull(integrator, "null integrator");
    _integrator = integrator;
  }

  public double price(final BlackFunctionData data, final EuropeanVanillaOption option, final MartingaleCharacteristicExponent ce, final double alpha, final double limitTolerance) {
    return price(data, option, ce, alpha, limitTolerance, false);
  }

  public double price(final BlackFunctionData data, final EuropeanVanillaOption option, final MartingaleCharacteristicExponent ce, final double alpha, final double limitTolerance,
      final boolean useVarianceReduction) {
    Validate.notNull(data, "data");
    Validate.notNull(option, "option");
    Validate.notNull(ce, "characteristic exponent");
    Validate.isTrue(limitTolerance > 0, "limit tolerance must be > 0");
    Validate.isTrue(alpha <= ce.getLargestAlpha() && alpha >= ce.getSmallestAlpha(),
        "The value of alpha is not valid for the Characteristic Exponent and will most likely lead to mispricing. Choose a value between " + ce.getSmallestAlpha() + " and " + ce.getLargestAlpha());
    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(ce, alpha, useVarianceReduction);
    final EuropeanCallFourierTransform psi = new EuropeanCallFourierTransform(ce);
    final double strike = option.getStrike();
    final double t = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    final double forward = data.getForward();
    final double discountFactor = data.getDiscountFactor();
    final Function1D<ComplexNumber, ComplexNumber> characteristicFunction = psi.getFunction(t);
    final double xMax = LIMIT_CALCULATOR.solve(characteristicFunction, alpha, limitTolerance);
    final Function1D<Double, Double> func = integrand.getFunction(data, option);
    final double integral = Math.exp(-alpha * Math.log(strike / forward)) * _integrator.integrate(func, 0.0, xMax) / Math.PI;
    if (useVarianceReduction) {
      final double black = BLACK_PRICE_FUNCTION.getPriceFunction(option).evaluate(data);
      final double diff = discountFactor * forward * integral;
      return diff + black;
    }
    
    if (isCall) {
      if (alpha > 0.0) {
        return discountFactor * forward * integral;
      } else if (alpha < -1.0) {
        return discountFactor * (forward * (1 + integral) - strike);
      } else {
        return discountFactor * forward * (integral + 1);
      }
    }
    if (alpha > 0.0) {
      return discountFactor * (forward * (integral - 1) + strike);
    } else if (alpha < -1.0) {
      return discountFactor * forward * integral;
    }
    return discountFactor * (forward * integral + strike);
  }

  public double priceFromVol(final BlackFunctionData data, final EuropeanVanillaOption option, final MartingaleCharacteristicExponent ce, final double alpha, final double limitTolerance,
      final boolean useVarianceReduction) {
    final double forward = data.getForward();
    final double discountFactor = data.getDiscountFactor();
    final double t = option.getTimeToExpiry();
    final double strike = option.getStrike();

    final EuropeanPriceIntegrand integrand = new EuropeanPriceIntegrand(ce, alpha, useVarianceReduction);
    final EuropeanCallFourierTransform callFourierTransform = new EuropeanCallFourierTransform(ce);

    final Function1D<ComplexNumber, ComplexNumber> psi = callFourierTransform.getFunction(t);
    final double xMax = LIMIT_CALCULATOR.solve(psi, alpha, limitTolerance);

    final double integral = Math.exp(-alpha * Math.log(strike / forward)) * _integrator.integrate(integrand.getFunction(data, option), 0.0, xMax) / Math.PI;
    final double black = BLACK_PRICE_FUNCTION.getPriceFunction(option).evaluate(data);
    final double diff = discountFactor * forward * integral;

    return diff + black;
  }
}
