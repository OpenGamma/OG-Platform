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
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class BlackImpliedVolatilityFormula {
  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private static final int MAX_ITERATIONS = 100;

  public double getImpliedVolatility(final BlackFunctionData data, final EuropeanVanillaOption option, final double optionPrice) {
    final double discountFactor = data.getDiscountFactor();
    final boolean isCall = option.isCall();
    final double f = data.getForward();
    final double k = option.getStrike();
    final double intrinsicPrice = discountFactor * Math.max(0, (isCall ? 1 : -1) * (f - k));
    Validate.isTrue(optionPrice >= intrinsicPrice, "option price (" + optionPrice + ") less than intrinsic value (" + intrinsicPrice + ")");

    if (optionPrice == intrinsicPrice) {
      return 0.0;
    }
    double sigma = 0.3;
    BlackFunctionData newData = new BlackFunctionData(f, discountFactor, sigma);
    final double maxChange = 0.5;
    final Function1D<BlackFunctionData, Double> priceFunction = BLACK_PRICE_FUNCTION.getPriceFunction(option);
    double price = priceFunction.evaluate(newData);
    final Function1D<BlackFunctionData, Double> vegaFunction = BLACK_PRICE_FUNCTION.getVegaFunction(option);
    double vega = vegaFunction.evaluate(newData);
    double change = (price - optionPrice) / vega;
    double sign = Math.signum(change);
    change = sign * Math.min(maxChange, Math.abs(change));
    if (change > 0 && change > sigma) {
      change = sigma;
    }
    int count = 0;
    while (Math.abs(change) > 1e-8) {
      sigma -= change;
      newData = new BlackFunctionData(f, discountFactor, sigma);
      price = priceFunction.evaluate(newData);
      vega = vegaFunction.evaluate(newData);
      change = (price - optionPrice) / vega;
      sign = Math.signum(change);
      change = sign * Math.min(maxChange, Math.abs(change));
      if (change > 0 && change > sigma) {
        change = sigma;
      }
      if (count++ > MAX_ITERATIONS) {
        throw new MathException();
        //        final BracketRoot bracketer = new BracketRoot();
        //        final VanWijngaardenDekkerBrentSingleRootFinder rootFinder = new VanWijngaardenDekkerBrentSingleRootFinder();
        //        final Function1D<Double, Double> func = new Function1D<Double, Double>() {
        //
        //          @SuppressWarnings({"synthetic-access", "hiding"})
        //          @Override
        //          public Double evaluate(final Double sigma) {
        //            final BlackFunctionData newData = new BlackFunctionData(data.getF(), data.getDf(), sigma);
        //            return BLACK_PRICE_FUNCTION.getPriceFunction(option).evaluate(newData) - optionPrice;
        //          }
        //        };
        //
        //        final double[] range = bracketer.getBracketedPoints(func, 0.0, 10.0);
        //        final double temp = rootFinder.getRoot(func, range[0], range[1]);
        //        System.out.println(temp);
        //        return temp;
      }
    }
    return sigma;
  }

}
