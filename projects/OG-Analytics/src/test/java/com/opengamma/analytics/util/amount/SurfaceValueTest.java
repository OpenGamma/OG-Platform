/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.amount;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SurfaceValueTest {

  private static final double TOLERANCE = 1.0E-10;

  @Test
  public void constructor() {
    SurfaceValue surf0 = new SurfaceValue();
    assertEquals("Surface value - constructor", 0, surf0.getMap().size());
    DoublesPair point1 = DoublesPair.of(1.0, 2.0);
    double value1 = 2345.678;
    SurfaceValue surf1 = SurfaceValue.from(point1, value1);
    assertEquals("Surface value - constructor", 1, surf1.getMap().size());
    assertTrue("Surface value - constructor", surf1.getMap().containsKey(point1));
    assertEquals("Surface value - constructor", value1, surf1.getMap().get(point1), TOLERANCE);
  }

  @Test
  public void plus() {
    DoublesPair point1 = DoublesPair.of(1.0, 2.0);
    double value1 = 2345.678;
    SurfaceValue surf1 = SurfaceValue.from(point1, value1);
    DoublesPair point2 = DoublesPair.of(2.0, Math.PI);
    double value2 = 10 * Math.E;
    SurfaceValue surfPlus1 = SurfaceValue.plus(surf1, point2, value2);
    assertEquals("Surface value - plus", 2, surfPlus1.getMap().size());
    assertTrue("Surface value - plus", surfPlus1.getMap().containsKey(point1));
    assertTrue("Surface value - plus", surfPlus1.getMap().containsKey(point2));
    assertEquals("Surface value - plus", value1, surfPlus1.getMap().get(point1), TOLERANCE);
    assertEquals("Surface value - plus", value2, surfPlus1.getMap().get(point2), TOLERANCE);
    SurfaceValue surf2 = SurfaceValue.from(point2, value2);
    SurfaceValue surfPlus2 = SurfaceValue.plus(surf1, surf2);
    assertEquals("Surface value - plus", 2, surfPlus2.getMap().size());
    assertTrue("Surface value - plus", surfPlus2.getMap().containsKey(point1));
    assertTrue("Surface value - plus", surfPlus2.getMap().containsKey(point2));
    assertEquals("Surface value - plus", value1, surfPlus2.getMap().get(point1), TOLERANCE);
    assertEquals("Surface value - plus", value2, surfPlus2.getMap().get(point2), TOLERANCE);
    assertTrue("Surface value - plus", SurfaceValue.compare(SurfaceValue.plus(surfPlus2, surfPlus2), SurfaceValue.multiplyBy(surfPlus2, 2), TOLERANCE));
    DoublesPair point3 = DoublesPair.of(2.0, 2.0);
    double value3 = 12.345;
    SurfaceValue surf3 = SurfaceValue.from(point3, value3);
    assertTrue("Surface value - plus", SurfaceValue.compare(SurfaceValue.plus(surfPlus2, point3, value3), SurfaceValue.plus(surfPlus2, surf3), TOLERANCE));
  }

  @Test
  public void multipliedBy() {
    DoublesPair point1 = DoublesPair.of(1.0, 2.0);
    double value1 = 2345.678;
    SurfaceValue surf1 = SurfaceValue.from(point1, value1);
    double factor = 3;
    SurfaceValue surf2 = SurfaceValue.multiplyBy(surf1, factor);
    SurfaceValue surf3 = SurfaceValue.from(point1, factor * value1);
    assertTrue("Surface value - multipliedBy", SurfaceValue.compare(surf3, surf2, TOLERANCE));
  }

  @Test
  public void compare() {
    DoublesPair point1 = DoublesPair.of(1.0, 2.0);
    double value1 = 2345.678;
    SurfaceValue surf1 = SurfaceValue.from(point1, value1);
    DoublesPair point2 = DoublesPair.of(2.0, Math.PI);
    double value2 = 10 * Math.E;
    SurfaceValue surf2 = SurfaceValue.from(point2, value2);
    SurfaceValue surfPlus1 = SurfaceValue.plus(surf1, surf2);
    SurfaceValue surfPlus2 = SurfaceValue.plus(surf2, surf1);
    assertTrue("Surface value - compare", SurfaceValue.compare(surfPlus1, surfPlus2, TOLERANCE));
  }

  @Test
  /**
   * Tests the toSingleValue method.
   */
  public void toSingleValue() {
    DoublesPair point1 = DoublesPair.of(1.0, 2.0);
    double value1 = 2345.678;
    SurfaceValue surf1 = SurfaceValue.from(point1, value1);
    DoublesPair point2 = DoublesPair.of(2.0, Math.PI);
    double value2 = 10 * Math.E;
    SurfaceValue surf2 = SurfaceValue.from(point2, value2);
    SurfaceValue surfPlus1 = SurfaceValue.plus(surf1, surf2);
    double amountComputed = surfPlus1.toSingleValue();
    assertEquals("Surface value - single value", amountComputed, value1 + value2, TOLERANCE);
  }

}
