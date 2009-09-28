/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public class LinearInterpolator2DTest {
  private static final Function1D<Double, Double> FUNCTION1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(Double x) {
      return 4. * x - 5.;
    }

  };
  private static final Function1D<Pair<Double, Double>, Double> FUNCTION2 = new Function1D<Pair<Double, Double>, Double>() {

    @Override
    public Double evaluate(Pair<Double, Double> xy) {
      return 2. * xy.getFirst() - 3. * xy.getSecond() + 10.;
    }

  };
  private static final Map<Pair<Double, Double>, Double> DATA1 = new HashMap<Pair<Double, Double>, Double>();
  private static final Map<Pair<Double, Double>, Double> DATA2 = new HashMap<Pair<Double, Double>, Double>();
  private static final Interpolator2D INTERPOLATOR = new LinearInterpolator2D();
  private static final double EPS = 1e-12;

  static {
    Pair<Double, Double> p1 = new Pair<Double, Double>(0., 0.);
    Pair<Double, Double> p2 = new Pair<Double, Double>(0., 1.);
    Pair<Double, Double> p3 = new Pair<Double, Double>(1., 0.);
    Pair<Double, Double> p4 = new Pair<Double, Double>(1., 1.);
    DATA1.put(p1, FUNCTION1.evaluate(p1.getFirst()));
    DATA1.put(p2, FUNCTION1.evaluate(p2.getFirst()));
    DATA1.put(p3, FUNCTION1.evaluate(p3.getFirst()));
    DATA1.put(p4, FUNCTION1.evaluate(p4.getFirst()));
    DATA2.put(p1, FUNCTION2.evaluate(p1));
    DATA2.put(p2, FUNCTION2.evaluate(p2));
    DATA2.put(p3, FUNCTION2.evaluate(p3));
    DATA2.put(p4, FUNCTION2.evaluate(p4));
  }

  @Test
  public void test() {
    try {
      INTERPOLATOR.interpolate(null, null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.interpolate(null, new Pair<Double, Double>(null, 0.));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.interpolate(null, new Pair<Double, Double>(0., null));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected;
    }
    Double x = 0.3;
    Double y = 0.1;
    Pair<Double, Double> xy = new Pair<Double, Double>(x, y);
    assertEquals(INTERPOLATOR.interpolate(DATA1, xy).getResult(), FUNCTION1.evaluate(x), EPS);
    assertEquals(INTERPOLATOR.interpolate(DATA2, xy).getResult(), FUNCTION2.evaluate(xy), EPS);
  }
}
