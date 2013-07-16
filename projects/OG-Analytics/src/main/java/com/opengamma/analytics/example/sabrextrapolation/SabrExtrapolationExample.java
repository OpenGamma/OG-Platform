/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.sabrextrapolation;

import java.io.IOException;
import java.io.PrintStream;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;

/**
 * Example of SABR extrapolation.
 */
public class SabrExtrapolationExample {

  private static final double ALPHA = 0.05;
  private static final double BETA = 0.50;
  private static final double RHO = -0.25;
  private static final double NU = 0.50;
  private static final SABRFormulaData SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);
  private static final double FORWARD = 0.05;
  private static final double CUT_OFF_STRIKE = 0.10; // Set low for the test
  private static final double RANGE_STRIKE = 0.02;
  private static final double N_PTS = 100;
  private static final double TIME_TO_EXPIRY = 2.0;
  private static final double[] MU_VALUES = {5.0, 40.0, 90.0, 150.0 };

  /**
   * Generates the SABR data.
   * 
   * @param out  the output stream, not null
   * @throws IOException if an error occurs
   */
  public static void generateSabrData(PrintStream out) throws IOException {
    double mu;
    double strike;
    double price;
    double impliedVolatilityPct;
    SABRExtrapolationRightFunction sabrExtra;

    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    BlackFunctionData blackData = new BlackFunctionData(FORWARD, 1.0, 0.0);

    out.println("Mu\tPrice\tStrike\tImpliedVolPct");

    for (int i = 0; i < MU_VALUES.length; i++) {
      mu = MU_VALUES[i];
      sabrExtra = new SABRExtrapolationRightFunction(FORWARD, SABR_DATA, CUT_OFF_STRIKE, TIME_TO_EXPIRY, mu);

      for (int p = 0; p <= N_PTS; p++) {
        strike = CUT_OFF_STRIKE - RANGE_STRIKE + p * 4.0 * RANGE_STRIKE / N_PTS;
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike, TIME_TO_EXPIRY, true);
        price = sabrExtra.price(option);
        impliedVolatilityPct = implied.getImpliedVolatility(blackData, option, price) * 100;
        out.format("%4.0f\t%1.10f\t%1.10f\t%1.10f%n", mu, price, strike, impliedVolatilityPct);
      }
    }
  }

  public static void main(String[] args) throws Exception {  // CSIGNORE
    generateSabrData(System.out);
  }

}
