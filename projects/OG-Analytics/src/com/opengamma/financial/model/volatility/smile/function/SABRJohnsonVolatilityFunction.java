/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * From the paper Johnson & Nonas, Arbitrage-free construction of the swaption cube (2009). <b>Note:</b> truncation weight does not seem to work
 */
public class SABRJohnsonVolatilityFunction implements VolatilityFunctionProvider<SABRFormulaData> {
  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option) {
    final double k = option.getK();
    final double t = option.getT();
    return new Function1D<SABRFormulaData, Double>() {

      @Override
      public final Double evaluate(final SABRFormulaData data) {
        final double alpha = data.getAlpha();
        final double beta = data.getBeta();
        final double rho = data.getRho();
        final double nu = data.getNu();
        final double f = data.getF();
        if (beta > 0) {
          final double sigmaDD = alpha * beta * Math.pow(f, beta - 1);
          final double eta = (1 - beta) / beta * f;
          double sigmaBBF;
          double sigmaTrunc;
          double sigmaBlend;
          if (CompareUtils.closeEquals(f, k, EPS)) {
            sigmaBlend = sigmaDD;
          } else {
            final double z = nu / sigmaDD * Math.log((f + eta) / (k + eta));
            sigmaBBF = sigmaDD * z / Math.log((z - rho + Math.sqrt(1 - 2 * rho * z + z * z)) / (1 - rho));
            sigmaTrunc = sigmaDD * Math.pow(1 - 4 * rho * z + (4.0 / 3.0 + 5 * rho * rho) * z * z, 1.0 / 8.0);
            final double w = Math.min(1.0, 1.0 / nu / Math.sqrt(t));
            sigmaBlend = 1.0 / (w / sigmaBBF + (1 - w) / sigmaTrunc);
          }
          sigmaBlend *= 1 + (rho * nu * sigmaDD / 4 + (2 - 3 * rho * rho) * nu * nu / 24) * t;
          final double sigmaCEV = sigmaBlend * Math.pow(f, 1 - beta) / beta;
          final double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigmaCEV, t, true);
          try {
            return BlackImpliedVolFormula.impliedVol(price, f, k, 1.0, t, true);
          } catch (final Exception e) {
            return 0.0;
          }
        }
        throw new NotImplementedException();
      }
    };
  }
}
