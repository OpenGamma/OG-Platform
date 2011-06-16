/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class MarkovChainApproxTest {
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final double T = 5.0;
  private static final double RATE = 0.0;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double VOL1 = 0.20;
  private static final double VOL2 = 0.70;
  private static final double LAMBDA12 = 0.2;
  private static final double LAMBDA21 = 2.0;
  private static final double INITIAL_PROB_STATE1 = 1.0;

  private static final MarkovChainApprox CHAIN_APPROX;
  private static final MarkovChain CHAIN;
  private static double[] SIMS;

  static {
    CHAIN = new MarkovChain(VOL1, VOL2, LAMBDA12, LAMBDA21, INITIAL_PROB_STATE1);
    CHAIN_APPROX = new MarkovChainApprox(VOL1, VOL2, LAMBDA12, LAMBDA21, INITIAL_PROB_STATE1, T);
    SIMS = CHAIN.simulate(T, 100000);
  }

  @Test
  public void test() {

    CHAIN.debug(T, SIMS);
    CHAIN_APPROX.debug(T);
  }

  @Test
  public void priceTest() {
    double forward = 0.04;
    double df = 0.9;
    double strike;
    double impVol;
    double mcImpVol;
    BlackFunctionData data = new BlackFunctionData(forward, df, 0.0);

    for (int i = 0; i < 101; i++) {
      strike = 0.01 + 0.1 * i / 100;
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
      BlackPriceFunction func = new BlackPriceFunction();
      double price = CHAIN_APPROX.price(forward, df, strike);
      double mcPrice = CHAIN.price(forward, df, strike, T, SIMS);
      try {
        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      } catch (Exception e) {
        impVol = 0;
      }
      try {
        mcImpVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, mcPrice);
      } catch (Exception e) {
        mcImpVol = 0;
      }
      System.out.println(strike + "\t" + price + "\t" + mcPrice + "\t" + impVol + "\t" + mcImpVol);
    }

  }
}
