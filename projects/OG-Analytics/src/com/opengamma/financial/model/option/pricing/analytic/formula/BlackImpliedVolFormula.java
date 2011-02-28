/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;

/**
 * The Black or log-normal implied volatility
 */
public class BlackImpliedVolFormula {
  private static BracketRoot s_bracketRoot = new BracketRoot();
  private static final RealSingleRootFinder s_root = new VanWijngaardenDekkerBrentSingleRootFinder();

  public static double impliedVol(final double optionPrice, final double f, final double k, final double discountFactor, final double t, final boolean isCall) {

    final double intrinsicPrice = discountFactor * Math.max(0, (isCall ? 1 : -1) * (f - k));
    Validate.isTrue(optionPrice >= intrinsicPrice, "option price less than intrinsic value");

    if (optionPrice == intrinsicPrice) {
      return 0.0;
    }

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double sigma) {
        return BlackFormula.optionPrice(f, k, discountFactor, sigma, t, isCall) - optionPrice;
      }
    };

    final double[] range = s_bracketRoot.getBracketedPoints(func, 0.0, 1.0);
    return s_root.getRoot(func, range[0], range[1]);

  }

  public static double impliedVolNewton(final double optionPrice, final double f, final double k, final double discountFactor, final double t, final boolean isCall) {

    final double intrinsicPrice = discountFactor * Math.max(0, (isCall ? 1 : -1) * (f - k));
    Validate.isTrue(optionPrice >= intrinsicPrice, "option price less than intrinsic value");

    double sigma = 0.3;
    final double maxChange = 0.5;
    double p = BlackFormula.optionPrice(f, k, discountFactor, sigma, t, isCall);
    double vega = BlackFormula.vega(f, k, discountFactor, sigma, t);
    double change = (p - optionPrice) / vega;
    double sign = Math.signum(change);
    change = sign * Math.min(maxChange, Math.abs(change));
    if (change > 0 && change > sigma) {
      change = sigma;
    }
    int count = 0;
    while (Math.abs(change) > 1e-8) {
      sigma -= change;
      p = BlackFormula.optionPrice(f, k, discountFactor, sigma, t, isCall);
      vega = BlackFormula.vega(f, k, discountFactor, sigma, t);
      change = (p - optionPrice) / vega;
      sign = Math.signum(change);
      change = sign * Math.min(maxChange, Math.abs(change));
      change = sign * Math.abs(change);
      if (change > 0 && change > sigma) {
        change = sigma;
      }

      if (count++ > 50) {
        throw new MathException("implied vol not found");
      }
    }
    return sigma;
  }

}
