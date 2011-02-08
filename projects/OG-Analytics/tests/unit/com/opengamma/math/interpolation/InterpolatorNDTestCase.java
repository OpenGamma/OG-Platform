/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterpolatorNDTestCase {
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  protected static final List<Pair<double[], Double>> FLAT_DATA = new ArrayList<Pair<double[], Double>>();
  protected static final List<Pair<double[], Double>> COS_EXP_DATA = new ArrayList<Pair<double[], Double>>();
  protected static final List<Pair<double[], Double>> SWAPTION_ATM_VOL_DATA = new ArrayList<Pair<double[], Double>>();
  protected static final double VALUE = 0.3;

  protected static Function1D<double[], Double> COS_EXP_FUNCTION = new Function1D<double[], Double>() {

    @Override
    public Double evaluate(double[] x) {
      return Math.sin(Math.PI * x[0] / 10.0) * Math.exp(-x[1] / 5.);
    }
  };

  static {
    double x, y, z;
    double[] temp;
    for (int i = 0; i < 200; i++) {
      x = 10 * RANDOM.nextDouble();
      y = 10 * RANDOM.nextDouble();
      z = 10 * RANDOM.nextDouble();
      FLAT_DATA.add(new ObjectsPair<double[], Double>(new double[] {x, y, z}, VALUE));
      temp = new double[] {x, y};
      COS_EXP_DATA.add(new ObjectsPair<double[], Double>(temp, COS_EXP_FUNCTION.evaluate(temp)));
    }

    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {1, 1}, 0.7332));
    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {1, 5}, 0.36995));
    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {5, 5}, 0.23845));
    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {5, 10}, 0.2177));
    // SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {10, 10}, 0.18745));
    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {10, 20}, 0.1697));
    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {15, 15}, 0.162));

    // x 1 5 10 15 20
    // 1 0.7332 0.36995 x x x
    // 5 x 0.23845 0.2177 x x
    // 10 x x 0.18745 x 0.1697
    // 15 x x x 0.162 x

  }

  protected <T extends InterpolatorNDDataBundle> void testFlat(InterpolatorND<T> interpolator, T dataBundle, double tol) {
    double x1, x2, x3;
    double[] x;
    for (int i = 0; i < 10; i++) {
      x1 = 10 * RANDOM.nextDouble();
      x2 = 10 * RANDOM.nextDouble();
      x3 = 10 * RANDOM.nextDouble();
      x = new double[] {x1, x2, x3};
      double fit = interpolator.interpolate(dataBundle, x);
      assertEquals(VALUE, fit, tol);
    }
  }

  // protected <T extends InterpolatorNDDataBundle> void printFlat(InterpolatorND<T> interpolator, T dataBundle) {
  //
  // // printout
  // double[] x = new double[3];
  // x[2] = 5.0;
  // System.out.print("\t");
  // for (int j = 0; j < 99; j++) {
  // System.out.print(j / 10.0 + "\t");
  // }
  // System.out.print(99 / 10.0 + "\n");
  //
  // for (int i = 0; i < 100; i++) {
  // System.out.print(i / 10.0 + "\t");
  // x[0] = i / 10.0;
  // for (int j = 0; j < 100; j++) {
  // x[1] = j / 10.0;
  // double fit = interpolator.interpolate(dataBundle, x);
  //
  // System.out.print(fit + "\t");
  // }
  // System.out.print("\n");
  // }
  // }

  protected <T extends InterpolatorNDDataBundle> void testCosExp(InterpolatorND<T> interpolator, T dataBundle, double tol) {
    double x1, x2;
    double[] x;
    for (int i = 0; i < 10; i++) {
      x1 = 10 * RANDOM.nextDouble();
      x2 = 10 * RANDOM.nextDouble();
      x = new double[] {x1, x2};
      double fit = interpolator.interpolate(dataBundle, x);
      assertEquals(COS_EXP_FUNCTION.evaluate(x), fit, tol);
    }
  }

  // protected <T extends InterpolatorNDDataBundle> void printSwaptionData(InterpolatorND<T> interpolator, T dataBundle) {
  // double[] x = new double[2];
  // for (int j = 0; j < 101; j++) {
  // System.out.print("\t" + j / 5.0);
  // }
  // System.out.print("\n");
  //
  // for (int i = 0; i < 76; i++) {
  // System.out.print(i / 5.0);
  // x[0] = i / 5.0;
  // for (int j = 0; j < 101; j++) {
  // x[1] = j / 5.0;
  // double fit = interpolator.interpolate(dataBundle, x);
  //
  // System.out.print("\t" + fit);
  // }
  // System.out.print("\n");
  // }
  // }
}
