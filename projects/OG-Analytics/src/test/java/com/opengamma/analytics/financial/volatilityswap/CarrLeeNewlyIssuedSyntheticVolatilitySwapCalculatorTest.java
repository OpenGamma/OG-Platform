/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculatorTest {

  private final static CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator MODEL = new CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator();
  private static final double EPS = 1.e-13;

  /**
   * 
   */
  @Test
  public void sampleData1Test() {
    final double timeToExpiry = 1.5;
    final double spot = 49.;
    //    final double forward = 102.;
    //    final double interestRate = Math.log(forward / spot) / timeToExpiry;
    final double interestRate = -0.05;
    final double dividend = -0.01;
    final double forward = spot * Math.exp((interestRate - dividend) * timeToExpiry);
    final double[] putStrikes = new double[] {30., 40. };
    final double[] callStrikes = new double[] {50., 60., 70. };

    final int nCalls = callStrikes.length;
    final int nPuts = putStrikes.length;
    final double[] callVols = new double[nCalls];
    final double[] putVols = new double[nPuts];
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = 0.5 - 0.005 * (callStrikes[i] - 50.);
    }
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = 0.5 - 0.005 * (putStrikes[i] - 50.);
    }
    final double strdVol = 0.5 - 0.005 * (forward - 50.);

    final VolatilitySwapCalculatorResult res = MODEL.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);

    final double ref = 57.538816886509770;
    assertEquals(ref, res.getFairValue(), ref * EPS);

    final double[] expPuts = new double[] {0.513350614584648, 0.308758357270425 };
    final double[] putPrems = new double[] {4.90559632142506, 9.19600014626863 };
    final double[] expCalls = new double[] {-0.154238701620069, -0.152104062947836, -0.116537598619695 };
    final double[] callPrems = new double[] {10.4905519833061, 6.29326958461010, 3.23838983555186 };
    final double expStrd = 2.217562648068431;
    final double strdPrem = 24.820912695461015;
    final double expWithoutCash = 57.446886504492230;
    final double cash = 0.091930382017545;
    for (int i = 0; i < nPuts; ++i) {
      assertEquals(putPrems[i], res.getPutPrices()[i], Math.max(Math.abs(putPrems[i]), 1.) * EPS);
      assertEquals(expPuts[i], res.getPutWeights()[i], Math.max(Math.abs(expPuts[i]), 1.) * EPS);
    }
    {
      assertEquals(expStrd, res.getStraddleWeight(), Math.max(Math.abs(expStrd), 1.) * EPS);
      assertEquals(strdPrem, res.getStraddlePrice(), Math.max(Math.abs(strdPrem), 1.) * EPS);
    }
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(callPrems[i], res.getCallPrices()[i], Math.max(Math.abs(callPrems[i]), 1.) * EPS);
      assertEquals(expCalls[i], res.getCallWeights()[i], Math.max(Math.abs(expCalls[i]), 1.) * EPS);
    }
    assertEquals(cash, res.getCash(), Math.max(Math.abs(cash), 1.) * EPS);
    assertEquals(expWithoutCash, res.getOptionTotal(), Math.max(Math.abs(expWithoutCash), 1.) * EPS);
  }

  /**
   * 
   */
  @Test
  public void noModTest() {
    final double timeToExpiry = 1.;
    final double interestRate = 0.1;
    final double dividend = 0.04;
    final double forward = 45.;
    final double spot = forward * Math.exp(-(interestRate - dividend) * timeToExpiry);
    final double[] putStrikes = new double[] {30., 40. };
    final double[] callStrikes = new double[] {50., 60., 70. };

    final int nCalls = callStrikes.length;
    final int nPuts = putStrikes.length;
    final double[] callVols = new double[nCalls];
    final double[] putVols = new double[nPuts];
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = 0.5 - 0.005 * (callStrikes[i] - 50.);
    }
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = 0.5 - 0.005 * (putStrikes[i] - 50.);
    }
    final double strdVol = 0.5 - 0.005 * (forward - 50.);

    final VolatilitySwapCalculatorResult res = MODEL.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);

    final double ref = 48.733554853596400;
    assertEquals(ref, res.getFairValue(), ref * EPS);

    final double[] expPuts = new double[] {0.632296228517047, 0.380459348608907 };
    final double[] putPrems = new double[] {2.89530163120534, 6.25593528240724 };
    final double[] expCalls = new double[] {-0.257443117987717, -0.187547911423368, -0.143731501185040 };
    final double[] callPrems = new double[] {6.40625025668058, 3.29997011943255, 1.36333840440528 };
    final double expStrd = 2.785142527367778;
    final double strdPrem = 16.862334153913586;
    final double expWithoutCash = 48.710719107228470;
    final double cash = 0.022835746367929;
    for (int i = 0; i < nPuts; ++i) {
      assertEquals(putPrems[i], res.getPutPrices()[i], Math.max(Math.abs(putPrems[i]), 1.) * EPS);
      assertEquals(expPuts[i], res.getPutWeights()[i], Math.max(Math.abs(expPuts[i]), 1.) * EPS);
    }
    {
      assertEquals(expStrd, res.getStraddleWeight(), Math.max(Math.abs(expStrd), 1.) * EPS);
      assertEquals(strdPrem, res.getStraddlePrice(), Math.max(Math.abs(strdPrem), 1.) * EPS);
    }
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(callPrems[i], res.getCallPrices()[i], Math.max(Math.abs(callPrems[i]), 1.) * EPS);
      assertEquals(expCalls[i], res.getCallWeights()[i], Math.max(Math.abs(expCalls[i]), 1.) * EPS);
    }
    assertEquals(cash, res.getCash(), Math.max(Math.abs(cash), 1.) * EPS);
    assertEquals(expWithoutCash, res.getOptionTotal(), Math.max(Math.abs(expWithoutCash), 1.) * EPS);
  }

  /**
   * 
   */
  @Test
  public void sampleData2Test() {
    final double timeToExpiry = 0.5;
    final double spot = 100.;
    //    final double forward = 102.;
    //    final double interestRate = Math.log(forward / spot) / timeToExpiry;
    final double interestRate = 0.04;
    final double forward = spot * Math.exp(interestRate * timeToExpiry);
    final double[] callStrikes = new double[] {105., 110., 115., 120., 125., 130. };
    final double[] putStrikes = new double[] {60., 65., 70., 75., 80., 85., 90., 95., 100. };

    final int nCalls = callStrikes.length;
    final int nPuts = putStrikes.length;
    final double[] callVols = new double[nCalls];
    final double[] putVols = new double[nPuts];
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = 0.2 - 0.002 * (callStrikes[i] - 100.);
    }
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = 0.2 - 0.002 * (putStrikes[i] - 100.);
    }
    final double strdVol = 0.2 - 0.002 * (forward - 100.);

    final VolatilitySwapCalculatorResult res = MODEL.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, 0., putVols, strdVol, callVols);
    final double ref = 19.408;
    assertEquals(ref, res.getFairValue(), ref * 1.e-4);

    final double[] expPuts = new double[] {0.109, 0.094, 0.083, 0.073, 0.065, 0.059, 0.053, 0.048, 0.036 };
    final double[] putPrems = new double[] {0.02, 0.05, 0.11, 0.25, 0.51, 0.97, 1.74, 2.92, 4.65 };
    final double[] expCalls = new double[] {-0.040, -0.037, -0.035, -0.032, -0.030, -0.028 };
    final double[] callPrems = new double[] {4.10, 2.26, 1.07, 0.42, 0.12, 0.03 };
    final double expStrd = 1.737;
    final double strdPrem = 11.05;
    final double expWithoutCash = 19.412;
    final double cash = -0.004;
    for (int i = 0; i < nPuts; ++i) {
      assertEquals(putPrems[i], res.getPutPrices()[i], 1.e-2);
      assertEquals(expPuts[i], res.getPutWeights()[i], 1.e-3);
    }
    {
      assertEquals(expStrd, res.getStraddleWeight(), 1.e-3);
      assertEquals(strdPrem, res.getStraddlePrice(), 1.e-2);
    }
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(callPrems[i], res.getCallPrices()[i], 1.e-2);
      assertEquals(expCalls[i], res.getCallWeights()[i], 1.e-3);
    }
    assertEquals(cash, res.getCash(), 5. * 1.e-4);
    assertEquals(expWithoutCash, res.getOptionTotal(), 5. * 1.e-4);
  }

  /**
   * 
   */
  @Test
  public void errorTest() {
    final double timeToExpiry = 1.2;
    final double spot = 45.;
    //    final double forward = 102.;
    //    final double interestRate = Math.log(forward / spot) / timeToExpiry;
    final double interestRate = 0.01;
    final double dividend = 0.005;
    final double forward = spot * Math.exp(interestRate * timeToExpiry);
    final double[] putStrikes = new double[] {35., 40., 45, };
    final double[] callStrikes = new double[] {50., 55. };

    final int nCalls = callStrikes.length;
    final int nPuts = putStrikes.length;
    final double[] callVols = new double[nCalls];
    final double[] putVols = new double[nPuts];
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = 0.5 - 0.005 * (callStrikes[i] - 50.);
    }
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = 0.5 - 0.005 * (putStrikes[i] - 50.);
    }
    final double strdVol = 0.5 - 0.005 * (forward - 50.);

    try {
      MODEL.evaluate(-spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, putStrikes, callStrikes, -timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, -strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, new double[] {40., 45, }, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, putStrikes, new double[] {35., 40., 45, }, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(1., new double[] {-10., -5., 0, }, new double[] {5., 10. }, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, putStrikes, new double[] {-50., 55. }, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, new double[] {0.5, -0.2, 0.3 }, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, new double[] {-0.3, 0.5 });
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, new double[] {35., 40., 42. }, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot, putStrikes, new double[] {53., 55. }, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot + 5., putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.evaluate(spot - 5., putStrikes, callStrikes, timeToExpiry, interestRate, dividend, putVols, strdVol, callVols);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }
}
