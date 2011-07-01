/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class DupireLocalVolatilityTest {
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final double SPOT = 0.04;
  private static final double ATM_VOL = 0.2;
  private static final double ALPHA;
  private static final double BETA = 0.5;
  private static final double RHO = -0.6;
  private static final double NU = 0.3;
  private static final double RATE = 0.05;

  private static final PriceSurface PRICE_SURFACE;
  private static final BlackVolatilitySurface SABR_SURFACE;
  /**
   * 
   */
  static {
    ALPHA = ATM_VOL * Math.pow(SPOT, 1 - BETA);

    final Function<Double, Double> sabrSurface = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final SABRFormulaData sabrdata = new SABRFormulaData(SPOT * Math.exp(RATE * t), ALPHA, BETA, NU, RHO);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<SABRFormulaData, Double> func = SABR.getVolatilityFunction(option);
        return func.evaluate(sabrdata);
      }
    };

    SABR_SURFACE = new BlackVolatilitySurface(FunctionalDoublesSurface.from(sabrSurface));

    final BlackPriceFunction func = new BlackPriceFunction();

    final Function<Double, Double> priceSurface = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final double sigma = sabrSurface.evaluate(x);
        final double df = Math.exp(-RATE * t);
        final BlackFunctionData data = new BlackFunctionData(SPOT / df, df, sigma);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<BlackFunctionData, Double> pfunc = func.getPriceFunction(option);
        final double price = pfunc.evaluate(data);
        if (Double.isNaN(price)) {
          System.out.println("fuck");
        }
        return price;
      }
    };

    PRICE_SURFACE = new PriceSurface(FunctionalDoublesSurface.from(priceSurface));

  }

  @SuppressWarnings("deprecation")
  @Test
  public void debugTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();

    final double t = 3.0;
    final double f = 0.04;
    cal.debug(PRICE_SURFACE, SABR_SURFACE, SPOT, RATE, t, f);
  }

  @Test(enabled = false)
  public void printPriceTest() {

    double t;
    double k;
    double price;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      k = 0.001 + 0.15 * i / 100.0;
      System.out.print(k);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        price = PRICE_SURFACE.getPrice(t, k);
        System.out.print("\t" + price);
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void priceTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    final LocalVolatilitySurface locVol = cal.getLocalVolatility(PRICE_SURFACE, SPOT, RATE);
    double t;
    double f;
    double vol;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      f = 0.001 + 0.15 * i / 100.0;
      System.out.print(f);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        vol = locVol.getVolatility(t, f);
        System.out.print("\t" + vol);
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void volTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    final LocalVolatilitySurface locVol = cal.getLocalVolatility(SABR_SURFACE, SPOT, RATE);
    double t;
    double f;
    double vol;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      f = 0.001 + 0.15 * i / 100.0;
      System.out.print(f);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        vol = locVol.getVolatility(t, f);
        System.out.print("\t" + vol);
      }
      System.out.print("\n");
    }
  }

}
