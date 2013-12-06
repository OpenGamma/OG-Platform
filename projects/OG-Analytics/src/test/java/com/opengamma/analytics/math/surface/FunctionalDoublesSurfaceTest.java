/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionalDoublesSurfaceTest {
  private static final String NAME1 = "P";
  private static final String NAME2 = "O";
  private static final Function<Double, Double> F1 = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xy) {
      return xy[0] + xy[1];
    }

  };
  private static final Function<Double, Double> F2 = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xy) {
      return xy[0] + xy[1];
    }

  };
  private static final FunctionalDoublesSurface SURFACE = new FunctionalDoublesSurface(F1, NAME1);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction1() {
    new FunctionalDoublesSurface(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction2() {
    new FunctionalDoublesSurface(null, NAME1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetXData() {
    SURFACE.getXData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetYData() {
    SURFACE.getYData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetZData() {
    SURFACE.getZData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetSize() {
    SURFACE.size();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullX() {
    SURFACE.getZValue(null, 2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullY() {
    SURFACE.getZValue(1., null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
    assertEquals(SURFACE.getZValue(1., 2.), F1.evaluate(1., 2.), 0);
    assertEquals(SURFACE.getZValue(DoublesPair.of(1., 4.)), F1.evaluate(1., 4.), 0);
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
