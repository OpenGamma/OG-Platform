/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;

/**
 * 
 */
public class BlackImpliedVolatilityFormula {
  private static final BracketRoot BRACKETER = new BracketRoot();
  private final RealSingleRootFinder _rootFinder;

  public BlackImpliedVolatilityFormula(final RealSingleRootFinder rootFinder) {
    _rootFinder = rootFinder;
  }

  public double getImpliedVolatility(final BlackFunctionData data, final EuropeanVanillaOption option, final double optionPrice) {
    final double discountFactor = data.getDf();
    final boolean isCall = option.isCall();
    final double f = data.getF();
    final double k = option.getK();
    final double t = option.getT();
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

    final double[] range = BRACKETER.getBracketedPoints(func, 0.0, 1.0);
    return _rootFinder.getRoot(func, range[0], range[1]);

  }

}
