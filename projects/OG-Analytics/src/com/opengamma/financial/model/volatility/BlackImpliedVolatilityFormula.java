/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.BracketRoot;

/**
 * 
 */
public class BlackImpliedVolatilityFormula {
  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private static final int MAX_ITERATIONS = 100;
  private static final double EPS = 1e-15;

  public double getImpliedVolatility(final BlackFunctionData data, final EuropeanVanillaOption option, final double optionPrice) {
    final double discountFactor = data.getDiscountFactor();
    final boolean isCall = option.isCall();
    final double f = data.getForward();
    final double k = option.getStrike();
    final double intrinsicPrice = discountFactor * Math.max(0, (isCall ? 1 : -1) * (f - k));
    Validate.isTrue(k > 0, "Cannot find an implied volatility when strike is zero as there is no optionality");
    Validate.isTrue(optionPrice >= intrinsicPrice, "option price (" + optionPrice + ") less than intrinsic value (" + intrinsicPrice + ")");

    //|| CompareUtils.closeEquals(optionPrice, intrinsicPrice, 1e-6), "option price (" + optionPrice + ") less than intrinsic value (" + intrinsicPrice;  + ")");

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
    if (vega == 0 || Double.isNaN(vega)) {
      return solveByBisection(data, option, optionPrice, sigma, 0.1);
    }
    double change = (price - optionPrice) / vega;

    double sign = Math.signum(change);
    change = sign * Math.min(maxChange, Math.abs(change));
    if (change > 0 && change > sigma) {
      change = sigma;
    }
    int count = 0;
    while (Math.abs(change) > EPS) {
      sigma -= change;
      newData = new BlackFunctionData(f, discountFactor, sigma);
      price = priceFunction.evaluate(newData);
      vega = vegaFunction.evaluate(newData);
      if (vega == 0 || Double.isNaN(vega)) {
        return solveByBisection(data, option, optionPrice, sigma, 0.01);
      }
      change = (price - optionPrice) / vega;
      sign = Math.signum(change);
      change = sign * Math.min(maxChange, Math.abs(change));
      if (change > 0 && change > sigma) {
        change = sigma;
      }
      if (count++ > MAX_ITERATIONS) {
        return solveByBisection(data, option, optionPrice, sigma, change);
      }
    }
    return sigma;
  }

  /**
   * @param data
   * @param option
   * @param optionPrice
   * @param sigma
   * @param change
   * @return
   */
  private double solveByBisection(final BlackFunctionData data, final EuropeanVanillaOption option, final double optionPrice, double sigma, double change) {
    final BracketRoot bracketer = new BracketRoot();
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(EPS);
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @SuppressWarnings({"synthetic-access" })
      @Override
      public Double evaluate(final Double volatility) {
        final BlackFunctionData myData = new BlackFunctionData(data.getForward(), data.getDiscountFactor(), volatility);
        return BLACK_PRICE_FUNCTION.getPriceFunction(option).evaluate(myData) - optionPrice;
      }
    };
    final double[] range = bracketer.getBracketedPoints(func, sigma - Math.abs(change), sigma + Math.abs(change));
    return rootFinder.getRoot(func, range[0], range[1]);
  }
}
