/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * From the paper Johnson & Nonas, Arbitrage-free construction of the swaption cube (2009). <b>Note:</b> truncation weight does not seem to work
 */
public class SABRJohnsonVolatilityFunction extends VolatilityFunctionProvider<SABRFormulaData> {
  private static final int NUM_PARAMETERS = 4;
  private static final double EPS = 1e-15;
  private static final CEVPriceFunction CEV_FUNCTION = new CEVPriceFunction();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final Function1D<CEVFunctionData, Double> priceFunction = CEV_FUNCTION.getPriceFunction(option);
    return new Function1D<SABRFormulaData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public final Double evaluate(final SABRFormulaData data) {
        Validate.notNull(data, "data");
        final double alpha = data.getAlpha();
        final double beta = data.getBeta();
        final double rho = data.getRho();
        final double nu = data.getNu();
        if (CompareUtils.closeEquals(nu, 0, EPS)) {
          if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
            return alpha; // this is just log-normal
          }
          throw new NotImplementedException("Have not implemented the case where nu = 0, beta != 0");
        }
        if (beta > 0) {
          final double sigmaDD = alpha * beta * Math.pow(forward, beta - 1);
          final double eta = (1 - beta) / beta * forward;
          double sigmaBlend;
          if (CompareUtils.closeEquals(forward, k, EPS)) {
            sigmaBlend = sigmaDD;
          } else {
            final double z = nu / sigmaDD * Math.log((forward + eta) / (k + eta));
            final double sigmaBBF = sigmaDD * z / Math.log((z - rho + Math.sqrt(1 - 2 * rho * z + z * z)) / (1 - rho));
            final double sigmaTrunc = sigmaDD * Math.pow(1 - 4 * rho * z + (4.0 / 3.0 + 5 * rho * rho) * z * z, 1.0 / 8.0);
            final double w = Math.min(1.0, 1.0 / nu / Math.sqrt(t));
            sigmaBlend = 1.0 / (w / sigmaBBF + (1 - w) / sigmaTrunc);
          }
          sigmaBlend *= 1 + (rho * nu * sigmaDD / 4 + (2 - 3 * rho * rho) * nu * nu / 24) * t;
          final double sigmaCEV = sigmaBlend * Math.pow(forward, 1 - beta) / beta;
          final CEVFunctionData cevData = new CEVFunctionData(forward, 1, sigmaCEV, beta);
          final double price = priceFunction.evaluate(cevData);
          return BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(forward, 1, sigmaCEV), option, price);
        }
        throw new NotImplementedException("Have not implemented the case where b <= 0");
      }
    };
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SABR (Johnson)";
  }

  @Override
  public int getNumberOfParameters() {
    return NUM_PARAMETERS;
  }

  @Override
  public SABRFormulaData toModelData(final double[] parameters) {
    return new SABRFormulaData(parameters);
  }
}
