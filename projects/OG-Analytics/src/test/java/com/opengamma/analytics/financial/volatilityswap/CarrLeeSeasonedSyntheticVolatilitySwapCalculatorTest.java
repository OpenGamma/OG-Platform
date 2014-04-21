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
    strikesData[2] = s0 * Math.exp((rd - rf) * time + 0.5 * volData[2] * volData[2] * time);
    strikesData[3] = BlackFormulaRepository.strikeForDelta(forward, deltas[3] / 100., time, volData[3], true);
    strikesData[4] = BlackFormulaRepository.strikeForDelta(forward, deltas[4] / 100., time, volData[4], true);

    final PolynomialsLeastSquaresFitter fitter = new PolynomialsLeastSquaresFitter();
    final LeastSquaresRegressionResult polyRes = fitter.regress(strikesData, volData, 2);
    final DoubleFunction1D func = new RealPolynomialFunction1D(polyRes.getBetas());

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

    final double[] putVolsExp = new double[] {0.0832552878863839, 0.0820658728548438, 0.0809211151890594, 0.0798210148890308, 0.0787655719547581, 0.0777547863862412, 0.0767886581834796,
        0.0758671873464743, 0.0749903738752249, 0.0741582177697308, 0.0733707190299930, 0.0726278776560110, 0.0719296936477843, 0.0712761670053135, 0.0706672977285990, 0.0701030858176398,
        0.0695835312724364, 0.0691086340929894, 0.0686783942792977, 0.0682928118313618 };
    final double[] callVolsExp = new double[] {0.0679518867491817, 0.0676556190327574, 0.0674040086820889, 0.0671970556971769, 0.0670347600780201, 0.0669171218246191, 0.0668441409369740,
        0.0668158174150847, 0.0668321512589506, 0.0668931424685729, 0.0669987910439511, 0.0671490969850845, 0.0673440602919743, 0.0675836809646199, 0.0678679590030208, 0.0681968944071775,
        0.0685704871770906, 0.0689887373127590, 0.0694516448141837, 0.0699592096813638, 0.0705114319142996 };

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
    final double[] putPricesExp = new double[] {0.00198741313032555, 0.00220991254128430, 0.00246339326567738, 0.00275207254083201, 0.00308064108585229, 0.00345428045427093, 0.00387867000987358,
        0.00435998037307128, 0.00490485005152944, 0.00552034205138247, 0.00621387763252368, 0.00699314508220150, 0.00786598247302106, 0.00884023484680213, 0.00992358807879118, 0.0111233837243506,
        0.0124464212698491, 0.0138987561867745, 0.0154855037767936, 0.0172106597478404 };
    final double[] callPricesExp = new double[] {0.0168498807190164, 0.0150193614714781, 0.0133312017321700, 0.0117838205591906, 0.0103742071364744, 0.00909801123052711, 0.00794968063068208,
        0.00692263713685887, 0.00600948031305543, 0.00520220691869372, 0.00449243374340727, 0.00387161243813322, 0.00333122666620575, 0.00286296421789065, 0.00245885933057685, 0.00211140303779320,
        0.00181362168791471, 0.00155912565745443, 0.00134213164620579, 0.00115746277012070, 0.00100053101036114 };
    final double cashExp = 3.300124997670261;
    final double optionTotalExp = 3.441793507870262;
    final double fairValueExp = 6.741918505540522;

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
    strikesData[2] = s0 * Math.exp((rd - rf) * time + 0.5 * volData[2] * volData[2] * time);
    strikesData[3] = BlackFormulaRepository.strikeForDelta(forward, deltas[3] / 100., time, volData[3], true);
    strikesData[4] = BlackFormulaRepository.strikeForDelta(forward, deltas[4] / 100., time, volData[4], true);

    final PolynomialsLeastSquaresFitter fitter = new PolynomialsLeastSquaresFitter();
    final LeastSquaresRegressionResult polyRes = fitter.regress(strikesData, volData, 2);
    final DoubleFunction1D func = new RealPolynomialFunction1D(polyRes.getBetas());

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
        -0.00550311359439542, -0.00625606821071965, };
    final double[] putPricesExp = new double[] {1.19412646189383, 0.895574027475140, 0.647382415012651, 0.448389905653081, 0.295854285146079, 0.185315976208589, 0.110656287875750, 0.0645371103123034,
        0.0394278594390807, 0.0294929145915459, 0.0349762054007776, 0.0813391379385813, 0.304192858788216, };
    final double[] callPricesExp = new double[] {0.799795056444808, 0.354129640932978, 0.222288963309337, 0.209442912694495, 0.258327513592855, 0.362120332889372, 0.528740559236759,
        0.770056173891117, 1.09820783807841, 1.52424446460731, 2.05768514647612, 2.70647688359544, 3.47710383758826, 4.37473310344900, 5.40334703896093, 6.56584443143825, 7.86410774179835,
        9.29903990372668 };
    final double cashExp = 9.267876087690137;
    final double optionTotalExp = 0.778792024176818;
    final double fairValueExp = 10.046668111866955;

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
