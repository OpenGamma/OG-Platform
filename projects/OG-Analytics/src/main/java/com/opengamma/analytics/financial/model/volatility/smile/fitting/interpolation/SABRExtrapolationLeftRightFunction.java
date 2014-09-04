/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Abstract class for {@link SABRExtrapolationRightFunction} and {@link SABRExtrapolationLeftFunction}
 */
public abstract class SABRExtrapolationLeftRightFunction {

  private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;
  
  /**
   * Constructor using default volatility function
   */
  public SABRExtrapolationLeftRightFunction() {
    _sabrFunction = new SABRHaganVolatilityFunction();
  }

  /**
   * Constructor specifying volatility function
   * @param volatilityFunction The volatility function
   */
  public SABRExtrapolationLeftRightFunction(final VolatilityFunctionProvider<SABRFormulaData> volatilityFunction) {
    Validate.notNull(volatilityFunction, "volatilityFunction");
    _sabrFunction = volatilityFunction;
  }
  
  /**
   * Get volatility function
   * @return The volatility function
   */
  public VolatilityFunctionProvider<SABRFormulaData> getVolatilityFunction() {
    return _sabrFunction;
  }

  /**
   * Computes the first and second order derivatives of the Black implied volatility in the SABR model. 
   * @param option The option
   * @param forward The forward value of the underlying
   * @param data The SABR data
   * @param volatilityD The array used to return the first order derivatives. [0] Derivative w.r.t the forward, [1] the derivative w.r.t the strike. 
   * @param volatilityD2 The array of array used to return the second order derivative. Only the second order derivative with respect to the forward and strike are implemented. 
   * [0][0] forward-forward; [0][1] forward-strike; [1][1] strike-strike.
   * @return The volatility
   */
  protected double getVolatilityAdjoint2(final EuropeanVanillaOption option, final double forward,
      final SABRFormulaData data, final double[] volatilityD, final double[][] volatilityD2) {
    if (_sabrFunction instanceof SABRHaganVolatilityFunction) {
      return ((SABRHaganVolatilityFunction) _sabrFunction).getVolatilityAdjoint2(option, forward, data, volatilityD, volatilityD2);
    }

    double eps = 1.0e-6;
    volatilityD[0] = fdSensitivity(option, forward, data, 1, eps);
    volatilityD[1] = fdSensitivity(option, forward, data, 2, eps);

    if (forward > eps) {
      double fwdUp = fdSensitivity(option, forward + eps, data, 1, eps);
      double fwdDw = fdSensitivity(option, forward - eps, data, 1, eps);
      double crUp = fdSensitivity(option, forward + eps, data, 2, eps);
      double crDw = fdSensitivity(option, forward - eps, data, 2, eps);
      volatilityD2[0][0] = 0.5 * (fwdUp - fwdDw) / eps;
      volatilityD2[1][0] = 0.5 * (crUp - crDw) / eps;
      volatilityD2[0][1] = volatilityD2[1][0];
    } else {
      double fwdBase = fdSensitivity(option, forward, data, 1, eps);
      double fwdUp = fdSensitivity(option, forward + eps, data, 1, eps);
      double fwdUpUp = fdSensitivity(option, forward + 2.0 * eps, data, 1, eps);
      double crBase = fdSensitivity(option, forward, data, 2, eps);
      double crUp = fdSensitivity(option, forward + eps, data, 2, eps);
      double crUpUp = fdSensitivity(option, forward + 2.0 * eps, data, 2, eps);
      volatilityD2[0][0] = (2.0 * fwdUp - 0.5 * fwdUpUp - 1.5 * fwdBase) / eps;
      volatilityD2[1][0] = (2.0 * crUp - 0.5 * crUpUp - 1.5 * crBase) / eps;
      volatilityD2[0][1] = volatilityD2[1][0];
    }
    double strike = option.getStrike();
    if (strike > eps) {
      double strUp = fdSensitivity(option.withStrike(strike + eps), forward, data, 2, eps);
      double strDw = fdSensitivity(option.withStrike(strike - eps), forward, data, 2, eps);
      volatilityD2[1][1] = 0.5 * (strUp - strDw) / eps;
    } else {
      double strBase = fdSensitivity(option.withStrike(strike), forward, data, 2, eps);
      double strUp = fdSensitivity(option.withStrike(strike + eps), forward, data, 2, eps);
      double strUpUp = fdSensitivity(option.withStrike(strike + 2.0 * eps), forward, data, 2, eps);
      volatilityD2[1][1] = 0.5 * (2.0 * strUp - 0.5 * strUpUp - 1.5 * strBase) / eps;
    }
    return _sabrFunction.getVolatilityFunction(option, forward).evaluate(data);
  }

  private double fdSensitivity(final EuropeanVanillaOption optionData, final double forward, final SABRFormulaData sabrData, final int sense, final double delta) {
    FiniteDifferenceType fdType = null;
    ScalarFirstOrderDifferentiator differentiator;
    Function1D<Double, Double> function;
    Double ref;

    switch (sense) {
      case 1:
        if (forward > delta) {
          fdType = FiniteDifferenceType.CENTRAL;
        } else {
          fdType = FiniteDifferenceType.FORWARD;
        }
        differentiator = new ScalarFirstOrderDifferentiator(fdType, delta);
        function = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double x) {
            return _sabrFunction.getVolatilityFunction(optionData, x).evaluate(sabrData);
          }
        };
        ref = forward;
        break;
      case 2:
        double strike = optionData.getStrike();
        if (strike >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
        } else {
          fdType = FiniteDifferenceType.FORWARD;
        }
        function = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double x) {
            return _sabrFunction.getVolatilityFunction(optionData.withStrike(x), forward).evaluate(sabrData);
          }
        };
        differentiator = new ScalarFirstOrderDifferentiator(fdType, delta);
        ref = strike;
        break;
      default:
        throw new MathException();
    }

    return differentiator.differentiate(function).evaluate(ref);
  }
  
  
}
