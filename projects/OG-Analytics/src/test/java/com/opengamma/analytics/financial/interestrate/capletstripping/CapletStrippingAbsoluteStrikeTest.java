/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

/**
 * 
 */
public abstract class CapletStrippingAbsoluteStrikeTest extends CapletStrippingSetup {

  public abstract CapletStrippingAbsoluteStrike getStripper(final List<CapFloor> caps);

  public void testVolStripping(boolean print) {

    if (print) {
      System.out.println("CapletStrippingAbsoluteStrikeTest " + this.getClass());
    }

    final int n = getNumberOfStrikes();

    final int samples = 101;
    double[][] mVols = new double[n][samples];

    for (int i = 0; i < n; i++) {
      List<CapFloor> caps = getCaps(i);
      CapletStrippingAbsoluteStrike stripper = getStripper(caps);

      double[] vols = getCapVols(i);
      CapletStrippingSingleStrikeResult res = stripper.solveForVol(vols);

      if (print) {
        System.out.println(i + " fit parameters: " + res.getFitParameters());
      }

      MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, getYieldCurves());
      double[] fitVols = pricer.impliedVols(res.getVolatilityCurve());

      final int m = vols.length;
      assertEquals(fitVols.length, m);
      for (int j = 0; j < m; j++) {
        assertEquals(vols[j], fitVols[j], 1e-9);
      }

      if (print) {
        for (int j = 0; j < samples; j++) {
          double t = j * 10.0 / (samples - 1);
          mVols[i][j] = res.getVolatilityCurve().getVolatility(t);
        }
      }
    }

    if (print) {
      System.out.print("\n");
      for (int j = 0; j < samples; j++) {
        double t = j * 10.0 / (samples - 1);
        System.out.print(t);
        for (int i = 0; i < n; i++) {
          System.out.print("\t" + mVols[i][j]);
        }
        System.out.print("\n");
      }
    }
  }

//  @Test
//  public void test() {
//    final boolean print = false;
//
//    if (print) {
//      System.out.println("CapletStrippingAbsoluteStrikeInterpolatorTest");
//    }
//
//    final int n = getNumberOfStrikes();
//
//    final int samples = 101;
//    double[][] mVols = new double[n][samples];
//
//    for (int i = 0; i < n; i++) {
//      List<CapFloor> caps = getCaps(i);
//      CapletStrippingAbsoluteStrikeInterpolation stripper = new CapletStrippingAbsoluteStrikeInterpolation(caps, getYieldCurves());
//
//      double[] vols = getCapVols(i);
//      DoubleMatrix1D nodes = stripper.solveForVol(vols);
//
//      if (print) {
//        System.out.println(i + "\t" + nodes);
//      }
//      VolatilityModel1D curve = stripper.getVolCurve(nodes);
//
//      Iterator<CapFloor> iter = caps.iterator();
//      int ii = 0;
//      while (iter.hasNext()) {
//        CapFloorPricer pricer = new CapFloorPricer(iter.next(), getYieldCurves());
//        double iv = pricer.impliedVol(curve);
//        assertEquals(vols[ii], iv, 1e-9);
//        ii++;
//      }
//
//      if (print) {
//        for (int j = 0; j < samples; j++) {
//          double t = j * 10.0 / (samples - 1);
//          mVols[i][j] = curve.getVolatility(0, 0, t);
//        }
//      }
//    }
//
//    if (print) {
//      System.out.print("\n");
//      for (int j = 0; j < samples; j++) {
//        double t = j * 10.0 / (samples - 1);
//        System.out.print(t);
//        for (int i = 0; i < n; i++) {
//          System.out.print("\t" + mVols[i][j]);
//        }
//        System.out.print("\n");
//      }
//    }
//  }

  // @SuppressWarnings("unused")
  // @Test
  // public void speedTest() {
  // final int warmup = 1;
  // final int benchmarkCycles = 0;
  //
  // final int n = getNumberOfStrikes();
  // final YieldCurveBundle yieldCurves = getYieldCurves();
  // List<List<CapFloor>> allMktCaps = new ArrayList<>();
  // double[][] mktVols = new double[n][];
  //
  // for (int i = 0; i < n; i++) {
  // allMktCaps.add(getCaps(i));
  // mktVols[i] = getCapVols(i);
  // }
  //
  // for (int i = 0; i < warmup; i++) {
  // stripe(allMktCaps, yieldCurves, mktVols, n);
  // }
  //
  // if (benchmarkCycles > 0) {
  // long start = System.nanoTime();
  // for (int i = 0; i < benchmarkCycles; i++) {
  // stripe(allMktCaps, yieldCurves, mktVols, n);
  // }
  // double time = (System.nanoTime() - start) / (1e6 * benchmarkCycles * n);
  // System.out.println("time per fit:" + time + "ms");
  // }
  //
  // }
  //
  // public void stripe(final List<List<CapFloor>> allMktCaps, final YieldCurveBundle yieldCurves, final double[][] mrkVols, final int nStrikes) {
  // for (int i = 0; i < nStrikes; i++) {
  // List<CapFloor> caps = allMktCaps.get(i);
  // double[] vols = mrkVols[i];
  // CapletStrippingAbsoluteStrikeInterpolation stripper = new CapletStrippingAbsoluteStrikeInterpolation(caps, yieldCurves);
  // @SuppressWarnings("unused")
  // DoubleMatrix1D nodes = stripper.solveForVol(vols);
  // }
  // }

}
