/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class FunctionalVolatilitySurfaceTest {
  private static final Function1D<DoublesPair, Double> F = new Function1D<DoublesPair, Double>() {

    @Override
    public Double evaluate(final DoublesPair xy) {
      return xy.first * xy.first - 3 * xy.second;
    }

  };
  private static final FunctionalVolatilitySurface SURFACE = new FunctionalVolatilitySurface(F);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    new FunctionalVolatilitySurface(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPair() {
    SURFACE.getVolatility(null);
  }

  @Test(expected = NotImplementedException.class)
  public void testSingleShift() {
    SURFACE.withSingleShift(DoublesPair.of(2., 3.), 0.4);
  }

  @Test(expected = NotImplementedException.class)
  public void testMultipleShift() {
    SURFACE.withMultipleShifts(Collections.<DoublesPair, Double> singletonMap(DoublesPair.of(2., 3.), 0.));
  }

  @Test(expected = NotImplementedException.class)
  public void testParallelShift() {
    SURFACE.withParallelShift(0.4);
  }

  @Test
  public void testEqualsAndHashCode() {
    final Function1D<DoublesPair, Double> otherF = new Function1D<DoublesPair, Double>() {

      @Override
      public Double evaluate(final DoublesPair xy) {
        return xy.first + xy.second;
      }

    };
    FunctionalVolatilitySurface other = new FunctionalVolatilitySurface(F);
    assertEquals(other, SURFACE);
    assertEquals(other.hashCode(), SURFACE.hashCode());
    other = new FunctionalVolatilitySurface(otherF);
    assertFalse(other.equals(SURFACE));
  }

  @Test
  public void test() {
    assertEquals(SURFACE.getVolatilityFunction(), F);
    DoublesPair pair;
    for (int i = 0; i < 100; i++) {
      pair = DoublesPair.of(Math.random(), Math.random());
      assertEquals(SURFACE.getVolatility(pair), F.evaluate(pair), 1e-15);
    }
  }
}
