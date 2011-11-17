/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.exp;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.subtract;
import static com.opengamma.math.number.ComplexNumber.MINUS_I;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class FourierModelGreeks {

  private static final IntegralLimitCalculator LIMIT_CALCULATOR = new IntegralLimitCalculator();
  private final Integrator1D<Double, Double> _integrator;

  public FourierModelGreeks() {
    this(new RungeKuttaIntegrator1D());
  }

  public FourierModelGreeks(final Integrator1D<Double, Double> integrator) {
    Validate.notNull(integrator, "null integrator");
    _integrator = integrator;
  }


  public double[] getGreeks(final BlackFunctionData data, final EuropeanVanillaOption option, final MartingaleCharacteristicExponent ce, final double alpha, final double limitTolerance) {
    Validate.notNull(data, "data");
    Validate.notNull(option, "option");
    Validate.notNull(ce, "characteristic exponent");
    Validate.isTrue(limitTolerance > 0, "limit tolerance must be > 0");
    Validate.isTrue(alpha <= ce.getLargestAlpha() && alpha >= ce.getSmallestAlpha(),
        "The value of alpha is not valid for the Characteristic Exponent and will most likely lead to mispricing. Choose a value between " + ce.getSmallestAlpha() + " and " + ce.getLargestAlpha());

    final EuropeanCallFourierTransform psi = new EuropeanCallFourierTransform(ce);
    final double strike = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double forward = data.getForward();
    final double discountFactor = data.getDiscountFactor();
    final Function1D<ComplexNumber, ComplexNumber> characteristicFunction = psi.getFunction(t);
    final double xMax = LIMIT_CALCULATOR.solve(characteristicFunction, alpha, limitTolerance);

    double kappa = Math.log(strike / forward);
    int n = ce.getCharacteristicExponentAdjoint(MINUS_I, 1.0).length; //TODO have method like getNumberOfparameters 
    
    Function1D<ComplexNumber, ComplexNumber[]> adjointFuncs = ce.getAdjointFunction(t);
    double[] res = new double[n - 1];
    
    //TODO This is inefficient as a call to ajointFuncs.evaluate(z), will return several values (the value of the characteristic function and its derivatives), but only one
    // of these values is used by each of the the integraters - a parallel quadrature scheme would be good here 
    for (int i = 0; i < n - 1; i++) {
      final Function1D<Double, Double> func = getIntegrandFunction(adjointFuncs, alpha, kappa,  i + 1);
      final double integral = Math.exp(-alpha * Math.log(strike / forward)) * _integrator.integrate(func, 0.0, xMax) / Math.PI;
      res[i] = discountFactor * forward * integral;
    }
    return res;
  }

  public Function1D<Double, Double> getIntegrandFunction(final  Function1D<ComplexNumber, ComplexNumber[]> ajointFunctions, final double alpha, final double kappa, final int index) {

    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        final ComplexNumber z = new ComplexNumber(x, -1 - alpha);
        ComplexNumber[] ajoint = ajointFunctions.evaluate(z);
        ComplexNumber num = exp(add(new ComplexNumber(0, -x * kappa), ajoint[0]));
        if (index > 0) {
          num = multiply(num, ajoint[index]);
        }
        final ComplexNumber denom = multiply(z, subtract(MINUS_I, z));
        final ComplexNumber res = divide(num, denom);
        return res.getReal();
      }
    };

  }

}
