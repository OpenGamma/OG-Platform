/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

/**
 * 
 */
public class CarrLeeSyntheticVolatilitySwapValuationModelTest {

  private final static CarrLeeSyntheticVolatilitySwapValuationModel MODEL = new CarrLeeSyntheticVolatilitySwapValuationModel();

  /**
   * 
   */
  @Test
  public void sampleData1Test() {
    final double timeToExpiry = 1.5;
    final double spot = 50.;
    //    final double forward = 102.;
    //    final double interestRate = Math.log(forward / spot) / timeToExpiry;
    final double interestRate = -0.05;
    final double forward = spot * Math.exp(interestRate * timeToExpiry);
    final double[] putStrikes = new double[] {35., 40. };
    final double[] callStrikes = new double[] {50., 55., 60. };

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

    final List<double[]> list = MODEL.getWeights(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);

    final double swap = MODEL.getVolatilitySwapValue(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    final double ref = 56.535733986954770;
    assertEquals(ref, swap, ref * 1.e-14);

    final double[] expPuts = new double[] {0.390226981434903, 0.308371351671963 };
    final double[] putPrems = new double[] {6.84298284816885, 9.12071106621717 };
    final double[] expCalls = new double[] {-0.142533192397159, -0.176664531831682, -0.151893098389423 };
    final double[] callPrems = new double[] {10.6387591870320, 8.37889134428360, 6.40283659877028 };
    final double expStrd = 2.206055279344932;
    final double strdPrem = 24.894471500870940;
    final double expWithoutCash = 56.432286905401780;
    final double cash = 0.103447081552988;
    for (int i = 0; i < nPuts; ++i) {
      final double exp = expPuts[i] * putPrems[i] / expWithoutCash;
      assertEquals(exp, list.get(0)[i], 1.e-14);
    }
    {
      final double exp = expStrd * strdPrem / expWithoutCash;
      assertEquals(exp, list.get(1)[0], 1.e-14);
    }
    for (int i = 0; i < nCalls; ++i) {
      final double exp = expCalls[i] * callPrems[i] / expWithoutCash;
      assertEquals(exp, list.get(2)[i], 1.e-14);
    }
    assertEquals(cash, list.get(3)[0], 1.e-14);
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

    final List<double[]> list = MODEL.getWeights(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);

    final double swap = MODEL.getVolatilitySwapValue(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    final double ref = 19.408;
    assertEquals(ref, swap, ref * 1.e-4);

    final double[] expPuts = new double[] {0.109, 0.094, 0.083, 0.073, 0.065, 0.059, 0.053, 0.048, 0.036 };
    final double[] putPrems = new double[] {0.02, 0.05, 0.11, 0.25, 0.51, 0.97, 1.74, 2.92, 4.65 };
    final double[] expCalls = new double[] {-0.040, -0.037, -0.035, -0.032, -0.030, -0.028 };
    final double[] callPrems = new double[] {4.10, 2.26, 1.07, 0.42, 0.12, 0.03 };
    final double expStrd = 1.737;
    final double strdPrem = 11.05;
    final double expWithoutCash = 19.412;
    final double cash = -0.004;
    for (int i = 0; i < nPuts; ++i) {
      final double exp = expPuts[i] * putPrems[i] / expWithoutCash;
      assertEquals(exp, list.get(0)[i], 1.e-4);
    }
    {
      final double exp = expStrd * strdPrem / expWithoutCash;
      assertEquals(exp, list.get(1)[0], 1.e-4);
    }
    for (int i = 0; i < nCalls; ++i) {
      final double exp = expCalls[i] * callPrems[i] / expWithoutCash;
      assertEquals(exp, list.get(2)[i], 1.e-4);
    }
    assertEquals(cash, list.get(3)[0], 5. * 1.e-4);
  }

  /**
   * 
   */
  @Test
  public void errorTest() {
    final double timeToExpiry = 1.2;
    final double spot = 50.;
    //    final double forward = 102.;
    //    final double interestRate = Math.log(forward / spot) / timeToExpiry;
    final double interestRate = 0.01;
    final double forward = spot * Math.exp(interestRate * timeToExpiry);
    final double[] putStrikes = new double[] {35., 40., 45, };
    final double[] callStrikes = new double[] {55., 60. };

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
      MODEL.getVolatilitySwapValue(spot, putStrikes, new double[] {55., 60., 6.5 }, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getVolatilitySwapValue(spot, new double[] {35., 40. }, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getVolatilitySwapValue(-spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getVolatilitySwapValue(spot, putStrikes, callStrikes, -timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getVolatilitySwapValue(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, -strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getVolatilitySwapValue(spot, new double[] {35., -40., 45, }, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getVolatilitySwapValue(spot, putStrikes, new double[] {55., -60. }, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    putVols[0] *= -1.;
    try {
      MODEL.getVolatilitySwapValue(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    putVols[0] *= -1.;
    callVols[0] *= -1.;
    try {
      MODEL.getVolatilitySwapValue(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    callVols[0] *= -1.;

    try {
      MODEL.getVolatilitySwapValue(spot, new double[] {35., 40., 55, }, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getVolatilitySwapValue(spot, putStrikes, new double[] {35., 60. }, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(spot, putStrikes, new double[] {55., 60., 6.5 }, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(spot, new double[] {35., 40. }, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(-spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(spot, putStrikes, callStrikes, -timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, -strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(spot, new double[] {35., -40., 45, }, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(spot, putStrikes, new double[] {55., -60. }, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    putVols[0] *= -1.;
    try {
      MODEL.getWeights(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    putVols[0] *= -1.;
    callVols[0] *= -1.;
    try {
      MODEL.getWeights(spot, putStrikes, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    callVols[0] *= -1.;

    try {
      MODEL.getWeights(spot, new double[] {35., 40., 55, }, callStrikes, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      MODEL.getWeights(spot, putStrikes, new double[] {35., 60. }, timeToExpiry, interestRate, putVols, strdVol, callVols);
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }
}
