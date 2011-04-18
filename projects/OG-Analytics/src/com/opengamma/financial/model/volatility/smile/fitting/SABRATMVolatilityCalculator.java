/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.RidderSingleRootFinder;

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

  public double calculate(final SABRFormulaData data, final EuropeanVanillaOption option, final double atmVol) {
    Validate.notNull(data, "data");
    Validate.notNull(option, "option");
    Validate.isTrue(atmVol > 0, "ATM vol must be > 0");
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double alpha) {
        final SABRFormulaData newData = new SABRFormulaData(data.getForward(), alpha, data.getBeta(), data.getNu(), data.getRho());
        return _sabrFormula.getVolatilityFunction(option).evaluate(newData) - atmVol;
      }
    };
    final double alphaTry = atmVol * Math.pow(data.getForward(), 1 - data.getBeta());
    final double[] range = _bracketer.getBracketedPoints(f, alphaTry / 2.0, 2 * alphaTry);
    return _rootFinder.getRoot(f, range[0], range[1]);
  }

}
