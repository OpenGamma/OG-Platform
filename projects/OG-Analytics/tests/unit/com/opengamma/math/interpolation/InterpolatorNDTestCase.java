/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

  protected <T extends InterpolatorNDDataBundle> void printFlat(InterpolatorND<T> interpolator, T dataBundle) {

    // printout
    double[] x = new double[3];
    x[2] = 5.0;
    System.out.print("\t");
    for (int j = 0; j < 99; j++) {
      System.out.print(j / 10.0 + "\t");
    }
    System.out.print(99 / 10.0 + "\n");

    for (int i = 0; i < 100; i++) {
      System.out.print(i / 10.0 + "\t");
      x[0] = i / 10.0;
      for (int j = 0; j < 100; j++) {
        x[1] = j / 10.0;
        double fit = interpolator.interpolate(dataBundle, x);

        System.out.print(fit + "\t");
      }
      System.out.print("\n");
    }
  }

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

  // public void testData(final InterpolatorND interpolator) {
  // try {
  // interpolator.checkData(null);
  // fail();
  // } catch (final IllegalArgumentException e) {
  // // Expected
  // }
  // try {
  // interpolator.checkData(Collections.singletonMap(Collections.singletonList(3.), 4.));
  // fail();
  // } catch (final IllegalArgumentException e) {
  // // Expected
  // }
  // final Map<List<Double>, Double> data = new HashMap<List<Double>, Double>();
  // final List<Double> l1 = Arrays.asList(1., 2., 3.);
  // final List<Double> l2 = Arrays.asList(4., 5., 6.);
  // final List<Double> l3 = Arrays.asList(7., 8., 9., 10.);
  // data.put(null, 0.1);
  // data.put(null, 0.1);
  // try {
  // interpolator.checkData(data);
  // fail();
  // } catch (final IllegalArgumentException e) {
  // // Expected
  // }
  // data.clear();
  // data.put(l1, 0.1);
  // data.put(l2, null);
  // try {
  // interpolator.checkData(data);
  // fail();
  // } catch (final IllegalArgumentException e) {
  // // Expected
  // }
  // data.put(l1, 0.1);
  // data.put(l2, 0.2);
  // data.put(l3, 0.3);
  // try {
  // interpolator.getDimension(data.keySet());
  // fail();
  // } catch (final IllegalArgumentException e) {
  // // Expected
  // }
  // data.clear();
  // data.put(l1, 0.1);
  // data.put(l2, 0.2);
  // data.put(l2, 0.3);
  // assertEquals(interpolator.getDimension(data.keySet()), 3);
  // }
}
