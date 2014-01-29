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
  public void callModTest() {
    final double s0 = 1.35;
    final double time = 0.246;
    final double[] volData = new double[] {8. / 100., 7.35 / 100., 6.75 / 100., 6.65 / 100., 6.85 / 100. };
    final double rd = 0.06 / 100.;
    final double rf = 0.082 / 100.;
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

    final double[] putVolsExp = new double[] {0.0804044638015739, 0.0795181724337925, 0.0786617291709581, 0.0778351340130695, 0.0770383869601272, 0.0762714880121314, 0.0755344371690825,
        0.0748272344309794, 0.0741498797978221, 0.0735023732696123, 0.0728847148463478, 0.0722969045280303, 0.0717389423146585, 0.0712108282062337, 0.0707125622027547, 0.0702441443042227,
        0.0698055745106365, 0.0693968528219966, 0.0690179792383032, 0.0686689537595561, 0.0683497763857554 };

    final double[] callVolsExp = new double[] {0.0680604471169011, 0.0678009659529931, 0.0675713328940310, 0.0673715479400158, 0.0672016110909465, 0.0670615223468241, 0.0669512817076475,
        0.0668708891734178, 0.0668203447441340, 0.0667996484197965, 0.0668088002004055, 0.0668478000859608, 0.0669166480764625, 0.0670153441719100, 0.0671438883723045, 0.0673022806776453,
        0.0674905210879325, 0.0677086096031656, 0.0679565462233450, 0.0682343309484708 };

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

    final double[] putWeightsExp = new double[] {3.86612724961741, 4.26676765505071, 4.68620366756127, 5.12150973652564, 5.56923104329309, 6.02541643821822, 6.48566755627858, 6.94520360550750,
        7.39894069312904, 7.84158393960672, 8.26773006135017, 8.67197760763825, 9.04904164583095, 9.39386940976287, 9.70175331078434, 9.96843770622082, 10.1902160019830, 10.3640149664027,
        10.4874635729286, 10.5589442366225, 10.5776249407862 };
    final double[] callWeightsExp = new double[] {10.5412992391437, 10.4572393997860, 10.3204471515968, 10.1353301600597, 9.90477918894068, 9.63226441028596, 9.32174820898218, 8.97758966663260,
        8.60444385140665, 8.20715905244955, 7.79067498936842, 7.35992481413708, 6.91974340807581, 6.47478411360794, 6.02944559125494, 5.58781004978312, 5.15359362362750, 4.73010922033863,
        4.32024173650165, 3.92643516054523 };
    final double[] putPricesExp = new double[] {0.00262561131930905, 0.00287524325897690, 0.00315263108963929, 0.00346067070117392, 0.00380248187028501, 0.00418140603736952, 0.00460099915560247,
        0.00506501881324489, 0.00557740488399044, 0.00614225306692678, 0.00676378084422608, 0.00744628561491090, 0.00819409505658242, 0.00901151011819429, 0.00990274144376413, 0.0108718404507486,
        0.0119226267126107, 0.0130586136926287, 0.0142829352116669, 0.0155982752725142, 0.0170068039765215 };
    final double[] callPricesExp = new double[] {0.0178544406075641, 0.0162838358216021, 0.0148093521977040, 0.0134306742702354, 0.0121468276067401, 0.0109561954331448, 0.00985655095673205,
        0.00884510413081263, 0.00791856094703064, 0.00707319280737784, 0.00630491315394521, 0.00560935834112925, 0.00498196972687753, 0.00441807412514250, 0.00391296007574618, 0.00346194781501044,
        0.00306045132973937, 0.00270403140628472, 0.00238843910636713, 0.00210964957915266 };
    final double cashExp = 6.69901115297871;
    final double optionTotalExp = 2.905063898591213;
    final double fairValueExp = 9.604075051569918;

    final VolatilitySwapCalculatorResult res = calculator.evaluate(s0, putStrikes, callStrikes, time, rd, rf, putVols, callVols, rQV);
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
    final double forward = 105.;
    final double rate = Math.log(forward / s0) / time;
    final double rQV = 6.7 * 6.7;

    final double[] putStrikes = new double[] {70., 80., 90., 100. };
    final double[] callStrikes = new double[] {110., 120., 130. };
    final double[] putVols = new double[] {0.01 * 30., 0.01 * 22., 0.01 * 19., 0.01 * 15. };
    final double[] callVols = new double[] {0.01 * 11., 0.01 * 11., 0.01 * 9. };
    final int nPuts = putStrikes.length;
    final int nCalls = callStrikes.length;

    final double[] putWeightsExp = new double[] {0.164219291887088, 0.129647191901360, 0.122359678082097, 1.77520099049661 };
    final double[] callWeightsExp = new double[] {1.50926504727883, -0.0216140021581800, -0.0553115195745108 };
    final double[] putPricesExp = new double[] {0.185069301821616, 0.220173838830819, 0.775393995934012, 2.17655187245948 };
    final double[] callPricesExp = new double[] {1.34648036125710, 0.145887392233156, 0.000727204210875021 };
    final double cashExp = 6.380952380952381;
    final double optionTotalExp = 6.046633182128545;
    final double fairValueExp = 12.427585563080925;

    final VolatilitySwapCalculatorResult res = calculator.evaluate(s0, putStrikes, callStrikes, time, rate, 0., putVols, callVols, rQV);
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
  public void putModTest() {
    final double s0 = 90.;
    final double time = 1.;
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

    final double[] putWeightsExp = new double[] {0.0632227679352531, 0.0644669470890660, 0.0654729247603504, 0.0662316827894234, 0.0667373368415006, 0.0669871521589274, 0.0669814960180471,
        0.0667237311723738, 0.0662200558624038, 0.0654698794450783 };
    final double[] callWeightsExp = new double[] {0.0645126645334880, 0.0633334734350443, 0.0619568441400882, 0.0603993870400588, 0.0586788805160578, 0.0568139494461217, 0.0548237510664958,
        0.0527276742223698, 0.0505450571244024, 0.0482949279405268, 0.0459957713717994, 0.0436653237074037, 0.0413203976238064, 0.0389767375417652, 0.0366489052420849, 0.0343501949781924,
        0.0320925766851622, 0.0298866654271784, 0.0277417148614030, 0.0256656322322753, 0.0236650122405786 };
    final double[] putPricesExp = new double[] {0.135620281646017, 0.207936239866960, 0.305278515154164, 0.431116145097279, 0.588082679355679, 0.777829492090586, 1.00099725987368, 1.25728819993949,
        1.54560893638445, 1.86425077100896 };
    final double[] callPricesExp = new double[] {1.70297188945804, 1.51684380305268, 1.35402378725570, 1.21203578498272, 1.08850289240265, 0.981203743350072, 0.888105204894451, 0.807376772663126,
        0.737391704577554, 0.676719187321567, 0.624110953813698, 0.578484922095083, 0.538907688456989, 0.504577110357600, 0.474805756880448, 0.449005669656014, 0.426674642886608, 0.407384074703788,
        0.390768343406078, 0.376515604629310, 0.364359876314382 };
    final double cashExp = 9.854175488261811;
    final double optionTotalExp = 1.364793985648267;
    final double fairValueExp = 11.218969473910079;

    final VolatilitySwapCalculatorResult res = calculator.evaluate(s0, putStrikes, callStrikes, time, rd, rf, putVols, callVols, rQV);

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
      calculator.evaluate(spot, putStrikes, new double[] {50., 55., 60. }, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, new double[] {30., 35., 40., 45, }, callStrikes, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(-spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, -timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, callVols, -rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, new double[] {-50., 55. }, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, new double[] {-10., -5., 0., }, new double[] {5., 10. }, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, new double[] {35., 40., 43, }, callStrikes, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, new double[] {50., 60. }, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot - 10., putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot + 10., putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, new double[] {-15., 20. }, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      calculator.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, new double[] {-15., 20., 10 }, callVols, rvReturns);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

  }
}
