/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class FunctionalDoublesSurfaceTest {
  private static final String NAME1 = "P";
  private static final String NAME2 = "O";
  private static final Function<DoublesPair, Double> F1 = new Function<DoublesPair, Double>() {

    @Override
    public Double evaluate(final DoublesPair... x) {
      return x[0].first + x[0].second;
    }

  };
  private static final Function<DoublesPair, Double> F2 = new Function<DoublesPair, Double>() {

    @Override
    public Double evaluate(final DoublesPair... x) {
      return x[0].first + x[0].second;
    }

  };
  private static final FunctionalDoublesSurface SURFACE = new FunctionalDoublesSurface(F1, NAME1);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction1() {
    new FunctionalDoublesSurface(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction2() {
    new FunctionalDoublesSurface(null, NAME1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetXData() {
    SURFACE.getXData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetYData() {
    SURFACE.getYData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetZData() {
    SURFACE.getZData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSize() {
    SURFACE.size();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullX() {
    SURFACE.getZValue(null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullY() {
    SURFACE.getZValue(1., null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPair() {
    SURFACE.getZValue(null);
  }

  @Test
  public void testEqualsAndHashCode() {
    FunctionalDoublesSurface other = new FunctionalDoublesSurface(F1, NAME1);
    assertEquals(SURFACE, other);
    assertEquals(SURFACE.hashCode(), other.hashCode());
    other = new FunctionalDoublesSurface(F2, NAME1);
    assertFalse(SURFACE.equals(other));
    other = new FunctionalDoublesSurface(F1, NAME2);
    assertFalse(SURFACE.equals(other));
    other = new FunctionalDoublesSurface(F1);
    assertFalse(SURFACE.equals(other));
  }

  @Test
  public void testGetters() {
    assertEquals(SURFACE.getName(), NAME1);
    assertEquals(SURFACE.getFunction(), F1);
    assertEquals(SURFACE.getZValue(1., 2.), F1.evaluate(DoublesPair.of(1., 2.)), 0);
    assertEquals(SURFACE.getZValue(DoublesPair.of(1., 4.)), F1.evaluate(DoublesPair.of(1., 4.)), 0);
  }

  @Test
  public void testStaticConstruction() {
    FunctionalDoublesSurface surface = new FunctionalDoublesSurface(F1);
    FunctionalDoublesSurface other = FunctionalDoublesSurface.from(F1);
    assertEquals(surface.getFunction(), other.getFunction());
    assertFalse(surface.equals(other));
    surface = new FunctionalDoublesSurface(F1, NAME1);
    other = FunctionalDoublesSurface.from(F1, NAME1);
    assertEquals(surface, other);
  }
}
