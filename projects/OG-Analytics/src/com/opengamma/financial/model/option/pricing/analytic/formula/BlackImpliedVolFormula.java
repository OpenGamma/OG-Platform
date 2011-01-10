/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

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

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double sigma) {
        return BlackFormula.optionPrice(f, k, discountFactor, sigma, t, isCall) - optionPrice;
      }
    };

    final double[] range = s_bracketRoot.getBracketedPoints(func, 0.0, 1.0);
    return s_root.getRoot(func, range[0], range[1]);

  }
}
