/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.InterpolatorND;
import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class InterpolatorNDTestCase {
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  protected static final List<Pair<double[], Double>> FLAT_DATA = new ArrayList<Pair<double[], Double>>();
  protected static final List<Pair<double[], Double>> COS_EXP_DATA = new ArrayList<Pair<double[], Double>>();
  protected static final List<Pair<double[], Double>> SWAPTION_ATM_VOL_DATA = new ArrayList<Pair<double[], Double>>();
  protected static final double VALUE = 0.3;

  protected static Function1D<double[], Double> COS_EXP_FUNCTION = new Function1D<double[], Double>() {

    @Override
    public Double evaluate(final double[] x) {
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
    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {10, 20}, 0.1697));
    SWAPTION_ATM_VOL_DATA.add(new ObjectsPair<double[], Double>(new double[] {15, 15}, 0.162));
  }

  protected void assertFlat(final InterpolatorND interpolator, final double tol) {
    double x1, x2, x3;
    double[] x;
    InterpolatorNDDataBundle dataBundle = interpolator.getDataBundle(FLAT_DATA);
    for (int i = 0; i < 10; i++) {
      x1 = 10 * RANDOM.nextDouble();
      x2 = 10 * RANDOM.nextDouble();
      x3 = 10 * RANDOM.nextDouble();
      x = new double[] {x1, x2, x3};
      final double fit = interpolator.interpolate(dataBundle, x);
      assertEquals(VALUE, fit, tol);
    }
    
    
  }

  protected void assertCosExp(final InterpolatorND interpolator, final double tol) {
    double x1, x2;
    double[] x;
    final InterpolatorNDDataBundle dataBundle = interpolator.getDataBundle(COS_EXP_DATA);
    for (int i = 0; i < 10; i++) {
      x1 = 10 * RANDOM.nextDouble();
      x2 = 10 * RANDOM.nextDouble();
      x = new double[] {x1, x2};
      final double fit = interpolator.interpolate(dataBundle, x);
      assertEquals(COS_EXP_FUNCTION.evaluate(x), fit, tol);
    }

    //check the input points are recovered exactly 
    for (int i = 0; i < 10; i++) {
      final Pair<double[], Double> t = COS_EXP_DATA.get(i);
      assertEquals(t.getSecond(), interpolator.interpolate(dataBundle, t.getFirst()), 1e-9);
    }
  }
}
