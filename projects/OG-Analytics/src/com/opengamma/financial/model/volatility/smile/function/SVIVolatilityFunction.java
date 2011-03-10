/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;

/**
 * Gatheral's Stochastic Volatility Inspired (SVI) model
 */
public class SVIVolatilityFunction implements VolatilityFunctionProvider<SVIFormulaData> {

  @Override
  public Function1D<SVIFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    return new Function1D<SVIFormulaData, Double>() {

      @Override
      public Double evaluate(final SVIFormulaData data) {
        Validate.notNull(data, "data");
        final double b = data.getB();
        final double rho = data.getRho();
        //Validate.isTrue(b * (1 + Math.abs(rho)) <= 4. / t, "No-arbitrage condition not satisfied"); //TODO doesn't seem to work when fitting
        final double a = data.getA();
        final double sigma = data.getSigma();
        final double m = data.getM();
        final double d = k - m;
        return Math.sqrt((a + b * (rho * d + Math.sqrt(d * d + sigma * sigma))) / t);
      }

    };
  }
}
