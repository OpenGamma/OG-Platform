/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_SLOW)
public class BlackScholesMertonPDEPricerTest {
  private static final BjerksundStenslandModel AMERICAN_APPOX_PRCIER = new BjerksundStenslandModel();
  private static final BlackScholesMertonPDEPricer PRICER = new BlackScholesMertonPDEPricer();

  /**
   * This tests that the run time scales as the grid size for moderately sized grids. The grid size is the total number of nodes in the grid (#space nodes x #time nodes)
   * and moderately sized means between 1e5 and 1e9 nodes (e.g. from 100 time nodes and 1000 space nodes to 1e4 time and 1e5 space nodes).
   * We also test that the relative error is inversely proportional to the grid size; the constant of proportionality depends on nu (the ration of space nodes to time nodes) and
   * is lowest for nu = 125 in this case.
   */
  @Test
  public void speedAccuracyTest() {
    final double s0 = 10.0;
    final double k = 13.0;
    final double r = 0.06;
    final double b = 0.04;
    final double t = 1.75;
    final double sigma = 0.5;
    final boolean isCall = true;

    // warm-up
    double nu = 125;
    int tSteps = 100;
    int sSteps = (int) (nu * tSteps);
    double pdePDE = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);
    nu = 125;

    //   double scale = 0;
    for (int i = 0; i < 5; i++) {
      tSteps = (int) (100 + Math.pow(10.0, (i + 6.0) / 4.0));
      sSteps = (int) (nu * tSteps);
      final double size = tSteps * sSteps;

      final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
      //      double startTime = System.nanoTime();
      pdePDE = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);
      //      double endTime = System.nanoTime();
      //     double duration = endTime - startTime; // in ns

      final double relErr = Math.abs(1 - pdePDE / bsPrice);
      // System.out.println(tSteps + "\t" + sSteps + "\t" + duration + "\t" + ((double) tSteps) * sSteps + "\t" + relErr);

      //Removed because fails on bamboo
      // check correct scaling of run time with grid size
      //      if (i == 0) {
      //        scale = duration / size; // time per node (ns/node)
      //      } else {
      //        assertTrue("runtime not scaling linearly with grid size\t" + duration / size + "\t" + scale, Math.abs(duration / size - scale) < 0.4 * scale);
      //      }

      // check correct scaling of error with grid size
      final double logErrorCeil = Math.log10(1.25 / size);
      final double logRelError = Math.log10(relErr);
      assertTrue("error exceeds ceiling " + logRelError + "\t" + logErrorCeil, logRelError < logErrorCeil);
      assertEquals("error smaller than expected", logErrorCeil, logRelError, 1e-1);
    }
  }

  /**
   * Test that a wide range of European options price to reasonable accuracy on a moderately sized grid
   */
  @Test
  public void europeanTest() {
    final double s0 = 10.0;
    final double[] kSet = {7.0, 9.0, 10.0, 13.0, 17.0 };
    final double[] rSet = {0.0, 0.04, 0.2 };
    final double[] qSet = {-0.05, 0.0, 0.1 };
    final double[] tSet = {0.1, 2.0 };
    final double sigma = 0.3;
    final boolean[] isCallSet = {true, false };

    final int tSteps = 100;
    final int nu = 80;
    final int sSteps = nu * tSteps;

    for (final double k : kSet) {
      for (final double r : rSet) {
        for (final double q : qSet) {
          final double b = r - q;
          for (final double t : tSet) {
            for (final boolean isCall : isCallSet) {
              final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
              final double pdePDE = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);
              final double absErr = Math.abs(pdePDE - bsPrice);
              final double relErr = Math.abs(1 - pdePDE / bsPrice);
              // System.out.println(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + bsPrice + "\t" + pdePDE + "\t" + absErr + "\t" + relErr);
              if (k < 17.0) {
                assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + bsPrice + "\t" + pdePDE + "\t" + absErr + "\t" + relErr, absErr < 3e-5);
              } else {
                assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + bsPrice + "\t" + pdePDE + "\t" + absErr + "\t" + relErr, absErr < 1e-4);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Test that a wide range of American options price to within the accuracy of the Bjerksund-Stensland approximation on a moderately sized grid
   */
  @Test(enabled = false)
  public void americanTest() {
    final double s0 = 10.0;
    final double[] kSet = {7.0, 9.0, 10.0, 13.0, 17.0 };
    final double[] rSet = {0.0, 0.04, 0.2 };
    final double[] qSet = {-0.05, 0.0, 0.1 };
    final double[] tSet = {0.05, 0.25 };
    final double sigma = 0.3;
    final boolean[] isCallSet = {true, false };

    // The Bjerksund-Stensland approximation is not that accurate, so there is no point using a fine grid for this test
    final int tSteps = 80;
    final int nu = 20;
    final int sSteps = nu * tSteps;

    for (final double k : kSet) {
      for (final double r : rSet) {
        for (final double q : qSet) {
          final double b = r - q;
          for (final double t : tSet) {
            for (final boolean isCall : isCallSet) {
              final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
              final double amAprox = AMERICAN_APPOX_PRCIER.price(s0, k, r, b, t, sigma, isCall);
              final double pdePrice = PRICER.price(s0, k, r, b, t, sigma, isCall, true, sSteps, tSteps);

              // Bjerksund-Stensland approximation set a lower limit for the price of an American option, thus the PDE price should always exceed it
              assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + bsPrice + "\t" + amAprox + "\t" + pdePrice, (pdePrice - amAprox) > -5e-6);

              final double absErr = Math.abs(pdePrice - amAprox);
              final double relErr = Math.abs(1 - pdePrice / amAprox);

              // System.out.println(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + amAprox + "\t" + pdePDE + "\t" + absErr + "\t" + relErr);
              assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + amAprox + "\t" + pdePrice + "\t" + absErr + "\t" + relErr, absErr < 1e-2);
            }
          }
        }
      }
    }
  }

  @Test
  public void nonuniformGridTest() {
    final double s0 = 10.0;
    final double k = 13.0;
    final double r = 0.06;
    final double b = 0.04;
    final double t = 1.75;
    final double sigma = 0.5;
    final boolean isCall = false;

    final double beta = 0.01;
    final double lambda = 0.0;
    final double sd = 6.0;

    final int tSteps = 100;
    final double nu = 10;
    final int sSteps = (int) (nu * tSteps);

    final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
    final double pdePrice1 = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);
    final double pdePrice2 = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps, beta, lambda, sd);
    final double relErr1 = Math.abs(pdePrice1 / bsPrice - 1.0);
    final double relErr2 = Math.abs(pdePrice2 / bsPrice - 1.0);
    //System.out.println(tSteps + "\t" + sSteps + "\t" + ((double) tSteps) * sSteps + "\t" + bsPrice + "\t" + pdePrice1 + "\t" + pdePrice2 + "\t" + relErr1 + "\t" + relErr2);

    assertEquals(0, relErr1, 5e-4);
    assertEquals(0, relErr2, 2e-6); // much better accuracy with non-uniform
  }

  @Test(enabled = false)
  public void optNuTest() {
    final double s0 = 10.0;
    final double k = 13.0;
    final double r = 0.06;
    final double b = 0.04;
    final double t = 1.75;
    final double sigma = 0.5;
    final boolean isCall = true;

    // warm-up
    final double nu = 80;
    int tSteps = 100;
    int sSteps = (int) (nu * tSteps);
    double pdePDE = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);

    final double[] nuSet = new double[] {1, 2, 5, 10, 20, 40, 60, 80, 100, 125, 150, 200, 500 };
    final int n = (int) 1e9;
    for (final double nu1 : nuSet) {
      tSteps = (int) Math.sqrt(n / nu1);
      sSteps = n / tSteps;
      final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
      final double startTime = System.nanoTime() / 1e6;
      pdePDE = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);
      final double endTime = System.nanoTime() / 1e6;
      final double duration = endTime - startTime;

      final double relErr = Math.abs(1 - pdePDE / bsPrice);
      System.out.println(tSteps + "\t" + sSteps + "\t" + duration + "\t" + nu1 + "\t" + relErr);
    }

  }

  @Test(enabled = false)
  public void unifromGridAmericanCallTest() {
    final double s0 = 90;
    final double k = 100;
    final double r = -1e-5;
    final double b = 0.04;
    final double sigma = 0.35;
    final double t = 0.5;
    final boolean isCall = false;
    // final double r = 1e-6 + b - 0.5 * (2 * b + sigma * sigma) * (2 * b + sigma * sigma) / 4 / sigma / sigma;
    System.out.println(r + "\t" + b + "\t" + -0.5 * sigma * sigma * (b / sigma / sigma - 0.5) * (b / sigma / sigma - 0.5));

    final int tSteps = 100;
    final int nu = 80;
    final int sSteps = nu * tSteps;

    // for (int i = 0; i < 50; i++) {
    // r = -0.08 + 0.1 * i / 49.0;
    // b = -0.02 + 0.12 * i / 49.0;
    // b = 0.061;

    final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
    final double bsPrice2 = Math.exp(-(r - b) * t) * BlackFormulaRepository.price(k * Math.exp(-b * t), s0, t, sigma, !isCall);
    final double amAprox = AMERICAN_APPOX_PRCIER.price(s0, k, r, b, t, sigma, isCall);
    final double pdePrice = PRICER.price(s0, k, r, b, t, sigma, isCall, true, sSteps, tSteps);
    final double pdePricePC = 0.0; // PRICER.price(k, s0, r - b, -b, t, sigma, !isCall, true, sSteps, tSteps);

    System.out.println(b + "\t" + bsPrice + "\t" + bsPrice2 + "\t" + amAprox + "\t" + pdePrice + "\t" + pdePricePC + "\t" + (1 - pdePrice / bsPrice));
  }

  @Test(enabled = false)
  public void debugTest() {
    System.out.println("BlackScholesMertonPDEPricerTest.debugTest");
    final BjerksundStenslandModel amPricer = new BjerksundStenslandModel();
    final BlackScholesMertonPDEPricer pricer = new BlackScholesMertonPDEPricer(false);
    final double s0 = 10.0;
    final double k = 7.0;
    final double r = 0.2;
    final double b = 0.1;
    final double t = 2.0;
    final double sigma = 0.3;
    final boolean isCall = false;
    final boolean isAmerican = true;

    final double nu = 80;
    final int tSteps = 500;
    final int sSteps = (int) (nu * tSteps);
    // warm-up
    pricer.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);

    final double df = Math.exp(-r * t);
    final double fwd = s0 * Math.exp(b * t);
    final double bsPrice = df * BlackFormulaRepository.price(fwd, k, t, sigma, isCall);
    final double startTime = System.nanoTime() / 1e6;
    final double pdePrice = pricer.price(s0, k, r, b, t, sigma, isCall, isAmerican, sSteps, tSteps);
    final double endTime = System.nanoTime() / 1e6;
    final double duration = endTime - startTime;


    final double amPrice = amPricer.price(s0, k, r, b, t, sigma, isCall);
    final double relErr = Math.abs(1 - pdePrice / amPrice);
    System.out.println(tSteps + "\t" + sSteps + "\t" + duration + df * fwd + "\t" + amPrice + "\t" + pdePrice + "\t" + relErr);

  }
}
// }
