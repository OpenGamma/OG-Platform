/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public class CapletStrippingDirect2DTest extends CapletStrippingSetup {

  @Test(enabled = false)
  public void test() {
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

    final CapletNodalSurfaceProvider provider = new CapletNodalSurfaceProvider(capStrikes, fixingTimes);
    final CapletStrippingDirect2D cpst = new CapletStrippingDirect2D(caps, getYieldCurves(), provider);
    //    LeastSquareResults res = cpst.solveForVol(cpst.makeMatrix(capVols));
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
    Double[][] resMatrix = new Double[nStrikes][nPayments];
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nPayments; ++j) {
        resMatrix[i][j] = resVec.getEntry(i * nPayments + j);
      }
    }

    double chiSq = 0.;
    for (int i = 0; i < nStrikes; ++i) {
      final int nCaps = caps[i].size();
      for (int j = 0; j < nCaps; ++j) {
        CapFloor cf = caps[i].get(j);
        CapFloorPricer pr = new CapFloorPricer(cf, getYieldCurves());
        final int nP = pr.getNumberCaplets();
        final Double[] vols = Arrays.copyOf(resMatrix[i], nP);
        System.out.println(capVols[i][j] + "\t" + pr.impliedVol(vols));
      }
    }

  }

  @Test
      (enabled = false)
      public void testWithAtm() {
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

    final CapletNodalSurfaceProvider provider = new CapletNodalSurfaceProvider(allCapStrikes, fixingTimes);
    final CapletStrippingDirect2D cpst = new CapletStrippingDirect2D(caps, getYieldCurves(), provider);
    //    LeastSquareResults res = cpst.solveForVol(cpst.makeMatrix(capVols));
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
    Double[][] resMatrix = new Double[nStrikes + nCapEndTimes][nPayments];
    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
      for (int j = 0; j < nPayments; ++j) {
        resMatrix[i][j] = resVec.getEntry(i * nPayments + j);
      }
    }

    double chiSq = 0.;
    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
      final int nCaps = caps[i].size();
      for (int j = 0; j < nCaps; ++j) {
        CapFloor cf = caps[i].get(j);
        CapFloorPricer pr = new CapFloorPricer(cf, getYieldCurves());
        final int nP = pr.getNumberCaplets();
        final Double[] vols = Arrays.copyOf(resMatrix[i], nP);
        System.out.println(capVols[i][j] + "\t" + pr.impliedVol(vols));
      }
    }
  }

  //  @Test
  //  public void testGrad() {
  //    final double[] capStrikes = getStrikes();
  //    final int nStrikes = capStrikes.length;
  //    final int nCapEndTimes = getCapEndTimes().length;
  //    final List<CapFloor> atmCaps = getATMCaps();
  //
  //    final List<CapFloor>[] caps = new List[nStrikes + nCapEndTimes];
  //    final double[][] capVols = new double[nStrikes + nCapEndTimes][];
  //    final int[] atmPos = new int[nCapEndTimes];
  //    int k = 0;
  //    for (int i = 0; i < nStrikes; ++i) {
  //      while (k < nCapEndTimes && capStrikes[i] > atmCaps.get(k).getStrike()) {
  //        caps[i + k] = new ArrayList<>(1);
  //        capVols[i + k] = new double[1];
  //        caps[i + k].add(atmCaps.get(k));
  //        capVols[i + k][0] = getAtmVols()[k];
  //        atmPos[k] = i + k;
  //        ++k;
  //      }
  //      caps[i + k] = getCaps(i);
  //      capVols[i + k] = getCapVols(i);
  //      if (i == nStrikes - 1 && k != nCapEndTimes) {
  //        while (k < nCapEndTimes) {
  //          caps[i + k] = new ArrayList<>(1);
  //          capVols[i + k] = new double[1];
  //          caps[i + k].add(atmCaps.get(k));
  //          capVols[i + k][0] = getAtmVols()[k];
  //          atmPos[k] = i + k;
  //          ++k;
  //        }
  //      }
  //    }
  //
  //    final double[] allCapStrikes = new double[nStrikes + nCapEndTimes];
  //    for (int i = 0; i < nStrikes + nCapEndTimes; ++i) {
  //      allCapStrikes[i] = caps[i].get(0).getStrike();
  //    }
  //
  //    final int sample = nStrikes + nCapEndTimes - 1;
  //    final int nCapsSample = caps[sample].size();
  //    final CapFloorIbor[] payments = caps[sample].get(nCapsSample - 1).getPayments();
  //    final int nPayments = payments.length;
  //    final double[] fixingTimes = new double[nPayments];
  //    for (int i = 0; i < nPayments; ++i) {
  //      fixingTimes[i] = payments[i].getFixingTime();
  //    }
  //
  //    final CapletNodalSurfaceProvider provider = new CapletNodalSurfaceProvider(allCapStrikes, fixingTimes);
  //    final CapletStrippingDirect2D cpst = new CapletStrippingDirect2D(caps, getYieldCurves(), provider);
  //
  //    final double[] params = new double[(nStrikes + nCapEndTimes) * nPayments];
  //    Arrays.fill(params, 0.5);
  //
  //    final DoubleMatrix2D res1 = cpst._capVolsDiff.evaluate(new DoubleMatrix1D(params));
  //    final DoubleMatrix2D res2 = cpst._capVolsDiffAnalytic.evaluate(new DoubleMatrix1D(params));
  //
  //    for (int i = 0; i < res1.getNumberOfRows(); ++i) {
  //      for (int j = 0; j < res1.getNumberOfColumns(); ++j) {
  //        if (Math.abs(res1.getData()[i][j] - res2.getData()[i][j]) > 1.e-5) {
  //          System.out.println(res1.getData()[i][j] + "\t" + res2.getData()[i][j]);
  //          //            assertEquals(res1.getData()[i][j], res2.getData()[i][j], 1.0e-4);
  //        }
  //      }
  //    }
  //
  //  }
}
