/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlackFormulaRepositoryTest {

  private static final double EPS = 1.e-10;
  private static final double DELTA = 1.e-6;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final double TIME_TO_EXPIRY = 4.5;
  private static final double FORWARD = 104;
  private static final double[] STRIKES_INPUT = new double[] {85.0, 90.0, 95.0, 100.0, 103.0, 108.0, 120.0, 150.0,
    250.0 };
  private static final double[] VOLS = new double[] {0.1, 0.12, 0.15, 0.2, 0.3, 0.5, 0.8 };

  private static final double[][] PRE_COMPUTER_PRICES = new double[][] {
    {20.816241352493662, 21.901361401145017, 23.739999392248883, 27.103751052550102, 34.22506482807403,
      48.312929458905, 66.87809290575849 },
    {17.01547107842069, 18.355904456594594, 20.492964568435653, 24.216799858954104, 31.81781516125381,
      46.52941355755593, 65.73985671517116 },
    {13.655000481751557, 15.203913570037663, 17.57850003037605, 21.58860329455819, 29.58397731664536, 44.842632571211,
      64.65045683512315 },
    {10.76221357246159, 12.452317171280882, 14.990716295389468, 19.207654124402573, 27.51258894693435,
      43.24555444486169, 63.606185385322505 },
    {9.251680464551534, 10.990050517334176, 13.589326615797177, 17.892024398947207, 26.343236303647927,
      42.327678792768694, 62.99989771948578 },
    {7.094602606393259, 8.852863501660629, 11.492701186228047, 15.876921735149438, 24.50948746286295,
      40.86105495729011, 62.02112426294542 },
    {3.523029591534474, 5.0769317175689395, 7.551079210499658, 11.857770325364342, 20.641589813250427,
      37.63447312094027, 59.81944968154744 },
    {0.4521972353043875, 1.0637022636084144, 2.442608010436077, 5.613178543779881, 13.579915684294491,
      31.040979917191127, 55.062112340600244 },
    {1.328198130230618E-4, 0.0029567128738985232, 0.04468941116428932, 0.47558224046532205, 3.8091577630027356,
      18.03481967011267, 43.99634090899799 } };

  @Test
  public void zeroVolTest() {
    final boolean isCall = true;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      final double intrinic = Math.max(0, FORWARD - STRIKES_INPUT[i]);
      final double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, 0.0, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void zeroExpiryTest() {
    final boolean isCall = false;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      final double intrinic = Math.max(0, STRIKES_INPUT[i] - FORWARD);
      final double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], 0.0, 0.3, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void tinyVolTest() {
    final double vol = 1e-4;
    final boolean isCall = true;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      final double intrinic = Math.max(0, FORWARD - STRIKES_INPUT[i]);
      final double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, vol, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void shortExpiryTest() {
    final double t = 1e-5;
    final double vol = 0.4;
    final boolean isCall = false;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      final double intrinic = Math.max(0, STRIKES_INPUT[i] - FORWARD);
      final double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], t, vol, isCall);
      assertEquals(intrinic, price, 1e-15);
    }
  }

  @Test
  public void massiveVolTest() {
    final double vol = 8.0; // 800% vol
    final boolean isCall = true;
    final int n = STRIKES_INPUT.length;
    for (int i = 0; i < n; i++) {
      final double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, vol, isCall);
      assertEquals(FORWARD, price, 1e-15);
    }
  }

  @Test
  public void zeroStikeTest() {
    final boolean isCall = true;
    final int n = VOLS.length;
    for (int i = 0; i < n; i++) {
      final double price = BlackFormulaRepository.price(FORWARD, 0.0, TIME_TO_EXPIRY, VOLS[i], isCall);
      assertEquals(FORWARD, price, 1e-15);
    }
  }

  @Test
  public void putCallParityTest() {
    final int n = VOLS.length;
    final int m = STRIKES_INPUT.length;
    for (int i = 0; i < m; i++) {
      final double fk = FORWARD - STRIKES_INPUT[i];
      for (int j = 0; j < n; j++) {
        final double call = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true);
        final double put = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], false);
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
        final double price = BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], isCall);
        assertEquals(PRE_COMPUTER_PRICES[i][j], price, 1e-13 * price);
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
    final BlackPriceFunction function = new BlackPriceFunction();
    final int nbStrike = STRIKES_INPUT.length;
    final int nbVols = VOLS.length;
    // double[][] delta = new double[2][nbStrike];
    // double[][] strikeOutput = new double[2][nbStrike];
    boolean callput = false;
    for (int loopcall = 0; loopcall < 2; loopcall++) {
      callput = !callput;
      for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
        for (int loopVols = 0; loopVols < nbVols; loopVols++) {
          final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKES_INPUT[loopstrike], TIME_TO_EXPIRY,
              callput);
          final BlackFunctionData data = new BlackFunctionData(FORWARD, 1.0, VOLS[loopVols]);
          final double[] d = function.getPriceAdjoint(option, data);
          final double delta = d[1];
          final double strikeOutput = BlackFormulaRepository.impliedStrike(delta, callput, FORWARD, TIME_TO_EXPIRY,
              VOLS[loopVols]);
          assertEquals("Implied strike: (data " + loopstrike + " / " + callput + ")", STRIKES_INPUT[loopstrike],
              strikeOutput, 1.0E-8);
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
    final double[] delta = new double[] {0.25, -0.25, 0.49 };
    final boolean[] cap = new boolean[] {true, false, true };
    final double[] forward = new double[] {104, 100, 10 };
    final double[] time = new double[] {2.5, 5.0, 0.5 };
    final double[] vol = new double[] {0.25, 0.10, 0.50 };
    final double shift = 0.000001;
    final double shiftF = 0.001;
    final double[] derivatives = new double[4];
    for (int loop = 0; loop < delta.length; loop++) {
      final double strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop],
          vol[loop], derivatives);
      final double strikeD = BlackFormulaRepository.impliedStrike(delta[loop] + shift, cap[loop], forward[loop],
          time[loop], vol[loop]);
      assertEquals("Implied strike: derivative delta", (strikeD - strike) / shift, derivatives[0], 1.0E-3);
      final double strikeF = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop] + shiftF,
          time[loop], vol[loop]);
      assertEquals("Implied strike: derivative forward", (strikeF - strike) / shiftF, derivatives[1], 1.0E-5);
      final double strikeT = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop] +
          shift, vol[loop]);
      assertEquals("Implied strike: derivative time", (strikeT - strike) / shift, derivatives[2], 1.0E-4);
      final double strikeV = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop],
          vol[loop] + shift);
      assertEquals("Implied strike: derivative volatility", (strikeV - strike) / shift, derivatives[3], 1.0E-3);
    }
  }

  @Test(enabled = false)
  /**
   * Assess the performance of the derivatives computation.
   */
  public void impliedStrikePerformanceDerivatives() {
    final double[] delta = new double[] {0.25, -0.25, 0.49 };
    final boolean[] cap = new boolean[] {true, false, true };
    final double[] forward = new double[] {104, 100, 10 };
    final double[] time = new double[] {2.5, 5.0, 0.5 };
    final double[] vol = new double[] {0.25, 0.10, 0.50 };
    final double[] derivatives = new double[4];

    long startTime, endTime;
    final int nbTest = 100000;
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
        strike = BlackFormulaRepository.impliedStrike(delta[loop], cap[loop], forward[loop], time[loop], vol[loop],
            derivatives);
      }
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " implied strike + derivatives : " + (endTime - startTime) + " ms");
    // Performance note: strike+derivatives: 18-Jul-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 80 ms for 100000.
  }

  @Test
  public void debugTest() {
    SimpleOptionData[] options = new SimpleOptionData[3];
    options[0] = new SimpleOptionData(0.00311372151156597, 0.035, 0.25, 0.24984014969067805, true);
    options[1] = new SimpleOptionData(0.003390446347019349, 0.035, 0.5, 0.24975245913931368, true);
    options[2] = new SimpleOptionData(0.0035037780530577933, 0.035, 0.75, 0.24965389467634747, true);
    double price = 4.378854241284628E-17;
    for (int i = 0; i < 20; i++) {
      double vol = 0.3 + 0.01 * i;
      System.out.println(vol + "\t" + BlackFormulaRepository.price(options, vol));

    }
    double iv = BlackFormulaRepository.impliedVolatility(options, price);
    System.out.println(iv);
  }

  /*
   * 
   * 
   * New tests added
   */
  /**
   * finite difference vs greek methods
   */
  @Test
  public void greeksTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    final double[] upStrikes = new double[nStrikes];
    final double[] dwStrikes = new double[nStrikes];
    final double upFwd = FORWARD * (1. + DELTA);
    final double dwFwd = FORWARD * (1. - DELTA);
    final double upTime = TIME_TO_EXPIRY * (1. + DELTA);
    final double dwTime = TIME_TO_EXPIRY * (1. - DELTA);
    final double[] upVOLS = new double[nVols];
    final double[] dwVOLS = new double[nVols];
    for (int i = 0; i < nStrikes; ++i) {
      upStrikes[i] = STRIKES_INPUT[i] * (1. + DELTA);
      dwStrikes[i] = STRIKES_INPUT[i] * (1. - DELTA);
    }
    for (int i = 0; i < nVols; ++i) {
      upVOLS[i] = VOLS[i] * (1. + DELTA);
      dwVOLS[i] = VOLS[i] * (1. - DELTA);
    }
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double finDeltaC = (BlackFormulaRepository.price(upFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true) - BlackFormulaRepository
            .price(dwFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true)) / 2. / FORWARD / DELTA;
        final double finDeltaP = (BlackFormulaRepository.price(upFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], false) - BlackFormulaRepository
            .price(dwFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], false)) / 2. / FORWARD / DELTA;
        assertEquals(finDeltaC, BlackFormulaRepository.delta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true),
            Math.abs(finDeltaC) * DELTA);
        assertEquals(finDeltaP,
            BlackFormulaRepository.delta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], false),
            Math.abs(finDeltaP) * DELTA);

        final double finDualDeltaC = (BlackFormulaRepository
            .price(FORWARD, upStrikes[i], TIME_TO_EXPIRY, VOLS[j], true) - BlackFormulaRepository.price(FORWARD,
            dwStrikes[i], TIME_TO_EXPIRY, VOLS[j], true)) /
            2. / STRIKES_INPUT[i] / DELTA;
        final double finDualDeltaP = (BlackFormulaRepository.price(FORWARD, upStrikes[i], TIME_TO_EXPIRY, VOLS[j],
            false) - BlackFormulaRepository.price(FORWARD, dwStrikes[i], TIME_TO_EXPIRY, VOLS[j], false)) /
            2. /
            STRIKES_INPUT[i] / DELTA;
        assertEquals(finDualDeltaC,
            BlackFormulaRepository.dualDelta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true),
            Math.abs(finDualDeltaC) * DELTA);
        assertEquals(finDualDeltaP,
            BlackFormulaRepository.dualDelta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], false),
            Math.abs(finDualDeltaP) * DELTA);

        final double finGamma = (BlackFormulaRepository.delta(upFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true) - BlackFormulaRepository
            .delta(dwFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true)) / 2. / FORWARD / DELTA;
        assertEquals(finGamma, BlackFormulaRepository.gamma(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finGamma) * DELTA);

        final double finDualGamma = (BlackFormulaRepository.dualDelta(FORWARD, upStrikes[i], TIME_TO_EXPIRY, VOLS[j],
            true) - BlackFormulaRepository.dualDelta(FORWARD, dwStrikes[i], TIME_TO_EXPIRY, VOLS[j], true)) /
            2. /
            STRIKES_INPUT[i] / DELTA;
        assertEquals(finDualGamma,
            BlackFormulaRepository.dualGamma(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finDualGamma) * DELTA);

        final double finCrossGamma = (BlackFormulaRepository.dualDelta(upFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY,
            VOLS[j], true) - BlackFormulaRepository.dualDelta(dwFwd, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j], true)) /
            2. / FORWARD / DELTA;
        assertEquals(finCrossGamma,
            BlackFormulaRepository.crossGamma(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finCrossGamma) * DELTA);

        final double finThetaC = -(BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[i], upTime, VOLS[j], true) - BlackFormulaRepository
            .price(FORWARD, STRIKES_INPUT[i], dwTime, VOLS[j], true)) / 2. / TIME_TO_EXPIRY / DELTA;
        assertEquals(finThetaC,
            BlackFormulaRepository.driftlessTheta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finThetaC) * DELTA);

        final double finVega = (BlackFormulaRepository
            .price(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, upVOLS[j], true) - BlackFormulaRepository.price(FORWARD,
            STRIKES_INPUT[i], TIME_TO_EXPIRY, dwVOLS[j], true)) /
            2. / VOLS[j] / DELTA;
        assertEquals(finVega, BlackFormulaRepository.vega(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finVega) * DELTA);

        final double finVanna = (BlackFormulaRepository.delta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, upVOLS[j],
            true) - BlackFormulaRepository.delta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, dwVOLS[j], true)) /
            2. /
            VOLS[j] / DELTA;
        assertEquals(finVanna, BlackFormulaRepository.vanna(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finVanna) * DELTA);

        final double finDualVanna = (BlackFormulaRepository.dualDelta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY,
            upVOLS[j], true) - BlackFormulaRepository.dualDelta(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, dwVOLS[j],
            true)) /
            2. / VOLS[j] / DELTA;
        assertEquals(finDualVanna,
            BlackFormulaRepository.dualVanna(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finDualVanna) * DELTA);

        final double finVomma = (BlackFormulaRepository.vega(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, upVOLS[j]) - BlackFormulaRepository
            .vega(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, dwVOLS[j])) / 2. / VOLS[j] / DELTA;
        assertEquals(finVomma, BlackFormulaRepository.vomma(FORWARD, STRIKES_INPUT[i], TIME_TO_EXPIRY, VOLS[j]),
            Math.abs(finVomma) * DELTA);
      }
    }

  }

  /*
   * 
   * 
   * Tests for "price" method
   */
  /**
   * Large/small values for price
   */
  @Test
  public void exPriceTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.price(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC1 = BlackFormulaRepository.price(0., strike, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.price(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.price(inf, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.price(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP1 = BlackFormulaRepository.price(0., strike, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.price(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.price(inf, strike, TIME_TO_EXPIRY, vol, false);
        assertEquals(0., resC1, EPS);
        assertEquals(1.e12 * strike - strike, resC2, EPS * 1.e12 * strike);
        assertEquals(strike - 1.e-12 * strike, resP1, EPS * strike);
        assertEquals(0., resP2, EPS * strike);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.price(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.price(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.price(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.price(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, false);
        final double refC1 = BlackFormulaRepository.price(forward, 0., TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.price(forward, inf, TIME_TO_EXPIRY, vol, true);
        final double refP1 = BlackFormulaRepository.price(forward, 0., TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.price(forward, inf, TIME_TO_EXPIRY, vol, false);
        assertEquals(forward, resC1, forward * EPS);
        assertEquals(0., resC2, EPS);
        assertEquals(1.e12 * forward, resP2, 1.e12 * forward * EPS);
        assertEquals(0., resP1, EPS);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.price(FORWARD, strike, 1e-24, vol, true);
        final double resC2 = BlackFormulaRepository.price(FORWARD, strike, 1e24, vol, true);
        final double resP1 = BlackFormulaRepository.price(FORWARD, strike, 1e-24, vol, false);
        final double resP2 = BlackFormulaRepository.price(FORWARD, strike, 1e24, vol, false);
        final double refC1 = BlackFormulaRepository.price(FORWARD, strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.price(FORWARD, strike, inf, vol, true);
        final double refP1 = BlackFormulaRepository.price(FORWARD, strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.price(FORWARD, strike, inf, vol, false);
        assertEquals(FORWARD > strike ? FORWARD - strike : 0., resC1, EPS);
        assertEquals(FORWARD, resC2, FORWARD * EPS);
        assertEquals(strike, resP2, strike * EPS);
        assertEquals(FORWARD > strike ? 0. : -FORWARD + strike, resP1, EPS);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double refC1 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, 0., true);
      final double resC2 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double refC2 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, inf, true);
      final double resP1 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double refP1 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, 0., false);
      final double resP2 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double refP2 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, inf, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.price(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resC2 = BlackFormulaRepository.price(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resC3 = BlackFormulaRepository.price(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resP1 = BlackFormulaRepository.price(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resP2 = BlackFormulaRepository.price(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false);
      final double resP3 = BlackFormulaRepository.price(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resC4 = BlackFormulaRepository.price(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resP4 = BlackFormulaRepository.price(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false);

      final double refC1 = BlackFormulaRepository.price(0., 0., TIME_TO_EXPIRY, vol, true);
      final double refC2 = BlackFormulaRepository.price(0., inf, TIME_TO_EXPIRY, vol, true);
      final double refC3 = BlackFormulaRepository.price(inf, 0., TIME_TO_EXPIRY, vol, true);
      final double refP1 = BlackFormulaRepository.price(0., 0., TIME_TO_EXPIRY, vol, false);
      final double refP2 = BlackFormulaRepository.price(0., inf, TIME_TO_EXPIRY, vol, false);
      final double refP3 = BlackFormulaRepository.price(inf, 0., TIME_TO_EXPIRY, vol, false);
      final double refC4 = BlackFormulaRepository.price(inf, inf, TIME_TO_EXPIRY, vol, true);
      final double refP4 = BlackFormulaRepository.price(inf, inf, TIME_TO_EXPIRY, vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.price(1.e-12, strike, 1.e-12, vol, true);
        final double resC2 = BlackFormulaRepository.price(1.e-12, strike, 1.e12, vol, true);
        final double resC3 = BlackFormulaRepository.price(1.e12, strike, 1.e-12, vol, true);
        final double resP1 = BlackFormulaRepository.price(1.e-12, strike, 1.e-12, vol, false);
        final double resP2 = BlackFormulaRepository.price(1.e-12, strike, 1.e12, vol, false);
        final double resP3 = BlackFormulaRepository.price(1.e12, strike, 1.e-12, vol, false);
        final double resC4 = BlackFormulaRepository.price(1.e12, strike, 1.e24, vol, true);
        final double resP4 = BlackFormulaRepository.price(1.e12, strike, 1.e24, vol, false);

        final double refC1 = BlackFormulaRepository.price(0., strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.price(0., strike, inf, vol, true);
        final double refC3 = BlackFormulaRepository.price(inf, strike, 0., vol, true);
        final double refP1 = BlackFormulaRepository.price(0., strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.price(0., strike, inf, vol, false);
        final double refP3 = BlackFormulaRepository.price(inf, strike, 0., vol, false);
        final double refC4 = BlackFormulaRepository.price(inf, strike, inf, vol, true);
        final double refP4 = BlackFormulaRepository.price(inf, strike, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.price(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.price(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.price(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.price(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.price(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.price(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC4 = BlackFormulaRepository.price(1.e12, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double resP4 = BlackFormulaRepository.price(1.e12, strike, TIME_TO_EXPIRY, 1.e12, false);

      final double refC1 = BlackFormulaRepository.price(0., strike, TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.price(0., strike, TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.price(inf, strike, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.price(0., strike, TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.price(0., strike, TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.price(inf, strike, TIME_TO_EXPIRY, 0., false);
      final double refC4 = BlackFormulaRepository.price(inf, strike, TIME_TO_EXPIRY, inf, true);
      final double refP4 = BlackFormulaRepository.price(inf, strike, TIME_TO_EXPIRY, inf, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.price(forward, 1.e-12, 1.e-12, vol, true);
        final double resC2 = BlackFormulaRepository.price(forward, 1.e-12, 1.e12, vol, true);
        final double resC3 = BlackFormulaRepository.price(forward, 1.e12, 1.e-12, vol, true);
        final double resP1 = BlackFormulaRepository.price(forward, 1.e-12, 1.e-12, vol, false);
        final double resP2 = BlackFormulaRepository.price(forward, 1.e-12, 1.e12, vol, false);
        final double resP3 = BlackFormulaRepository.price(forward, 1.e12, 1.e-12, vol, false);
        final double resC4 = BlackFormulaRepository.price(forward, 1.e12, 1.e24, vol, true);
        final double resP4 = BlackFormulaRepository.price(forward, 1.e12, 1.e24, vol, false);

        final double refC1 = BlackFormulaRepository.price(forward, 0., 0., vol, true);
        final double refC2 = BlackFormulaRepository.price(forward, 0., inf, vol, true);
        final double refC3 = BlackFormulaRepository.price(forward, inf, 0., vol, true);
        final double refP1 = BlackFormulaRepository.price(forward, 0., 0., vol, false);
        final double refP2 = BlackFormulaRepository.price(forward, 0., inf, vol, false);
        final double refP3 = BlackFormulaRepository.price(forward, inf, 0., vol, false);
        final double refC4 = BlackFormulaRepository.price(forward, inf, inf, vol, true);
        final double refP4 = BlackFormulaRepository.price(forward, inf, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.price(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.price(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.price(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.price(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.price(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.price(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC4 = BlackFormulaRepository.price(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, true);
      final double resP4 = BlackFormulaRepository.price(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, false);

      final double refC1 = BlackFormulaRepository.price(forward, 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.price(forward, 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.price(forward, inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.price(forward, 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.price(forward, 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.price(forward, inf, TIME_TO_EXPIRY, 0., false);
      final double refC4 = BlackFormulaRepository.price(forward, inf, TIME_TO_EXPIRY, inf, true);
      final double refP4 = BlackFormulaRepository.price(forward, inf, TIME_TO_EXPIRY, inf, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.price(1.e-12, 1.e-12, 1.e-24, vol, true);
      final double resC2 = BlackFormulaRepository.price(1.e-12, 1.e-12, 1.e24, vol, true);
      final double resC3 = BlackFormulaRepository.price(1.e-12, 1.e12, 1.e-24, vol, true);
      final double resP1 = BlackFormulaRepository.price(1.e-12, 1.e-12, 1.e-24, vol, false);
      final double resP2 = BlackFormulaRepository.price(1.e-12, 1.e-12, 1.e24, vol, false);
      final double resP3 = BlackFormulaRepository.price(1.e-12, 1.e12, 1.e-24, vol, false);
      final double resC4 = BlackFormulaRepository.price(1.e12, 1.e-12, 1.e-24, vol, true);
      final double resP4 = BlackFormulaRepository.price(1.e12, 1.e-12, 1.e-24, vol, false);
      final double resC5 = BlackFormulaRepository.price(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, true);
      final double resP5 = BlackFormulaRepository.price(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, false);
      final double resC6 = BlackFormulaRepository.price(1.e12, 1.e12, 1.e24, vol, true);
      final double resP6 = BlackFormulaRepository.price(1.e12, 1.e12, 1.e24, vol, false);

      final double refC1 = BlackFormulaRepository.price(0., 0., 0., vol, true);
      final double refC2 = BlackFormulaRepository.price(0., 0., inf, vol, true);
      final double refC3 = BlackFormulaRepository.price(0., inf, 0., vol, true);
      final double refP1 = BlackFormulaRepository.price(0., 0., 0., vol, false);
      final double refP2 = BlackFormulaRepository.price(0., 0., inf, vol, false);
      final double refP3 = BlackFormulaRepository.price(0., inf, 0., vol, false);
      final double refC4 = BlackFormulaRepository.price(inf, 0., 0., vol, true);
      final double refP4 = BlackFormulaRepository.price(inf, 0., 0., vol, false);
      final double refC5 = BlackFormulaRepository.price(FORWARD, FORWARD, 0., vol, true);
      final double refP5 = BlackFormulaRepository.price(FORWARD, FORWARD, 0., vol, false);
      final double refC6 = BlackFormulaRepository.price(inf, inf, inf, vol, true);
      final double refP6 = BlackFormulaRepository.price(inf, inf, inf, vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6 };

      for (int k = 0; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-9);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.price(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.price(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.price(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.price(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.price(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.price(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC4 = BlackFormulaRepository.price(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP4 = BlackFormulaRepository.price(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC5 = BlackFormulaRepository.price(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12, true);
      final double resP5 = BlackFormulaRepository
          .price(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12, false);

      final double refC1 = BlackFormulaRepository.price(0., 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.price(0., 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.price(0., inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.price(0., 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.price(0., 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.price(0., inf, TIME_TO_EXPIRY, 0., false);
      final double refC4 = BlackFormulaRepository.price(inf, 0., TIME_TO_EXPIRY, 0., true);
      final double refP4 = BlackFormulaRepository.price(inf, 0., TIME_TO_EXPIRY, 0., false);
      final double refC5 = BlackFormulaRepository.price(FORWARD, FORWARD, TIME_TO_EXPIRY, 1.e-12, true);
      final double refP5 = BlackFormulaRepository.price(FORWARD, FORWARD, TIME_TO_EXPIRY, 1.e-12, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5 };

      for (int k = 0; k < 10; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.price(strike, strike, inf, 0., true);
      final double resP1 = BlackFormulaRepository.price(strike, strike, inf, 0., false);
      final double refC1 = strike * NORMAL.getCDF(0.5) - strike * NORMAL.getCDF(-0.5);
      final double refP1 = -strike * NORMAL.getCDF(-0.5) + strike * NORMAL.getCDF(0.5);

      final double[] resVec = new double[] {resC1, resP1 };
      final double[] refVec = new double[] {refC1, refP1 };
      for (int k = 0; k < 2; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorPriceTest() {
    BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5, true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorPriceTest() {
    BlackFormulaRepository.price(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorPriceTest() {
    BlackFormulaRepository.price(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorPriceTest() {
    BlackFormulaRepository.price(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   * Use SimpleOptionData class for price
   */
  @Test
  public void useSimpleOptionDataTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final SimpleOptionData dataC = new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., true);
        final SimpleOptionData dataP = new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., false);
        final SimpleOptionData[] dataV = new SimpleOptionData[] {
          new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., true),
          new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., true) };
        final double resC1 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.price(dataC, vol);
        final double resC3 = BlackFormulaRepository.price(dataV, vol);
        final double resP1 = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.price(dataP, vol);
        assertEquals(resC1, resC2, EPS);
        assertEquals(2. * resC1, resC3, EPS);
        assertEquals(resP1, resP2, EPS);
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSimpleOptionDataTest() {
    SimpleOptionData data = new SimpleOptionData(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, 1., true);
    data = null;
    BlackFormulaRepository.price(data, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSimpleOptionDataArrayTest() {
    SimpleOptionData[] data = new SimpleOptionData[] {
      new SimpleOptionData(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, 1., true),
      new SimpleOptionData(FORWARD, STRIKES_INPUT[0], TIME_TO_EXPIRY, 1., true) };
    data = null;
    BlackFormulaRepository.price(data, VOLS[0]);
  }

  /*
   * 
   * 
   * Tests for "delta"
   */
  /**
   * Large/small value for delta
   */
  @Test
  public void exDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.delta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC1 = BlackFormulaRepository.delta(0., strike, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.delta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.delta(inf, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.delta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP1 = BlackFormulaRepository.delta(0., strike, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.delta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.delta(inf, strike, TIME_TO_EXPIRY, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.delta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.delta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.delta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.delta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, false);
        final double refC1 = BlackFormulaRepository.delta(forward, 0., TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.delta(forward, inf, TIME_TO_EXPIRY, vol, true);
        final double refP1 = BlackFormulaRepository.delta(forward, 0., TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.delta(forward, inf, TIME_TO_EXPIRY, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.delta(FORWARD, strike, 1e-24, vol, true);
        final double resC2 = BlackFormulaRepository.delta(FORWARD, strike, 1e24, vol, true);
        final double resP1 = BlackFormulaRepository.delta(FORWARD, strike, 1e-24, vol, false);
        final double resP2 = BlackFormulaRepository.delta(FORWARD, strike, 1e24, vol, false);
        final double refC1 = BlackFormulaRepository.delta(FORWARD, strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.delta(FORWARD, strike, inf, vol, true);
        final double refP1 = BlackFormulaRepository.delta(FORWARD, strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.delta(FORWARD, strike, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double refC1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, 0., true);
      final double resC2 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double refC2 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, inf, true);
      final double resP1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double refP1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, 0., false);
      final double resP2 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double refP2 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, inf, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.delta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resC2 = BlackFormulaRepository.delta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resC3 = BlackFormulaRepository.delta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resP1 = BlackFormulaRepository.delta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resP2 = BlackFormulaRepository.delta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false);
      final double resP3 = BlackFormulaRepository.delta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resC4 = BlackFormulaRepository.delta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resP4 = BlackFormulaRepository.delta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false);

      final double refC1 = BlackFormulaRepository.delta(0., 0., TIME_TO_EXPIRY, vol, true);
      final double refC2 = BlackFormulaRepository.delta(0., inf, TIME_TO_EXPIRY, vol, true);
      final double refC3 = BlackFormulaRepository.delta(inf, 0., TIME_TO_EXPIRY, vol, true);
      final double refP1 = BlackFormulaRepository.delta(0., 0., TIME_TO_EXPIRY, vol, false);
      final double refP2 = BlackFormulaRepository.delta(0., inf, TIME_TO_EXPIRY, vol, false);
      final double refP3 = BlackFormulaRepository.delta(inf, 0., TIME_TO_EXPIRY, vol, false);
      final double refC4 = BlackFormulaRepository.delta(inf, inf, TIME_TO_EXPIRY, vol, true);
      final double refP4 = BlackFormulaRepository.delta(inf, inf, TIME_TO_EXPIRY, vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.delta(1.e-12, strike, 1.e-12, vol, true);
        final double resC2 = BlackFormulaRepository.delta(1.e-12, strike, 1.e12, vol, true);
        final double resC3 = BlackFormulaRepository.delta(1.e12, strike, 1.e-12, vol, true);
        final double resP1 = BlackFormulaRepository.delta(1.e-12, strike, 1.e-12, vol, false);
        final double resP2 = BlackFormulaRepository.delta(1.e-12, strike, 1.e12, vol, false);
        final double resP3 = BlackFormulaRepository.delta(1.e12, strike, 1.e-12, vol, false);
        final double resC4 = BlackFormulaRepository.delta(1.e12, strike, 1.e12, vol, true);
        final double resP4 = BlackFormulaRepository.delta(1.e12, strike, 1.e12, vol, false);

        final double refC1 = BlackFormulaRepository.delta(0., strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.delta(0., strike, inf, vol, true);
        final double refC3 = BlackFormulaRepository.delta(inf, strike, 0., vol, true);
        final double refP1 = BlackFormulaRepository.delta(0., strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.delta(0., strike, inf, vol, false);
        final double refP3 = BlackFormulaRepository.delta(inf, strike, 0., vol, false);
        final double refC4 = BlackFormulaRepository.delta(inf, strike, inf, vol, true);
        final double refP4 = BlackFormulaRepository.delta(inf, strike, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.delta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.delta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.delta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.delta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.delta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.delta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false);

      final double refC1 = BlackFormulaRepository.delta(0., strike, TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.delta(0., strike, TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.delta(inf, strike, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.delta(0., strike, TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.delta(0., strike, TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.delta(inf, strike, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

      for (int k = 0; k < 6; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.delta(forward, 1.e-12, 1.e-12, vol, true);
        final double resC2 = BlackFormulaRepository.delta(forward, 1.e-12, 1.e12, vol, true);
        final double resC3 = BlackFormulaRepository.delta(forward, 1.e12, 1.e-12, vol, true);
        final double resP1 = BlackFormulaRepository.delta(forward, 1.e-12, 1.e-12, vol, false);
        final double resP2 = BlackFormulaRepository.delta(forward, 1.e-12, 1.e12, vol, false);
        final double resP3 = BlackFormulaRepository.delta(forward, 1.e12, 1.e-12, vol, false);
        final double resC4 = BlackFormulaRepository.delta(forward, 1.e12, 1.e12, vol, true);
        final double resP4 = BlackFormulaRepository.delta(forward, 1.e12, 1.e12, vol, false);

        final double refC1 = BlackFormulaRepository.delta(forward, 0., 0., vol, true);
        final double refC2 = BlackFormulaRepository.delta(forward, 0., inf, vol, true);
        final double refC3 = BlackFormulaRepository.delta(forward, inf, 0., vol, true);
        final double refP1 = BlackFormulaRepository.delta(forward, 0., 0., vol, false);
        final double refP2 = BlackFormulaRepository.delta(forward, 0., inf, vol, false);
        final double refP3 = BlackFormulaRepository.delta(forward, inf, 0., vol, false);
        final double refC4 = BlackFormulaRepository.delta(forward, inf, inf, vol, true);
        final double refP4 = BlackFormulaRepository.delta(forward, inf, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.delta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.delta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.delta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.delta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.delta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.delta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);

      final double refC1 = BlackFormulaRepository.delta(forward, 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.delta(forward, 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.delta(forward, inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.delta(forward, 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.delta(forward, 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.delta(forward, inf, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

      for (int k = 0; k < 6; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.delta(1.e-12, 1.e-12, 1.e-24, vol, true);
      final double resC2 = BlackFormulaRepository.delta(1.e-12, 1.e-12, 1.e24, vol, true);
      final double resC3 = BlackFormulaRepository.delta(1.e-12, 1.e12, 1.e-24, vol, true);
      final double resP1 = BlackFormulaRepository.delta(1.e-12, 1.e-12, 1.e-24, vol, false);
      final double resP2 = BlackFormulaRepository.delta(1.e-12, 1.e-12, 1.e24, vol, false);
      final double resP3 = BlackFormulaRepository.delta(1.e-12, 1.e12, 1.e-24, vol, false);
      final double resC4 = BlackFormulaRepository.delta(1.e12, 1.e-12, 1.e-24, vol, true);
      final double resP4 = BlackFormulaRepository.delta(1.e12, 1.e-12, 1.e-24, vol, false);
      final double resC5 = BlackFormulaRepository.delta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, true);
      final double resP5 = BlackFormulaRepository.delta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, false);
      final double resC6 = BlackFormulaRepository.delta(1.e12, 1.e12, 1.e24, vol, true);
      final double resP6 = BlackFormulaRepository.delta(1.e12, 1.e12, 1.e24, vol, false);
      final double resC7 = BlackFormulaRepository.delta(1.e12, 1.e12, 1.e-24, vol, true);
      final double resP7 = BlackFormulaRepository.delta(1.e12, 1.e12, 1.e-24, vol, false);

      final double refC1 = BlackFormulaRepository.delta(0., 0., 0., vol, true);
      final double refC2 = BlackFormulaRepository.delta(0., 0., inf, vol, true);
      final double refC3 = BlackFormulaRepository.delta(0., inf, 0., vol, true);
      final double refP1 = BlackFormulaRepository.delta(0., 0., 0., vol, false);
      final double refP2 = BlackFormulaRepository.delta(0., 0., inf, vol, false);
      final double refP3 = BlackFormulaRepository.delta(0., inf, 0., vol, false);
      final double refC4 = BlackFormulaRepository.delta(inf, 0., 0., vol, true);
      final double refP4 = BlackFormulaRepository.delta(inf, 0., 0., vol, false);
      final double refC5 = BlackFormulaRepository.delta(FORWARD, FORWARD, 0., vol, true);
      final double refP5 = BlackFormulaRepository.delta(FORWARD, FORWARD, 0., vol, false);
      final double refC6 = BlackFormulaRepository.delta(inf, inf, inf, vol, true);
      final double refP6 = BlackFormulaRepository.delta(inf, inf, inf, vol, false);
      final double refC7 = BlackFormulaRepository.delta(inf, inf, 0., vol, true);
      final double refP7 = BlackFormulaRepository.delta(inf, inf, 0., vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if ((refVec[k] != 0.5) && (refVec[k] != -0.5)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.delta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.delta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.delta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.delta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.delta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.delta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC4 = BlackFormulaRepository.delta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP4 = BlackFormulaRepository.delta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC5 = BlackFormulaRepository.delta(FORWARD, FORWARD * (1. + 1.e-13), TIME_TO_EXPIRY, 1.e-13, true);
      final double resP5 = BlackFormulaRepository
          .delta(FORWARD, FORWARD * (1. + 1.e-13), TIME_TO_EXPIRY, 1.e-13, false);

      final double refC1 = BlackFormulaRepository.delta(0., 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.delta(0., 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.delta(0., inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.delta(0., 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.delta(0., 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.delta(0., inf, TIME_TO_EXPIRY, 0., false);
      final double refC4 = BlackFormulaRepository.delta(inf, 0., TIME_TO_EXPIRY, 0., true);
      final double refP4 = BlackFormulaRepository.delta(inf, 0., TIME_TO_EXPIRY, 0., false);
      final double refC5 = BlackFormulaRepository.delta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., true);
      final double refP5 = BlackFormulaRepository.delta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5 };

      for (int k = 0; k < 10; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if ((refVec[k] != 0.5) && (refVec[k] != -0.5)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.delta(strike, strike, inf, 0., true);
      final double resP1 = BlackFormulaRepository.delta(strike, strike, inf, 0., false);
      final double refC1 = NORMAL.getCDF(0.5);
      final double refP1 = -NORMAL.getCDF(-0.5);

      final double[] resVec = new double[] {resC1, resP1 };
      final double[] refVec = new double[] {refC1, refP1 };
      for (int k = 0; k < 2; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test
  public void parityDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, vol, false);
        assertEquals(1., resC1 - resP1, EPS);
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorDeltaTest() {
    BlackFormulaRepository.delta(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5, true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorDeltaTest() {
    BlackFormulaRepository.delta(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorDeltaTest() {
    BlackFormulaRepository.delta(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorDeltaTest() {
    BlackFormulaRepository.delta(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1], true);
  }

  /*
   * 
   * 
   * Tests for "strikeForDelta"
   */
  /**
   *
   */
  @Test
  public void strikeForDeltaRecoveryTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, vol, false);
        final double strRecovC1 = BlackFormulaRepository.strikeForDelta(FORWARD, resC1, TIME_TO_EXPIRY, vol, true);
        final double strRecovP1 = BlackFormulaRepository.strikeForDelta(FORWARD, resP1, TIME_TO_EXPIRY, vol, false);
        assertEquals(strike, strRecovC1, strike * EPS);
        assertEquals(strike, strRecovP1, strike * EPS);
      }
    }
  }

  /**
   * Note that the inverse is not necessarily possible because \pm 1, 0 are not taken by strikeForDelta method
   */
  @Test
  public void exDeltaStrikeForDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double fwd = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.strikeForDelta(fwd, 1. - 1.e-12, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.strikeForDelta(fwd, 1.e-12, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.strikeForDelta(fwd, -1. + 1.e-12, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.strikeForDelta(fwd, -1.e-12, TIME_TO_EXPIRY, vol, false);
        final double strRecovC1 = BlackFormulaRepository.delta(fwd, resC1, TIME_TO_EXPIRY, vol, true);
        final double strRecovC2 = BlackFormulaRepository.delta(fwd, resC2, TIME_TO_EXPIRY, vol, true);
        final double strRecovP1 = BlackFormulaRepository.delta(fwd, resP1, TIME_TO_EXPIRY, vol, false);
        final double strRecovP2 = BlackFormulaRepository.delta(fwd, resP2, TIME_TO_EXPIRY, vol, false);

        assertEquals(1. - 1.e-12, strRecovC1, EPS);
        assertEquals(1.e-12, strRecovC2, EPS);
        assertEquals(-1. + 1.e-12, strRecovP1, EPS);
        assertEquals(-1.e-12, strRecovP2, EPS);
      }
    }
  }

  /**
   *
   */
  @Test
  public void exFwdStrikeForDeltaTest() {
    final int nVols = VOLS.length;

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.strikeForDelta(1.e12, 1. - 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resC2 = BlackFormulaRepository.strikeForDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resP1 = BlackFormulaRepository.strikeForDelta(1.e12, -1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resP2 = BlackFormulaRepository.strikeForDelta(1.e-12, -1. + 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double strRecovC1 = BlackFormulaRepository.delta(1.e12, resC1, TIME_TO_EXPIRY, vol, true);
      final double strRecovC2 = BlackFormulaRepository.delta(1.e-12, resC2, TIME_TO_EXPIRY, vol, true);
      final double strRecovP1 = BlackFormulaRepository.delta(1.e12, resP1, TIME_TO_EXPIRY, vol, false);
      final double strRecovP2 = BlackFormulaRepository.delta(1.e-12, resP2, TIME_TO_EXPIRY, vol, false);

      assertEquals(1. - 1.e-12, strRecovC1, EPS);
      assertEquals(1.e-12, strRecovC2, EPS);
      assertEquals(-1.e-12, strRecovP1, EPS);
      assertEquals(-1. + 1.e-12, strRecovP2, EPS);
    }

  }

  /**
   *
   */
  @Test
  public void exTimeStrikeForDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    final double red = Math.sqrt(1.e12);

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double fwd = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.strikeForDelta(fwd, 1. - 1.e-12, 1.e-12, vol, true);
        // final double resC2 = BlackFormulaRepository.strikeForDelta(fwd, 1. - 1.e-12, 1.e12, vol, true);
        final double resP1 = BlackFormulaRepository.strikeForDelta(fwd, -0.5, 1.e-12, vol, false);
        // final double resP2 = BlackFormulaRepository.strikeForDelta(fwd, -1. + 1.e-12, 1.e12, vol, false);
        final double strRecovC1 = BlackFormulaRepository.delta(fwd, resC1, 1.e-12, vol, true);
        // final double strRecovC2 = BlackFormulaRepository.delta(fwd, resC2, 1.e12, vol, true);
        final double strRecovP1 = BlackFormulaRepository.delta(fwd, resP1, 1.e-12, vol, false);
        // final double strRecovP2 = BlackFormulaRepository.delta(fwd, resP2, 1.e12, vol, false);

        assertEquals(1. - 1.e-12, strRecovC1, EPS * red);
        /*
         * This case is not correctly recovered because strike = infinity is obtained by strikeForDelta, coming from
         * exp( 1.e12 ), which always results in delta = 0
         */
        // assertEquals(1. - 1.e-12, strRecovC2, EPS * red);
        assertEquals(-0.5, strRecovP1, EPS * red);
        /*
         * This case gives strike = infinity
         */
        // assertEquals(-1., strRecovP2, EPS * red);
      }
    }
  }

  /**
   *
   */
  @Test
  public void exVolStrikeForDeltaTest() {
    final double small = 1.e-12;
    final double inf = Double.POSITIVE_INFINITY;

    // final double resC1 = BlackFormulaRepository.strikeForDelta(FORWARD, 1. - 1.e-12, TIME_TO_EXPIRY, large, true);
    final double resC2 = BlackFormulaRepository.strikeForDelta(FORWARD, 1.e-12, TIME_TO_EXPIRY, small, true);
    // final double resP1 = BlackFormulaRepository.strikeForDelta(FORWARD, -1. + 1.e-12, TIME_TO_EXPIRY, large, false);
    final double resP2 = BlackFormulaRepository.strikeForDelta(FORWARD, -1.e-12, TIME_TO_EXPIRY, small, false);
    // final double strRecovC1 = BlackFormulaRepository.delta(FORWARD, resC1, TIME_TO_EXPIRY, large, true);
    final double strRecovC2 = BlackFormulaRepository.delta(FORWARD, resC2, TIME_TO_EXPIRY, small, true);
    // final double strRecovP1 = BlackFormulaRepository.delta(FORWARD, resP1, TIME_TO_EXPIRY, large, false);
    final double strRecovP2 = BlackFormulaRepository.delta(FORWARD, resP2, TIME_TO_EXPIRY, small, false);
    final double resC3 = BlackFormulaRepository.strikeForDelta(FORWARD, 0.5, inf, 0., true);
    final double resP3 = BlackFormulaRepository.strikeForDelta(FORWARD, -0.5, inf, 0., false);
    final double strRecovC3 = BlackFormulaRepository.delta(FORWARD, resC3, inf, 0., true);
    final double strRecovP3 = BlackFormulaRepository.delta(FORWARD, resP3, inf, 0., false);

    // assertEquals(1. - 1.e-12, strRecovC1, EPS);
    assertEquals(1.e-12, strRecovC2, EPS);
    // assertEquals(-1. + 1.e-12, strRecovP1, EPS);
    assertEquals(-1.e-12, strRecovP2, EPS);
    assertEquals(0.5, strRecovC3, EPS);
    assertEquals(-0.5, strRecovP3, EPS);

  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeStrikeForDeltaCall1Test() {
    BlackFormulaRepository.strikeForDelta(FORWARD, -0.1, TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeStrikeForDeltaCall2Test() {
    BlackFormulaRepository.strikeForDelta(FORWARD, 1.1, TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeStrikeForDeltaPut1Test() {
    BlackFormulaRepository.strikeForDelta(FORWARD, 0.5, TIME_TO_EXPIRY, VOLS[1], false);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeStrikeForDeltaPut2Test() {
    BlackFormulaRepository.strikeForDelta(FORWARD, -1.5, TIME_TO_EXPIRY, VOLS[1], false);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdStrikeForDeltaCall2Test() {
    BlackFormulaRepository.strikeForDelta(-FORWARD, 0.5, TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeStrikeForDeltaPut1Test() {
    BlackFormulaRepository.strikeForDelta(FORWARD, 0.5, -TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolStrikeForDeltaPut2Test() {
    BlackFormulaRepository.strikeForDelta(FORWARD, 0.5, TIME_TO_EXPIRY, -VOLS[1], true);
  }

  /*
   * 
   * 
   * Tests for "dualDelta"
   */
  /**
   * large/small values for dual delta
   */
  @Test
  public void exDualDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualDelta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC1 = BlackFormulaRepository.dualDelta(0., strike, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.dualDelta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.dualDelta(inf, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.dualDelta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP1 = BlackFormulaRepository.dualDelta(0., strike, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.dualDelta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.dualDelta(inf, strike, TIME_TO_EXPIRY, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualDelta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.dualDelta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.dualDelta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.dualDelta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, false);
        final double refC1 = BlackFormulaRepository.dualDelta(forward, 0., TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.dualDelta(forward, inf, TIME_TO_EXPIRY, vol, true);
        final double refP1 = BlackFormulaRepository.dualDelta(forward, 0., TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.dualDelta(forward, inf, TIME_TO_EXPIRY, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualDelta(FORWARD, strike, 1e-24, vol, true);
        final double resC2 = BlackFormulaRepository.dualDelta(FORWARD, strike, 1e24, vol, true);
        final double resP1 = BlackFormulaRepository.dualDelta(FORWARD, strike, 1e-24, vol, false);
        final double resP2 = BlackFormulaRepository.dualDelta(FORWARD, strike, 1e24, vol, false);
        final double refC1 = BlackFormulaRepository.dualDelta(FORWARD, strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.dualDelta(FORWARD, strike, inf, vol, true);
        final double refP1 = BlackFormulaRepository.dualDelta(FORWARD, strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.dualDelta(FORWARD, strike, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double refC1 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, 0., true);
      final double resC2 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double refC2 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, inf, true);
      final double resP1 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double refP1 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, 0., false);
      final double resP2 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double refP2 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, inf, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resC2 = BlackFormulaRepository.dualDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resC3 = BlackFormulaRepository.dualDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resP1 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resP2 = BlackFormulaRepository.dualDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false);
      final double resP3 = BlackFormulaRepository.dualDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resC4 = BlackFormulaRepository.dualDelta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resP4 = BlackFormulaRepository.dualDelta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false);

      final double refC1 = BlackFormulaRepository.dualDelta(0., 0., TIME_TO_EXPIRY, vol, true);
      final double refC2 = BlackFormulaRepository.dualDelta(0., inf, TIME_TO_EXPIRY, vol, true);
      final double refC3 = BlackFormulaRepository.dualDelta(inf, 0., TIME_TO_EXPIRY, vol, true);
      final double refP1 = BlackFormulaRepository.dualDelta(0., 0., TIME_TO_EXPIRY, vol, false);
      final double refP2 = BlackFormulaRepository.dualDelta(0., inf, TIME_TO_EXPIRY, vol, false);
      final double refP3 = BlackFormulaRepository.dualDelta(inf, 0., TIME_TO_EXPIRY, vol, false);
      final double refC4 = BlackFormulaRepository.dualDelta(inf, inf, TIME_TO_EXPIRY, vol, true);
      final double refP4 = BlackFormulaRepository.dualDelta(inf, inf, TIME_TO_EXPIRY, vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualDelta(1.e-12, strike, 1.e-12, vol, true);
        final double resC2 = BlackFormulaRepository.dualDelta(1.e-12, strike, 1.e12, vol, true);
        final double resC3 = BlackFormulaRepository.dualDelta(1.e12, strike, 1.e-12, vol, true);
        final double resP1 = BlackFormulaRepository.dualDelta(1.e-12, strike, 1.e-12, vol, false);
        final double resP2 = BlackFormulaRepository.dualDelta(1.e-12, strike, 1.e12, vol, false);
        final double resP3 = BlackFormulaRepository.dualDelta(1.e12, strike, 1.e-12, vol, false);
        final double resC4 = BlackFormulaRepository.dualDelta(1.e12, strike, 1.e12, vol, true);
        final double resP4 = BlackFormulaRepository.dualDelta(1.e12, strike, 1.e12, vol, false);

        final double refC1 = BlackFormulaRepository.dualDelta(0., strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.dualDelta(0., strike, inf, vol, true);
        final double refC3 = BlackFormulaRepository.dualDelta(inf, strike, 0., vol, true);
        final double refP1 = BlackFormulaRepository.dualDelta(0., strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.dualDelta(0., strike, inf, vol, false);
        final double refP3 = BlackFormulaRepository.dualDelta(inf, strike, 0., vol, false);
        final double refC4 = BlackFormulaRepository.dualDelta(inf, strike, inf, vol, true);
        final double refP4 = BlackFormulaRepository.dualDelta(inf, strike, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.dualDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.dualDelta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.dualDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.dualDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.dualDelta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false);

      final double refC1 = BlackFormulaRepository.dualDelta(0., strike, TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.dualDelta(0., strike, TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.dualDelta(inf, strike, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.dualDelta(0., strike, TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.dualDelta(0., strike, TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.dualDelta(inf, strike, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

      for (int k = 0; k < 6; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualDelta(forward, 1.e-12, 1.e-12, vol, true);
        final double resC2 = BlackFormulaRepository.dualDelta(forward, 1.e-12, 1.e12, vol, true);
        final double resC3 = BlackFormulaRepository.dualDelta(forward, 1.e12, 1.e-12, vol, true);
        final double resP1 = BlackFormulaRepository.dualDelta(forward, 1.e-12, 1.e-12, vol, false);
        final double resP2 = BlackFormulaRepository.dualDelta(forward, 1.e-12, 1.e12, vol, false);
        final double resP3 = BlackFormulaRepository.dualDelta(forward, 1.e12, 1.e-12, vol, false);
        final double resC4 = BlackFormulaRepository.dualDelta(forward, 1.e12, 1.e12, vol, true);
        final double resP4 = BlackFormulaRepository.dualDelta(forward, 1.e12, 1.e12, vol, false);

        final double refC1 = BlackFormulaRepository.dualDelta(forward, 0., 0., vol, true);
        final double refC2 = BlackFormulaRepository.dualDelta(forward, 0., inf, vol, true);
        final double refC3 = BlackFormulaRepository.dualDelta(forward, inf, 0., vol, true);
        final double refP1 = BlackFormulaRepository.dualDelta(forward, 0., 0., vol, false);
        final double refP2 = BlackFormulaRepository.dualDelta(forward, 0., inf, vol, false);
        final double refP3 = BlackFormulaRepository.dualDelta(forward, inf, 0., vol, false);
        final double refC4 = BlackFormulaRepository.dualDelta(forward, inf, inf, vol, true);
        final double refP4 = BlackFormulaRepository.dualDelta(forward, inf, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.dualDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.dualDelta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.dualDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.dualDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.dualDelta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);

      final double refC1 = BlackFormulaRepository.dualDelta(forward, 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.dualDelta(forward, 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.dualDelta(forward, inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.dualDelta(forward, 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.dualDelta(forward, 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.dualDelta(forward, inf, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

      for (int k = 0; k < 6; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, 1.e-24, vol, true);
      final double resC2 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, 1.e24, vol, true);
      final double resC3 = BlackFormulaRepository.dualDelta(1.e-12, 1.e12, 1.e-24, vol, true);
      final double resP1 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, 1.e-24, vol, false);
      final double resP2 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, 1.e24, vol, false);
      final double resP3 = BlackFormulaRepository.dualDelta(1.e-12, 1.e12, 1.e-24, vol, false);
      final double resC4 = BlackFormulaRepository.dualDelta(1.e12, 1.e-12, 1.e-24, vol, true);
      final double resP4 = BlackFormulaRepository.dualDelta(1.e12, 1.e-12, 1.e-24, vol, false);
      final double resC5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, true);
      final double resP5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, false);
      final double resC6 = BlackFormulaRepository.dualDelta(1.e12, 1.e12, 1.e24, vol, true);
      final double resP6 = BlackFormulaRepository.dualDelta(1.e12, 1.e12, 1.e24, vol, false);
      final double resC7 = BlackFormulaRepository.dualDelta(1.e12, 1.e12, 1.e-24, vol, true);
      final double resP7 = BlackFormulaRepository.dualDelta(1.e12, 1.e12, 1.e-24, vol, false);

      final double refC1 = BlackFormulaRepository.dualDelta(0., 0., 0., vol, true);
      final double refC2 = BlackFormulaRepository.dualDelta(0., 0., inf, vol, true);
      final double refC3 = BlackFormulaRepository.dualDelta(0., inf, 0., vol, true);
      final double refP1 = BlackFormulaRepository.dualDelta(0., 0., 0., vol, false);
      final double refP2 = BlackFormulaRepository.dualDelta(0., 0., inf, vol, false);
      final double refP3 = BlackFormulaRepository.dualDelta(0., inf, 0., vol, false);
      final double refC4 = BlackFormulaRepository.dualDelta(inf, 0., 0., vol, true);
      final double refP4 = BlackFormulaRepository.dualDelta(inf, 0., 0., vol, false);
      final double refC5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD, 0., vol, true);
      final double refP5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD, 0., vol, false);
      final double refC6 = BlackFormulaRepository.dualDelta(inf, inf, inf, vol, true);
      final double refP6 = BlackFormulaRepository.dualDelta(inf, inf, inf, vol, false);
      final double refC7 = BlackFormulaRepository.dualDelta(inf, inf, 0., vol, true);
      final double refP7 = BlackFormulaRepository.dualDelta(inf, inf, 0., vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if ((refVec[k] != 0.5) && (refVec[k] != -0.5)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.dualDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.dualDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.dualDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC4 = BlackFormulaRepository.dualDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP4 = BlackFormulaRepository.dualDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12,
          true);
      final double resP5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12,
          false);

      final double refC1 = BlackFormulaRepository.dualDelta(0., 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.dualDelta(0., 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.dualDelta(0., inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.dualDelta(0., 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.dualDelta(0., 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.dualDelta(0., inf, TIME_TO_EXPIRY, 0., false);
      final double refC4 = BlackFormulaRepository.dualDelta(inf, 0., TIME_TO_EXPIRY, 0., true);
      final double refP4 = BlackFormulaRepository.dualDelta(inf, 0., TIME_TO_EXPIRY, 0., false);
      final double refC5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., true);
      final double refP5 = BlackFormulaRepository.dualDelta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5 };

      for (int k = 0; k < 10; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if ((refVec[k] != 0.5) && (refVec[k] != -0.5)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualDelta(strike, strike, inf, 0., true);
      final double resP1 = BlackFormulaRepository.dualDelta(strike, strike, inf, 0., false);
      final double refC1 = -NORMAL.getCDF(-0.5);
      final double refP1 = NORMAL.getCDF(0.5);

      final double[] resVec = new double[] {resC1, resP1 };
      final double[] refVec = new double[] {refC1, refP1 };
      for (int k = 0; k < 2; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test
  public void parityDualDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.dualDelta(FORWARD, strike, TIME_TO_EXPIRY, vol, false);
        assertEquals(-1., resC1 - resP1, EPS);
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorDualDeltaTest() {
    BlackFormulaRepository.dualDelta(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5, true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorDualDeltaTest() {
    BlackFormulaRepository.dualDelta(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorDualDeltaTest() {
    BlackFormulaRepository.dualDelta(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorDualDeltaTest() {
    BlackFormulaRepository.dualDelta(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1], true);
  }

  /*
   * 
   * 
   * Tests for "simpleDelta"
   */
  /**
   * large/small values
   */
  @Test
  public void exSimpleDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.simpleDelta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC1 = BlackFormulaRepository.simpleDelta(0., strike, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.simpleDelta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.simpleDelta(inf, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.simpleDelta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP1 = BlackFormulaRepository.simpleDelta(0., strike, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.simpleDelta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.simpleDelta(inf, strike, TIME_TO_EXPIRY, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.simpleDelta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resC2 = BlackFormulaRepository.simpleDelta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.simpleDelta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, false);
        final double resP2 = BlackFormulaRepository.simpleDelta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, false);
        final double refC1 = BlackFormulaRepository.simpleDelta(forward, 0., TIME_TO_EXPIRY, vol, true);
        final double refC2 = BlackFormulaRepository.simpleDelta(forward, inf, TIME_TO_EXPIRY, vol, true);
        final double refP1 = BlackFormulaRepository.simpleDelta(forward, 0., TIME_TO_EXPIRY, vol, false);
        final double refP2 = BlackFormulaRepository.simpleDelta(forward, inf, TIME_TO_EXPIRY, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, 1e-24, vol, true);
        final double resC2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, 1e24, vol, true);
        final double resP1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, 1e-24, vol, false);
        final double resP2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, 1e24, vol, false);
        final double refC1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, inf, vol, true);
        final double refP1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double refC1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, 0., true);
      final double resC2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double refC2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, inf, true);
      final double resP1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double refP1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, 0., false);
      final double resP2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double refP2 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, inf, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resC2 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resC3 = BlackFormulaRepository.simpleDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true);
      final double resP1 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resP2 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false);
      final double resP3 = BlackFormulaRepository.simpleDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false);
      final double resC4 = BlackFormulaRepository.simpleDelta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true);
      final double resP4 = BlackFormulaRepository.simpleDelta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false);

      final double refC1 = BlackFormulaRepository.simpleDelta(0., 0., TIME_TO_EXPIRY, vol, true);
      final double refC2 = BlackFormulaRepository.simpleDelta(0., inf, TIME_TO_EXPIRY, vol, true);
      final double refC3 = BlackFormulaRepository.simpleDelta(inf, 0., TIME_TO_EXPIRY, vol, true);
      final double refP1 = BlackFormulaRepository.simpleDelta(0., 0., TIME_TO_EXPIRY, vol, false);
      final double refP2 = BlackFormulaRepository.simpleDelta(0., inf, TIME_TO_EXPIRY, vol, false);
      final double refP3 = BlackFormulaRepository.simpleDelta(inf, 0., TIME_TO_EXPIRY, vol, false);
      final double refC4 = BlackFormulaRepository.simpleDelta(inf, inf, TIME_TO_EXPIRY, vol, true);
      final double refP4 = BlackFormulaRepository.simpleDelta(inf, inf, TIME_TO_EXPIRY, vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.simpleDelta(1.e-12, strike, 1.e-24, vol, true);
        final double resC2 = BlackFormulaRepository.simpleDelta(1.e-12, strike, 1.e24, vol, true);
        final double resC3 = BlackFormulaRepository.simpleDelta(1.e12, strike, 1.e-24, vol, true);
        final double resP1 = BlackFormulaRepository.simpleDelta(1.e-12, strike, 1.e-24, vol, false);
        final double resP2 = BlackFormulaRepository.simpleDelta(1.e-12, strike, 1.e24, vol, false);
        final double resP3 = BlackFormulaRepository.simpleDelta(1.e12, strike, 1.e-24, vol, false);
        final double resC4 = BlackFormulaRepository.simpleDelta(1.e12, strike, 1.e24, vol, true);
        final double resP4 = BlackFormulaRepository.simpleDelta(1.e12, strike, 1.e24, vol, false);

        final double refC1 = BlackFormulaRepository.simpleDelta(0., strike, 0., vol, true);
        final double refC2 = BlackFormulaRepository.simpleDelta(0., strike, inf, vol, true);
        final double refC3 = BlackFormulaRepository.simpleDelta(inf, strike, 0., vol, true);
        final double refP1 = BlackFormulaRepository.simpleDelta(0., strike, 0., vol, false);
        final double refP2 = BlackFormulaRepository.simpleDelta(0., strike, inf, vol, false);
        final double refP3 = BlackFormulaRepository.simpleDelta(inf, strike, 0., vol, false);
        final double refC4 = BlackFormulaRepository.simpleDelta(inf, strike, inf, vol, true);
        final double refP4 = BlackFormulaRepository.simpleDelta(inf, strike, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.simpleDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.simpleDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.simpleDelta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.simpleDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.simpleDelta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.simpleDelta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false);

      final double refC1 = BlackFormulaRepository.simpleDelta(0., strike, TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.simpleDelta(0., strike, TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.simpleDelta(inf, strike, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.simpleDelta(0., strike, TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.simpleDelta(0., strike, TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.simpleDelta(inf, strike, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

      for (int k = 0; k < 6; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, 1.e-24, vol, true);
        final double resC2 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, 1.e24, vol, true);
        final double resC3 = BlackFormulaRepository.simpleDelta(forward, 1.e12, 1.e-24, vol, true);
        final double resP1 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, 1.e-24, vol, false);
        final double resP2 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, 1.e24, vol, false);
        final double resP3 = BlackFormulaRepository.simpleDelta(forward, 1.e12, 1.e-24, vol, false);
        final double resC4 = BlackFormulaRepository.simpleDelta(forward, 1.e12, 1.e24, vol, true);
        final double resP4 = BlackFormulaRepository.simpleDelta(forward, 1.e12, 1.e24, vol, false);

        final double refC1 = BlackFormulaRepository.simpleDelta(forward, 0., 0., vol, true);
        final double refC2 = BlackFormulaRepository.simpleDelta(forward, 0., inf, vol, true);
        final double refC3 = BlackFormulaRepository.simpleDelta(forward, inf, 0., vol, true);
        final double refP1 = BlackFormulaRepository.simpleDelta(forward, 0., 0., vol, false);
        final double refP2 = BlackFormulaRepository.simpleDelta(forward, 0., inf, vol, false);
        final double refP3 = BlackFormulaRepository.simpleDelta(forward, inf, 0., vol, false);
        final double refC4 = BlackFormulaRepository.simpleDelta(forward, inf, inf, vol, true);
        final double refP4 = BlackFormulaRepository.simpleDelta(forward, inf, inf, vol, false);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.simpleDelta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.simpleDelta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.simpleDelta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);

      final double refC1 = BlackFormulaRepository.simpleDelta(forward, 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.simpleDelta(forward, 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.simpleDelta(forward, inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.simpleDelta(forward, 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.simpleDelta(forward, 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.simpleDelta(forward, inf, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

      for (int k = 0; k < 6; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, 1.e-24, vol, true);
      final double resC2 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, 1.e24, vol, true);
      final double resC3 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e12, 1.e-24, vol, true);
      final double resP1 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, 1.e-24, vol, false);
      final double resP2 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, 1.e24, vol, false);
      final double resP3 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e12, 1.e-24, vol, false);
      final double resC4 = BlackFormulaRepository.simpleDelta(1.e12, 1.e-12, 1.e-24, vol, true);
      final double resP4 = BlackFormulaRepository.simpleDelta(1.e12, 1.e-12, 1.e-24, vol, false);
      final double resC5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, true);
      final double resP5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, false);
      final double resC6 = BlackFormulaRepository.simpleDelta(1.e12, 1.e12, 1.e24, vol, true);
      final double resP6 = BlackFormulaRepository.simpleDelta(1.e12, 1.e12, 1.e24, vol, false);
      final double resC7 = BlackFormulaRepository.simpleDelta(1.e12, 1.e12, 1.e-24, vol, true);
      final double resP7 = BlackFormulaRepository.simpleDelta(1.e12, 1.e12, 1.e-24, vol, false);

      final double refC1 = BlackFormulaRepository.simpleDelta(0., 0., 0., vol, true);
      final double refC2 = BlackFormulaRepository.simpleDelta(0., 0., inf, vol, true);
      final double refC3 = BlackFormulaRepository.simpleDelta(0., inf, 0., vol, true);
      final double refP1 = BlackFormulaRepository.simpleDelta(0., 0., 0., vol, false);
      final double refP2 = BlackFormulaRepository.simpleDelta(0., 0., inf, vol, false);
      final double refP3 = BlackFormulaRepository.simpleDelta(0., inf, 0., vol, false);
      final double refC4 = BlackFormulaRepository.simpleDelta(inf, 0., 0., vol, true);
      final double refP4 = BlackFormulaRepository.simpleDelta(inf, 0., 0., vol, false);
      final double refC5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD, 0., vol, true);
      final double refP5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD, 0., vol, false);
      final double refC6 = BlackFormulaRepository.simpleDelta(inf, inf, inf, vol, true);
      final double refP6 = BlackFormulaRepository.simpleDelta(inf, inf, inf, vol, false);
      final double refC7 = BlackFormulaRepository.simpleDelta(inf, inf, 0., vol, true);
      final double refP7 = BlackFormulaRepository.simpleDelta(inf, inf, 0., vol, false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if ((refVec[k] != 0.5) && (refVec[k] != -0.5)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resC2 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, true);
      final double resC3 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP1 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resP2 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, false);
      final double resP3 = BlackFormulaRepository.simpleDelta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC4 = BlackFormulaRepository.simpleDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true);
      final double resP4 = BlackFormulaRepository.simpleDelta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false);
      final double resC5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12,
          true);
      final double resP5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12,
          false);

      final double refC1 = BlackFormulaRepository.simpleDelta(0., 0., TIME_TO_EXPIRY, 0., true);
      final double refC2 = BlackFormulaRepository.simpleDelta(0., 0., TIME_TO_EXPIRY, inf, true);
      final double refC3 = BlackFormulaRepository.simpleDelta(0., inf, TIME_TO_EXPIRY, 0., true);
      final double refP1 = BlackFormulaRepository.simpleDelta(0., 0., TIME_TO_EXPIRY, 0., false);
      final double refP2 = BlackFormulaRepository.simpleDelta(0., 0., TIME_TO_EXPIRY, inf, false);
      final double refP3 = BlackFormulaRepository.simpleDelta(0., inf, TIME_TO_EXPIRY, 0., false);
      final double refC4 = BlackFormulaRepository.simpleDelta(inf, 0., TIME_TO_EXPIRY, 0., true);
      final double refP4 = BlackFormulaRepository.simpleDelta(inf, 0., TIME_TO_EXPIRY, 0., false);
      final double refC5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., true);
      final double refP5 = BlackFormulaRepository.simpleDelta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., false);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5 };

      for (int k = 0; k < 10; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if ((refVec[k] != 0.5) && (refVec[k] != -0.5)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.simpleDelta(strike, strike, inf, 0., true);
      final double resP1 = BlackFormulaRepository.simpleDelta(strike, strike, inf, 0., false);
      final double refC1 = NORMAL.getCDF(0.);
      final double refP1 = -NORMAL.getCDF(0.);

      final double[] resVec = new double[] {resC1, resP1 };
      final double[] refVec = new double[] {refC1, refP1 };
      for (int k = 0; k < 2; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test
  public void paritySimpleDeltaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, vol, true);
        final double resP1 = BlackFormulaRepository.simpleDelta(FORWARD, strike, TIME_TO_EXPIRY, vol, false);
        assertEquals(1., resC1 - resP1, EPS);
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorSimpleDeltaTest() {
    BlackFormulaRepository.simpleDelta(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5, true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorSimpleDeltaTest() {
    BlackFormulaRepository.simpleDelta(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorSimpleDeltaTest() {
    BlackFormulaRepository.simpleDelta(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorSimpleDeltaTest() {
    BlackFormulaRepository.simpleDelta(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1], true);
  }

  /*
   * 
   * 
   * Tests for "gamma"
   */
  /**
   * large/small values
   */
  @Test
  public void exGammaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.gamma(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.gamma(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.gamma(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.gamma(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.gamma(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.gamma(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.gamma(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.gamma(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.gamma(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.gamma(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.gamma(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.gamma(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.gamma(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.gamma(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.gamma(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.gamma(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.gamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.gamma(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.gamma(1.e12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resP1 = BlackFormulaRepository.gamma(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.gamma(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.gamma(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.gamma(inf, 0., TIME_TO_EXPIRY, vol);
      final double refP1 = BlackFormulaRepository.gamma(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.gamma(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.gamma(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.gamma(1.e12, strike, 1.e-24, vol);
        final double resP1 = BlackFormulaRepository.gamma(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.gamma(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.gamma(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.gamma(inf, strike, 0., vol);
        final double refP1 = BlackFormulaRepository.gamma(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.gamma(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.gamma(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.gamma(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.gamma(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.gamma(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.gamma(inf, strike, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refC2, refC3 };

      for (int k = 0; k < 3; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.gamma(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.gamma(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.gamma(forward, 1.e12, 1.e-24, vol);
        final double resP1 = BlackFormulaRepository.gamma(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.gamma(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.gamma(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.gamma(forward, inf, 0., vol);
        final double refP1 = BlackFormulaRepository.gamma(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.gamma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.gamma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.gamma(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.gamma(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.gamma(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.gamma(forward, inf, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refC2, refC3 };

      for (int k = 0; k < 3; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.gamma(1.e-12, 1.e-12, 1.e-24, vol);
      final double resC2 = BlackFormulaRepository.gamma(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.gamma(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.gamma(1.e12, 1.e12, 1.e24, vol);
      final double resC4 = BlackFormulaRepository.gamma(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.gamma(FORWARD, FORWARD, 1.e-24, vol); // / "* (1. + 1.e-12) " removed
      final double resP2 = BlackFormulaRepository.gamma(1.e12, 1.e12, 1.e-24, vol);

      final double refC1 = BlackFormulaRepository.gamma(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.gamma(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.gamma(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.gamma(inf, inf, inf, vol);
      final double refC4 = BlackFormulaRepository.gamma(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.gamma(FORWARD, FORWARD, 0., vol);
      final double refP2 = BlackFormulaRepository.gamma(inf, inf, 0., vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resC3, resC4, resC5, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refC3, refC4, refC5, refP2 };

      for (int k = 0; k < 6; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e9);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.gamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.gamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.gamma(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.gamma(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.gamma(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.gamma(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.gamma(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.gamma(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.gamma(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.gamma(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3, resC4, resC5 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refC4, refC5 };

      for (int k = 0; k < 5; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e9);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.gamma(strike, strike, inf, 0.);
      final double refC1 = NORMAL.getPDF(0.5) / strike;
      final double[] resVec = new double[] {resC1 };
      final double[] refVec = new double[] {refC1 };
      for (int k = 0; k < 1; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorGammaTest() {
    BlackFormulaRepository.gamma(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorGammaTest() {
    BlackFormulaRepository.gamma(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorGammaTest() {
    BlackFormulaRepository.gamma(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorGammaTest() {
    BlackFormulaRepository.gamma(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /*
   * 
   * 
   * Tests for "dualGamma"
   */
  /**
   * large/small values
   */
  @Test
  public void exDualGammaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualGamma(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.dualGamma(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.dualGamma(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.dualGamma(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualGamma(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.dualGamma(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.dualGamma(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.dualGamma(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualGamma(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.dualGamma(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.dualGamma(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.dualGamma(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualGamma(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.dualGamma(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.dualGamma(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.dualGamma(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.dualGamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.dualGamma(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.dualGamma(1.e12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resP1 = BlackFormulaRepository.dualGamma(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.dualGamma(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.dualGamma(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.dualGamma(inf, 0., TIME_TO_EXPIRY, vol);
      final double refP1 = BlackFormulaRepository.dualGamma(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualGamma(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.dualGamma(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.dualGamma(1.e12, strike, 1.e-24, vol);
        final double resP1 = BlackFormulaRepository.dualGamma(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.dualGamma(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.dualGamma(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.dualGamma(inf, strike, 0., vol);
        final double refP1 = BlackFormulaRepository.dualGamma(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualGamma(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.dualGamma(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.dualGamma(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.dualGamma(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.dualGamma(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.dualGamma(inf, strike, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refC2, refC3 };

      for (int k = 0; k < 3; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualGamma(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.dualGamma(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.dualGamma(forward, 1.e12, 1.e-24, vol);
        final double resP1 = BlackFormulaRepository.dualGamma(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.dualGamma(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.dualGamma(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.dualGamma(forward, inf, 0., vol);
        final double refP1 = BlackFormulaRepository.dualGamma(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualGamma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.dualGamma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.dualGamma(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.dualGamma(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.dualGamma(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.dualGamma(forward, inf, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refC2, refC3 };

      for (int k = 0; k < 3; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.dualGamma(1.e-12, 1.e-12, 1.e-24, vol);
      final double resC2 = BlackFormulaRepository.dualGamma(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.dualGamma(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.dualGamma(1.e12, 1.e12, 1.e24, vol);
      final double resC4 = BlackFormulaRepository.dualGamma(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.dualGamma(FORWARD, FORWARD, 1.e-24, vol); // / "* (1. + 1.e-12) "
                                                                                            // removed
      final double resP2 = BlackFormulaRepository.dualGamma(1.e12, 1.e12, 1.e-24, vol);

      final double refC1 = BlackFormulaRepository.dualGamma(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.dualGamma(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.dualGamma(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.dualGamma(inf, inf, inf, vol);
      final double refC4 = BlackFormulaRepository.dualGamma(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.dualGamma(FORWARD, FORWARD, 0., vol);
      final double refP2 = BlackFormulaRepository.dualGamma(inf, inf, 0., vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resC3, resC4, resC5, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refC3, refC4, refC5, refP2 };

      for (int k = 0; k < 6; ++k) {// k=7 ref value is not accurate due to non-unity of vol
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e9);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.dualGamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.dualGamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.dualGamma(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.dualGamma(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.dualGamma(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.dualGamma(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.dualGamma(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.dualGamma(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.dualGamma(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.dualGamma(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3, resC4, resC5 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refC4, refC5 };

      for (int k = 0; k < 5; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e9);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualGamma(strike, strike, inf, 0.);
      final double refC1 = NORMAL.getPDF(0.5) / strike;
      final double[] resVec = new double[] {resC1 };
      final double[] refVec = new double[] {refC1 };
      for (int k = 0; k < 1; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorDualGammaTest() {
    BlackFormulaRepository.dualGamma(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorDualGammaTest() {
    BlackFormulaRepository.dualGamma(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorDualGammaTest() {
    BlackFormulaRepository.dualGamma(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorDualGammaTest() {
    BlackFormulaRepository.dualGamma(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /*
   * 
   * crossGamma
   */
  /**
   * large/small value
   */
  @Test
  public void exCrossGammaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.crossGamma(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.crossGamma(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.crossGamma(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.crossGamma(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.crossGamma(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.crossGamma(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.crossGamma(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.crossGamma(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.crossGamma(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.crossGamma(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.crossGamma(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.crossGamma(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.crossGamma(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.crossGamma(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.crossGamma(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.crossGamma(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.crossGamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.crossGamma(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.crossGamma(1.e12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resP1 = BlackFormulaRepository.crossGamma(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.crossGamma(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.crossGamma(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.crossGamma(inf, 0., TIME_TO_EXPIRY, vol);
      final double refP1 = BlackFormulaRepository.crossGamma(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.crossGamma(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.crossGamma(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.crossGamma(1.e12, strike, 1.e-24, vol);
        final double resP1 = BlackFormulaRepository.crossGamma(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.crossGamma(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.crossGamma(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.crossGamma(inf, strike, 0., vol);
        final double refP1 = BlackFormulaRepository.crossGamma(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.crossGamma(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.crossGamma(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.crossGamma(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.crossGamma(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.crossGamma(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.crossGamma(inf, strike, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refC2, refC3 };

      for (int k = 0; k < 3; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.crossGamma(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.crossGamma(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.crossGamma(forward, 1.e12, 1.e-24, vol);
        final double resP1 = BlackFormulaRepository.crossGamma(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.crossGamma(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.crossGamma(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.crossGamma(forward, inf, 0., vol);
        final double refP1 = BlackFormulaRepository.crossGamma(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resP1, resC2, resC3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refC3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.crossGamma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.crossGamma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.crossGamma(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.crossGamma(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.crossGamma(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.crossGamma(forward, inf, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3 };
      final double[] refVec = new double[] {refC1, refC2, refC3 };

      for (int k = 0; k < 3; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.crossGamma(1.e-12, 1.e-12, 1.e-24, vol);
      final double resC2 = BlackFormulaRepository.crossGamma(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.crossGamma(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.crossGamma(1.e12, 1.e12, 1.e24, vol);
      final double resC4 = BlackFormulaRepository.crossGamma(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.crossGamma(FORWARD, FORWARD, 1.e-24, vol); // / "* (1. + 1.e-12) "
                                                                                             // removed
      final double resP2 = BlackFormulaRepository.crossGamma(1.e12, 1.e12, 1.e-24, vol);

      final double refC1 = BlackFormulaRepository.crossGamma(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.crossGamma(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.crossGamma(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.crossGamma(inf, inf, inf, vol);
      final double refC4 = BlackFormulaRepository.crossGamma(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.crossGamma(FORWARD, FORWARD, 0., vol);
      final double refP2 = BlackFormulaRepository.crossGamma(inf, inf, 0., vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resC3, resC4, resC5, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refC3, refC4, refC5, refP2 };

      for (int k = 0; k < 6; ++k) {// k=7 ref value is not accurate due to non-unity of vol
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e9);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e9);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.crossGamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.crossGamma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.crossGamma(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.crossGamma(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.crossGamma(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);

      final double refC1 = BlackFormulaRepository.crossGamma(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.crossGamma(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.crossGamma(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.crossGamma(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.crossGamma(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);

      final double[] resVec = new double[] {resC1, resC2, resC3, resC4, resC5 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refC4, refC5 };

      for (int k = 0; k < 5; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e9);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e9);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.crossGamma(strike, strike, inf, 0.);
      final double refC1 = -NORMAL.getPDF(0.5) / strike;
      final double[] resVec = new double[] {resC1 };
      final double[] refVec = new double[] {refC1 };
      for (int k = 0; k < 1; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorCrossGammaTest() {
    BlackFormulaRepository.crossGamma(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorCrossGammaTest() {
    BlackFormulaRepository.crossGamma(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorCrossGammaTest() {
    BlackFormulaRepository.crossGamma(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorCrossGammaTest() {
    BlackFormulaRepository.crossGamma(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /*
   * 
   * Theta tests
   */
  /**
   * large/small input
   */
  @Test
  public void exThetaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double refC1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.theta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.theta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, false, 0.05);
        final double refP1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.theta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(forward, 1.e-14 * forward, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.theta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.theta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.theta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, false, 0.05);
        final double refC1 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(FORWARD, strike, 1e-12, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.theta(FORWARD, strike, 1e12, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.theta(FORWARD, strike, 1e-12, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.theta(FORWARD, strike, 1e12, vol, false, 0.05);
        final double refC1 = BlackFormulaRepository.theta(FORWARD, strike, 0., vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.theta(FORWARD, strike, inf, vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.theta(FORWARD, strike, 0., vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.theta(FORWARD, strike, inf, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-24, true, 0.05);
      final double refC1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 0., true, 0.05);
      final double resC2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double refC2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, inf, true, 0.05);
      final double resP1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-24, false, 0.05);
      final double refP1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 0., false, 0.05);
      final double resP2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e24, false, 0.05);
      final double refP2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double refC1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, false, 0.);
        final double resC2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, false, 1.e12);
        final double refC2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, true, inf);
        final double refP2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e8);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e9);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
              }
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resC2 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resC3 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resP1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resP2 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resP3 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resC4 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resP4 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resC5 = BlackFormulaRepository.theta(1.e10, 1.e11, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resP5 = BlackFormulaRepository.theta(1.e11, 1.e10, TIME_TO_EXPIRY, vol, false, 0.05);

      final double refC1 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, vol, true, 0.05);
      final double refC2 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, vol, true, 0.05);
      final double refC3 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, vol, true, 0.05);
      final double refP1 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, vol, false, 0.05);
      final double refP2 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, vol, false, 0.05);
      final double refP3 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, vol, false, 0.05);
      final double refC4 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, vol, true, 0.05);
      final double refP4 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, vol, false, 0.05);
      final double refC5 = BlackFormulaRepository.theta(1.e15, 1.e16, TIME_TO_EXPIRY, vol, true, 0.05);
      final double refP5 = BlackFormulaRepository.theta(1.e16, 1.e15, TIME_TO_EXPIRY, vol, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5 };

      for (int k = 0; k < 6; ++k) {// ref values
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 6 && k != 7) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e8);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e9);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(1.e-12, strike, 1.e-24, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.theta(1.e-12, strike, 1.e24, vol, true, 0.05);
        final double resC3 = BlackFormulaRepository.theta(1.e12, strike, 1.e-24, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.theta(1.e-12, strike, 1.e-24, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.theta(1.e-12, strike, 1.e24, vol, false, 0.05);
        final double resP3 = BlackFormulaRepository.theta(1.e12, strike, 1.e-24, vol, false, 0.05);
        final double resC4 = BlackFormulaRepository.theta(1.e12, strike, 1.e24, vol, true, 0.05);
        final double resP4 = BlackFormulaRepository.theta(1.e12, strike, 1.e24, vol, false, 0.05);

        final double refC1 = BlackFormulaRepository.theta(0., strike, 0., vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.theta(0., strike, inf, vol, true, 0.05);
        final double refC3 = BlackFormulaRepository.theta(inf, strike, 0., vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.theta(0., strike, 0., vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.theta(0., strike, inf, vol, false, 0.05);
        final double refP3 = BlackFormulaRepository.theta(inf, strike, 0., vol, false, 0.05);
        final double refC4 = BlackFormulaRepository.theta(inf, strike, inf, vol, true, 0.05);
        final double refP4 = BlackFormulaRepository.theta(inf, strike, inf, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-24, true, 0.05);
      final double resC2 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double resC3 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e-24, true, 0.05);
      final double resP1 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-24, false, 0.05);
      final double resP2 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e24, false, 0.05);
      final double resP3 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e-24, false, 0.05);
      final double resC4 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double resP4 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e24, false, 0.05);

      final double refC1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refC2 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refC3 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refP2 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, inf, false, 0.05);
      final double refP3 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC4 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refP4 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e-24, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e24, vol, true, 0.05);
        final double resC3 = BlackFormulaRepository.theta(forward, 1.e12, 1.e-24, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e-24, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e24, vol, false, 0.05);
        final double resP3 = BlackFormulaRepository.theta(forward, 1.e12, 1.e-24, vol, false, 0.05);
        final double resC4 = BlackFormulaRepository.theta(forward, 1.e12, 1.e24, vol, true, 0.05);
        final double resP4 = BlackFormulaRepository.theta(forward, 1.e12, 1.e24, vol, false, 0.05);

        final double refC1 = BlackFormulaRepository.theta(forward, 0., 0., vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.theta(forward, 0., inf, vol, true, 0.05);
        final double refC3 = BlackFormulaRepository.theta(forward, inf, 0., vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.theta(forward, 0., 0., vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.theta(forward, 0., inf, vol, false, 0.05);
        final double refP3 = BlackFormulaRepository.theta(forward, inf, 0., vol, false, 0.05);
        final double refC4 = BlackFormulaRepository.theta(forward, inf, inf, vol, true, 0.05);
        final double refP4 = BlackFormulaRepository.theta(forward, inf, inf, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resC2 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 0.05);
      final double resC3 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resP1 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resP2 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 0.05);
      final double resP3 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resC4 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, true, 0.05);
      final double resP4 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, false, 0.05);

      final double refC1 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, 0., true, 0.05);
      final double refC2 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, inf, true, 0.05);
      final double refC3 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP1 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, 0., false, 0.05);
      final double refP2 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, inf, false, 0.05);
      final double refP3 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC4 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refP4 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(FORWARD, strike, 1.e-12, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.theta(FORWARD, strike, 1.e12, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.theta(FORWARD, strike, 1.e-12, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.theta(FORWARD, strike, 1.e12, vol, false, 1.e-12);
        final double resC3 = BlackFormulaRepository.theta(FORWARD, strike, 1.e12, vol, true, 1.e12);
        final double resP3 = BlackFormulaRepository.theta(FORWARD, strike, 1.e12, vol, false, 1.e12);
        final double resC4 = BlackFormulaRepository.theta(FORWARD, strike, 1.e-12, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.theta(FORWARD, strike, 1.e-12, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.theta(FORWARD, strike, 0., vol, true, 0.);
        final double refC2 = BlackFormulaRepository.theta(FORWARD, strike, inf, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.theta(FORWARD, strike, 0., vol, false, 0.);
        final double refP2 = BlackFormulaRepository.theta(FORWARD, strike, inf, vol, false, 0.);
        final double refC3 = BlackFormulaRepository.theta(FORWARD, strike, inf, vol, true, inf);
        final double refP3 = BlackFormulaRepository.theta(FORWARD, strike, inf, vol, false, inf);
        final double refC4 = BlackFormulaRepository.theta(FORWARD, strike, 0., vol, true, inf);
        final double refP4 = BlackFormulaRepository.theta(FORWARD, strike, 0., vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 6; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(strike, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.theta(strike, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.theta(strike, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.theta(strike, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resC3 = BlackFormulaRepository.theta(strike, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP3 = BlackFormulaRepository.theta(strike, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.theta(strike, 0., TIME_TO_EXPIRY, vol, true, 0.);
        final double refC2 = BlackFormulaRepository.theta(strike, inf, TIME_TO_EXPIRY, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.theta(strike, 0., TIME_TO_EXPIRY, vol, false, 0.);
        final double refP2 = BlackFormulaRepository.theta(strike, inf, TIME_TO_EXPIRY, vol, false, 0.);
        final double refC3 = BlackFormulaRepository.theta(strike, inf, TIME_TO_EXPIRY, vol, true, inf);
        final double refP3 = BlackFormulaRepository.theta(strike, inf, TIME_TO_EXPIRY, vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

        for (int k = 0; k < 6; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 3) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resC3 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP3 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC4 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP4 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);

      final double refC1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 0., true, 0.);
      final double refC2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, inf, true, 0.);
      final double refP1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 0., false, 0.);
      final double refP2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, inf, false, 0.);
      final double refC3 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, inf, true, inf);
      final double refP3 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, inf, false, inf);
      final double refC4 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 0., true, inf);
      final double refP4 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, 0., false, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 6; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e9);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-9);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resC3 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP3 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, vol, false, 1.e12);
        final double resC4 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, vol, true, 0.);
        final double refC2 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, vol, false, 0.);
        final double refP2 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, vol, false, 0.);
        final double refC3 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, vol, true, inf);
        final double refP3 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, vol, false, inf);
        final double refC4 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, vol, true, inf);
        final double refP4 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 2 && k != 7) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
                }
              }
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, 1.e-24, vol, true, 0.05);
      final double resC2 = BlackFormulaRepository.theta(1.e-12, 1.e-12, 1.e24, vol, true, 0.05);
      final double resC3 = BlackFormulaRepository.theta(1.e-12, 1.e12, 1.e-24, vol, true, 0.05);
      final double resP1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, 1.e-24, vol, false, 0.05);
      final double resP2 = BlackFormulaRepository.theta(1.e-12, 1.e-12, 1.e24, vol, false, 0.05);
      final double resP3 = BlackFormulaRepository.theta(1.e-12, 1.e12, 1.e-24, vol, false, 0.05);
      final double resC4 = BlackFormulaRepository.theta(1.e12, 1.e-12, 1.e-24, vol, true, 0.05);
      final double resP4 = BlackFormulaRepository.theta(1.e12, 1.e-12, 1.e-24, vol, false, 0.05);
      final double resC5 = BlackFormulaRepository.theta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, true, 0.05);
      final double resP5 = BlackFormulaRepository.theta(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, false, 0.05);
      final double resC6 = BlackFormulaRepository.theta(1.e12, 1.e12, 1.e24, vol, true, 0.05);
      final double resP6 = BlackFormulaRepository.theta(1.e12, 1.e12, 1.e24, vol, false, 0.05);

      final double refC1 = BlackFormulaRepository.theta(0., 0., 0., vol, true, 0.05);
      final double refC2 = BlackFormulaRepository.theta(0., 0., inf, vol, true, 0.05);
      final double refC3 = BlackFormulaRepository.theta(0., inf, 0., vol, true, 0.05);
      final double refP1 = BlackFormulaRepository.theta(0., 0., 0., vol, false, 0.05);
      final double refP2 = BlackFormulaRepository.theta(0., 0., inf, vol, false, 0.05);
      final double refP3 = BlackFormulaRepository.theta(0., inf, 0., vol, false, 0.05);
      final double refC4 = BlackFormulaRepository.theta(inf, 0., 0., vol, true, 0.05);
      final double refP4 = BlackFormulaRepository.theta(inf, 0., 0., vol, false, 0.05);
      final double refC5 = BlackFormulaRepository.theta(FORWARD, FORWARD, 0., vol, true, 0.05);
      final double refP5 = BlackFormulaRepository.theta(FORWARD, FORWARD, 0., vol, false, 0.05);
      final double refC6 = BlackFormulaRepository.theta(inf, inf, inf, vol, true, 0.05);
      final double refP6 = BlackFormulaRepository.theta(inf, inf, inf, vol, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6 };
      for (int k = 0; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if ((refVec[k] != -0.5 * vol) && (refVec[k] != -0.5 * FORWARD) && (refVec[k] != Double.NEGATIVE_INFINITY) &&
            k != 11) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-7);// //should be rechecked
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resC2 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 0.05);
      final double resC3 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resP1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resP2 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 0.05);
      final double resP3 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resC4 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resP4 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resC5 = BlackFormulaRepository.theta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12, true,
          0.05);
      final double resP5 = BlackFormulaRepository.theta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12,
          false, 0.05);
      final double resC6 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double resP6 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e24, false, 0.05);

      final double refC1 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, 0., true, 0.05);
      final double refC2 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, inf, true, 0.05);
      final double refC3 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP1 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, 0., false, 0.05);
      final double refP2 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, inf, false, 0.05);
      final double refP3 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC4 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP4 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC5 = BlackFormulaRepository.theta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP5 = BlackFormulaRepository.theta(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC6 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refP6 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC6, resP6,
        resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC6, refP6,
        refC5, refP5 };

      for (int k = 0; k < 10; ++k) {// The last two cases return reference values
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    /*******************************************************
     *
     */
    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resC3 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.theta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e-12);
      final double resP3 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e-12);
      final double resC4 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP4 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC5 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP5 = BlackFormulaRepository.theta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC6 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP6 = BlackFormulaRepository.theta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC7 = BlackFormulaRepository.theta(1.e-12, 2.e-12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP7 = BlackFormulaRepository.theta(1.e-12, 0.5e-12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC8 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resP8 = BlackFormulaRepository.theta(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e-12);

      final double refC1 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, vol, true, 0.);
      final double refC2 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, vol, true, 0.);
      final double refC3 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, vol, true, 0.);
      final double refP1 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, vol, false, 0.);
      final double refP2 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, vol, false, 0.);
      final double refP3 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, vol, false, 0.);
      final double refC4 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, vol, true, inf);
      final double refP4 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, vol, false, inf);
      final double refC5 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, vol, true, inf);
      final double refP5 = BlackFormulaRepository.theta(inf, 0., TIME_TO_EXPIRY, vol, false, inf);
      final double refC6 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, vol, true, inf);
      final double refP6 = BlackFormulaRepository.theta(0., inf, TIME_TO_EXPIRY, vol, false, inf);
      final double refC7 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, vol, true, inf);
      final double refP7 = BlackFormulaRepository.theta(0., 0., TIME_TO_EXPIRY, vol, false, inf);
      final double refC8 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, vol, true, 0.);
      final double refP8 = BlackFormulaRepository.theta(inf, inf, TIME_TO_EXPIRY, vol, false, 0.);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resC8, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refC8, refP8 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 3 && k != 4 && k != 7 && k != 11) {
          if (k != 12 && k != 13) {// ref values are returned
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e9);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e9);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-10);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(1.e-12, strike, 1.e-24, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.theta(1.e-12, strike, 1.e24, vol, true, 1.e-12);
        final double resC3 = BlackFormulaRepository.theta(1.e12, strike, 1.e-24, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.theta(1.e-12, strike, 1.e-24, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.theta(1.e-12, strike, 1.e24, vol, false, 1.e-12);
        final double resP3 = BlackFormulaRepository.theta(1.e12, strike, 1.e-24, vol, false, 1.e-12);
        final double resC4 = BlackFormulaRepository.theta(1.e12, strike, 1.e24, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.theta(1.e12, strike, 1.e24, vol, false, 1.e12);
        final double resC5 = BlackFormulaRepository.theta(1.e-12, strike, 1.e24, vol, true, 1.e12);
        final double resP5 = BlackFormulaRepository.theta(1.e-12, strike, 1.e24, vol, false, 1.e12);
        final double resC6 = BlackFormulaRepository.theta(1.e12, strike, 1.e-24, vol, true, 1.e12);
        final double resP6 = BlackFormulaRepository.theta(1.e12, strike, 1.e-24, vol, false, 1.e12);
        final double resC7 = BlackFormulaRepository.theta(1.e12, strike, 1.e24, vol, true, 1.e-12);
        final double resP7 = BlackFormulaRepository.theta(1.e12, strike, 1.e24, vol, false, 1.e-12);
        final double resC8 = BlackFormulaRepository.theta(1.e-12, strike, 1.e-24, vol, true, 1.e12);
        final double resP8 = BlackFormulaRepository.theta(1.e-12, strike, 1.e-24, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.theta(0., strike, 0., vol, true, 0.);
        final double refC2 = BlackFormulaRepository.theta(0., strike, inf, vol, true, 0.);
        final double refC3 = BlackFormulaRepository.theta(inf, strike, 0., vol, true, 0.);
        final double refP1 = BlackFormulaRepository.theta(0., strike, 0., vol, false, 0.);
        final double refP2 = BlackFormulaRepository.theta(0., strike, inf, vol, false, 0.);
        final double refP3 = BlackFormulaRepository.theta(inf, strike, 0., vol, false, 0.);
        final double refC4 = BlackFormulaRepository.theta(inf, strike, inf, vol, true, inf);
        final double refP4 = BlackFormulaRepository.theta(inf, strike, inf, vol, false, inf);
        final double refC5 = BlackFormulaRepository.theta(0., strike, inf, vol, true, inf);
        final double refP5 = BlackFormulaRepository.theta(0., strike, inf, vol, false, inf);
        final double refC6 = BlackFormulaRepository.theta(inf, strike, 0., vol, true, inf);
        final double refP6 = BlackFormulaRepository.theta(inf, strike, 0., vol, false, inf);
        final double refC7 = BlackFormulaRepository.theta(inf, strike, inf, vol, true, 0.);
        final double refP7 = BlackFormulaRepository.theta(inf, strike, inf, vol, false, 0.);
        final double refC8 = BlackFormulaRepository.theta(0., strike, 0., vol, true, inf);
        final double refP8 = BlackFormulaRepository.theta(0., strike, 0., vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
          resC6, resP6, resC7, resP7, resC8, resP8 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
          refC6, refP6, refC7, refP7, refC8, refP8 };

        for (int k = 0; k < 16; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 4 && k != 8 && k != 12) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resC3 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resP3 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resC4 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP4 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC5 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP5 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);
      final double resC6 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP6 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);
      final double resC7 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP7 = BlackFormulaRepository.theta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC8 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resP8 = BlackFormulaRepository.theta(1.e12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);

      final double refC1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, 0., true, 0.);
      final double refC2 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, inf, true, 0.);
      final double refC3 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, 0., true, 0.);
      final double refP1 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, 0., false, 0.);
      final double refP2 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, inf, false, 0.);
      final double refP3 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, 0., false, 0.);
      final double refC4 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, inf, true, inf);
      final double refP4 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, inf, false, inf);
      final double refC5 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, 0., true, inf);
      final double refP5 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, 0., false, inf);
      final double refC6 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, 0., true, inf);
      final double refP6 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, 0., false, inf);
      final double refC7 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, inf, true, inf);
      final double refP7 = BlackFormulaRepository.theta(0., strike, TIME_TO_EXPIRY, inf, false, inf);
      final double refC8 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, inf, true, 0.);
      final double refP8 = BlackFormulaRepository.theta(inf, strike, TIME_TO_EXPIRY, inf, false, 0.);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resC8, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refC8, refP8 };

      for (int k = 0; k < 16; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 4 && k != 9 && k != 12 && k != 14) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e-24, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e24, vol, true, 1.e-12);
        final double resC3 = BlackFormulaRepository.theta(forward, 1.e12, 1.e-24, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e-24, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e24, vol, false, 1.e-12);
        final double resP3 = BlackFormulaRepository.theta(forward, 1.e12, 1.e-24, vol, false, 1.e-12);
        final double resC4 = BlackFormulaRepository.theta(forward, 1.e12, 1.e24, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.theta(forward, 1.e12, 1.e24, vol, false, 1.e12);
        final double resC5 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e24, vol, true, 1.e12);
        final double resP5 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e24, vol, false, 1.e12);
        final double resC6 = BlackFormulaRepository.theta(forward, 1.e12, 1.e-12, vol, true, 1.e12);
        final double resP6 = BlackFormulaRepository.theta(forward, 1.e12, 1.e-12, vol, false, 1.e12);
        final double resC7 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e-12, vol, true, 1.e12);
        final double resP7 = BlackFormulaRepository.theta(forward, 1.e-12, 1.e-12, vol, false, 1.e12);
        final double resC8 = BlackFormulaRepository.theta(forward, 1.e12, 1.e24, vol, true, 1.e-12);
        final double resP8 = BlackFormulaRepository.theta(forward, 1.e12, 1.e24, vol, false, 1.e-12);

        final double refC1 = BlackFormulaRepository.theta(forward, 0., 0., vol, true, 0.);
        final double refC2 = BlackFormulaRepository.theta(forward, 0., inf, vol, true, 0.);
        final double refC3 = BlackFormulaRepository.theta(forward, inf, 0., vol, true, 0.);
        final double refP1 = BlackFormulaRepository.theta(forward, 0., 0., vol, false, 0.);
        final double refP2 = BlackFormulaRepository.theta(forward, 0., inf, vol, false, 0.);
        final double refP3 = BlackFormulaRepository.theta(forward, inf, 0., vol, false, 0.);
        final double refC4 = BlackFormulaRepository.theta(forward, inf, inf, vol, true, inf);
        final double refP4 = BlackFormulaRepository.theta(forward, inf, inf, vol, false, inf);
        final double refC5 = BlackFormulaRepository.theta(forward, 0., inf, vol, true, inf);
        final double refP5 = BlackFormulaRepository.theta(forward, 0., inf, vol, false, inf);
        final double refC6 = BlackFormulaRepository.theta(forward, inf, 0., vol, true, inf);
        final double refP6 = BlackFormulaRepository.theta(forward, inf, 0., vol, false, inf);
        final double refC7 = BlackFormulaRepository.theta(forward, 0., inf, vol, true, inf);
        final double refP7 = BlackFormulaRepository.theta(forward, 0., inf, vol, false, inf);
        final double refC8 = BlackFormulaRepository.theta(forward, 0., 0., vol, true, inf);
        final double refP8 = BlackFormulaRepository.theta(forward, 0., 0., vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
          resC6, resP6, resC7, resP7, resC8, resP8 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
          refC6, refP6, refC7, refP7, refC8, refP8 };

        for (int k = 0; k < 14; ++k) {// some of ref values skipped
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 5 && k != 9) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resC3 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resP3 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resC4 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP4 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC5 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP5 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC6 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP6 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);
      final double resC7 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resP7 = BlackFormulaRepository.theta(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resC8 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP8 = BlackFormulaRepository.theta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);

      final double refC1 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, 0., true, 0.);
      final double refC2 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, inf, true, 0.);
      final double refC3 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, 0., true, 0.);
      final double refP1 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, 0., false, 0.);
      final double refP2 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, inf, false, 0.);
      final double refP3 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, 0., false, 0.);
      final double refC4 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, inf, true, inf);
      final double refP4 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, inf, false, inf);
      final double refC5 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, inf, true, inf);
      final double refP5 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, inf, false, inf);
      final double refC6 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, 0., true, inf);
      final double refP6 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, 0., false, inf);
      final double refC7 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, inf, true, 0.);
      final double refP7 = BlackFormulaRepository.theta(forward, inf, TIME_TO_EXPIRY, inf, false, 0.);
      final double refC8 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, 0., true, inf);
      final double refP8 = BlackFormulaRepository.theta(forward, 0., TIME_TO_EXPIRY, 0., false, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resC8, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refC8, refP8 };

      for (int k = 0; k < 16; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 5 && k != 9 && k != 11 && k != 13) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(strike, strike, inf, 0., true, 1.);
      final double resP1 = BlackFormulaRepository.theta(strike, strike, inf, 0., false, 1.);
      final double resC2 = BlackFormulaRepository.theta(strike, strike, inf, 0., true, 0.);
      final double resP2 = BlackFormulaRepository.theta(strike, strike, inf, 0., false, 0.);
      final double refC1 = strike * (NORMAL.getCDF(0.5));
      final double refP1 = -strike * (NORMAL.getCDF(-0.5));

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, 0., 0. };
      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, VOLS[0], true, -inf);
      final double resP1 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, VOLS[1], false, -inf);
      final double resC2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, VOLS[2], true, -inf);
      final double resP2 = BlackFormulaRepository.theta(FORWARD, strike, TIME_TO_EXPIRY, VOLS[3], false, -inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {0., 0., 0., 0. };
      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorThetaTest() {
    BlackFormulaRepository.theta(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5, true, 0.1);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorThetaTest() {
    BlackFormulaRepository.theta(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true, 0.1);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorThetaTest() {
    BlackFormulaRepository.theta(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true, 0.1);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorThetaTest() {
    BlackFormulaRepository.theta(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1], true, 0.1);
  }

  /*
   * 
   * 
   * 
   * driftlessTheta
   */
  /**
   * large/small input
   */
  @Test
  public void exDriftlessThetaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.driftlessTheta(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.driftlessTheta(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.driftlessTheta(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.driftlessTheta(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.driftlessTheta(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.driftlessTheta(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.driftlessTheta(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.driftlessTheta(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.driftlessTheta(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resP3 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.driftlessTheta(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.driftlessTheta(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.driftlessTheta(inf, 0., TIME_TO_EXPIRY, vol);
      final double refP3 = BlackFormulaRepository.driftlessTheta(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e12) {
          assertTrue(resVec[k] > 1.e9);
        } else {
          if (refVec[k] < -1.e12) {
            assertTrue(resVec[k] < -1.e9);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.driftlessTheta(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.driftlessTheta(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.driftlessTheta(1.e12, strike, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.driftlessTheta(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.driftlessTheta(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.driftlessTheta(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.driftlessTheta(inf, strike, 0., vol);
        final double refP3 = BlackFormulaRepository.driftlessTheta(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.driftlessTheta(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.driftlessTheta(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.driftlessTheta(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.driftlessTheta(1.e12, strike, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.driftlessTheta(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.driftlessTheta(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.driftlessTheta(inf, strike, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.driftlessTheta(inf, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.driftlessTheta(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.driftlessTheta(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.driftlessTheta(forward, 1.e12, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.driftlessTheta(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.driftlessTheta(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.driftlessTheta(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.driftlessTheta(forward, inf, 0., vol);
        final double refP3 = BlackFormulaRepository.driftlessTheta(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.driftlessTheta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.driftlessTheta(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.driftlessTheta(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.driftlessTheta(forward, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.driftlessTheta(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.driftlessTheta(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.driftlessTheta(forward, inf, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.driftlessTheta(forward, inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.driftlessTheta(1.e-14, 1.e-14, 1.e-11, vol);
      final double resC2 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.driftlessTheta(1.e-14, 1.e-14, 1.e-11, vol);
      final double resP2 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, 1.e24, vol);
      final double resP3 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e12, 1.e-24, vol);
      final double resC4 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e-12, 1.e-24, vol);
      final double resP4 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resP5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resC6 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e12, 1.e24, vol);
      final double resP6 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e12, 1.e-24, vol);
      final double resC7 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e-12, 1.e24, vol);
      final double resP7 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e12, 1.e24, vol);
      final double resP8 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, 1.e-24, vol);

      final double refC1 = BlackFormulaRepository.driftlessTheta(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.driftlessTheta(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.driftlessTheta(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.driftlessTheta(0., 0., 0., vol);
      final double refP2 = BlackFormulaRepository.driftlessTheta(0., 0., inf, vol);
      final double refP3 = BlackFormulaRepository.driftlessTheta(0., inf, 0., vol);
      final double refC4 = BlackFormulaRepository.driftlessTheta(inf, 0., 0., vol);
      final double refP4 = BlackFormulaRepository.driftlessTheta(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD, 0., vol);
      final double refP5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD, 0., vol);
      final double refC6 = BlackFormulaRepository.driftlessTheta(inf, inf, inf, vol);
      final double refP6 = BlackFormulaRepository.driftlessTheta(inf, inf, 0., vol);
      final double refC7 = BlackFormulaRepository.driftlessTheta(inf, 0., inf, vol);
      final double refP7 = BlackFormulaRepository.driftlessTheta(0., inf, inf, vol);
      final double refP8 = BlackFormulaRepository.driftlessTheta(0., 0., 0., vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refP8 };

      for (int k = 0; k < 15; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if ((refVec[k] != -0.5 * vol * NORMAL.getPDF(0.)) && (refVec[k] != -0.5 * FORWARD * NORMAL.getPDF(0.)) &&
            (refVec[k] != Double.NEGATIVE_INFINITY)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP1 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP2 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resP3 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP4 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY,
          1.e-12);
      final double resP5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY,
          1.e-12);
      final double resC6 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e12);
      final double resP6 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC7 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP7 = BlackFormulaRepository.driftlessTheta(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e12);
      final double resP8 = BlackFormulaRepository.driftlessTheta(1.e12, 1.e12, 1.e-24, 1.e-12);

      final double refC1 = BlackFormulaRepository.driftlessTheta(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.driftlessTheta(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.driftlessTheta(0., inf, TIME_TO_EXPIRY, 0.);
      final double refP1 = BlackFormulaRepository.driftlessTheta(0., 0., TIME_TO_EXPIRY, 0.);
      final double refP2 = BlackFormulaRepository.driftlessTheta(0., 0., TIME_TO_EXPIRY, inf);
      final double refP3 = BlackFormulaRepository.driftlessTheta(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.driftlessTheta(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refP4 = BlackFormulaRepository.driftlessTheta(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD, TIME_TO_EXPIRY, 1.e-12);
      final double refP5 = BlackFormulaRepository.driftlessTheta(FORWARD, FORWARD, TIME_TO_EXPIRY, 1.e-12);
      final double refC6 = BlackFormulaRepository.driftlessTheta(inf, inf, TIME_TO_EXPIRY, inf);
      final double refP6 = BlackFormulaRepository.driftlessTheta(inf, 0., TIME_TO_EXPIRY, inf);
      final double refC7 = BlackFormulaRepository.driftlessTheta(inf, inf, TIME_TO_EXPIRY, 0.);
      final double refP7 = BlackFormulaRepository.driftlessTheta(0., inf, TIME_TO_EXPIRY, inf);
      final double refP8 = BlackFormulaRepository.driftlessTheta(inf, inf, 0., 0.);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refP8 };

      for (int k = 0; k < 15; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 12) {// ref value
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (Math.abs(refVec[k]) < 1.e-10) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorDriftlessThetaTest() {
    BlackFormulaRepository.driftlessTheta(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorDriftlessThetaTest() {
    BlackFormulaRepository.driftlessTheta(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorDriftlessThetaTest() {
    BlackFormulaRepository.driftlessTheta(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorDriftlessThetaTest() {
    BlackFormulaRepository.driftlessTheta(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /*
   * 
   * thetaMod tests
   */
  /**
   * large/small input
   */
  @Test
  public void exthetaModTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double refC1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.thetaMod(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.thetaMod(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol, false, 0.05);
        final double refP1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.thetaMod(1.e12 * strike, strike, TIME_TO_EXPIRY, vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository
            .thetaMod(forward, 1.e-14 * forward, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.thetaMod(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.thetaMod(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol, false,
            0.05);
        final double resP2 = BlackFormulaRepository
            .thetaMod(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol, false, 0.05);
        final double refC1 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1e-12, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1e12, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1e-12, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1e12, vol, false, 0.05);
        final double refC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 0., vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, inf, vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 0., vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, inf, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e-24, true, 0.05);
      final double refC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 0., true, 0.05);
      final double resC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double refC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, inf, true, 0.05);
      final double resP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e-24, false, 0.05);
      final double refP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 0., false, 0.05);
      final double resP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e24, false, 0.05);
      final double refP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double refC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, false, 0.);
        final double resC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, false, 1.e12);
        final double refC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, true, inf);
        final double refP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e8);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e9);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
              }
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resC3 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resP3 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resC4 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resP4 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false, 0.05);
      final double resC5 = BlackFormulaRepository.thetaMod(1.e10, 1.e11, TIME_TO_EXPIRY, vol, true, 0.05);
      final double resP5 = BlackFormulaRepository.thetaMod(1.e11, 1.e10, TIME_TO_EXPIRY, vol, false, 0.05);

      final double refC1 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, vol, true, 0.05);
      final double refC2 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, vol, true, 0.05);
      final double refC3 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, vol, true, 0.05);
      final double refP1 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, vol, false, 0.05);
      final double refP2 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, vol, false, 0.05);
      final double refP3 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, vol, false, 0.05);
      final double refC4 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, vol, true, 0.05);
      final double refP4 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, vol, false, 0.05);
      final double refC5 = BlackFormulaRepository.thetaMod(1.e15, 1.e16, TIME_TO_EXPIRY, vol, true, 0.05);
      final double refP5 = BlackFormulaRepository.thetaMod(1.e16, 1.e15, TIME_TO_EXPIRY, vol, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5 };

      for (int k = 0; k < 6; ++k) {// ref values
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 6 && k != 7) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e8);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e9);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e-24, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e24, vol, true, 0.05);
        final double resC3 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e-24, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e-24, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e24, vol, false, 0.05);
        final double resP3 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e-24, vol, false, 0.05);
        final double resC4 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e24, vol, true, 0.05);
        final double resP4 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e24, vol, false, 0.05);

        final double refC1 = BlackFormulaRepository.thetaMod(0., strike, 0., vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.thetaMod(0., strike, inf, vol, true, 0.05);
        final double refC3 = BlackFormulaRepository.thetaMod(inf, strike, 0., vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.thetaMod(0., strike, 0., vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.thetaMod(0., strike, inf, vol, false, 0.05);
        final double refP3 = BlackFormulaRepository.thetaMod(inf, strike, 0., vol, false, 0.05);
        final double refC4 = BlackFormulaRepository.thetaMod(inf, strike, inf, vol, true, 0.05);
        final double refP4 = BlackFormulaRepository.thetaMod(inf, strike, inf, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e-24, true, 0.05);
      final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double resC3 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e-24, true, 0.05);
      final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e-24, false, 0.05);
      final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e24, false, 0.05);
      final double resP3 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e-24, false, 0.05);
      final double resC4 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double resP4 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e24, false, 0.05);

      final double refC1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refC2 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refC3 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refP2 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, inf, false, 0.05);
      final double refP3 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC4 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refP4 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e-24, vol, true, 0.05);
        final double resC2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e24, vol, true, 0.05);
        final double resC3 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e-24, vol, true, 0.05);
        final double resP1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e-24, vol, false, 0.05);
        final double resP2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e24, vol, false, 0.05);
        final double resP3 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e-24, vol, false, 0.05);
        final double resC4 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e24, vol, true, 0.05);
        final double resP4 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e24, vol, false, 0.05);

        final double refC1 = BlackFormulaRepository.thetaMod(forward, 0., 0., vol, true, 0.05);
        final double refC2 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, true, 0.05);
        final double refC3 = BlackFormulaRepository.thetaMod(forward, inf, 0., vol, true, 0.05);
        final double refP1 = BlackFormulaRepository.thetaMod(forward, 0., 0., vol, false, 0.05);
        final double refP2 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, false, 0.05);
        final double refP3 = BlackFormulaRepository.thetaMod(forward, inf, 0., vol, false, 0.05);
        final double refC4 = BlackFormulaRepository.thetaMod(forward, inf, inf, vol, true, 0.05);
        final double refP4 = BlackFormulaRepository.thetaMod(forward, inf, inf, vol, false, 0.05);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resC2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 0.05);
      final double resC3 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resP1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resP2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 0.05);
      final double resP3 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resC4 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, true, 0.05);
      final double resP4 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, false, 0.05);

      final double refC1 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, 0., true, 0.05);
      final double refC2 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, inf, true, 0.05);
      final double refC3 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP1 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, 0., false, 0.05);
      final double refP2 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, inf, false, 0.05);
      final double refP3 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC4 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refP4 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 8; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e-12, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e12, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e-12, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e12, vol, false, 1.e-12);
        final double resC3 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e12, vol, true, 1.e12);
        final double resP3 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e12, vol, false, 1.e12);
        final double resC4 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e-12, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.thetaMod(FORWARD, strike, 1.e-12, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 0., vol, true, 0.);
        final double refC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, inf, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, 0., vol, false, 0.);
        final double refP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, inf, vol, false, 0.);
        final double refC3 = BlackFormulaRepository.thetaMod(FORWARD, strike, inf, vol, true, inf);
        final double refP3 = BlackFormulaRepository.thetaMod(FORWARD, strike, inf, vol, false, inf);
        final double refC4 = BlackFormulaRepository.thetaMod(FORWARD, strike, 0., vol, true, inf);
        final double refP4 = BlackFormulaRepository.thetaMod(FORWARD, strike, 0., vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 6; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(strike, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.thetaMod(strike, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.thetaMod(strike, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.thetaMod(strike, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resC3 = BlackFormulaRepository.thetaMod(strike, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP3 = BlackFormulaRepository.thetaMod(strike, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.thetaMod(strike, 0., TIME_TO_EXPIRY, vol, true, 0.);
        final double refC2 = BlackFormulaRepository.thetaMod(strike, inf, TIME_TO_EXPIRY, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.thetaMod(strike, 0., TIME_TO_EXPIRY, vol, false, 0.);
        final double refP2 = BlackFormulaRepository.thetaMod(strike, inf, TIME_TO_EXPIRY, vol, false, 0.);
        final double refC3 = BlackFormulaRepository.thetaMod(strike, inf, TIME_TO_EXPIRY, vol, true, inf);
        final double refP3 = BlackFormulaRepository.thetaMod(strike, inf, TIME_TO_EXPIRY, vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3 };

        for (int k = 0; k < 6; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 3) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resC3 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP3 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC4 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP4 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);

      final double refC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 0., true, 0.);
      final double refC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, inf, true, 0.);
      final double refP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 0., false, 0.);
      final double refP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, inf, false, 0.);
      final double refC3 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, inf, true, inf);
      final double refP3 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, inf, false, inf);
      final double refC4 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 0., true, inf);
      final double refP4 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, 0., false, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

      for (int k = 0; k < 6; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e9);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-9);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, vol, false, 1.e-12);
        final double resC3 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP3 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, vol, false, 1.e12);
        final double resC4 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, vol, true, 0.);
        final double refC2 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, vol, true, 0.);
        final double refP1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, vol, false, 0.);
        final double refP2 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, vol, false, 0.);
        final double refC3 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, vol, true, inf);
        final double refP3 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, vol, false, inf);
        final double refC4 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, vol, true, inf);
        final double refP4 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4 };

        for (int k = 0; k < 8; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 2 && k != 7) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-9, 1.e-9));
                }
              }
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, 1.e-24, vol, true, 0.05);
      final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, 1.e24, vol, true, 0.05);
      final double resC3 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, 1.e-24, vol, true, 0.05);
      final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, 1.e-24, vol, false, 0.05);
      final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, 1.e24, vol, false, 0.05);
      final double resP3 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, 1.e-24, vol, false, 0.05);
      final double resC4 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, 1.e-24, vol, true, 0.05);
      final double resP4 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, 1.e-24, vol, false, 0.05);
      final double resC5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, true, 0.05);
      final double resP5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD * (1. + 1.e-12), 1.e-24, vol, false, 0.05);
      final double resC6 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, 1.e24, vol, true, 0.05);
      final double resP6 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, 1.e24, vol, false, 0.05);

      final double refC1 = BlackFormulaRepository.thetaMod(0., 0., 0., vol, true, 0.05);
      final double refC2 = BlackFormulaRepository.thetaMod(0., 0., inf, vol, true, 0.05);
      final double refC3 = BlackFormulaRepository.thetaMod(0., inf, 0., vol, true, 0.05);
      final double refP1 = BlackFormulaRepository.thetaMod(0., 0., 0., vol, false, 0.05);
      final double refP2 = BlackFormulaRepository.thetaMod(0., 0., inf, vol, false, 0.05);
      final double refP3 = BlackFormulaRepository.thetaMod(0., inf, 0., vol, false, 0.05);
      final double refC4 = BlackFormulaRepository.thetaMod(inf, 0., 0., vol, true, 0.05);
      final double refP4 = BlackFormulaRepository.thetaMod(inf, 0., 0., vol, false, 0.05);
      final double refC5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD, 0., vol, true, 0.05);
      final double refP5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD, 0., vol, false, 0.05);
      final double refC6 = BlackFormulaRepository.thetaMod(inf, inf, inf, vol, true, 0.05);
      final double refP6 = BlackFormulaRepository.thetaMod(inf, inf, inf, vol, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6 };
      for (int k = 0; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        if ((refVec[k] != -0.5 * vol) && (refVec[k] != -0.5 * FORWARD) && (refVec[k] != Double.NEGATIVE_INFINITY) &&
            k != 11) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-7);// //should be rechecked
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 0.05);
      final double resC3 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 0.05);
      final double resP3 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resC4 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 0.05);
      final double resP4 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 0.05);
      final double resC5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12,
          true, 0.05);
      final double resP5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12,
          false, 0.05);
      final double resC6 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e24, true, 0.05);
      final double resP6 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e24, false, 0.05);

      final double refC1 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, 0., true, 0.05);
      final double refC2 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, inf, true, 0.05);
      final double refC3 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP1 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, 0., false, 0.05);
      final double refP2 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, inf, false, 0.05);
      final double refP3 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC4 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP4 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., true, 0.05);
      final double refP5 = BlackFormulaRepository.thetaMod(FORWARD, FORWARD, TIME_TO_EXPIRY, 0., false, 0.05);
      final double refC6 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, inf, true, 0.05);
      final double refP6 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, inf, false, 0.05);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC6, resP6,
        resC5, resP5 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC6, refP6,
        refC5, refP5 };

      for (int k = 0; k < 10; ++k) {// The last two cases return reference values
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    /*******************************************************
     *
     */
    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resC3 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e-12);
      final double resP3 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e-12);
      final double resC4 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP4 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC5 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP5 = BlackFormulaRepository.thetaMod(1.e12, 1.e-12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC6 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP6 = BlackFormulaRepository.thetaMod(1.e-12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC7 = BlackFormulaRepository.thetaMod(1.e-12, 2.e-12, TIME_TO_EXPIRY, vol, true, 1.e12);
      final double resP7 = BlackFormulaRepository.thetaMod(1.e-12, 0.5e-12, TIME_TO_EXPIRY, vol, false, 1.e12);
      final double resC8 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, vol, true, 1.e-12);
      final double resP8 = BlackFormulaRepository.thetaMod(1.e12, 1.e12, TIME_TO_EXPIRY, vol, false, 1.e-12);

      final double refC1 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, vol, true, 0.);
      final double refC2 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, vol, true, 0.);
      final double refC3 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, vol, true, 0.);
      final double refP1 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, vol, false, 0.);
      final double refP2 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, vol, false, 0.);
      final double refP3 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, vol, false, 0.);
      final double refC4 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, vol, true, inf);
      final double refP4 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, vol, false, inf);
      final double refC5 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, vol, true, inf);
      final double refP5 = BlackFormulaRepository.thetaMod(inf, 0., TIME_TO_EXPIRY, vol, false, inf);
      final double refC6 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, vol, true, inf);
      final double refP6 = BlackFormulaRepository.thetaMod(0., inf, TIME_TO_EXPIRY, vol, false, inf);
      final double refC7 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, vol, true, inf);
      final double refP7 = BlackFormulaRepository.thetaMod(0., 0., TIME_TO_EXPIRY, vol, false, inf);
      final double refC8 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, vol, true, 0.);
      final double refP8 = BlackFormulaRepository.thetaMod(inf, inf, TIME_TO_EXPIRY, vol, false, 0.);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resC8, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refC8, refP8 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 3 && k != 8) {
          if (k != 12 && k != 13) {// ref values are returned
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e9);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e9);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e-24, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e24, vol, true, 1.e-12);
        final double resC3 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e-24, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e-24, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e24, vol, false, 1.e-12);
        final double resP3 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e-24, vol, false, 1.e-12);
        final double resC4 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e24, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e24, vol, false, 1.e12);
        final double resC5 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e24, vol, true, 1.e12);
        final double resP5 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e24, vol, false, 1.e12);
        final double resC6 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e-24, vol, true, 1.e12);
        final double resP6 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e-24, vol, false, 1.e12);
        final double resC7 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e24, vol, true, 1.e-12);
        final double resP7 = BlackFormulaRepository.thetaMod(1.e12, strike, 1.e24, vol, false, 1.e-12);
        final double resC8 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e-24, vol, true, 1.e12);
        final double resP8 = BlackFormulaRepository.thetaMod(1.e-12, strike, 1.e-24, vol, false, 1.e12);

        final double refC1 = BlackFormulaRepository.thetaMod(0., strike, 0., vol, true, 0.);
        final double refC2 = BlackFormulaRepository.thetaMod(0., strike, inf, vol, true, 0.);
        final double refC3 = BlackFormulaRepository.thetaMod(inf, strike, 0., vol, true, 0.);
        final double refP1 = BlackFormulaRepository.thetaMod(0., strike, 0., vol, false, 0.);
        final double refP2 = BlackFormulaRepository.thetaMod(0., strike, inf, vol, false, 0.);
        final double refP3 = BlackFormulaRepository.thetaMod(inf, strike, 0., vol, false, 0.);
        final double refC4 = BlackFormulaRepository.thetaMod(inf, strike, inf, vol, true, inf);
        final double refP4 = BlackFormulaRepository.thetaMod(inf, strike, inf, vol, false, inf);
        final double refC5 = BlackFormulaRepository.thetaMod(0., strike, inf, vol, true, inf);
        final double refP5 = BlackFormulaRepository.thetaMod(0., strike, inf, vol, false, inf);
        final double refC6 = BlackFormulaRepository.thetaMod(inf, strike, 0., vol, true, inf);
        final double refP6 = BlackFormulaRepository.thetaMod(inf, strike, 0., vol, false, inf);
        final double refC7 = BlackFormulaRepository.thetaMod(inf, strike, inf, vol, true, 0.);
        final double refP7 = BlackFormulaRepository.thetaMod(inf, strike, inf, vol, false, 0.);
        final double refC8 = BlackFormulaRepository.thetaMod(0., strike, 0., vol, true, inf);
        final double refP8 = BlackFormulaRepository.thetaMod(0., strike, 0., vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
          resC6, resP6, resC7, resP7, resC8, resP8 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
          refC6, refP6, refC7, refP7, refC8, refP8 };

        for (int k = 0; k < 16; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 4 && k != 8 && k != 12) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resC3 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resP3 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resC4 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP4 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC5 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP5 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);
      final double resC6 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP6 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);
      final double resC7 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP7 = BlackFormulaRepository.thetaMod(1.e-12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC8 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resP8 = BlackFormulaRepository.thetaMod(1.e12, strike, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);

      final double refC1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, 0., true, 0.);
      final double refC2 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, inf, true, 0.);
      final double refC3 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, 0., true, 0.);
      final double refP1 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, 0., false, 0.);
      final double refP2 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, inf, false, 0.);
      final double refP3 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, 0., false, 0.);
      final double refC4 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, inf, true, inf);
      final double refP4 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, inf, false, inf);
      final double refC5 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, 0., true, inf);
      final double refP5 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, 0., false, inf);
      final double refC6 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, 0., true, inf);
      final double refP6 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, 0., false, inf);
      final double refC7 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, inf, true, inf);
      final double refP7 = BlackFormulaRepository.thetaMod(0., strike, TIME_TO_EXPIRY, inf, false, inf);
      final double refC8 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, inf, true, 0.);
      final double refP8 = BlackFormulaRepository.thetaMod(inf, strike, TIME_TO_EXPIRY, inf, false, 0.);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resC8, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refC8, refP8 };

      for (int k = 0; k < 16; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 9 && k != 10) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e-24, vol, true, 1.e-12);
        final double resC2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e24, vol, true, 1.e-12);
        final double resC3 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e-24, vol, true, 1.e-12);
        final double resP1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e-24, vol, false, 1.e-12);
        final double resP2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e24, vol, false, 1.e-12);
        final double resP3 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e-24, vol, false, 1.e-12);
        final double resC4 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e24, vol, true, 1.e12);
        final double resP4 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e24, vol, false, 1.e12);
        final double resC5 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e24, vol, true, 1.e12);
        final double resP5 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e24, vol, false, 1.e12);
        final double resC6 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e-12, vol, true, 1.e12);
        final double resP6 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e-12, vol, false, 1.e12);
        final double resC7 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e-12, vol, true, 1.e12);
        final double resP7 = BlackFormulaRepository.thetaMod(forward, 1.e-12, 1.e-12, vol, false, 1.e12);
        final double resC8 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e24, vol, true, 1.e-12);
        final double resP8 = BlackFormulaRepository.thetaMod(forward, 1.e12, 1.e24, vol, false, 1.e-12);

        final double refC1 = BlackFormulaRepository.thetaMod(forward, 0., 0., vol, true, 0.);
        final double refC2 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, true, 0.);
        final double refC3 = BlackFormulaRepository.thetaMod(forward, inf, 0., vol, true, 0.);
        final double refP1 = BlackFormulaRepository.thetaMod(forward, 0., 0., vol, false, 0.);
        final double refP2 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, false, 0.);
        final double refP3 = BlackFormulaRepository.thetaMod(forward, inf, 0., vol, false, 0.);
        final double refC4 = BlackFormulaRepository.thetaMod(forward, inf, inf, vol, true, inf);
        final double refP4 = BlackFormulaRepository.thetaMod(forward, inf, inf, vol, false, inf);
        final double refC5 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, true, inf);
        final double refP5 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, false, inf);
        final double refC6 = BlackFormulaRepository.thetaMod(forward, inf, 0., vol, true, inf);
        final double refP6 = BlackFormulaRepository.thetaMod(forward, inf, 0., vol, false, inf);
        final double refC7 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, true, inf);
        final double refP7 = BlackFormulaRepository.thetaMod(forward, 0., inf, vol, false, inf);
        final double refC8 = BlackFormulaRepository.thetaMod(forward, 0., 0., vol, true, inf);
        final double refP8 = BlackFormulaRepository.thetaMod(forward, 0., 0., vol, false, inf);

        final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
          resC6, resP6, resC7, resP7, resC8, resP8 };
        final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
          refC6, refP6, refC7, refP7, refC8, refP8 };

        for (int k = 0; k < 14; ++k) {// some of ref values skipped
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (k != 5 && k != 9 && k != 12) {
            if (refVec[k] > 1.e10) {
              assertTrue(resVec[k] > 1.e10);
            } else {
              if (refVec[k] < -1.e10) {
                assertTrue(resVec[k] < -1.e10);
              } else {
                if (refVec[k] == 0.) {
                  assertTrue(Math.abs(resVec[k]) < 1.e-9);
                } else {
                  assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resC2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resC3 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 1.e-12);
      final double resP1 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resP2 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resP3 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 1.e-12);
      final double resC4 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP4 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC5 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, true, 1.e12);
      final double resP5 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12, false, 1.e12);
      final double resC6 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP6 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);
      final double resC7 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, true, 1.e-12);
      final double resP7 = BlackFormulaRepository.thetaMod(forward, 1.e12, TIME_TO_EXPIRY, 1.e12, false, 1.e-12);
      final double resC8 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, true, 1.e12);
      final double resP8 = BlackFormulaRepository.thetaMod(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12, false, 1.e12);

      final double refC1 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, 0., true, 0.);
      final double refC2 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, inf, true, 0.);
      final double refC3 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, 0., true, 0.);
      final double refP1 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, 0., false, 0.);
      final double refP2 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, inf, false, 0.);
      final double refP3 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, 0., false, 0.);
      final double refC4 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, inf, true, inf);
      final double refP4 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, inf, false, inf);
      final double refC5 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, inf, true, inf);
      final double refP5 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, inf, false, inf);
      final double refC6 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, 0., true, inf);
      final double refP6 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, 0., false, inf);
      final double refC7 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, inf, true, 0.);
      final double refP7 = BlackFormulaRepository.thetaMod(forward, inf, TIME_TO_EXPIRY, inf, false, 0.);
      final double refC8 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, 0., true, inf);
      final double refP8 = BlackFormulaRepository.thetaMod(forward, 0., TIME_TO_EXPIRY, 0., false, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7, resC8, resP8 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7, refC8, refP8 };

      for (int k = 0; k < 16; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 5 && k != 9 && k != 11 && k != 13 && k != 14) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-9);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(strike, strike, inf, 0., true, 1.);
      final double resP1 = BlackFormulaRepository.thetaMod(strike, strike, inf, 0., false, 1.);
      final double resC2 = BlackFormulaRepository.thetaMod(strike, strike, inf, 0., true, 0.);
      final double resP2 = BlackFormulaRepository.thetaMod(strike, strike, inf, 0., false, 0.);
      final double refC1 = strike * (NORMAL.getCDF(0.5));
      final double refP1 = -strike * (NORMAL.getCDF(-0.5));

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {refC1, refP1, 0., 0. };
      for (int k = 2; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, VOLS[0], true, -inf);
      final double resP1 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, VOLS[1], false, -inf);
      final double resC2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, VOLS[2], true, -inf);
      final double resP2 = BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, VOLS[3], false, -inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2 };
      final double[] refVec = new double[] {0., 0., 0., 0. };
      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.max(Math.abs(refVec[k]) * 1.e-10, 1.e-10));
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorthetaModTest() {
    BlackFormulaRepository.thetaMod(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5, true, 0.1);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorthetaModTest() {
    BlackFormulaRepository.thetaMod(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true, 0.1);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorthetaModTest() {
    BlackFormulaRepository.thetaMod(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1], true, 0.1);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorthetaModTest() {
    BlackFormulaRepository.thetaMod(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1], true, 0.1);
  }

  /**
   * 
   */
  @Test
  public void consistencyWithBlackScholestest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double interestRate = 0.02;
    final double df = Math.exp(-interestRate * TIME_TO_EXPIRY);
    final double spot = FORWARD * df;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (int i = 0; i < nStrikes; ++i) {
        for (int j = 0; j < nVols; ++j) {
          final double strike = STRIKES_INPUT[i];
          final double vol = VOLS[j];
          final double price1 = df *
              BlackFormulaRepository.thetaMod(FORWARD, strike, TIME_TO_EXPIRY, vol, isCall, interestRate);
          final double price2 = BlackScholesFormulaRepository.theta(spot, strike, TIME_TO_EXPIRY, vol, interestRate,
              interestRate, isCall);
          assertEquals(price1, price2, 1.e-14);
        }
      }
    }
  }

  /*
   * 
   * 
   * 
   * vega
   */
  /**
   * large/small input
   */
  @Test
  public void exVegaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vega(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.vega(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.vega(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.vega(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        // System.out.println(resC1 + "\t" + refC1);
        // System.out.println(resP1 + "\t" + refP1);
        // System.out.println(resC2 + "\t" + refC2);
        // System.out.println(resP2 + "\t" + refP2);

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vega(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.vega(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.vega(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.vega(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vega(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.vega(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.vega(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.vega(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vega(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.vega(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.vega(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.vega(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.vega(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.vega(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.vega(1.e12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resP3 = BlackFormulaRepository.vega(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.vega(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.vega(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.vega(inf, 0., TIME_TO_EXPIRY, vol);
      final double refP3 = BlackFormulaRepository.vega(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e12) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e12) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vega(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.vega(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.vega(1.e12, strike, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.vega(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.vega(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.vega(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.vega(inf, strike, 0., vol);
        final double refP3 = BlackFormulaRepository.vega(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vega(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vega(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vega(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.vega(1.e12, strike, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vega(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vega(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vega(inf, strike, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.vega(inf, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vega(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.vega(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.vega(forward, 1.e12, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.vega(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.vega(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.vega(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.vega(forward, inf, 0., vol);
        final double refP3 = BlackFormulaRepository.vega(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vega(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vega(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vega(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.vega(forward, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vega(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vega(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vega(forward, inf, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.vega(forward, inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.vega(1.e-12, 1.e-14, 1.e-24, vol);
      final double resC2 = BlackFormulaRepository.vega(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.vega(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.vega(1.e-12, 1.e-14, 1.e-24, vol);
      final double resP2 = BlackFormulaRepository.vega(1.e-12, 1.e-12, 1.e24, vol);
      final double resP3 = BlackFormulaRepository.vega(1.e-12, 1.e12, 1.e-24, vol);
      final double resC4 = BlackFormulaRepository.vega(1.e12, 1.e-12, 1.e-24, vol);
      final double resP4 = BlackFormulaRepository.vega(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.vega(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resP5 = BlackFormulaRepository.vega(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resC6 = BlackFormulaRepository.vega(1.e12, 1.e12, 1.e24, vol);
      final double resP6 = BlackFormulaRepository.vega(1.e12, 1.e-12, 1.e24, vol);
      final double resC7 = BlackFormulaRepository.vega(1.e12, 1.e12, 1.e-24, vol);
      final double resP7 = BlackFormulaRepository.vega(1.e-12, 1.e12, 1.e24, vol);

      final double refC1 = BlackFormulaRepository.vega(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.vega(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.vega(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.vega(0., 0., 0., vol);
      final double refP2 = BlackFormulaRepository.vega(0., 0., inf, vol);
      final double refP3 = BlackFormulaRepository.vega(0., inf, 0., vol);
      final double refC4 = BlackFormulaRepository.vega(inf, 0., 0., vol);
      final double refP4 = BlackFormulaRepository.vega(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.vega(FORWARD, FORWARD, 0., vol);
      final double refP5 = BlackFormulaRepository.vega(FORWARD, FORWARD, 0., vol);
      final double refC6 = BlackFormulaRepository.vega(inf, inf, inf, vol);
      final double refP6 = BlackFormulaRepository.vega(inf, 0., inf, vol);
      final double refC7 = BlackFormulaRepository.vega(inf, inf, 0., vol);
      final double refP7 = BlackFormulaRepository.vega(0., inf, inf, vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        // if (refVec[k] != forward * rootT * NORMAL.getPDF(0.);)
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-9);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.vega(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vega(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vega(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP1 = BlackFormulaRepository.vega(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP2 = BlackFormulaRepository.vega(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resP3 = BlackFormulaRepository.vega(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.vega(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP4 = BlackFormulaRepository.vega(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.vega(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resP5 = BlackFormulaRepository.vega(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resC6 = BlackFormulaRepository.vega(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e12);
      final double resP6 = BlackFormulaRepository.vega(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC7 = BlackFormulaRepository.vega(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP7 = BlackFormulaRepository.vega(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vega(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vega(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vega(0., inf, TIME_TO_EXPIRY, 0.);
      final double refP1 = BlackFormulaRepository.vega(0., 0., TIME_TO_EXPIRY, 0.);
      final double refP2 = BlackFormulaRepository.vega(0., 0., TIME_TO_EXPIRY, inf);
      final double refP3 = BlackFormulaRepository.vega(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.vega(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refP4 = BlackFormulaRepository.vega(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.vega(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refP5 = BlackFormulaRepository.vega(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refC6 = BlackFormulaRepository.vega(inf, inf, TIME_TO_EXPIRY, inf);
      final double refP6 = BlackFormulaRepository.vega(inf, 0., TIME_TO_EXPIRY, inf);
      final double refC7 = BlackFormulaRepository.vega(inf, inf, TIME_TO_EXPIRY, 0.);
      final double refP7 = BlackFormulaRepository.vega(0., inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] != FORWARD * Math.sqrt(TIME_TO_EXPIRY) * NORMAL.getPDF(0.)) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (Math.abs(refVec[k]) < 1.e-10) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      // final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.vega(FORWARD, 1.e-12, 1.e-24, 1.e-12);
      final double resC2 = BlackFormulaRepository.vega(FORWARD, 1.e-12, 1.e-24, 1.e12);
      final double resC3 = BlackFormulaRepository.vega(FORWARD, 1.e-12, 1.e24, 1.e-12);
      final double resP1 = BlackFormulaRepository.vega(FORWARD, 1.e-12, 1.e-24, 1.e-12);
      final double resP2 = BlackFormulaRepository.vega(FORWARD, 1.e-12, 1.e-24, 1.e12);
      final double resP3 = BlackFormulaRepository.vega(FORWARD, 1.e-12, 1.e24, 1.e-12);
      final double resC4 = BlackFormulaRepository.vega(FORWARD, 1.e12, 1.e-24, 1.e-12);
      final double resP4 = BlackFormulaRepository.vega(FORWARD, 1.e12, 1.e-24, 1.e-12);
      final double resC6 = BlackFormulaRepository.vega(FORWARD, 1.e12, 1.e24, 1.e12);
      final double resP6 = BlackFormulaRepository.vega(FORWARD, 1.e12, 1.e-24, 1.e12);
      final double resC7 = BlackFormulaRepository.vega(FORWARD, 1.e12, 1.e24, 1.e-12);
      final double resP7 = BlackFormulaRepository.vega(FORWARD, 1.e-12, 1.e24, 1.e12);

      final double refC1 = BlackFormulaRepository.vega(FORWARD, 0., 0., 0.);
      final double refC2 = BlackFormulaRepository.vega(FORWARD, 0., 0., inf);
      final double refC3 = BlackFormulaRepository.vega(FORWARD, 0., inf, 0.);
      final double refP1 = BlackFormulaRepository.vega(FORWARD, 0., 0., 0.);
      final double refP2 = BlackFormulaRepository.vega(FORWARD, 0., 0., inf);
      final double refP3 = BlackFormulaRepository.vega(FORWARD, 0., inf, 0.);
      final double refC4 = BlackFormulaRepository.vega(FORWARD, inf, 0., 0.);
      final double refP4 = BlackFormulaRepository.vega(FORWARD, inf, 0., 0.);
      final double refC6 = BlackFormulaRepository.vega(FORWARD, inf, inf, inf);
      final double refP6 = BlackFormulaRepository.vega(FORWARD, inf, 0., inf);
      final double refC7 = BlackFormulaRepository.vega(FORWARD, inf, inf, 0.);
      final double refP7 = BlackFormulaRepository.vega(FORWARD, 0., inf, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC6, resP6,
        resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC6, refP6,
        refC7, refP7 };

      for (int k = 0; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);

        // if (refVec[k] != forward * rootT * NORMAL.getPDF(0.);)
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-9);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorVegaTest() {
    BlackFormulaRepository.vega(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorVegaTest() {
    BlackFormulaRepository.vega(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorVegaTest() {
    BlackFormulaRepository.vega(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorVegaTest() {
    BlackFormulaRepository.vega(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test
  public void useSimpleOptionDataVegaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final SimpleOptionData dataC = new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., true);
        final SimpleOptionData dataP = new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., false);
        final double resC1 = BlackFormulaRepository.vega(FORWARD, strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.vega(dataC, vol);
        final double resP1 = BlackFormulaRepository.vega(FORWARD, strike, TIME_TO_EXPIRY, vol);
        final double resP2 = BlackFormulaRepository.vega(dataP, vol);
        assertEquals(resC1, resC2, EPS);
        assertEquals(resP1, resP2, EPS);
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSimpleOptionDataVegaTest() {
    SimpleOptionData data = new SimpleOptionData(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, 1., true);
    data = null;
    BlackFormulaRepository.vega(data, VOLS[1]);
  }

  /*
   * 
   * 
   * 
   * vanna
   */
  /**
   *
   */
  @Test
  public void exVannaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vanna(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.vanna(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.vanna(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.vanna(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vanna(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.vanna(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.vanna(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.vanna(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vanna(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.vanna(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.vanna(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.vanna(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vanna(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.vanna(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.vanna(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.vanna(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.vanna(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.vanna(1.e12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resP3 = BlackFormulaRepository.vanna(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.vanna(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.vanna(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.vanna(inf, inf, TIME_TO_EXPIRY, vol);
      final double refP3 = BlackFormulaRepository.vanna(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e12) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e12) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vanna(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.vanna(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.vanna(1.e12, strike, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.vanna(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.vanna(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.vanna(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.vanna(inf, strike, 0., vol);
        final double refP3 = BlackFormulaRepository.vanna(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vanna(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vanna(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vanna(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.vanna(1.e12, strike, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vanna(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vanna(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vanna(inf, strike, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.vanna(inf, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vanna(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.vanna(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.vanna(forward, 1.e12, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.vanna(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.vanna(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.vanna(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.vanna(forward, inf, 0., vol);
        final double refP3 = BlackFormulaRepository.vanna(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vanna(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vanna(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vanna(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.vanna(forward, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vanna(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vanna(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vanna(forward, inf, TIME_TO_EXPIRY, 0.);
      ;
      final double refP3 = BlackFormulaRepository.vanna(forward, inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, 1.e-24, vol);
      final double resC2 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.vanna(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.vanna(1.e-12, 1.e-14, 1.e-24, vol);
      final double resP2 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, 1.e24, vol);
      final double resP3 = BlackFormulaRepository.vanna(1.e-12, 1.e12, 1.e-24, vol);
      final double resC4 = BlackFormulaRepository.vanna(1.e12, 1.e-12, 1.e-24, vol);
      final double resP4 = BlackFormulaRepository.vanna(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.vanna(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resP5 = BlackFormulaRepository.vanna(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resC6 = BlackFormulaRepository.vanna(1.e12, 1.e12, 1.e24, vol);
      final double resP6 = BlackFormulaRepository.vanna(1.e12, 1.e-12, 1.e24, vol);
      final double resC7 = BlackFormulaRepository.vanna(1.e12, 1.e12, 1.e-24, vol);
      final double resP7 = BlackFormulaRepository.vanna(1.e-12, 1.e12, 1.e24, vol);

      final double refC1 = BlackFormulaRepository.vanna(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.vanna(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.vanna(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.vanna(0., 0., 0., vol);
      final double refP2 = BlackFormulaRepository.vanna(0., 0., inf, vol);
      final double refP3 = BlackFormulaRepository.vanna(0., inf, 0., vol);
      final double refC4 = BlackFormulaRepository.vanna(inf, 0., 0., vol);
      final double refP4 = BlackFormulaRepository.vanna(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.vanna(FORWARD, FORWARD, 0., vol);
      final double refP5 = BlackFormulaRepository.vanna(FORWARD, FORWARD, 0., vol);
      final double refC6 = BlackFormulaRepository.vanna(inf, inf, inf, vol);
      final double refP6 = BlackFormulaRepository.vanna(inf, 0., inf, vol);
      final double refC7 = BlackFormulaRepository.vanna(inf, inf, 0., vol);
      final double refP7 = BlackFormulaRepository.vanna(0., inf, inf, vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // refC5 and refP5 are ambiguous cases
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 8 && k != 9) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vanna(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP1 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP2 = BlackFormulaRepository.vanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resP3 = BlackFormulaRepository.vanna(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.vanna(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP4 = BlackFormulaRepository.vanna(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.vanna(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resP5 = BlackFormulaRepository.vanna(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resC6 = BlackFormulaRepository.vanna(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e12);
      final double resP6 = BlackFormulaRepository.vanna(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC7 = BlackFormulaRepository.vanna(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP7 = BlackFormulaRepository.vanna(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vanna(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vanna(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vanna(0., inf, TIME_TO_EXPIRY, 0.);
      final double refP1 = BlackFormulaRepository.vanna(0., 0., TIME_TO_EXPIRY, 0.);
      final double refP2 = BlackFormulaRepository.vanna(0., 0., TIME_TO_EXPIRY, inf);
      final double refP3 = BlackFormulaRepository.vanna(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.vanna(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refP4 = BlackFormulaRepository.vanna(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.vanna(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refP5 = BlackFormulaRepository.vanna(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refC6 = BlackFormulaRepository.vanna(inf, inf, TIME_TO_EXPIRY, inf);
      final double refP6 = BlackFormulaRepository.vanna(inf, 0., TIME_TO_EXPIRY, inf);
      final double refC7 = BlackFormulaRepository.vanna(inf, inf, TIME_TO_EXPIRY, 0.);
      final double refP7 = BlackFormulaRepository.vanna(0., inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 2; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 8 && k != 9) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (Math.abs(refVec[k]) < 1.e-10) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.vanna(FORWARD, 1.e-12, 1.e-24, 1.e-12);
      final double resC2 = BlackFormulaRepository.vanna(FORWARD, 1.e-12, 1.e-24, 1.e12);
      final double resC3 = BlackFormulaRepository.vanna(FORWARD, 1.e-12, 1.e24, 1.e-12);
      final double resP1 = BlackFormulaRepository.vanna(FORWARD, 1.e-12, 1.e-24, 1.e-12);
      final double resP2 = BlackFormulaRepository.vanna(FORWARD, 1.e-12, 1.e-24, 1.e12);
      final double resP3 = BlackFormulaRepository.vanna(FORWARD, 1.e-12, 1.e24, 1.e-12);
      final double resC4 = BlackFormulaRepository.vanna(FORWARD, 1.e12, 1.e-24, 1.e-12);
      final double resP4 = BlackFormulaRepository.vanna(FORWARD, 1.e12, 1.e-24, 1.e-12);
      final double resC6 = BlackFormulaRepository.vanna(FORWARD, 1.e12, 1.e24, 1.e12);
      final double resP6 = BlackFormulaRepository.vanna(FORWARD, 1.e12, 1.e-24, 1.e12);
      final double resC7 = BlackFormulaRepository.vanna(FORWARD, 1.e12, 1.e24, 1.e-12);
      final double resP7 = BlackFormulaRepository.vanna(FORWARD, 1.e-12, 1.e24, 1.e12);

      final double refC1 = BlackFormulaRepository.vanna(FORWARD, 0., 0., 0.);
      final double refC2 = BlackFormulaRepository.vanna(FORWARD, 0., 0., inf);
      final double refC3 = BlackFormulaRepository.vanna(FORWARD, 0., inf, 0.);
      final double refP1 = BlackFormulaRepository.vanna(FORWARD, 0., 0., 0.);
      final double refP2 = BlackFormulaRepository.vanna(FORWARD, 0., 0., inf);
      final double refP3 = BlackFormulaRepository.vanna(FORWARD, 0., inf, 0.);
      final double refC4 = BlackFormulaRepository.vanna(FORWARD, inf, 0., 0.);
      final double refP4 = BlackFormulaRepository.vanna(FORWARD, inf, 0., 0.);
      final double refC6 = BlackFormulaRepository.vanna(FORWARD, inf, inf, inf);
      final double refP6 = BlackFormulaRepository.vanna(FORWARD, inf, 0., inf);
      final double refC7 = BlackFormulaRepository.vanna(FORWARD, inf, inf, 0.);
      final double refP7 = BlackFormulaRepository.vanna(FORWARD, 0., inf, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC6, resP6,
        resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC6, refP6,
        refC7, refP7 };

      for (int k = 0; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (Math.abs(refVec[k]) < 1.e-10) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorVannaTest() {
    BlackFormulaRepository.vanna(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorVannaTest() {
    BlackFormulaRepository.vanna(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorVannaTest() {
    BlackFormulaRepository.vanna(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorVannaTest() {
    BlackFormulaRepository.vanna(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /*
   * 
   * 
   * 
   * dualVanna
   */
  /**
   * large/small input
   */
  @Test
  public void exDualVannaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualVanna(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.dualVanna(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.dualVanna(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.dualVanna(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualVanna(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.dualVanna(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.dualVanna(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.dualVanna(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualVanna(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.dualVanna(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.dualVanna(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.dualVanna(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualVanna(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.dualVanna(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.dualVanna(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.dualVanna(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.dualVanna(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.dualVanna(1.e12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resP3 = BlackFormulaRepository.dualVanna(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.dualVanna(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.dualVanna(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.dualVanna(inf, 0., TIME_TO_EXPIRY, vol);
      final double refP3 = BlackFormulaRepository.dualVanna(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e12) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e12) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualVanna(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.dualVanna(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.dualVanna(1.e12, strike, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.dualVanna(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.dualVanna(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.dualVanna(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.dualVanna(inf, strike, 0., vol);
        final double refP3 = BlackFormulaRepository.dualVanna(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualVanna(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.dualVanna(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.dualVanna(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.dualVanna(1.e12, strike, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.dualVanna(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.dualVanna(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.dualVanna(inf, strike, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.dualVanna(inf, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.dualVanna(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.dualVanna(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.dualVanna(forward, 1.e12, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.dualVanna(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.dualVanna(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.dualVanna(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.dualVanna(forward, inf, 0., vol);
        final double refP3 = BlackFormulaRepository.dualVanna(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.dualVanna(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.dualVanna(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.dualVanna(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.dualVanna(forward, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.dualVanna(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.dualVanna(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.dualVanna(forward, inf, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.dualVanna(forward, inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, 1.e-24, vol);
      final double resC2 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.dualVanna(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-14, 1.e-24, vol);
      final double resP2 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, 1.e24, vol);
      final double resP3 = BlackFormulaRepository.dualVanna(1.e-12, 1.e12, 1.e-24, vol);
      final double resC4 = BlackFormulaRepository.dualVanna(1.e12, 1.e-12, 1.e-24, vol);
      final double resP4 = BlackFormulaRepository.dualVanna(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resP5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resC6 = BlackFormulaRepository.dualVanna(1.e12, 1.e12, 1.e24, vol);
      final double resP6 = BlackFormulaRepository.dualVanna(1.e12, 1.e-12, 1.e24, vol);
      final double resC7 = BlackFormulaRepository.dualVanna(1.e12, 1.e12, 1.e-24, vol);
      final double resP7 = BlackFormulaRepository.dualVanna(1.e-12, 1.e12, 1.e24, vol);

      final double refC1 = BlackFormulaRepository.dualVanna(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.dualVanna(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.dualVanna(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.dualVanna(0., 0., 0., vol);
      final double refP2 = BlackFormulaRepository.dualVanna(0., 0., inf, vol);
      final double refP3 = BlackFormulaRepository.dualVanna(0., inf, 0., vol);
      final double refC4 = BlackFormulaRepository.dualVanna(inf, 0., 0., vol);
      final double refP4 = BlackFormulaRepository.dualVanna(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD, 0., vol);
      final double refP5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD, 0., vol);
      final double refC6 = BlackFormulaRepository.dualVanna(inf, inf, inf, vol);
      final double refP6 = BlackFormulaRepository.dualVanna(inf, 0., inf, vol);
      final double refC7 = BlackFormulaRepository.dualVanna(inf, inf, 0., vol);
      final double refP7 = BlackFormulaRepository.dualVanna(0., inf, inf, vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 8 && k != 9) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.dualVanna(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP1 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP2 = BlackFormulaRepository.dualVanna(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resP3 = BlackFormulaRepository.dualVanna(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.dualVanna(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP4 = BlackFormulaRepository.dualVanna(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resP5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resC6 = BlackFormulaRepository.dualVanna(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e12);
      final double resP6 = BlackFormulaRepository.dualVanna(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC7 = BlackFormulaRepository.dualVanna(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP7 = BlackFormulaRepository.dualVanna(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.dualVanna(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.dualVanna(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.dualVanna(0., inf, TIME_TO_EXPIRY, 0.);
      final double refP1 = BlackFormulaRepository.dualVanna(0., 0., TIME_TO_EXPIRY, 0.);
      final double refP2 = BlackFormulaRepository.dualVanna(0., 0., TIME_TO_EXPIRY, inf);
      final double refP3 = BlackFormulaRepository.dualVanna(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.dualVanna(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refP4 = BlackFormulaRepository.dualVanna(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refP5 = BlackFormulaRepository.dualVanna(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refC6 = BlackFormulaRepository.dualVanna(inf, inf, TIME_TO_EXPIRY, inf);
      final double refP6 = BlackFormulaRepository.dualVanna(inf, 0., TIME_TO_EXPIRY, inf);
      final double refC7 = BlackFormulaRepository.dualVanna(inf, inf, TIME_TO_EXPIRY, 0.);
      final double refP7 = BlackFormulaRepository.dualVanna(0., inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 2; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 8 && k != 9) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (Math.abs(refVec[k]) < 1.e-10) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.dualVanna(FORWARD, 1.e-12, 1.e-12, 1.e-12);
      final double resC2 = BlackFormulaRepository.dualVanna(FORWARD, 1.e-12, 1.e-12, 1.e12);
      final double resC3 = BlackFormulaRepository.dualVanna(FORWARD, 1.e-12, 1.e12, 1.e-12);
      final double resP1 = BlackFormulaRepository.dualVanna(FORWARD, 1.e-12, 1.e-12, 1.e-12);
      final double resP2 = BlackFormulaRepository.dualVanna(FORWARD, 1.e-12, 1.e-12, 1.e12);
      final double resP3 = BlackFormulaRepository.dualVanna(FORWARD, 1.e-12, 1.e12, 1.e-12);
      final double resC4 = BlackFormulaRepository.dualVanna(FORWARD, 1.e12, 1.e-12, 1.e-12);
      final double resP4 = BlackFormulaRepository.dualVanna(FORWARD, 1.e12, 1.e-12, 1.e-12);
      final double resC6 = BlackFormulaRepository.dualVanna(FORWARD, 1.e12, 1.e12, 1.e12);
      final double resP6 = BlackFormulaRepository.dualVanna(FORWARD, 1.e12, 1.e-12, 1.e12);
      final double resC7 = BlackFormulaRepository.dualVanna(FORWARD, 1.e12, 1.e12, 1.e-12);
      final double resP7 = BlackFormulaRepository.dualVanna(FORWARD, 1.e-12, 1.e12, 1.e12);

      final double refC1 = BlackFormulaRepository.dualVanna(FORWARD, 0., 0., 0.);
      final double refC2 = BlackFormulaRepository.dualVanna(FORWARD, 0., 0., inf);
      final double refC3 = BlackFormulaRepository.dualVanna(FORWARD, 0., inf, 0.);
      final double refP1 = BlackFormulaRepository.dualVanna(FORWARD, 0., 0., 0.);
      final double refP2 = BlackFormulaRepository.dualVanna(FORWARD, 0., 0., inf);
      final double refP3 = BlackFormulaRepository.dualVanna(FORWARD, 0., inf, 0.);
      final double refC4 = BlackFormulaRepository.dualVanna(FORWARD, inf, 0., 0.);
      final double refP4 = BlackFormulaRepository.dualVanna(FORWARD, inf, 0., 0.);
      final double refC6 = BlackFormulaRepository.dualVanna(FORWARD, inf, inf, inf);
      final double refP6 = BlackFormulaRepository.dualVanna(FORWARD, inf, 0., inf);
      final double refC7 = BlackFormulaRepository.dualVanna(FORWARD, inf, inf, 0.);
      final double refP7 = BlackFormulaRepository.dualVanna(FORWARD, 0., inf, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC6, resP6,
        resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC6, refP6,
        refC7, refP7 };

      for (int k = 0; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (Math.abs(refVec[k]) < 1.e-10) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorDualVannaTest() {
    BlackFormulaRepository.dualVanna(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorDualVannaTest() {
    BlackFormulaRepository.dualVanna(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorDualVannaTest() {
    BlackFormulaRepository.dualVanna(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorDualVannaTest() {
    BlackFormulaRepository.dualVanna(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /*
   * 
   * 
   * 
   * vomma
   */
  /**
   * large/small input
   */
  @Test
  public void exVommaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    final double inf = Double.POSITIVE_INFINITY;

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vomma(1.e-12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.vomma(0., strike, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.vomma(1.e12 * strike, strike, TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.vomma(inf, strike, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-11);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-11);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vomma(forward, 1.e-12 * forward, TIME_TO_EXPIRY, vol);
        final double resC2 = BlackFormulaRepository.vomma(forward, 1.e12 * forward, TIME_TO_EXPIRY, vol);
        final double refC1 = BlackFormulaRepository.vomma(forward, 0., TIME_TO_EXPIRY, vol);
        final double refC2 = BlackFormulaRepository.vomma(forward, inf, TIME_TO_EXPIRY, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vomma(FORWARD, strike, 1e-24, vol);
        final double resC2 = BlackFormulaRepository.vomma(FORWARD, strike, 1e24, vol);
        final double refC1 = BlackFormulaRepository.vomma(FORWARD, strike, 0., vol);
        final double refC2 = BlackFormulaRepository.vomma(FORWARD, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2 };
        final double[] refVec = new double[] {refC1, refC2 };

        for (int k = 0; k < 2; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e12);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-12);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vomma(FORWARD, strike, TIME_TO_EXPIRY, 1.e-12);
      final double refC1 = BlackFormulaRepository.vomma(FORWARD, strike, TIME_TO_EXPIRY, 0.);
      final double resC2 = BlackFormulaRepository.vomma(FORWARD, strike, TIME_TO_EXPIRY, 1.e12);
      final double refC2 = BlackFormulaRepository.vomma(FORWARD, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2 };
      final double[] refVec = new double[] {refC1, refC2 };

      for (int k = 0; k < 2; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e12);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resC2 = BlackFormulaRepository.vomma(1.e-12, 1.e12, TIME_TO_EXPIRY, vol);
      final double resC3 = BlackFormulaRepository.vomma(1.e12, 1.e-12, TIME_TO_EXPIRY, vol);
      final double resP3 = BlackFormulaRepository.vomma(1.e12, 1.e12, TIME_TO_EXPIRY, vol);

      final double refC1 = BlackFormulaRepository.vomma(0., 0., TIME_TO_EXPIRY, vol);
      final double refC2 = BlackFormulaRepository.vomma(0., inf, TIME_TO_EXPIRY, vol);
      final double refC3 = BlackFormulaRepository.vomma(inf, 0., TIME_TO_EXPIRY, vol);
      final double refP3 = BlackFormulaRepository.vomma(inf, inf, TIME_TO_EXPIRY, vol);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e12) {
          assertTrue(resVec[k] > 1.e12);
        } else {
          if (refVec[k] < -1.e12) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-12);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-12);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vomma(1.e-12, strike, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.vomma(1.e-12, strike, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.vomma(1.e12, strike, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.vomma(1.e12, strike, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.vomma(0., strike, 0., vol);
        final double refC2 = BlackFormulaRepository.vomma(0., strike, inf, vol);
        final double refC3 = BlackFormulaRepository.vomma(inf, strike, 0., vol);
        final double refP3 = BlackFormulaRepository.vomma(inf, strike, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double strike = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vomma(1.e-12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vomma(1.e-12, strike, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vomma(1.e12, strike, TIME_TO_EXPIRY, 1.e-12);
      final double resP3 = BlackFormulaRepository.vomma(1.e12, strike, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vomma(0., strike, TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vomma(0., strike, TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vomma(inf, strike, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.vomma(inf, strike, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double forward = STRIKES_INPUT[i];
        final double vol = VOLS[j];
        final double resC1 = BlackFormulaRepository.vomma(forward, 1.e-12, 1.e-24, vol);
        final double resC2 = BlackFormulaRepository.vomma(forward, 1.e-12, 1.e24, vol);
        final double resC3 = BlackFormulaRepository.vomma(forward, 1.e12, 1.e-24, vol);
        final double resP3 = BlackFormulaRepository.vomma(forward, 1.e12, 1.e24, vol);

        final double refC1 = BlackFormulaRepository.vomma(forward, 0., 0., vol);
        final double refC2 = BlackFormulaRepository.vomma(forward, 0., inf, vol);
        final double refC3 = BlackFormulaRepository.vomma(forward, inf, 0., vol);
        final double refP3 = BlackFormulaRepository.vomma(forward, inf, inf, vol);

        final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
        final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

        for (int k = 0; k < 4; ++k) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < nStrikes; ++i) {
      final double forward = STRIKES_INPUT[i];
      final double resC1 = BlackFormulaRepository.vomma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vomma(forward, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vomma(forward, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      ;
      final double resP3 = BlackFormulaRepository.vomma(forward, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vomma(forward, 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vomma(forward, 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vomma(forward, inf, TIME_TO_EXPIRY, 0.);
      final double refP3 = BlackFormulaRepository.vomma(forward, inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resC2, resC3, resP3 };
      final double[] refVec = new double[] {refC1, refC2, refC3, refP3 };

      for (int k = 0; k < 4; ++k) {
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (refVec[k] == 0.) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

    for (int j = 0; j < nVols; ++j) {
      final double vol = VOLS[j];
      final double resC1 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, 1.e-24, vol);
      final double resC2 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, 1.e24, vol);
      final double resC3 = BlackFormulaRepository.vomma(1.e-12, 1.e12, 1.e-24, vol);
      final double resP1 = BlackFormulaRepository.vomma(1.e-12, 1.e-14, 1.e-24, vol);
      final double resP2 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, 1.e24, vol);
      final double resP3 = BlackFormulaRepository.vomma(1.e-12, 1.e12, 1.e-24, vol);
      final double resC4 = BlackFormulaRepository.vomma(1.e12, 1.e-12, 1.e-24, vol);
      final double resP4 = BlackFormulaRepository.vomma(1.e12, 1.e-12, 1.e-24, vol);
      final double resC5 = BlackFormulaRepository.vomma(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resP5 = BlackFormulaRepository.vomma(FORWARD, FORWARD * (1. + 1.e-14), 1.e-24, vol);
      final double resC6 = BlackFormulaRepository.vomma(1.e12, 1.e12, 1.e24, vol);
      final double resP6 = BlackFormulaRepository.vomma(1.e12, 1.e-12, 1.e24, vol);
      final double resC7 = BlackFormulaRepository.vomma(1.e12, 1.e12, 1.e-24, vol);
      final double resP7 = BlackFormulaRepository.vomma(1.e-12, 1.e12, 1.e24, vol);

      final double refC1 = BlackFormulaRepository.vomma(0., 0., 0., vol);
      final double refC2 = BlackFormulaRepository.vomma(0., 0., inf, vol);
      final double refC3 = BlackFormulaRepository.vomma(0., inf, 0., vol);
      final double refP1 = BlackFormulaRepository.vomma(0., 0., 0., vol);
      final double refP2 = BlackFormulaRepository.vomma(0., 0., inf, vol);
      final double refP3 = BlackFormulaRepository.vomma(0., inf, 0., vol);
      final double refC4 = BlackFormulaRepository.vomma(inf, 0., 0., vol);
      final double refP4 = BlackFormulaRepository.vomma(inf, 0., 0., vol);
      final double refC5 = BlackFormulaRepository.vomma(FORWARD, FORWARD, 0., vol);
      final double refP5 = BlackFormulaRepository.vomma(FORWARD, FORWARD, 0., vol);
      final double refC6 = BlackFormulaRepository.vomma(inf, inf, inf, vol);
      final double refP6 = BlackFormulaRepository.vomma(inf, 0., inf, vol);
      final double refC7 = BlackFormulaRepository.vomma(inf, inf, 0., vol);
      final double refP7 = BlackFormulaRepository.vomma(0., inf, inf, vol);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 0; k < 14; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 12) {// ref val
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e12);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (refVec[k] == 0.) {
                assertTrue(Math.abs(resVec[k]) < 1.e-9);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC2 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC3 = BlackFormulaRepository.vomma(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP1 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP2 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resP3 = BlackFormulaRepository.vomma(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resC4 = BlackFormulaRepository.vomma(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resP4 = BlackFormulaRepository.vomma(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e-12);
      final double resC5 = BlackFormulaRepository.vomma(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resP5 = BlackFormulaRepository.vomma(FORWARD, FORWARD * (1. + 1.e-12), TIME_TO_EXPIRY, 1.e-12);
      final double resC6 = BlackFormulaRepository.vomma(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e12);
      final double resP6 = BlackFormulaRepository.vomma(1.e12, 1.e-12, TIME_TO_EXPIRY, 1.e12);
      final double resC7 = BlackFormulaRepository.vomma(1.e12, 1.e12, TIME_TO_EXPIRY, 1.e-12);
      final double resP7 = BlackFormulaRepository.vomma(1.e-12, 1.e12, TIME_TO_EXPIRY, 1.e12);

      final double refC1 = BlackFormulaRepository.vomma(0., 0., TIME_TO_EXPIRY, 0.);
      final double refC2 = BlackFormulaRepository.vomma(0., 0., TIME_TO_EXPIRY, inf);
      final double refC3 = BlackFormulaRepository.vomma(0., inf, TIME_TO_EXPIRY, 0.);
      final double refP1 = BlackFormulaRepository.vomma(0., 0., TIME_TO_EXPIRY, 0.);
      final double refP2 = BlackFormulaRepository.vomma(0., 0., TIME_TO_EXPIRY, inf);
      final double refP3 = BlackFormulaRepository.vomma(0., inf, TIME_TO_EXPIRY, 0.);
      final double refC4 = BlackFormulaRepository.vomma(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refP4 = BlackFormulaRepository.vomma(inf, 0., TIME_TO_EXPIRY, 0.);
      final double refC5 = BlackFormulaRepository.vomma(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refP5 = BlackFormulaRepository.vomma(FORWARD, FORWARD, TIME_TO_EXPIRY, 0.);
      final double refC6 = BlackFormulaRepository.vomma(inf, inf, TIME_TO_EXPIRY, inf);
      final double refP6 = BlackFormulaRepository.vomma(inf, 0., TIME_TO_EXPIRY, inf);
      final double refC7 = BlackFormulaRepository.vomma(inf, inf, TIME_TO_EXPIRY, 0.);
      final double refP7 = BlackFormulaRepository.vomma(0., inf, TIME_TO_EXPIRY, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC5, resP5,
        resC6, resP6, resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC5, refP5,
        refC6, refP6, refC7, refP7 };

      for (int k = 2; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (k != 8 && k != 9) {
          if (refVec[k] > 1.e10) {
            assertTrue(resVec[k] > 1.e10);
          } else {
            if (refVec[k] < -1.e10) {
              assertTrue(resVec[k] < -1.e10);
            } else {
              if (Math.abs(refVec[k]) < 1.e-10) {
                assertTrue(Math.abs(resVec[k]) < 1.e-10);
              } else {
                assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
              }
            }
          }
        }
      }
    }

    {
      final double resC1 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e-12, 1.e-12);
      final double resC2 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e-12, 1.e12);
      final double resC3 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e12, 1.e-12);
      final double resP1 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e-12, 1.e-12);
      final double resP2 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e-12, 1.e12);
      final double resP3 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e12, 1.e-12);
      final double resC4 = BlackFormulaRepository.vomma(FORWARD, 1.e12, 1.e-12, 1.e-12);
      final double resP4 = BlackFormulaRepository.vomma(FORWARD, 1.e12, 1.e-12, 1.e-12);
      final double resC6 = BlackFormulaRepository.vomma(FORWARD, 1.e12, 1.e12, 1.e12);
      final double resP6 = BlackFormulaRepository.vomma(FORWARD, 1.e12, 1.e-12, 1.e12);
      final double resC7 = BlackFormulaRepository.vomma(FORWARD, 1.e12, 1.e12, 1.e-12);
      final double resP7 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e12, 1.e12);

      final double refC1 = BlackFormulaRepository.vomma(FORWARD, 0., 0., 0.);
      final double refC2 = BlackFormulaRepository.vomma(FORWARD, 0., 0., inf);
      final double refC3 = BlackFormulaRepository.vomma(FORWARD, 0., inf, 0.);
      final double refP1 = BlackFormulaRepository.vomma(FORWARD, 0., 0., 0.);
      final double refP2 = BlackFormulaRepository.vomma(FORWARD, 0., 0., inf);
      final double refP3 = BlackFormulaRepository.vomma(FORWARD, 0., inf, 0.);
      final double refC4 = BlackFormulaRepository.vomma(FORWARD, inf, 0., 0.);
      final double refP4 = BlackFormulaRepository.vomma(FORWARD, inf, 0., 0.);
      final double refC6 = BlackFormulaRepository.vomma(FORWARD, inf, inf, inf);
      final double refP6 = BlackFormulaRepository.vomma(FORWARD, inf, 0., inf);
      final double refC7 = BlackFormulaRepository.vomma(FORWARD, inf, inf, 0.);
      final double refP7 = BlackFormulaRepository.vomma(FORWARD, 0., inf, inf);

      final double[] resVec = new double[] {resC1, resP1, resC2, resP2, resC3, resP3, resC4, resP4, resC6, resP6,
        resC7, resP7 };
      final double[] refVec = new double[] {refC1, refP1, refC2, refP2, refC3, refP3, refC4, refP4, refC6, refP6,
        refC7, refP7 };

      for (int k = 2; k < 12; ++k) {
        // System.out.println(k + "\t" + refVec[k] + "\t" + resVec[k]);
        if (refVec[k] > 1.e10) {
          assertTrue(resVec[k] > 1.e10);
        } else {
          if (refVec[k] < -1.e10) {
            assertTrue(resVec[k] < -1.e10);
          } else {
            if (Math.abs(refVec[k]) < 1.e-10) {
              assertTrue(Math.abs(resVec[k]) < 1.e-10);
            } else {
              assertEquals(refVec[k], resVec[k], Math.abs(refVec[k]) * 1.e-10);
            }
          }
        }
      }
    }

  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolErrorVommaTest() {
    BlackFormulaRepository.vomma(FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, -0.5);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorVommaTest() {
    BlackFormulaRepository.vomma(-FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorVommaTest() {
    BlackFormulaRepository.vomma(FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, VOLS[1]);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorVommaTest() {
    BlackFormulaRepository.vomma(FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, VOLS[1]);
  }

  /*
   * 
   * Volga test
   */
  /**
   *
   */
  @Test
  public void volgaTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];

        final double volga = BlackFormulaRepository.volga(FORWARD, strike, TIME_TO_EXPIRY, vol);
        final double vomma = BlackFormulaRepository.vomma(strike, FORWARD, TIME_TO_EXPIRY, vol);
        assertEquals(vomma, volga, Math.abs(vomma) * 1.e-8);

      }
    }
  }

  /*
   * 
   * Implied vol tests
   */
  /**
   *
   */
  @Test
  public void volRecoveryTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];

        final double cPrice = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, vol, true);
        final double pPrice = BlackFormulaRepository.price(FORWARD, strike, TIME_TO_EXPIRY, vol, false);
        final double cRes = BlackFormulaRepository.impliedVolatility(cPrice, FORWARD, strike, TIME_TO_EXPIRY, true);
        final double pRes = BlackFormulaRepository.impliedVolatility(pPrice, FORWARD, strike, TIME_TO_EXPIRY, false);
        assertEquals(vol, cRes, Math.abs(vol) * 1.e-8);
        assertEquals(vol, pRes, Math.abs(vol) * 1.e-8);

      }
    }
  }

  @Test
  public void impliedVolTest() {

    final double vol = 0.4342; // Deliberately picked an arbitrary vol
    final double t = 0.1;
    final double f = 0.01;
    final double p = 4.1;
    double ivCall = 0;
    double ivPut = 0;
    double iv = 0;

    for (int i = 0; i < 100; i++) {
      final double k = 0.004 + 0.022 * i / 100.;
      // final double k = 0.0327;
      final double cPrice = p * BlackFormulaRepository.price(f, k, t, vol, true);
      final double pPrice = p * BlackFormulaRepository.price(f, k, t, vol, false);

      ivCall = BlackFormulaRepository.impliedVolatility(cPrice / p, f, k, t, true);
      ivPut = BlackFormulaRepository.impliedVolatility(pPrice / p, f, k, t, false);
      final boolean isCall = k > f;
      final double otmP = (isCall ? cPrice : pPrice) / p;
      iv = BlackFormulaRepository.impliedVolatility(otmP, f, k, t, isCall);

      // System.out.println(k + "\t" + cPrice + "\t" + pPrice + "\t" + ivCall + "\t" + ivPut + "\t" + iv);

      // this is why we should compute OTM prices if an implied vol is required
      assertEquals(vol, ivCall, 5e-4);
      assertEquals(vol, ivPut, 2e-3);
      assertEquals(vol, iv, 1e-9);
    }

  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativePriceErrorImpliedVolatilityTest() {
    BlackFormulaRepository.impliedVolatility(-10., FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdErrorImpliedVolatilityTest() {
    BlackFormulaRepository.impliedVolatility(10., -FORWARD, STRIKES_INPUT[1], TIME_TO_EXPIRY, true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikeErrorImpliedVolatilityTest() {
    BlackFormulaRepository.impliedVolatility(10., FORWARD, -STRIKES_INPUT[1], TIME_TO_EXPIRY, true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeErrorImpliedVolatilityTest() {
    BlackFormulaRepository.impliedVolatility(10., FORWARD, STRIKES_INPUT[1], -TIME_TO_EXPIRY, true);
  }

  /**
   *
   */
  @Test
  public void volInitialGuessTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];

        final double zero = BlackFormulaRepository.impliedVolatility(0., FORWARD, strike, TIME_TO_EXPIRY, vol);
        final double atm = BlackFormulaRepository.impliedVolatility(Math.pow(strike, 0.6), strike, strike,
            TIME_TO_EXPIRY, vol);
        assertEquals(0., zero, Math.abs(vol) * 1.e-13);
        assertEquals(NORMAL.getInverseCDF(0.5 * (Math.pow(strike, 0.6) / strike + 1)) * 2 / Math.sqrt(TIME_TO_EXPIRY),
            atm, 1.e-13);

      }
    }
  }

  /**
   *
   */
  @Test
  public void volRecoveryFromDataTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];

        final SimpleOptionData dataC = new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., true);
        final SimpleOptionData dataP = new SimpleOptionData(FORWARD, strike, TIME_TO_EXPIRY, 1., false);
        final SimpleOptionData[] dataVec = new SimpleOptionData[] {dataC, dataC };

        final double cPrice = BlackFormulaRepository.price(dataC, vol);
        final double pPrice = BlackFormulaRepository.price(dataP, vol);
        final double cRes = BlackFormulaRepository.impliedVolatility(dataC, cPrice);
        final double pRes = BlackFormulaRepository.impliedVolatility(dataP, pPrice);
        final double res = BlackFormulaRepository.impliedVolatility(dataVec, 2. * cPrice);
        assertEquals(vol, cRes, Math.abs(vol) * 1.e-8);
        assertEquals(vol, pRes, Math.abs(vol) * 1.e-8);
        assertEquals(vol, res, Math.abs(vol) * 1.e-8);

      }
    }
  }

  /*
   * 
   * Implied strike tests
   */
  /**
   *
   */
  @Test
  public void strikeRecoveryTest() {
    final int nStrikes = STRIKES_INPUT.length;
    final int nVols = VOLS.length;
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nVols; ++j) {
        final double strike = STRIKES_INPUT[i];
        final double vol = VOLS[j];

        final double cDelta = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, vol, true);
        final double pdelta = BlackFormulaRepository.delta(FORWARD, strike, TIME_TO_EXPIRY, vol, false);
        final double cRes = BlackFormulaRepository.impliedStrike(cDelta, true, FORWARD, TIME_TO_EXPIRY, vol);
        final double pRes = BlackFormulaRepository.impliedStrike(pdelta, false, FORWARD, TIME_TO_EXPIRY, vol);
        assertEquals(strike, cRes, Math.abs(strike) * 1.e-8);
        assertEquals(strike, pRes, Math.abs(strike) * 1.e-8);

      }
    }
  }

  /*
   * 
   * Tests below are for debugging
   */

  /**
   *
   */
  @Test(enabled = false)
  public void sampleTest() {
    final double inf = Double.POSITIVE_INFINITY;
    // final double nan = Double.NaN;
    final double resC0 = BlackFormulaRepository.crossGamma(inf, FORWARD, 0.01, VOLS[2]);
    final double resC00 = BlackFormulaRepository.crossGamma(1.e12, FORWARD, 0.01, VOLS[2]);
    System.out.println(resC0 + "\t" + resC00);
    System.out.println("\n");
    final double resP0 = BlackFormulaRepository.crossGamma(inf, FORWARD, 0.01, VOLS[2]);
    final double resP00 = BlackFormulaRepository.crossGamma(1.e12, FORWARD, 0.01, VOLS[2]);
    System.out.println(resP0 + "\t" + resP00);
    System.out.println("\n");
    final double resC1 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, inf, 0.001, VOLS[2]);
    final double resC2 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, 1.e12, 0.01, VOLS[2]);
    System.out.println(resC1 + "\t" + resC2);
    System.out.println("\n");
    final double resP1 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, inf, 0.01, VOLS[2]);
    final double resP2 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, 1.e12, 0.01, VOLS[2]);
    System.out.println(resP1 + "\t" + resP2);
    System.out.println("\n");
    final double resC3 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, FORWARD, inf, VOLS[2]);
    final double resC4 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2]);
    System.out.println(resC3 + "\t" + resC4);
    System.out.println("\n");
    final double resP3 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, FORWARD, inf, VOLS[2]);
    final double resP4 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2]);
    System.out.println(resP3 + "\t" + resP4);
    System.out.println("\n");
    final double resC5 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, FORWARD, 1.e-12, VOLS[2]);
    final double resC6 = BlackFormulaRepository.crossGamma(FORWARD * 0.9, FORWARD, 1.e-11, VOLS[2]);
    System.out.println(resC5 + "\t" + resC6);
    System.out.println("\n");
    final double resP5 = BlackFormulaRepository.crossGamma(0., FORWARD, 0.01, VOLS[2]);
    final double resP6 = BlackFormulaRepository.crossGamma(1.e-12, FORWARD, 0.01, VOLS[2]);
    System.out.println(resP5 + "\t" + resP6);
    System.out.println("\n");
    final double resC7 = BlackFormulaRepository.crossGamma(0., 0., 0.01, VOLS[2]);
    final double resC8 = BlackFormulaRepository.crossGamma(1.e-12, 1.e-12, 0.01, VOLS[2]);
    System.out.println(resC7 + "\t" + resC8);
    System.out.println("\n");
    final double resP7 = BlackFormulaRepository.crossGamma(FORWARD, FORWARD, 0., VOLS[2]);
    final double resP8 = BlackFormulaRepository.crossGamma(FORWARD, FORWARD, 1.e-60, VOLS[2]);
    System.out.println(resP7 + "\t" + resP8);
    System.out.println("\n");
    final double resP9 = BlackFormulaRepository.crossGamma(FORWARD, 0., 0.01, VOLS[2]);
    final double resP10 = BlackFormulaRepository.crossGamma(FORWARD, 1.e-12, 0.01, VOLS[2]);
    System.out.println(resP9 + "\t" + resP10);
    System.out.println("\n");
    final double resC11 = BlackFormulaRepository.crossGamma(0., 0., 0., VOLS[2]);
    final double resC12 = BlackFormulaRepository.crossGamma(1.e-12, 1.e-12, 1.e-20, VOLS[2]);
    System.out.println(resC11 + "\t" + resC12);
    System.out.println("\n");
    final double resC13 = BlackFormulaRepository.crossGamma(FORWARD, 0., 0., VOLS[2]);
    final double resC14 = BlackFormulaRepository.crossGamma(FORWARD, 1.e-12, 1.e-20, VOLS[2]);
    System.out.println(resC13 + "\t" + resC14);
    System.out.println("\n");
    final double resC15 = BlackFormulaRepository.crossGamma(0., FORWARD, 0., VOLS[2]);
    final double resC16 = BlackFormulaRepository.crossGamma(1.e-12, FORWARD, 1.e-20, VOLS[2]);
    System.out.println(resC15 + "\t" + resC16);
    System.out.println("\n");
  }

  /**
   *
   */
  @Test(enabled = false)
  public void sample2Test() {
    final double inf = Double.POSITIVE_INFINITY;
    // final double nan = Double.NaN;
    final double resC0 = BlackFormulaRepository.price(inf, FORWARD, 0.01, VOLS[2], true);
    final double resC00 = BlackFormulaRepository.price(1.e14, FORWARD, 0.01, VOLS[2], true);
    System.out.println(resC0 + "\t" + resC00);
    System.out.println("\n");
    final double resP0 = BlackFormulaRepository.price(inf, FORWARD, 0.01, VOLS[2], false);
    final double resP00 = BlackFormulaRepository.price(1.e12, FORWARD, 0.01, VOLS[2], false);
    System.out.println(resP0 + "\t" + resP00);
    System.out.println("\n");
    final double resC1 = BlackFormulaRepository.price(FORWARD * 0.9, inf, 0.001, VOLS[2], true);
    final double resC2 = BlackFormulaRepository.price(FORWARD * 0.9, 1.e12, 0.01, VOLS[2], true);
    System.out.println(resC1 + "\t" + resC2);
    System.out.println("\n");
    final double resP1 = BlackFormulaRepository.price(FORWARD * 0.9, inf, 0.01, VOLS[2], false);
    final double resP2 = BlackFormulaRepository.price(FORWARD * 0.9, 1.e12, 0.01, VOLS[2], false);
    System.out.println(resP1 + "\t" + resP2);
    System.out.println("\n");
    final double resC3 = BlackFormulaRepository.price(FORWARD * 0.9, FORWARD, inf, VOLS[2], true);
    final double resC4 = BlackFormulaRepository.price(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2], true);
    System.out.println(resC3 + "\t" + resC4);
    System.out.println("\n");
    final double resP3 = BlackFormulaRepository.price(FORWARD * 0.9, FORWARD, inf, VOLS[2], false);
    final double resP4 = BlackFormulaRepository.price(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2], false);
    System.out.println(resP3 + "\t" + resP4);
    System.out.println("\n");
    final double resC5 = BlackFormulaRepository.price(FORWARD * 0.9, FORWARD, 1.e-12, VOLS[2], true);
    final double resC6 = BlackFormulaRepository.price(FORWARD * 0.9, FORWARD, 1.e-11, VOLS[2], true);
    System.out.println(resC5 + "\t" + resC6);
    System.out.println("\n");
    final double resP5 = BlackFormulaRepository.price(0., FORWARD, 0.01, VOLS[2], false);
    final double resP6 = BlackFormulaRepository.price(1.e-12, FORWARD, 0.01, VOLS[2], false);
    System.out.println(resP5 + "\t" + resP6);
    System.out.println("\n");
    final double resC7 = BlackFormulaRepository.price(0., 0., 0.01, VOLS[2], true);
    final double resC8 = BlackFormulaRepository.price(1.e-12, 1.e-12, 0.01, VOLS[2], true);
    System.out.println(resC7 + "\t" + resC8);
    System.out.println("\n");
    final double resP7 = BlackFormulaRepository.price(FORWARD, FORWARD, 0., VOLS[2], false);
    final double resP8 = BlackFormulaRepository.price(FORWARD, FORWARD, 1.e-60, VOLS[2], false);
    System.out.println(resP7 + "\t" + resP8);
    System.out.println("\n");
    final double resP9 = BlackFormulaRepository.price(FORWARD, 0., 0.01, VOLS[2], true);
    final double resP10 = BlackFormulaRepository.price(FORWARD, 1.e-12, 0.01, VOLS[2], true);
    System.out.println(resP9 + "\t" + resP10);
    System.out.println("\n");
    final double resC11 = BlackFormulaRepository.price(0., 0., 0., VOLS[2], false);
    final double resC12 = BlackFormulaRepository.price(1.e-12, 1.e-12, 1.e-20, VOLS[2], false);
    System.out.println(resC11 + "\t" + resC12);
    System.out.println("\n");
    final double resC13 = BlackFormulaRepository.price(FORWARD, 0., 0., VOLS[2], true);
    final double resC14 = BlackFormulaRepository.price(FORWARD, 1.e-12, 1.e-20, VOLS[2], true);
    System.out.println(resC13 + "\t" + resC14);
    System.out.println("\n");
    final double resC15 = BlackFormulaRepository.price(0., FORWARD, 0., VOLS[2], false);
    final double resC16 = BlackFormulaRepository.price(1.e-12, FORWARD, 1.e-20, VOLS[2], false);
    System.out.println(resC15 + "\t" + resC16);
    System.out.println("\n");
    final double resP17 = BlackFormulaRepository.price(FORWARD, 0., 0.01, VOLS[2], false);
    final double resP18 = BlackFormulaRepository.price(FORWARD, 1.e-12, 0.01, VOLS[2], false);
    System.out.println(resP17 + "\t" + resP18);
    System.out.println("\n");
  }

  /**
   *
   */
  @Test(enabled = false)
  public void sample3Test() {
    final double inf = Double.POSITIVE_INFINITY;
    // final double nan = Double.NaN;
    final double resC0 = BlackFormulaRepository.theta(inf, FORWARD, 0.01, VOLS[2], true, 0.05);
    final double resC00 = BlackFormulaRepository.theta(1.e14, FORWARD, 0.01, VOLS[2], true, 0.05);
    System.out.println(resC0 + "\t" + resC00);
    System.out.println("\n");
    final double resP0 = BlackFormulaRepository.theta(inf, FORWARD, 0.01, VOLS[2], false, 0.05);
    final double resP00 = BlackFormulaRepository.theta(1.e12, FORWARD, 0.01, VOLS[2], false, 0.05);
    System.out.println(resP0 + "\t" + resP00);
    System.out.println("\n");
    final double resC1 = BlackFormulaRepository.theta(FORWARD * 0.9, inf, 0.001, VOLS[2], true, 0.05);
    final double resC2 = BlackFormulaRepository.theta(FORWARD * 0.9, 1.e12, 0.01, VOLS[2], true, 0.05);
    System.out.println(resC1 + "\t" + resC2);
    System.out.println("\n");
    final double resP1 = BlackFormulaRepository.theta(FORWARD * 0.9, inf, 0.01, VOLS[2], false, 0.05);
    final double resP2 = BlackFormulaRepository.theta(FORWARD * 0.9, 1.e12, 0.01, VOLS[2], false, 0.05);
    System.out.println(resP1 + "\t" + resP2);
    System.out.println("\n");
    final double resC3 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, inf, VOLS[2], true, 0.05);
    final double resC4 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2], true, 0.05);
    System.out.println(resC3 + "\t" + resC4);
    System.out.println("\n");
    final double resP3 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, inf, VOLS[2], false, 0.05);
    final double resP4 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2], false, 0.05);
    System.out.println(resP3 + "\t" + resP4);
    System.out.println("\n");
    final double resC5 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, 1.e-12, VOLS[2], true, 0.05);
    final double resC6 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, 1.e-11, VOLS[2], true, 0.05);
    System.out.println(resC5 + "\t" + resC6);
    System.out.println("\n");
    final double resP5 = BlackFormulaRepository.theta(0., FORWARD, 0.01, VOLS[2], false, 0.05);
    final double resP6 = BlackFormulaRepository.theta(1.e-12, FORWARD, 0.01, VOLS[2], false, 0.05);
    System.out.println(resP5 + "\t" + resP6);
    System.out.println("\n");
    final double resC7 = BlackFormulaRepository.theta(0., 0., 0.01, VOLS[2], true, 0.05);
    final double resC8 = BlackFormulaRepository.theta(1.e-12, 1.e-12, 0.01, VOLS[2], true, 0.05);
    System.out.println(resC7 + "\t" + resC8);
    System.out.println("\n");
    final double resP7 = BlackFormulaRepository.theta(FORWARD, FORWARD, 0., VOLS[2], false, 0.05);
    final double resP8 = BlackFormulaRepository.theta(FORWARD, FORWARD, 1.e-60, VOLS[2], false, 0.05);
    System.out.println(resP7 + "\t" + resP8);
    System.out.println("\n");
    final double resP9 = BlackFormulaRepository.theta(FORWARD, 0., 0.01, VOLS[2], true, 0.05);
    final double resP10 = BlackFormulaRepository.theta(FORWARD, 1.e-12, 0.01, VOLS[2], true, 0.05);
    System.out.println(resP9 + "\t" + resP10);
    System.out.println("\n");
    final double resC11 = BlackFormulaRepository.theta(0., 0., 0., VOLS[2], false, 0.05);
    final double resC12 = BlackFormulaRepository.theta(1.e-12, 1.e-12, 1.e-24, VOLS[2], false, 0.05);
    System.out.println(resC11 + "\t" + resC12);
    System.out.println("\n");
    final double resC13 = BlackFormulaRepository.theta(FORWARD, 0., 0., VOLS[2], true, 0.05);
    final double resC14 = BlackFormulaRepository.theta(FORWARD, 1.e-12, 1.e-20, VOLS[2], true, 0.05);
    System.out.println(resC13 + "\t" + resC14);
    System.out.println("\n");
    final double resC15 = BlackFormulaRepository.theta(0., FORWARD, 0., VOLS[2], false, 0.05);
    final double resC16 = BlackFormulaRepository.theta(1.e-12, FORWARD, 1.e-20, VOLS[2], false, 0.05);
    System.out.println(resC15 + "\t" + resC16);
    System.out.println("\n");
    final double resC17 = BlackFormulaRepository.theta(FORWARD, inf, 1., VOLS[2], false, 0.05);
    final double resC18 = BlackFormulaRepository.theta(FORWARD, 1.e12, 1., VOLS[2], false, 0.05);
    System.out.println(resC17 + "\t" + resC18);
    System.out.println("\n");
    final double resC19 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, 1., inf, false, 0.05);
    final double resC20 = BlackFormulaRepository.theta(FORWARD * 0.9, FORWARD, 1., 1.e15, false, 0.05);
    System.out.println(resC19 + "\t" + resC20);
    System.out.println("\n");
  }

  /**
   *
   */
  @Test(enabled = false)
  public void sample4Test() {
    final double inf = Double.POSITIVE_INFINITY;
    // final double nan = Double.NaN;
    final double resC0 = BlackFormulaRepository.vomma(inf, FORWARD, 0.01, VOLS[2]);
    final double resC00 = BlackFormulaRepository.vomma(1.e14, FORWARD, 0.01, VOLS[2]);
    System.out.println(resC0 + "\t" + resC00);
    System.out.println("\n");
    final double resP0 = BlackFormulaRepository.vomma(inf, FORWARD, 0.01, VOLS[2]);
    final double resP00 = BlackFormulaRepository.vomma(1.e12, FORWARD, 0.01, VOLS[2]);
    System.out.println(resP0 + "\t" + resP00);
    System.out.println("\n");
    final double resC1 = BlackFormulaRepository.vomma(FORWARD * 0.9, inf, 0.001, VOLS[2]);
    final double resC2 = BlackFormulaRepository.vomma(FORWARD * 0.9, 1.e12, 0.01, VOLS[2]);
    System.out.println(resC1 + "\t" + resC2);
    System.out.println("\n");
    final double resP1 = BlackFormulaRepository.vomma(FORWARD * 0.9, inf, 0.01, VOLS[2]);
    final double resP2 = BlackFormulaRepository.vomma(FORWARD * 0.9, 1.e12, 0.01, VOLS[2]);
    System.out.println(resP1 + "\t" + resP2);
    System.out.println("\n");
    final double resC3 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, inf, VOLS[2]);
    final double resC4 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2]);
    System.out.println(resC3 + "\t" + resC4);
    System.out.println("\n");
    final double resP3 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, inf, VOLS[2]);
    final double resP4 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, 1.e12, VOLS[2]);
    System.out.println(resP3 + "\t" + resP4);
    System.out.println("\n");
    final double resC5 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, 1.e-12, VOLS[2]);
    final double resC6 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, 1.e-11, VOLS[2]);
    System.out.println(resC5 + "\t" + resC6);
    System.out.println("\n");
    final double resP5 = BlackFormulaRepository.vomma(0., FORWARD, 0.01, VOLS[2]);
    final double resP6 = BlackFormulaRepository.vomma(1.e-12, FORWARD, 0.01, VOLS[2]);
    System.out.println(resP5 + "\t" + resP6);
    System.out.println("\n");
    final double resC7 = BlackFormulaRepository.vomma(0., 0., 0.01, VOLS[2]);
    final double resC8 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, 0.01, VOLS[2]);
    System.out.println(resC7 + "\t" + resC8);
    System.out.println("\n");
    final double resP7 = BlackFormulaRepository.vomma(FORWARD, FORWARD, 0., VOLS[2]);
    final double resP8 = BlackFormulaRepository.vomma(FORWARD, FORWARD, 1.e-60, VOLS[2]);
    System.out.println(resP7 + "\t" + resP8);
    System.out.println("\n");
    final double resP9 = BlackFormulaRepository.vomma(FORWARD, 0., 0.01, VOLS[2]);
    final double resP10 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 0.01, VOLS[2]);
    System.out.println(resP9 + "\t" + resP10);
    System.out.println("\n");
    final double resC11 = BlackFormulaRepository.vomma(0., 0., 0., VOLS[2]);
    final double resC12 = BlackFormulaRepository.vomma(1.e-12, 1.e-12, 1.e-60, VOLS[2]);
    System.out.println(resC11 + "\t" + resC12);
    System.out.println("\n");
    final double resC13 = BlackFormulaRepository.vomma(FORWARD, 0., 0., VOLS[2]);
    final double resC14 = BlackFormulaRepository.vomma(FORWARD, 1.e-12, 1.e-12, VOLS[2]);
    System.out.println(resC13 + "\t" + resC14);
    System.out.println("\n");
    final double resC15 = BlackFormulaRepository.vomma(0., FORWARD, 0., VOLS[2]);
    final double resC16 = BlackFormulaRepository.vomma(1.e-12, FORWARD, 1.e-20, VOLS[2]);
    System.out.println(resC15 + "\t" + resC16);
    System.out.println("\n");
    final double resC17 = BlackFormulaRepository.vomma(FORWARD, inf, 1., VOLS[2]);
    final double resC18 = BlackFormulaRepository.vomma(FORWARD, 1.e12, 1., VOLS[2]);
    System.out.println(resC17 + "\t" + resC18);
    System.out.println("\n");
    final double resC19 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, 1., inf);
    final double resC20 = BlackFormulaRepository.vomma(FORWARD * 0.9, FORWARD, 1., 1.e15);
    System.out.println(resC19 + "\t" + resC20);
    System.out.println("\n");
  }

  /**
   *
   */
  @Test(enabled = false)
  public void sTest() {

    double forward = 140.;
    double strike = 140 + 1.e-10;
    double lognormalVol = 1.e-26;
    final double rootT = 2.;

    double d1 = Math.log(forward / strike) / lognormalVol / rootT + 0.5 * lognormalVol * rootT;
    final double d2 = Math.log(forward / strike) / lognormalVol / rootT - 0.5 * lognormalVol * rootT;
    System.out.println((-d2 * NORMAL.getPDF(d1) / lognormalVol));

    forward = 140.;
    strike = 140.;
    lognormalVol = 0.;
    d1 = Math.log(forward / strike) / lognormalVol / rootT + 0.5 * lognormalVol * rootT;
    System.out.println((-d2 * NORMAL.getPDF(d1) / lognormalVol));

  }
}
