/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.surface.NodalObjectsSurface;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CapletStrippingDirectGlobalWithPenaltyTest extends CapletStrippingSetup {

  /**
   * Use all of the market data
   */
  @Test(enabled = false)
  public void MarketDataTestWithATM() {
    final double[] capStrikes = getStrikes();
    final int nStrikes = capStrikes.length;
    final int nCapEndTimes = getCapEndTimes().length;
    final List<CapFloor> atmCaps = getATMCaps();

    final List<CapFloor>[] caps = new List[nStrikes + nCapEndTimes];
    final double[][] capVols = new double[nStrikes + nCapEndTimes][];
    final int[] atmPos = new int[nCapEndTimes];
    int k = 0;
    for (int i = 0; i < nStrikes; ++i) {
      while (k < nCapEndTimes && capStrikes[i] > atmCaps.get(k).getStrike()) {
        caps[i + k] = new ArrayList<>(1);
        capVols[i + k] = new double[1];
        caps[i + k].add(atmCaps.get(k));
        capVols[i + k][0] = getAtmVols()[k];
        atmPos[k] = i + k;
        ++k;
      }
      caps[i + k] = getCaps(i);
      capVols[i + k] = getCapVols(i);
      if (i == nStrikes - 1 && k != nCapEndTimes) {
        while (k < nCapEndTimes) {
          caps[i + k] = new ArrayList<>(1);
          capVols[i + k] = new double[1];
          caps[i + k].add(atmCaps.get(k));
          capVols[i + k][0] = getAtmVols()[k];
          atmPos[k] = i + k;
          ++k;
        }
      }
    }

    final double[] allCapStrikes = new double[nStrikes + nCapEndTimes];
    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
      allCapStrikes[i] = caps[i].get(0).getStrike();
    }

    final int sample = nStrikes + nCapEndTimes - 1;
    final int nCapsSample = caps[sample].size();
    final CapFloorIbor[] payments = caps[sample].get(nCapsSample - 1).getPayments();
    final int nPayments = payments.length;
    final double[] fixingTimes = new double[nPayments];
    final double[] PaymentTimes = new double[nPayments];
    for (int i = 0; i < nPayments; ++i) {
      fixingTimes[i] = payments[i].getFixingTime();
      PaymentTimes[i] = payments[i].getPaymentTime();
    }

    final CapletVolatilityNodalSurfaceProvider provider = new CapletVolatilityNodalSurfaceProvider(allCapStrikes, fixingTimes);
    final CapletStrippingDirectGlobalWithPenalty cpst = new CapletStrippingDirectGlobalWithPenalty(caps, getYieldCurves(), provider);
    LeastSquareResults res = cpst.solveForVol(capVols);

    int l = 0;
    System.out.print("\t");
    for (int j = 0; j < nPayments; ++j) {
      System.out.print(PaymentTimes[j] + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
      if (l < nCapEndTimes && i == atmPos[l]) { // skipping atm strikes
        ++l;
      } else {
        System.out.print(allCapStrikes[i] + "\t");
        //      final int nCaps = caps[i].size();
        //      final CapFloor lastCap = caps[i].get(nCaps - 1);
        //      final int nLongPayments = lastCap.getNumberOfPayments();
        //      for (int j = 0; j < nLongPayments; ++j) {
        for (int j = 0; j < nPayments; ++j) {
          System.out.print(res.getFitParameters().getData()[i * nPayments + j] + "\t");
        }
        System.out.print("\n");
      }
    }
    System.out.println(res.getChiSq());

    DoubleMatrix1D resVec = res.getFitParameters();
    double[][] resMatrix = new double[nStrikes + nCapEndTimes][nPayments];
    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
      for (int j = 0; j < nPayments; ++j) {
        resMatrix[i][j] = resVec.getEntry(i * nPayments + j);
      }
    }

    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
      final int nCaps = caps[i].size();
      for (int j = 0; j < nCaps; ++j) {
        CapFloor cf = caps[i].get(j);
        CapFloorPricer pr = new CapFloorPricer(cf, getYieldCurves());
        final int nP = pr.getNumberCaplets();
        final double[] vols = Arrays.copyOf(resMatrix[i], nP);
        System.out.println(capVols[i][j] + "\t" + pr.impliedVol(vols));
      }
    }
  }

  /**
   * Exclude ATM caps, that is, grid is almost homogeneous
   */
  @Test(enabled = false)
  public void MarketDataTestExcATM() {
    final double[] capStrikes = getStrikes();
    final int nStrikes = capStrikes.length;

    final List<CapFloor>[] caps = new List[nStrikes];
    final double[][] capVols = new double[nStrikes][];
    int k = 0;
    for (int i = 0; i < nStrikes; ++i) {
      caps[i] = getCaps(i);
      capVols[i] = getCapVols(i);
    }

    final int sample = 5;
    final int nCapsSample = caps[sample].size();
    final CapFloorIbor[] payments = caps[sample].get(nCapsSample - 1).getPayments();
    final int nPayments = payments.length;
    final double[] fixingTimes = new double[nPayments];
    for (int i = 0; i < nPayments; ++i) {
      fixingTimes[i] = payments[i].getFixingTime();
    }

    final CapletVolatilityNodalSurfaceProvider provider = new CapletVolatilityNodalSurfaceProvider(capStrikes, fixingTimes);
    final CapletStrippingDirectGlobalWithPenalty cpst = new CapletStrippingDirectGlobalWithPenalty(caps, getYieldCurves(), provider);
    LeastSquareResults res = cpst.solveForVol(capVols);

    System.out.print("\t");
    for (int j = 0; j < nPayments; ++j) {
      System.out.print(fixingTimes[j] + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < nStrikes; ++i) {
      System.out.print(capStrikes[i] + "\t");
      for (int j = 0; j < nPayments; ++j) {
        System.out.print(res.getFitParameters().getData()[i * nPayments + j] + "\t");
      }
      System.out.print("\n");
    }
    System.out.println(res.getChiSq());

    DoubleMatrix1D resVec = res.getFitParameters();
    double[][] resMatrix = new double[nStrikes][nPayments];
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nPayments; ++j) {
        resMatrix[i][j] = resVec.getEntry(i * nPayments + j);
      }
    }

    //    double chiSq = 0.;
    for (int i = 0; i < nStrikes; ++i) {
      final int nCaps = caps[i].size();
      for (int j = 0; j < nCaps; ++j) {
        CapFloor cf = caps[i].get(j);
        CapFloorPricer pr = new CapFloorPricer(cf, getYieldCurves());
        final int nP = pr.getNumberCaplets();
        final double[] vols = Arrays.copyOf(resMatrix[i], nP);
        System.out.println(capVols[i][j] + "\t" + pr.impliedVol(vols));
      }
    }

  }

  /**
   * Tests below are unit tests for {@link CapletStrippingDirectGlobalWithPenalty} and {@link CapletVolatilityNodalSurfaceProvider}
   */

  /**
   * 
   */
  @Test
  public void NodalProvidorOutputTest() {
    final double epsLocal = 1.e-12;
    double[] strikes = new double[] {2.0, 3.5, 4.5 };
    double[] times = new double[] {0.5, 2.2, 3.4, 5.1 };
    CapletVolatilityNodalSurfaceProvider provider = new CapletVolatilityNodalSurfaceProvider(strikes, times);

    int nStrikes = strikes.length;
    int nTimes = times.length;
    int expNumber = nStrikes * nTimes;
    Integer[] expStrikeIntegerNodes = new Integer[] {0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2 };
    Integer[] expTimeIntegerNodes = new Integer[] {0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3 };

    assertEquals(expNumber, provider.getNumberOfNodes());
    assertArray(expStrikeIntegerNodes, provider.getStrikeIntegerNodes());
    assertArray(expTimeIntegerNodes, provider.getTimeIntegerNodes());
    assertArray(strikes, provider.getStrikes(), epsLocal);
    assertArray(times, provider.getFixingTimes(), epsLocal);

    double[] data = new double[expNumber];
    Double[] Data = new Double[expNumber];
    for (int i = 0; i < expNumber; ++i) {
      data[i] = i;
      Data[i] = (double) i;
    }
    NodalObjectsSurface<Integer, Integer, Double> expSurface = new NodalObjectsSurface<>(expStrikeIntegerNodes, expTimeIntegerNodes, Data);
    NodalObjectsSurface<Integer, Integer, Double> surface = provider.evaluate(new DoubleMatrix1D(data));

    assertArray(expSurface.getXData(), surface.getXData());
    assertArray(expSurface.getYData(), surface.getYData());
    assertArray(expSurface.getZData(), surface.getZData(), epsLocal);

    double lambdaK = 1.5;
    double lambdaT = 1.8;
    DoubleMatrix2D matrix = provider.getPenaltyMatrix(lambdaK, lambdaT);
    double[] dk = new double[] {strikes[1] - strikes[0], strikes[2] - strikes[1] };
    double[] dt = new double[] {times[1] - times[0], times[2] - times[1], times[3] - times[2] };
    final DoubleMatrix1D sampleVec1D = new DoubleMatrix1D(data);

    final MatrixAlgebra alg = new ColtMatrixAlgebra();
    double penalty = alg.getInnerProduct(sampleVec1D, alg.multiply(matrix, sampleVec1D));
    double expPenalty = 4.0 * Math.pow(lambdaK * (4.0 / dk[1] - 4.0 / dk[0]) / dk[0], 2.0) + 3.0 * Math.pow(lambdaT * (1.0 / dt[1] - 1.0 / dt[0]) / dt[0], 2.0) + 3.0 *
        Math.pow(lambdaT * (1.0 / dt[2] - 1.0 / dt[1]) / dt[1], 2.0);
    assertEquals(expPenalty, penalty, epsLocal);
  }

  /**
   * 
   */
  @Test
  public void NodalProvidorHashCodeEqualsTest() {
    double[] strikes1 = new double[] {2.0, 3.5, 4.5 };
    double[] times1 = new double[] {0.5, 2.2, 3.4, 5.1 };
    CapletVolatilityNodalSurfaceProvider provider1 = new CapletVolatilityNodalSurfaceProvider(strikes1, times1);
    double[] strikes2 = new double[] {2.1, 3.5, 4.5 };
    double[] times2 = new double[] {0.5, 2.2, 3.4, 5.2 };
    CapletVolatilityNodalSurfaceProvider provider2 = new CapletVolatilityNodalSurfaceProvider(strikes1, times2);
    CapletVolatilityNodalSurfaceProvider provider3 = new CapletVolatilityNodalSurfaceProvider(strikes2, times1);
    CapletVolatilityNodalSurfaceProvider provider4 = new CapletVolatilityNodalSurfaceProvider(strikes1, times1);
    double[] strikes3 = new double[] {2.1, 3.5, 4.5, 5.2 };
    double[] times3 = new double[] {0.5, 2.2, 3.4, 5.2, 10.0 };
    CapletVolatilityNodalSurfaceProvider provider5 = new CapletVolatilityNodalSurfaceProvider(strikes3, times1);
    CapletVolatilityNodalSurfaceProvider provider6 = new CapletVolatilityNodalSurfaceProvider(strikes1, times3);

    assertTrue(provider1.equals(provider1));

    assertTrue(provider1.equals(provider4));
    assertEquals(provider1.hashCode(), provider4.hashCode());

    assertFalse(provider1.equals(provider2));
    assertFalse(provider1.equals(provider3));
    assertFalse(provider1.equals(null));
    assertFalse(provider1.equals(new double[] {}));

    assertFalse(provider1.hashCode() == provider5.hashCode());
    assertFalse(provider1.hashCode() == provider6.hashCode());
    assertFalse(provider1.equals(provider5));
    assertFalse(provider1.equals(provider6));
  }

  @SuppressWarnings("unused")
  @Test
  public void CapletStrippingDirectGlobalWithPenaltyAllTest() {
    /*
     * Preparing data
     */
    final double[] capStrikes = getStrikes();
    final int nStrikes = capStrikes.length;
    final int nCapEndTimes = getCapEndTimes().length;
    final List<CapFloor> atmCaps = getATMCaps();

    final List<CapFloor>[] caps = new List[nStrikes + nCapEndTimes];
    final double[][] capVols = new double[nStrikes + nCapEndTimes][];
    final int[] atmPos = new int[nCapEndTimes];
    int k = 0;
    for (int i = 0; i < nStrikes; ++i) {
      while (k < nCapEndTimes && capStrikes[i] > atmCaps.get(k).getStrike()) {
        caps[i + k] = new ArrayList<>(1);
        capVols[i + k] = new double[1];
        caps[i + k].add(atmCaps.get(k));
        capVols[i + k][0] = getAtmVols()[k];
        atmPos[k] = i + k;
        ++k;
      }
      caps[i + k] = getCaps(i);
      capVols[i + k] = getCapVols(i);
      if (i == nStrikes - 1 && k != nCapEndTimes) {
        while (k < nCapEndTimes) {
          caps[i + k] = new ArrayList<>(1);
          capVols[i + k] = new double[1];
          caps[i + k].add(atmCaps.get(k));
          capVols[i + k][0] = getAtmVols()[k];
          atmPos[k] = i + k;
          ++k;
        }
      }
    }

    final double[] allCapStrikes = new double[nStrikes + nCapEndTimes];
    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
      allCapStrikes[i] = caps[i].get(0).getStrike();
    }

    final int sample = nStrikes + nCapEndTimes - 1;
    final int nCapsSample = caps[sample].size();
    final CapFloorIbor[] payments = caps[sample].get(nCapsSample - 1).getPayments();
    final int nPayments = payments.length;
    final double[] fixingTimes = new double[nPayments];
    final double[] PaymentTimes = new double[nPayments];
    for (int i = 0; i < nPayments; ++i) {
      fixingTimes[i] = payments[i].getFixingTime();
      PaymentTimes[i] = payments[i].getPaymentTime();
    }

    /*
     * Use subset of full data
     */
    int nStrikesUse = 5;
    double[] strikesUse = new double[nStrikesUse];
    List<CapFloor>[] capsUse = new List[nStrikesUse];
    double[][] capVolsUse = new double[nStrikesUse][];
    double[][] capVolsErrorUse = new double[nStrikesUse][];
    double[][] guessUse = new double[nStrikesUse][nPayments];
    for (int i = 0; i < nStrikesUse; ++i) {
      strikesUse[i] = allCapStrikes[i];
      capsUse[i] = caps[i];
      capVolsUse[i] = capVols[i];
      for (int j = 0; j < nPayments; ++j) {
        guessUse[i][j] = 0.4;
      }
      capVolsErrorUse[i] = new double[capsUse[i].size()];
      for (int j = 0; j < capsUse[i].size(); ++j) {
        capVolsErrorUse[i][j] = 0.0001;
      }
    }

    /*
     * Solve
     */
    final CapletVolatilityNodalSurfaceProvider provider = new CapletVolatilityNodalSurfaceProvider(strikesUse, fixingTimes);
    final CapletStrippingDirectGlobalWithPenalty cpst = new CapletStrippingDirectGlobalWithPenalty(capsUse, getYieldCurves(), provider);
    LeastSquareResults res = cpst.solveForVol(capVolsUse);
    LeastSquareResults res1 = cpst.solveForVol(capVolsUse, capVolsErrorUse, guessUse);

    DoubleMatrix1D resVec = res.getFitParameters();
    DoubleMatrix1D resVec1 = res1.getFitParameters();
    double[][] resMatrix = new double[nStrikesUse][nPayments];
    double[][] resMatrix1 = new double[nStrikesUse][nPayments];
    for (int i = 0; i < nStrikesUse; ++i) {
      for (int j = 0; j < nPayments; ++j) {
        resMatrix[i][j] = resVec.getEntry(i * nPayments + j);
        resMatrix1[i][j] = resVec1.getEntry(i * nPayments + j);
      }
    }
    for (int i = 0; i < nStrikesUse; ++i) {
      final int nCaps = capsUse[i].size();
      for (int j = 0; j < nCaps; ++j) {
        CapFloor cf = capsUse[i].get(j);
        CapFloorPricer pr = new CapFloorPricer(cf, getYieldCurves());
        final int nP = pr.getNumberCaplets();
        final double[] vols = Arrays.copyOf(resMatrix[i], nP);
        assertEquals(capVolsUse[i][j], pr.impliedVol(vols), 1.e-2);

        final double[] vols1 = Arrays.copyOf(resMatrix1[i], nP);
        assertEquals(capVolsUse[i][j], pr.impliedVol(vols1), 1.e-2);
      }
    }

    /**
     * hashCode and equals
     */
    final CapletStrippingDirectGlobalWithPenalty cpst1 = new CapletStrippingDirectGlobalWithPenalty(capsUse, getYieldCurves(), provider);
    assertTrue(cpst.equals(cpst));
    assertFalse(cpst.hashCode() == cpst1.hashCode());
    assertFalse(cpst.equals(cpst1));

    /**
     * Exception expected
     */
    try {
      new CapletStrippingDirectGlobalWithPenalty(capsUse, getYieldCurves(), provider, 1.0, -1.0);
    } catch (final Exception e) {
      assertEquals("lambdaTime should be non-negative", e.getMessage());
    }
    try {
      new CapletStrippingDirectGlobalWithPenalty(capsUse, getYieldCurves(), provider, -1.0, 1.0);
    } catch (final Exception e) {
      assertEquals("lambdaStrike should be non-negative", e.getMessage());
    }
    try {
      cpst.solveForVol(capVols);
    } catch (final Exception e) {
      assertEquals("number of elements in input is different form expected vector length", e.getMessage());
    }
    try {
      cpst.solveForVol(new double[][] {capVols[0] });
    } catch (final Exception e) {
      assertEquals("number of elements in input is different form expected vector length", e.getMessage());
    }
  }

  private void assertArray(final Integer[] x, final Integer[] y) {
    int n = x.length;
    for (int i = 0; i < n; ++i) {
      assertEquals(x[i], y[i]);
    }
  }

  private void assertArray(final double[] x, final double[] y, final double eps) {
    int n = x.length;
    for (int i = 0; i < n; ++i) {
      assertEquals(x[i], y[i], eps);
    }
  }

  private void assertArray(final Double[] x, final Double[] y, final double eps) {
    int n = x.length;
    for (int i = 0; i < n; ++i) {
      assertEquals(x[i], y[i], eps);
    }
  }
}
