/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class FunctionalVolatilityCurveTest {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x + 3 * x * x;
    }

  };
  private static final FunctionalVolatilityCurve CURVE = new FunctionalVolatilityCurve(F);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    new FunctionalVolatilityCurve(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPair() {
    CURVE.getVolatility(null);
  }

  @Test(expected = NotImplementedException.class)
  public void testSingleShift() {
    CURVE.withSingleShift(3., 0.4);
  }

  @Test(expected = NotImplementedException.class)
  public void testMultipleShift() {
    CURVE.withMultipleShifts(Collections.<Double, Double> singletonMap(1., 0.));
  }

  @Test(expected = NotImplementedException.class)
  public void testParallelShift() {
    CURVE.withParallelShift(0.4);
  }

  @Test
  public void testEqualsAndHashCode() {
    final Function1D<Double, Double> otherF = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return x;
      }

    };
    FunctionalVolatilityCurve other = new FunctionalVolatilityCurve(F);
    assertEquals(other, CURVE);
    assertEquals(other.hashCode(), CURVE.hashCode());
    other = new FunctionalVolatilityCurve(otherF);
    assertFalse(other.equals(CURVE));
  }

  @Test
  public void test() {
    assertEquals(CURVE.getVolatilityFunction(), F);
    Double x;
    for (int i = 0; i < 100; i++) {
      x = Math.random();
      assertEquals(CURVE.getVolatility(x), F.evaluate(x), 1e-15);
    }
  }
}
