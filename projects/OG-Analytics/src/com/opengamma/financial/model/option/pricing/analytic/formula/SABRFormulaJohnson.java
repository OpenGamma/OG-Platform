/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.util.CompareUtils;

/**
 * From the paper Johnson & Nonas, Arbitrage-free construction of the swaption cube (2009). <b>Note:</b> truncation weight does not seem to work
 */
public class SABRFormulaJohnson implements SABRFormula {
  private static final double EPS = 1e-15;

  public double impliedVolitility(final double f, final double alpha, final double beta, final double nu, final double rho, final double k, final double t) {

    if (beta > 0) {
      double sigmaDD = alpha * beta * Math.pow(f, beta - 1);
      double eta = (1 - beta) / beta * f;
      double sigmaBBF;
      double sigmaTrunc;
      double sigmaBlend;

      if (CompareUtils.closeEquals(f, k, EPS)) {
        sigmaBlend = sigmaDD;
      } else {
        double z = nu / sigmaDD * Math.log((f + eta) / (k + eta));
        sigmaBBF = sigmaDD * z / Math.log((z - rho + Math.sqrt(1 - 2 * rho * z + z * z)) / (1 - rho));
        sigmaTrunc = sigmaDD * Math.pow(1 - 4 * rho * z + (4.0 / 3.0 + 5 * rho * rho) * z * z, 1.0 / 8.0);
        double w = Math.min(1.0, 1.0 / nu / Math.sqrt(t));
        sigmaBlend = 1.0 / (w / sigmaBBF + (1 - w) / sigmaTrunc);
      }

      sigmaBlend *= 1 + (rho * nu * sigmaDD / 4 + (2 - 3 * rho * rho) * nu * nu / 24) * t;
      double sigmaCEV = sigmaBlend * Math.pow(f, 1 - beta) / beta;
      double price = CEVFormula.optionPrice(f, k, beta, 1.0, sigmaCEV, t, true);
      try {
        return BlackImpliedVolFormula.impliedVol(price, f, k, 1.0, t, true);
      } catch (Exception e) {
        return 0.0;
      }
    }
    throw new NotImplementedException();
  }
}
