/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.PolynomialsLeastSquaresFitter;
import com.opengamma.analytics.math.regression.LeastSquaresRegressionResult;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CarrLeeSeasonedSyntheticVolatilitySwapCalculatorTest {
  private static final CarrLeeSeasonedSyntheticVolatilitySwapCalculator calculator = new CarrLeeSeasonedSyntheticVolatilitySwapCalculator();
  private static final double EPS = 1.e-10;

  /**
   * 
   */
  @Test
  public void putModTest() {
    final double s0 = 1.35;
    final double time = 3. / 12.;
    final double timeS = 1. / 12.;
    final double[] volData = new double[] {8. / 100., 7.35 / 100., 6.75 / 100., 6.65 / 100., 6.85 / 100. };
    final double rd = 6. * 0.01;
    final double rf = 8.2 * 0.01;
    final double rQV = 6.7 * 6.7;

    final double forward = s0 * Math.exp((rd - rf) * time);
    final double[] deltas = new double[] {10.0, 25.0, 50.0, 25.0, 10.0 };
    final int nPoints = 40;

    final double[] strikesData = new double[5];
    strikesData[0] = BlackFormulaRepository.strikeForDelta(forward, -deltas[0] / 100., time, volData[0], false);
    strikesData[1] = BlackFormulaRepository.strikeForDelta(forward, -deltas[1] / 100., time, volData[1], false);
    strikesData[2] = s0 * Math.exp((rf - rd) * time);
    strikesData[3] = BlackFormulaRepository.strikeForDelta(forward, deltas[3] / 100., time, volData[3], true);
    strikesData[4] = BlackFormulaRepository.strikeForDelta(forward, deltas[4] / 100., time, volData[4], true);

    final double tmp = 3.0 * Math.sqrt(rQV * timeS) / 100.;
    strikesData[0] = Math.min(strikesData[0], forward * Math.exp(-tmp));
    strikesData[4] = Math.max(strikesData[4], forward * Math.exp(tmp));

    final double deltaK = (strikesData[4] - strikesData[0]) / nPoints;
    final double[] strikes = new double[nPoints + 1];
    for (int i = 0; i < nPoints; ++i) {
      strikes[i] = strikesData[0] + deltaK * i;
    }
    strikes[nPoints] = strikesData[4];

    final int index = FunctionUtils.getLowerBoundIndex(strikes, forward); //wrong if strikes[i] == forward
    final int nPuts = index + 1;
    final int nCalls = nPoints - index;
    final double[] putStrikes = new double[nPuts];
    final double[] callStrikes = new double[nCalls];
    final double[] putVols = new double[nPuts];
    final double[] callVols = new double[nCalls];

    System.arraycopy(strikes, 0, putStrikes, 0, nPuts);
    System.arraycopy(strikes, index + 1, callStrikes, 0, nCalls);

    final double[] putVolsExp = new double[] {0.0804340060863720, 0.0795761243670020, 0.0787465184703351, 0.0779451883963705, 0.0771721341451092, 0.0764273557165501, 0.0757108531106940,
        0.0750226263275405, 0.0743626753670895, 0.0737310002293418, 0.0731276009142962, 0.0725524774219539, 0.0720056297523135, 0.0714870579053769, 0.0709967618811422, 0.0705347416796104,
        0.0701009973007811, 0.0696955287446548, 0.0693183360112315, 0.0689694191005103 };

    final double[] callVolsExp = new double[] {0.0686487780124921, 0.0683564127471763, 0.0680923233045635, 0.0678565096846532, 0.0676489718874458, 0.0674697099129409, 0.0673187237611396,
        0.0671960134320400, 0.0671015789256435, 0.0670354202419495, 0.0669975373809584, 0.0669879303426697, 0.0670065991270843, 0.0670535437342014, 0.0671287641640208, 0.0672322604165433,
        0.0673640324917682, 0.0675240803896961, 0.0677124041103269, 0.0679290036536600, 0.0681738790196960 };

    final PolynomialsLeastSquaresFitter fitter = new PolynomialsLeastSquaresFitter();
    final LeastSquaresRegressionResult polyRes = fitter.regress(strikesData, volData, 2);
    final DoubleFunction1D func = new RealPolynomialFunction1D(polyRes.getBetas());
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = func.evaluate(putStrikes[i]);
      assertEquals(putVolsExp[i], putVols[i], 1.e-12);
    }
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = func.evaluate(callStrikes[i]);
      assertEquals(callVolsExp[i], callVols[i], 1.e-12);
    }

    final double[] putWeightsExp = new double[] {0.493726715658473, 0.628628866130493, 0.827612209950565, 1.11222515249467, 1.50711421506441, 2.03860572903232, 2.73241720665300, 3.61052218485728,
        4.68736633875543, 5.96582751113782, 7.43348794602065, 9.05989559493323, 10.7954886714047, 12.5727165376716, 14.3096121480158, 15.9156924643967, 17.2996501421644, 18.3779367534582,
        19.0831073735407, 19.3525727276836 };
    final double[] callWeightsExp = new double[] {19.2240572893118, 18.6552385457138, 17.7038720939929, 16.4322515091469, 14.9186283170502, 13.2493060912483, 11.5106812103248, 9.78221050947683,
        8.13103318509909, 6.60864038436859, 5.24964209388712, 4.07238883753048, 3.08100597371603, 2.26830633613504, 1.61905459910282, 1.11313960277885, 0.728336809569212, 0.442480546242545,
        0.234989746438180, 0.0877860725848968, -0.0142962965209807 };
    final double[] putPricesExp = new double[] {0.00172289028574982, 0.00195648039309593, 0.00222331473644613, 0.00252775510621570, 0.00287459167863147, 0.00326904601130995, 0.00371676325061859,
        0.00422379133699968, 0.00479654510618413, 0.00544175346638975, 0.00616638831037136, 0.00697757451104625, 0.00788248124764240, 0.00888819599117097, 0.0100015836931923, 0.0112291349938216,
        0.0125768084935084, 0.0140498732014309, 0.0156527580585205, 0.0173889158205927 };
    final double[] callPricesExp = new double[] {0.0170336406385245, 0.0152029725262308, 0.0135090785741024, 0.0119506671568582, 0.0105252235222882, 0.00922907110103699, 0.00805746835819465,
        0.00700473636749643, 0.00606441061399821, 0.00522940936931082, 0.00449221042015183, 0.00384502798574513, 0.00327998228742407, 0.00278925533444108, 0.00236522792464938, 0.00200059447001055,
        0.00168845388930539, 0.00142237632781486, 0.00119644676171908, 0.00100528755731055, 0.000844062754189821 };
    final double cashExp = 3.300124997670261;
    final double optionTotalExp = 3.471819024521614;
    final double fairValueExp = 6.771944022191875;

    final VolatilitySwapCalculatorResult res = calculator.evaluate(s0, putStrikes, callStrikes, time, timeS, rd, rf, putVols, callVols, rQV);

    final double[] putWeights = res.getPutWeights();
    final double[] callWeights = res.getCallWeights();
    final double[] putPrices = res.getPutPrices();
    final double[] callPrices = res.getCallPrices();

    for (int i = 0; i < nPuts; ++i) {
      assertEquals(putWeightsExp[i], putWeights[i], Math.max(Math.abs(putWeightsExp[i]), 1.) * EPS);
      assertEquals(putPricesExp[i], putPrices[i], Math.max(Math.abs(putPricesExp[i]), 1.) * EPS);
    }
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(callWeightsExp[i], callWeights[i], Math.max(Math.abs(callWeightsExp[i]), 1.) * EPS);
      assertEquals(callPricesExp[i], callPrices[i], Math.max(Math.abs(callPricesExp[i]), 1.) * EPS);
    }
    assertEquals(cashExp, res.getCash(), Math.max(Math.abs(cashExp), 1.) * EPS);
    assertEquals(optionTotalExp, res.getOptionTotal(), Math.max(Math.abs(optionTotalExp), 1.) * EPS);
    assertEquals(fairValueExp, res.getFairValue(), Math.max(Math.abs(fairValueExp), 1.) * EPS);

    assertEquals(0., res.getStraddleWeight());
    assertEquals(0., res.getStraddlePrice());
  }

  /**
   * 
   */
  @Test
  public void noModTest() {
    final double s0 = 100.;
    final double time = 0.5;
    final double timeS = 0.6;
    final double forward = 105.;
    final double rate = Math.log(forward / s0) / time;
    final double rQV = 6.7 * 6.7;

    final double[] putStrikes = new double[] {70., 80., 90., 100. };
    final double[] callStrikes = new double[] {110., 120., 130. };
    final double[] putVols = new double[] {0.01 * 30., 0.01 * 22., 0.01 * 19., 0.01 * 15. };
    final double[] callVols = new double[] {0.01 * 11., 0.01 * 11., 0.01 * 9. };
    final int nPuts = putStrikes.length;
    final int nCalls = callStrikes.length;

    final double[] putWeightsExp = new double[] {0.110713674914064, 0.0874082803121186, 0.0962983872737028, 1.19103156830239 };
    final double[] callWeightsExp = new double[] {1.00843583960796, 0.00731609252897404, -0.0370857540745987 };
    final double[] putPricesExp = new double[] {0.185069301821616, 0.220173838830819, 0.775393995934012, 2.17655187245948 };
    final double[] callPricesExp = new double[] {1.34648036125710, 0.145887392233156, 0.000727204210875021 };
    final double cashExp = 4.712645654637311;
    final double optionTotalExp = 4.065625310961250;
    final double fairValueExp = 8.778270965598562;

    final VolatilitySwapCalculatorResult res = calculator.evaluate(s0, putStrikes, callStrikes, time, timeS, rate, 0., putVols, callVols, rQV);
    final double[] putWeights = res.getPutWeights();
    final double[] callWeights = res.getCallWeights();
    final double[] putPrices = res.getPutPrices();
    final double[] callPrices = res.getCallPrices();

    for (int i = 0; i < nPuts; ++i) {
      assertEquals(putWeightsExp[i], putWeights[i], Math.max(Math.abs(putWeightsExp[i]), 1.) * EPS);
      assertEquals(putPricesExp[i], putPrices[i], Math.max(Math.abs(putPricesExp[i]), 1.) * EPS);
    }
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(callWeightsExp[i], callWeights[i], Math.max(Math.abs(callWeightsExp[i]), 1.) * EPS);
      assertEquals(callPricesExp[i], callPrices[i], Math.max(Math.abs(callPricesExp[i]), 1.) * EPS);
    }
    assertEquals(cashExp, res.getCash(), Math.max(Math.abs(cashExp), 1.) * EPS);
    assertEquals(optionTotalExp, res.getOptionTotal(), Math.max(Math.abs(optionTotalExp), 1.) * EPS);
    assertEquals(fairValueExp, res.getFairValue(), Math.max(Math.abs(fairValueExp), 1.) * EPS);

    assertEquals(0., res.getStraddleWeight());
    assertEquals(0., res.getStraddlePrice());
  }

  /**
   * 
   */
  @Test
  public void callModTest() {
    final double s0 = 93.;
    final double time = 3. / 12.;
    final double timeS = 9. / 12.;
    final double[] volData = new double[] {5. / 100., 5.35 / 100., 3.75 / 100., 6.65 / 100., 9.85 / 100. };
    final double rd = 0.01 * 11.;
    final double rf = 0.01 * 7.6;
    final double rQV = 11. * 11.;

    final double forward = s0 * Math.exp((rd - rf) * time);
    final double[] deltas = new double[] {10.0, 25.0, 50.0, 25.0, 10.0 };
    final int nPoints = 30;

    final double[] strikesData = new double[5];
    strikesData[0] = BlackFormulaRepository.strikeForDelta(forward, -deltas[0] / 100., time, volData[0], false);
    strikesData[1] = BlackFormulaRepository.strikeForDelta(forward, -deltas[1] / 100., time, volData[1], false);
    strikesData[2] = s0 * Math.exp((rf - rd) * time);
    strikesData[3] = BlackFormulaRepository.strikeForDelta(forward, deltas[3] / 100., time, volData[3], true);
    strikesData[4] = BlackFormulaRepository.strikeForDelta(forward, deltas[4] / 100., time, volData[4], true);

    final double tmp = 3.0 * Math.sqrt(rQV * timeS) / 100.;
    strikesData[0] = Math.min(strikesData[0], forward * Math.exp(-tmp));
    strikesData[4] = Math.max(strikesData[4], forward * Math.exp(tmp));

    final double deltaK = (strikesData[4] - strikesData[0]) / nPoints;
    final double[] strikes = new double[nPoints + 1];
    for (int i = 0; i < nPoints; ++i) {
      strikes[i] = strikesData[0] + deltaK * i;
    }
    strikes[nPoints] = strikesData[4];

    final int index = FunctionUtils.getLowerBoundIndex(strikes, forward); //wrong if strikes[i] == forward
    final int nPuts = index + 1;
    final int nCalls = nPoints - index;
    final double[] putStrikes = new double[nPuts];
    final double[] callStrikes = new double[nCalls];
    final double[] putVols = new double[nPuts];
    final double[] callVols = new double[nCalls];

    System.arraycopy(strikes, 0, putStrikes, 0, nPuts);
    System.arraycopy(strikes, index + 1, callStrikes, 0, nCalls);

    final PolynomialsLeastSquaresFitter fitter = new PolynomialsLeastSquaresFitter();
    final LeastSquaresRegressionResult polyRes = fitter.regress(strikesData, volData, 2);
    final DoubleFunction1D func = new RealPolynomialFunction1D(polyRes.getBetas());
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = func.evaluate(putStrikes[i]);
    }
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = func.evaluate(callStrikes[i]);
    }

    final double[] putWeightsExp = new double[] {0.0249474493801584, 0.0278727573073654, 0.0336865845443051, 0.0434456985487412, 0.0580049953306695, 0.0776753838333571, 0.101919999499214,
        0.129212732725244, 0.157145855665674, 0.182792230742784, 0.203236485321117, 0.216128826978535, 0.220109753615713 };
    final double[] callWeightsExp = new double[] {0.214660120905978, 0.201745432852571, 0.182119954984908, 0.158359265590309, 0.132753739494195, 0.107329622250425, 0.0836477737614967,
        0.0627278642026246, 0.0450752973751680, 0.0307742980521283, 0.0196098079587889, 0.0111885052370588, 0.00504032962375662, 0.000692458429669695, -0.00228441565968938, -0.00425299389125248,
        -0.00550311359439542, -0.00625606821071965 };
    final double[] putPricesExp = new double[] {1.20495882111873e-32, 3.30831077550115e-28, 5.65674306042692e-24, 5.19862512756670e-20, 2.25876128639990e-16, 4.19066519964571e-13,
        3.09643184999959e-10, 8.80773444437869e-08, 9.67406079316530e-06, 0.000426604440314463, 0.00811624526118482, 0.0736397606725694, 0.359935395501147 };
    final double[] callPricesExp = new double[] {0.856472747478669, 0.305773377958982, 0.0878441615157666, 0.0212128441537016, 0.00455088435984047, 0.000918360481547489, 0.000183780702638331,
        3.81651640754552e-05, 8.53170609677501e-06, 2.11104302097817e-06, 5.89621768995195e-07, 1.88210261079810e-07, 6.90926903853884e-08, 2.92088165207333e-08, 1.41835145257063e-08,
        7.86830752379933e-09, 4.94943320546829e-09, 3.49920498057163e-09 };
    final double cashExp = 9.267876087690137;
    final double optionTotalExp = 0.362487290806207;
    final double fairValueExp = 9.630363378496344;

    final VolatilitySwapCalculatorResult res = calculator.evaluate(s0, putStrikes, callStrikes, time, timeS, rd, rf, putVols, callVols, rQV);

    final double[] putWeights = res.getPutWeights();
    final double[] callWeights = res.getCallWeights();
    final double[] putPrices = res.getPutPrices();
    final double[] callPrices = res.getCallPrices();

    for (int i = 0; i < nPuts; ++i) {
      assertEquals(putWeightsExp[i], putWeights[i], Math.max(Math.abs(putWeightsExp[i]), 1.) * EPS);
      assertEquals(putPricesExp[i], putPrices[i], Math.max(Math.abs(putPricesExp[i]), 1.) * EPS);
    }
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(callWeightsExp[i], callWeights[i], Math.max(Math.abs(callWeightsExp[i]), 1.) * EPS);
      assertEquals(callPricesExp[i], callPrices[i], Math.max(Math.abs(callPricesExp[i]), 1.) * EPS);
    }
    assertEquals(cashExp, res.getCash(), Math.max(Math.abs(cashExp), 1.) * EPS);
    assertEquals(optionTotalExp, res.getOptionTotal(), Math.max(Math.abs(optionTotalExp), 1.) * EPS);
    assertEquals(fairValueExp, res.getFairValue(), Math.max(Math.abs(fairValueExp), 1.) * EPS);

    assertEquals(0., res.getStraddleWeight());
    assertEquals(0., res.getStraddlePrice());
  }

  /**
   * 
   */
  @Test
  public void errorTest() {
    final double timeToExpiry = 3.;
    final double timeSeasoned = 2.;
    final double spot = 45.;
    //    final double forward = 102.;
    //    final double interestRate = Math.log(forward / spot) / timeToExpiry;
    final double interestRate = 0.025;
    final double dividend = 0.015;
    final double[] putStrikes = new double[] {35., 40., 45, };
    final double[] callStrikes = new double[] {50., 55. };
    final double rvReturns = 100.;

    final int nCalls = callStrikes.length;
    final int nPuts = putStrikes.length;
    final double[] callVols = new double[nCalls];
    final double[] putVols = new double[nPuts];
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = (0.5 - 0.005 * (callStrikes[i] - 50.));
    }
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = (0.5 - 0.005 * (putStrikes[i] - 50.));
    }

    try {
      calculator.evaluate(spot, putStrikes, new double[] {50., 55., 60. }, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, new double[] {30., 35., 40., 45, }, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(-spot, putStrikes, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, -timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, timeToExpiry, -timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, -rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, new double[] {-50., 55. }, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, new double[] {-10., -5., 0., }, new double[] {5., 10. }, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, new double[] {35., 40., 43, }, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, new double[] {50., 60. }, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot - 10., putStrikes, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot + 10., putStrikes, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, putVols, new double[] {-15., 20. }, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, timeToExpiry, timeSeasoned, interestRate, dividend, new double[] {-15., 20., 10 }, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

  }
}
