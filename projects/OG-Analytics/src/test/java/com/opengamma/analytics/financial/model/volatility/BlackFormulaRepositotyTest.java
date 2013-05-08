/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;


public class BlackFormulaRepositotyTest {

  private static final double TIME_TO_EXPIRY = 4.5;
  private static final double FORWARD = 104;
  private static final double[] STRIKES_INPUT = new double[] {85.0, 90.0, 95.0, 100.0, 103.0, 108.0, 120.0, 150.0, 250.0};
  private static final double[] VOLS = new double[] {0.1, 0.12, 0.15, 0.2, 0.3, 0.5, 0.8};

  private static final double[][] PRE_COMPUTER_PRICES = new double[][] {
      {20.816241352493662, 21.901361401145017, 23.739999392248883, 27.103751052550102, 34.22506482807403, 48.312929458905, 66.87809290575849},
      {17.01547107842069, 18.355904456594594, 20.492964568435653, 24.216799858954104, 31.81781516125381, 46.52941355755593, 65.73985671517116},
      {13.655000481751557, 15.203913570037663, 17.57850003037605, 21.58860329455819, 29.58397731664536, 44.842632571211, 64.65045683512315},
      {10.76221357246159, 12.452317171280882, 14.990716295389468, 19.207654124402573, 27.51258894693435, 43.24555444486169, 63.606185385322505},
      {9.251680464551534, 10.990050517334176, 13.589326615797177, 17.892024398947207, 26.343236303647927, 42.327678792768694, 62.99989771948578},
      {7.094602606393259, 8.852863501660629, 11.492701186228047, 15.876921735149438, 24.50948746286295, 40.86105495729011, 62.02112426294542},
      {3.523029591534474, 5.0769317175689395, 7.551079210499658, 11.857770325364342, 20.641589813250427, 37.63447312094027, 59.81944968154744},
      {0.4521972353043875, 1.0637022636084144, 2.442608010436077, 5.613178543779881, 13.579915684294491, 31.040979917191127, 55.062112340600244},
      {1.328198130230618E-4, 0.0029567128738985232, 0.04468941116428932, 0.47558224046532205, 3.8091577630027356, 18.03481967011267, 43.99634090899799}};

  @Test
  public void zeroVolTest() {
    boolean isCall = true;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      double intrinic = Math.max(0, FORWARD - STRIKES_INPUT[i]);
      double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, 0.0, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void zeroExpiryTest() {
    boolean isCall = false;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      double intrinic = Math.max(0, STRIKES_INPUT[i] - FORWARD);
      double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], 0.0, 0.3, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void tinyVolTest() {
    final double vol = 1e-4;
    boolean isCall = true;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      double intrinic = Math.max(0, FORWARD - STRIKES_INPUT[i]);
      double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, vol, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void shortExpiryTest() {
    double t = 1e-5;
    double vol = 0.4;
    boolean isCall = false;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      double intrinic = Math.max(0, STRIKES_INPUT[i] - FORWARD);
      double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], t, vol, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void massiveVolTest() {
    final double vol = 8.0; // 800% vol
    boolean isCall = true;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, vol, isCall);
      assertEquals(FORWARD, price, 1e-15);
    }
  }

  @Test
  public void zeroStikeTest() {
    boolean isCall = true;
    final int n = VOLS.length;
    for (int i = 0; i < n; i++) {
      double price = BlackFormulaRepository.price(FORWARD, 0.0, TIME_TO_EXPIRY, VOLS[i], isCall);
      assertEquals(FORWARD, price, 1e-15);
    }
  }

  @Test
  public void putCallParityTest() {
    final int n = VOLS.length;
    final int m = STRIKES_INPUT.length;
    for (int i = 0; i < m; i++) {
      double fk = FORWARD - STRIKES_INPUT[i];
      for (int j = 0; j < n; j++) {
        double call = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true);
        double put = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], false);
        assertEquals(fk, call - put, 1e-13);
      }
    }
  }

  @Test
  public void nonEdgeCaseTest() {
    final boolean print = false;
    if (print) {
      System.out.println("BlackFormulaRepositotyTest");
    }
    final boolean isCall = true;
    final int n = VOLS.length;
    final int m = STRIKES_INPUT.length;
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], isCall);
        assertEquals(PRE_COMPUTER_PRICES[i][j],price,1e-18*price);
        if (print) {
          if (j == 0) {
            System.out.print(price);
          } else {
            System.out.print(", " + price);
          }
        }
      }
      if (print) {
        System.out.print("\n");
      }
    }
  }

  @Test
  /**
   * Tests the strikes in a range of strikes, volatilities and call/put.
   */
  public void impliedStrike() {
    BlackPriceFunction function = new BlackPriceFunction();
    final int nbStrike = STRIKES_INPUT.length;
    final int nbVols = VOLS.length;
    // double[][] delta = new double[2][nbStrike];
    // double[][] strikeOutput = new double[2][nbStrike];
    boolean callput = false;
    for (int loopcall = 0; loopcall < 2; loopcall++) {
      callput = !callput;
      for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
        for (int loopVols = 0; loopVols < nbVols; loopVols++) {
          EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKES_INPUT[loopstrike], TIME_TO_EXPIRY, callput);
          BlackFunctionData data = new BlackFunctionData(FORWARD, 1.0, VOLS[loopVols]);
          double[] d = function.getPriceAdjoint(option, data);
          double delta = d[1];
          double strikeOutput = BlackFormulaRepository.impliedStrike(delta, callput, FORWARD, TIME_TO_EXPIRY, VOLS[loopVols]);
          assertEquals("Implied strike: (data " + loopstrike + " / " + callput + ")", STRIKES_INPUT[loopstrike], strikeOutput, 1.0E-8);
        }
      }
    }
  }

  // TODO: test the conditions.

  @Test
  /**
   * Tests the strikes in a range of strikes, volatilities and call/put.
   */
  public void impliedStrikeDerivatives() {
    double[] delta = new double[] {0.25, -0.25, 0.49};
    boolean[] cap = new boolean[] {true, false, true};
    double[] forward = new double[] {104, 100, 10};
    double[] time = new double[] {2.5, 5.0, 0.5};
    double[] vol = new double[] {0.25, 0.10, 0.50};
    double shift = 0.000001;
    double shiftF = 0.001;
    double[] derivatives = new double[4];
    for (int loop = 0; loop < delta.length; loop++) {
      double strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop], derivatives);
      double strikeD = BlackFormulaRepository.impliedStrike(delta[loop] + shift, cap[loop], forward[loop], time[loop], vol[loop]);
      assertEquals("Implied strike: derivative delta", (strikeD - strike) / shift, derivatives[0], 1.0E-3);
      double strikeF = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop] + shiftF, time[loop], vol[loop]);
      assertEquals("Implied strike: derivative forward", (strikeF - strike) / shiftF, derivatives[1], 1.0E-5);
      double strikeT = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop] + shift, vol[loop]);
      assertEquals("Implied strike: derivative time", (strikeT - strike) / shift, derivatives[2], 1.0E-4);
      double strikeV = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop] + shift);
      assertEquals("Implied strike: derivative volatility", (strikeV - strike) / shift, derivatives[3], 1.0E-3);
    }
  }

  @Test(enabled = false)
  /**
   * Assess the performance of the derivatives computation.
   */
  public void impliedStrikePerformanceDerivatives() {
    double[] delta = new double[] {0.25, -0.25, 0.49};
    boolean[] cap = new boolean[] {true, false, true};
    double[] forward = new double[] {104, 100, 10};
    double[] time = new double[] {2.5, 5.0, 0.5};
    double[] vol = new double[] {0.25, 0.10, 0.50};
    double[] derivatives = new double[4];

    long startTime, endTime;
    int nbTest = 100000;
    @SuppressWarnings("unused")
    double strike;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loop = 0; loop < delta.length; loop++) {
        strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop]);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " implied strike: " + (endTime - startTime) + " ms");
    // Performance note: strike: 18-Jul-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 70 ms for 100000.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      for (int loop = 0; loop < delta.length; loop++) {
        strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop], derivatives);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " implied strike + derivatives : " + (endTime - startTime) + " ms");
    // Performance note: strike+derivatives: 18-Jul-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 80 ms for 100000.
  }

  // @Test
  // public void debugTest() {
  // System.out.println("debug test of American put");
  //
  // double spot = 520.15;
  // double df = 0.997669333179294;
  // double fv = 521.365128406249;
  // double k = 945.0;
  // double t = 0.769863014;
  //
  // double r = -Math.log(df)/t;
  // double q = 0.0;
  //
  // double fwd = spot*Math.exp(t*(r-q));
  //
  //
  // double vol = BlackFormulaRepository.impliedVolatility(fv, fwd, k, t, false);
  // System.out.println(vol);
  //
  // }

}
