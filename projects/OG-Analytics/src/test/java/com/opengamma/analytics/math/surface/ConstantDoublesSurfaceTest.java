/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConstantDoublesSurfaceTest {
  private static final double Z1 = 12;
  private static final double Z2 = 34;
  private static final String NAME1 = "a";
  private static final String NAME2 = "s";
  private static final ConstantDoublesSurface SURFACE = new ConstantDoublesSurface(Z1, NAME1);

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetXData() {
    SURFACE.getXData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetYData() {
    SURFACE.getYData();
  }

  @Test
  public void testEqualsAndHashCode() {
    ConstantDoublesSurface other = new ConstantDoublesSurface(Z1, NAME1);
    assertEquals(SURFACE, other);
    assertEquals(SURFACE.hashCode(), other.hashCode());
    other = new ConstantDoublesSurface(Z2, NAME1);
    assertFalse(SURFACE.equals(other));
    other = new ConstantDoublesSurface(Z1, NAME2);
    assertFalse(SURFACE.equals(other));
    other = new ConstantDoublesSurface(Z1);
    assertFalse(SURFACE.equals(other));
  }

  @Test
  public void testGetters() {
    assertEquals(SURFACE.getName(), NAME1);
    assertArrayEquals(SURFACE.getZData(), new Double[] {Z1});
    assertEquals(SURFACE.size(), 1);
    assertEquals(SURFACE.getZValue(1., 2.), Z1, 0);
    assertEquals(SURFACE.getZValue(DoublesPair.of(1., 4.)), Z1, 0);
    final Double[] z = SURFACE.getZData();
    assertTrue(z == SURFACE.getZData());
  }

  @Test
  public void testStaticConstruction() {
    ConstantDoublesSurface surface = new ConstantDoublesSurface(Z1);
    ConstantDoublesSurface other = ConstantDoublesSurface.from(Z1);
    assertArrayEquals(surface.getZData(), other.getZData());
    assertFalse(surface.equals(other));
    surface = new ConstantDoublesSurface(Z1, NAME1);
    other = ConstantDoublesSurface.from(Z1, NAME1);
    assertEquals(surface, other);
  }
}
