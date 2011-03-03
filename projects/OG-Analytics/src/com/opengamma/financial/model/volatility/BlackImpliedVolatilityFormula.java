/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;

/**
 * 
 */
public class BlackImpliedVolatilityFormula {
  private static final BracketRoot BRACKETER = new BracketRoot();
  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private final RealSingleRootFinder _rootFinder;

  public BlackImpliedVolatilityFormula(final RealSingleRootFinder rootFinder) {
    _rootFinder = rootFinder;
  }

  public double getImpliedVolatility(final BlackFunctionData data, final EuropeanVanillaOption option, final double optionPrice) {
    final double discountFactor = data.getDf();
    final boolean isCall = option.isCall();
    final double f = data.getF();
    final double k = option.getK();
    final double intrinsicPrice = discountFactor * Math.max(0, (isCall ? 1 : -1) * (f - k));
    Validate.isTrue(optionPrice >= intrinsicPrice, "option price (" + optionPrice + ") less than intrinsic value (" + intrinsicPrice + ")");

    if (optionPrice == intrinsicPrice) {
      return 0.0;
    }

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double sigma) {
        final BlackFunctionData newData = new BlackFunctionData(data.getF(), data.getDf(), sigma);
        return BLACK_PRICE_FUNCTION.getPriceFunction(option).evaluate(newData) - optionPrice;
      }
    };

    final double[] range = BRACKETER.getBracketedPoints(func, 0.0, 10.0);
    return _rootFinder.getRoot(func, range[0], range[1]);

  }

}
