/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class ClampedCubicSplineInterpolator1DTest {

  private static final Random randObj = new Random();
  private static final CubicSplineInterpolator INTERP = new CubicSplineInterpolator();
  private static final ClampedCubicSplineInterpolator1D INTERP1D = new ClampedCubicSplineInterpolator1D();
  //  private static final NaturalCubicSplineInterpolator1D INTERP_NAT_1D = new NaturalCubicSplineInterpolator1D();

  private static final double EPS = 1.e-7;

  /**
   * Recovery test on polynomial, rational, exponential functions, and node sensitivity test by finite difference method
   * Note that when conditioning number is large, both of the interpolation and node sensitivity produce a poor result
   */
  @Test
  public void sampleFunctionTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    final double[] yValues1 = new double[nData];
    final double[] yValues2 = new double[nData];
    final double[] yValues3 = new double[nData];
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    for (int i = 0; i < nData; ++i) {
      xValues[i] = 0.1 * i * i + i - 8.;
      //      xValues[i] = i + 1;
      yValues1[i] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValues2[i] = Math.exp(0.1 * xValues[i] - 6.);
      yValues3[i] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      //      yValues3[i] = xValues[i] * xValues[i];
      //      System.out.println(yValues1[i] + "\t" + yValues2[i] + "\t" + yValues3[i]);
      yValues1Up[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Dw[i] = yValues3[i];
      //      System.out.println(xValues[i] + "\t" + yValues1[i]);
    }
    //    System.out.println("\n");

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final double[] yValues1Add = new double[nData + 2];
    final double[] yValues2Add = new double[nData + 2];
    final double[] yValues3Add = new double[nData + 2];
    for (int k = 0; k < 5; ++k) {
      final double grad1 = -0.5 + 0.25 * k;
      final double grad2 = 0.5 - 0.25 * k;
      yValues1Add[0] = grad1;
      yValues1Add[nData + 1] = grad2;
      yValues2Add[0] = grad1;
      yValues2Add[nData + 1] = grad2;
      yValues3Add[0] = grad1;
      yValues3Add[nData + 1] = grad2;
      for (int i = 1; i < nData + 1; ++i) {
        yValues1Add[i] = yValues1[i - 1];
        yValues2Add[i] = yValues2[i - 1];
        yValues3Add[i] = yValues3[i - 1];
      }

      final double[] resPrim1 = INTERP.interpolate(xValues, yValues1Add, xKeys).getData();
      final double[] resPrim2 = INTERP.interpolate(xValues, yValues2Add, xKeys).getData();
      final double[] resPrim3 = INTERP.interpolate(xValues, yValues3Add, xKeys).getData();

      Interpolator1DDataBundle dataBund1 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1, grad1, grad2);
      Interpolator1DDataBundle dataBund2 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2, grad1, grad2);
      Interpolator1DDataBundle dataBund3 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3, grad1, grad2);

      //    Interpolator1DDataBundle nat1 = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues1);
      //    Interpolator1DDataBundle nat2 = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues2);
      //    Interpolator1DDataBundle nat3 = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues3);
      //    Interpolator1DCubicSplineDataBundle cub1 = new Interpolator1DCubicSplineDataBundle(nat1, grad1, grad2);
      //    Interpolator1DCubicSplineDataBundle cub2 = new Interpolator1DCubicSplineDataBundle(nat2, grad1, grad2);
      //    Interpolator1DCubicSplineDataBundle cub3 = new Interpolator1DCubicSplineDataBundle(nat3, grad1, grad2);

      for (int i = 0; i < 10 * nData; ++i) {
        assertEquals(resPrim1[i], INTERP1D.interpolate(dataBund1, xKeys[i]), 1.e-15);
        assertEquals(resPrim2[i], INTERP1D.interpolate(dataBund2, xKeys[i]), 1.e-15);
        assertEquals(resPrim3[i], INTERP1D.interpolate(dataBund3, xKeys[i]), 1.e-15);
        //      System.out.println(xKeys[i] + "\t" + INTERP1D.interpolate(dataBund1, xKeys[i]) + "\t" + INTERP_NAT_1D.interpolate(cub1, xKeys[i]));
      }
      //    System.out.println("\n");

      for (int j = 0; j < nData; ++j) {
        yValues1Up[j] = yValues1[j] == 0. ? EPS : yValues1[j] * (1. + EPS);
        yValues2Up[j] = yValues2[j] == 0. ? EPS : yValues2[j] * (1. + EPS);
        yValues3Up[j] = yValues3[j] == 0. ? EPS : yValues3[j] * (1. + EPS);
        yValues1Dw[j] = yValues1[j] == 0. ? -EPS : yValues1[j] * (1. - EPS);
        yValues2Dw[j] = yValues2[j] == 0. ? -EPS : yValues2[j] * (1. - EPS);
        yValues3Dw[j] = yValues3[j] == 0. ? -EPS : yValues3[j] * (1. - EPS);
        Interpolator1DDataBundle dataBund1Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Up);
        Interpolator1DDataBundle dataBund2Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Up);
        Interpolator1DDataBundle dataBund3Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Up);
        Interpolator1DDataBundle dataBund1Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Dw);
        Interpolator1DDataBundle dataBund2Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Dw);
        Interpolator1DDataBundle dataBund3Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Dw);
        //      Interpolator1DDataBundle nat1Up = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues1Up);
        //      Interpolator1DDataBundle nat2Up = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues2Up);
        //      Interpolator1DDataBundle nat3Up = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues3Up);
        //      Interpolator1DCubicSplineDataBundle cub1Up = new Interpolator1DCubicSplineDataBundle(nat1Up, grad1, grad2);
        //      Interpolator1DCubicSplineDataBundle cub2Up = new Interpolator1DCubicSplineDataBundle(nat2Up, grad1, grad2);
        //      Interpolator1DCubicSplineDataBundle cub3Up = new Interpolator1DCubicSplineDataBundle(nat3Up, grad1, grad2);
        //      Interpolator1DDataBundle nat1Dw = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues1Dw);
        //      Interpolator1DDataBundle nat2Dw = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues2Dw);
        //      Interpolator1DDataBundle nat3Dw = INTERP_NAT_1D.getDataBundleFromSortedArrays(xValues, yValues3Dw);
        //      Interpolator1DCubicSplineDataBundle cub1Dw = new Interpolator1DCubicSplineDataBundle(nat1Dw, grad1, grad2);
        //      Interpolator1DCubicSplineDataBundle cub2Dw = new Interpolator1DCubicSplineDataBundle(nat2Dw, grad1, grad2);
        //      Interpolator1DCubicSplineDataBundle cub3Dw = new Interpolator1DCubicSplineDataBundle(nat3Dw, grad1, grad2);
        for (int i = 0; i < 10 * nData; ++i) {
          final double ref1 = yValues1[j] == 0. ? EPS : yValues1[j] * EPS;
          final double ref2 = yValues2[j] == 0. ? EPS : yValues2[j] * EPS;
          final double ref3 = yValues3[j] == 0. ? EPS : yValues3[j] * EPS;
          double res1 = 0.5 * (INTERP1D.interpolate(dataBund1Up, xKeys[i]) - INTERP1D.interpolate(dataBund1Dw, xKeys[i])) / ref1;
          double res2 = 0.5 * (INTERP1D.interpolate(dataBund2Up, xKeys[i]) - INTERP1D.interpolate(dataBund2Dw, xKeys[i])) / ref2;
          double res3 = 0.5 * (INTERP1D.interpolate(dataBund3Up, xKeys[i]) - INTERP1D.interpolate(dataBund3Dw, xKeys[i])) / ref3;
          //        double res1Nat = 0.5 * (INTERP_NAT_1D.interpolate(cub1Up, xKeys[i]) - INTERP_NAT_1D.interpolate(cub1Dw, xKeys[i])) / ref1;
          //        double res2Nat = 0.5 * (INTERP_NAT_1D.interpolate(cub2Up, xKeys[i]) - INTERP_NAT_1D.interpolate(cub2Dw, xKeys[i])) / ref2;
          //        double res3Nat = 0.5 * (INTERP_NAT_1D.interpolate(cub3Up, xKeys[i]) - INTERP_NAT_1D.interpolate(cub3Dw, xKeys[i])) / ref3;
          //                System.out.println(res1 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j] + "\t" + res1Nat + "\t" + INTERP_NAT_1D.getNodeSensitivitiesForValue(cub1, xKeys[i])[j]);
          //        System.out.println(res2 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j]);
          //        System.out.println(res3 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j]);
          //        System.out.println(i + "\t" + j);
          assertEquals(res1, INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
          assertEquals(res2, INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
          assertEquals(res3, INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
        }
        yValues1Up[j] = yValues1[j];
        yValues2Up[j] = yValues2[j];
        yValues3Up[j] = yValues3[j];
        yValues1Dw[j] = yValues1[j];
        yValues2Dw[j] = yValues2[j];
        yValues3Dw[j] = yValues3[j];
      }
    }
  }

}
