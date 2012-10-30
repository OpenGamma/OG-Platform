/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class BarrierOptionPricerTest {
  private static final boolean PRINT = false;
  private static final double SPOT = 100;
  private static final double REBATE = 3.0;
  private static final double EXPIRY = 0.5;
  private static final double R = 0.08;
  private static final double B = 0.04;
  private static final double SIGMA = 0.3;
  private static final BarrierOptionPricer PRICER = new BarrierOptionPricer(100, 50, 0.5, 0.1);
  private static final BlackBarrierPriceFunction ANAL_PRICER = BlackBarrierPriceFunction.getInstance();

  @Test
  public void outBarrierTest() {
    if (PRINT) {
      System.out.println("BarrierOptionPricerTest");
    }

    for (int i = 0; i < 5; i++) {
      double strike = SPOT * (0.8 + i * 0.1);
      for (int j = 0; j < 5; j++) {
        double h = SPOT * (0.9 + j * 0.05);
        for (int k = 0; k < 2; k++) {
          boolean isCall = k == 0;
          BarrierType bt = SPOT > h ? BarrierType.DOWN : BarrierType.UP;
          double anPrice = ANAL_PRICER.getPrice(new EuropeanVanillaOption(strike, EXPIRY, isCall), new Barrier(KnockType.OUT, bt, ObservationType.CONTINUOUS, h), REBATE, SPOT, B, R, SIGMA);
          double fdPrice = PRICER.outBarrier(SPOT, h, strike, EXPIRY, R, B, SIGMA, isCall, REBATE);

          if (PRINT) {
            String call = isCall ? "call" : "put ";
            System.out.format(call + " x = %.0f h = %.0f\tprice = %.4f\tfdPrice = %.4f\n", strike, h, anPrice, fdPrice);
          }
          //TODO not splendid accuracy here - need to use discrete versions of r,b and sigma on the fd-grid to improve things
          assertEquals(anPrice, fdPrice, 5e-2);
        }
      }
    }
  }

  @Test
  public void inBarrierTest() {
    if (PRINT) {
      System.out.println("BarrierOptionPricerTest");
    }

    for (int i = 0; i < 5; i++) {
      double strike = SPOT * (0.9 + i * 0.1);
      for (int j = 0; j < 5; j++) {
        double h = SPOT * (0.9 + j * 0.05);
        for (int k = 0; k < 2; k++) {
          boolean isCall = k == 0;
          BarrierType bt = SPOT > h ? BarrierType.DOWN : BarrierType.UP;
          double anPrice = ANAL_PRICER.getPrice(new EuropeanVanillaOption(strike, EXPIRY, isCall), new Barrier(KnockType.IN, bt, ObservationType.CONTINUOUS, h), REBATE, SPOT, B, R, SIGMA);
          double fdPrice = PRICER.inBarrier(SPOT, h, strike, EXPIRY, R, B, SIGMA, isCall, REBATE);

          if (PRINT) {
            String call = isCall ? "call" : "put ";
            System.out.format(call + " x = %.0f h = %.0f\tprice = %.4f\tfdPrice = %.4f\n", strike, h, anPrice, fdPrice);
          }
          //TODO not splendid accuracy here - need to use discrete versions of r,b and sigma on the fd-grid to improve things
          assertEquals(anPrice, fdPrice, 5e-2);
        }
      }
    }
  }

  @Test
  public void barrierTest() {
    if (PRINT) {
      System.out.println("BarrierOptionPricerTest");
    }
    EuropeanVanillaOption option = new EuropeanVanillaOption(89.0, 0.25, false);
    Barrier barrier = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 80.0);
    double p1 = ANAL_PRICER.getPrice(option, barrier, REBATE, SPOT, R, REBATE, SIGMA);
    double p2 = PRICER.getPrice(option, barrier, REBATE, SPOT, R, REBATE, SIGMA);
    assertEquals(p1, p2, 1e-2);

  }

}
