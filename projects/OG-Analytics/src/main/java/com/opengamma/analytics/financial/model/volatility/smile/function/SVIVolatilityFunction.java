/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.lang.annotation.ExternalFunction;

/**
 * Gatheral's Stochastic Volatility Inspired (SVI) model
 */
public class SVIVolatilityFunction extends VolatilityFunctionProvider<SVIFormulaData> {
  private static final int NUM_PARAMETERS = 5;

  @Override
  public Function1D<SVIFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward > 0, "Need forward >= 0");
    final double strike = option.getStrike();
    Validate.isTrue(strike > 0, "Need strike >= 0");
    final double kappa = Math.log(strike / forward);

    return new Function1D<SVIFormulaData, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final SVIFormulaData data) {
        return getVolatility(kappa, data);
      }
    };
  }

  @ExternalFunction
  public double getVolatility(final double forward, final double strike, final double a, final double b, final double rho, final double nu, final double m) {

    final SVIFormulaData data = new SVIFormulaData(a, b, rho, nu, m);
    return getVolatility(forward, strike, data);
  }

  public double getVolatility(final double forward, final double strike, final SVIFormulaData data) {
    Validate.isTrue(forward > 0, "Need forward >= 0");
    Validate.isTrue(strike > 0, "Need strike >= 0");
    Validate.notNull(data, "null SVI parameters");

    final double kappa = Math.log(strike / forward);
    return getVolatility(kappa, data);
  }

  private double getVolatility(final double kappa, final SVIFormulaData data) {
    Validate.notNull(data, "null SVI parameters");
    final double d = kappa - data.getM();
    final double nu = data.getNu();
    return Math.sqrt(data.getA() + data.getB() * (data.getRho() * d + Math.sqrt(d * d + nu * nu)));
  }

  @Override
  public Function1D<SVIFormulaData, double[]> getVolatilityAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward > 0, "Need forward >= 0");
    final double strike = option.getStrike();
    Validate.isTrue(strike > 0, "Need strike >= 0");
    final double kappa = Math.log(strike / forward);

    return new Function1D<SVIFormulaData, double[]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public double[] evaluate(final SVIFormulaData data) {
        return getVolatilityAdjoint(forward, strike, kappa, data);
      }
    };
  }

  @Override
  public Function1D<SVIFormulaData, double[][]> getVolatilityAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {
    return getVolatilityAdjointFunctionByCallingSingleStrikes(forward, strikes, timeToExpiry);
  }

  @Override
  public Function1D<SVIFormulaData, double[]> getModelAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward > 0, "Need forward >= 0");
    final double strike = option.getStrike();
    Validate.isTrue(strike > 0, "Need strike >= 0");
    final double kappa = Math.log(strike / forward);

    return new Function1D<SVIFormulaData, double[]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public double[] evaluate(final SVIFormulaData data) {
        return getModelAdjoint(kappa, data);
      }
    };
  }

  @Override
  public Function1D<SVIFormulaData, double[][]> getModelAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {
    return getModelAdjointFunctionByCallingSingleStrikes(forward, strikes, timeToExpiry);
  }

  @ExternalFunction
  public double[] getVolatilityAjoint(final double forward, final double strike, final double a, final double b, final double rho, final double nu, final double m) {
    final SVIFormulaData data = new SVIFormulaData(a, b, rho, nu, m);
    return getVolatilityAjoint(forward, strike, data);

  }

  public double[] getVolatilityAjoint(final double forward, final double strike, final SVIFormulaData data) {
    Validate.isTrue(forward > 0, "Need forward >= 0");
    Validate.isTrue(strike > 0, "Need strike >= 0");

    final double kappa = Math.log(strike / forward);
    return getVolatilityAdjoint(forward, strike, kappa, data);
  }

  private double[] getVolatilityAdjoint(final double forward, final double strike, final double kappa, final SVIFormulaData data) {
    Validate.notNull(data, "null data");

    final double b = data.getB();
    final double rho = data.getRho();
    final double nu = data.getNu();

    final double d = kappa - data.getM();
    final double r = Math.sqrt(d * d + nu * nu);
    final double s = rho * d + r;
    final double sigma = Math.sqrt(data.getA() + b * s);

    final double kappaBar = b * (rho + d / r) / 2 / sigma;

    final double[] res = new double[8];
    res[0] = sigma;
    res[1] = -kappaBar / forward; //fBar
    res[2] = kappaBar / strike; //strikebar
    res[3] = 1. / 2. / sigma; //aBar
    res[4] = s / 2 / sigma; //bBar
    res[5] = b * d / 2 / sigma; //rhoBar
    res[6] = nu * b / r / 2 / sigma; //nuBar
    res[7] = -kappaBar; //mBar

    return res;
  }

  private double[] getModelAdjoint(final double kappa, final SVIFormulaData data) {
    Validate.notNull(data, "null data");

    final double b = data.getB();
    final double rho = data.getRho();
    final double nu = data.getNu();

    final double d = kappa - data.getM();
    final double r = Math.sqrt(d * d + nu * nu);
    final double s = rho * d + r;
    final double sigma = Math.sqrt(data.getA() + b * s);

    final double kappaBar = b * (rho + d / r) / 2 / sigma;

    final double[] res = new double[5];

    res[0] = 1. / 2. / sigma; //aBar
    res[1] = s / 2 / sigma; //bBar
    res[2] = b * d / 2 / sigma; //rhoBar
    res[3] = nu * b / r / 2 / sigma; //nuBar
    res[4] = -kappaBar; //mBar

    return res;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SVI";
  }

  @Override
  public int getNumberOfParameters() {
    return NUM_PARAMETERS;
  }

  @Override
  public SVIFormulaData toModelData(final double[] parameters) {
    return new SVIFormulaData(parameters);
  }
}
