/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public class CapletStrippingAbsoluteStrikePSplineTest extends CapletStrippingSetup {

  @Test
  public void test() {
    final boolean print = false;

    if (print) {
      System.out.println("CapletStrippingAbsoluteStrikePSplineTest");
    }

    final int n = getNumberOfStrikes();

    final int samples = 101;
    double[][] mVols = new double[n][samples];

    for (int i = 9; i < 10; i++) {
      List<CapFloor> caps = getCaps(i);
      CapletStrippingAbsoluteStrikePSpline stripper = new CapletStrippingAbsoluteStrikePSpline(caps, getYieldCurves());

      double[] vols = getCapVols(i);

      LeastSquareResults fitted = stripper.solveForVol(vols);
      if (print) {
        System.out.println(fitted.getChiSq() + "\t" + fitted.getFitParameters());
      }

      // System.out.println(i + "\t" + fitted);
      // VolatilityModel1D curve = stripper.getVolCurve(fitted);
      //
      // Iterator<CapFloor> iter = caps.iterator();
      // int ii = 0;
      // while (iter.hasNext()) {
      // CapFloorPricer pricer = new CapFloorPricer(iter.next(), getYieldCurves());
      // double iv = pricer.impliedVol(curve);
      // assertEquals(vols[ii], iv, 1e-9);
      // ii++;
      // }
      //
      // if (print) {
      // for (int j = 0; j < samples; j++) {
      // double t = j * 10.0 / (samples - 1);
      // mVols[i][j] = curve.getVolatility(0, 0, t);
      // }
      // }
    }

    // if (print) {
    // for (int j = 0; j < samples; j++) {
    // double t = j * 10.0 / (samples - 1);
    // System.out.print(t);
    // for (int i = 0; i < n; i++) {
    // System.out.print("\t" + mVols[i][j]);
    // }
    // System.out.print("\n");
    // }
    // }
  }

  @Test
  public void test2() {
    final boolean print = false;
    final int samples = 101;

    if (print) {
      System.out.println("CapletStrippingAbsoluteStrikePSplineTest");
    }

    final int n = getNumberOfStrikes();
    double[][] mVols = new double[n][samples];

    for (int i = 9; i < 10; i++) {
      List<CapFloor> caps = getCaps(i);
      CapletStrippingAbsoluteStrikePSpline stripper = new CapletStrippingAbsoluteStrikePSpline(caps, getYieldCurves());

      double[] vols = getCapVols(i);

      LeastSquareResults lsRes = stripper.solveForVolViaPrice(vols);
      if (print) {
        System.out.println(i + "\t" + lsRes.getChiSq() + "\t" + lsRes.getFitParameters());
//        VolatilityModel1D volCurve = stripper.getVolCurve(lsRes.getFitParameters());
//        for (int j = 0; j < samples; j++) {
//          double t = 2 + j * 2.0 / (samples - 1);
//          System.out.println(t + "\t" + volCurve.getVolatility(0, 0, t));
//        }
      }
    }
  }
  
  @Test
  public void test3() {
    final boolean print = false;
    final int samples = 101;

    if (print) {
      System.out.println("CapletStrippingAbsoluteStrikePSplineTest");
    }

    final int n = getNumberOfStrikes();
    double[][] mVols = new double[n][samples];

    for (int i = 9; i < 10; i++) {
      List<CapFloor> caps = getCaps(i);
      CapletStrippingAbsoluteStrikePSpline stripper = new CapletStrippingAbsoluteStrikePSpline(caps, getYieldCurves());

      double[] vols = getCapVols(i);

      LeastSquareResults lsRes = stripper.solveForVolViaPrice2(vols);
      if (print) {
        System.out.println(i + "\t" + lsRes.getChiSq() + "\t" + lsRes.getFitParameters());
//        VolatilityModel1D volCurve = stripper.getVolCurve(lsRes.getFitParameters());
//        for (int j = 0; j < samples; j++) {
//          double t = 2 + j * 2.0 / (samples - 1);
//          System.out.println(t + "\t" + volCurve.getVolatility(0, 0, t));
//        }
      }
    }
  }

}
