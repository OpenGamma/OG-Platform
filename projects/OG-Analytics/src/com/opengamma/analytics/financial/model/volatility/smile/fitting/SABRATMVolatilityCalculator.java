/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;

/**
 * 
 */
public class SABRATMVolatilityCalculator {
  private final VolatilityFunctionProvider<SABRFormulaData> _sabrFormula;
  private final BracketRoot _bracketer = new BracketRoot();
  private final RealSingleRootFinder _rootFinder = new RidderSingleRootFinder();

  public SABRATMVolatilityCalculator(final VolatilityFunctionProvider<SABRFormulaData> formula) {
    Validate.notNull(formula, "formula");
    _sabrFormula = formula;
  }

  /**
   * Finds the alpha that gives the required ATM volatility 
   * @param data SABR parameters - the alpha value is ignored 
   * @param option The option
   * @param forward the forward
   * @param atmVol The ATM volatility
   * @return the value of alpha
   */
  public double calculate(final SABRFormulaData data, final EuropeanVanillaOption option, final double forward, final double atmVol) {
    Validate.notNull(data, "data");
    Validate.notNull(option, "option");
    Validate.isTrue(atmVol > 0, "ATM vol must be > 0");
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double alpha) {
        final SABRFormulaData newData = new SABRFormulaData(alpha, data.getBeta(), data.getRho(), data.getNu());
        return _sabrFormula.getVolatilityFunction(option, forward).evaluate(newData) - atmVol;
      }
    };
    final double alphaTry = atmVol * Math.pow(forward, 1 - data.getBeta());
    final double[] range = _bracketer.getBracketedPoints(f, alphaTry / 2.0, 2 * alphaTry);
    return _rootFinder.getRoot(f, range[0], range[1]);
  }

}
