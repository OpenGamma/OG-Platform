/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class StringValueTest {

  private static final double TOLERANCE = 1.0E-10;

  @Test
  public void constructor() {
    StringValue surf0 = new StringValue();
    assertEquals("String value - constructor", 0, surf0.getMap().size());
    String point1 = "String 1";
    double value1 = 2345.678;
    StringValue surf1 = StringValue.from(point1, value1);
    assertEquals("Surface value - constructor", 1, surf1.getMap().size());
    assertTrue("Surface value - constructor", surf1.getMap().containsKey(point1));
    assertEquals("Surface value - constructor", value1, surf1.getMap().get(point1), TOLERANCE);
  }

  @Test
  public void plus() {
    String point1 = "Point 1";
    double value1 = 2345.678;
    StringValue surf1 = StringValue.from(point1, value1);
    String point2 = "Point 2";
    double value2 = 10 * Math.E;
    StringValue surfPlus1 = StringValue.plus(surf1, point2, value2);
    assertEquals("Surface value - plus", 2, surfPlus1.getMap().size());
    assertTrue("Surface value - plus", surfPlus1.getMap().containsKey(point1));
    assertTrue("Surface value - plus", surfPlus1.getMap().containsKey(point2));
    assertEquals("Surface value - plus", value1, surfPlus1.getMap().get(point1), TOLERANCE);
    assertEquals("Surface value - plus", value2, surfPlus1.getMap().get(point2), TOLERANCE);
    StringValue surf2 = StringValue.from(point2, value2);
    StringValue surfPlus2 = StringValue.plus(surf1, surf2);
    assertEquals("Surface value - plus", 2, surfPlus2.getMap().size());
    assertTrue("Surface value - plus", surfPlus2.getMap().containsKey(point1));
    assertTrue("Surface value - plus", surfPlus2.getMap().containsKey(point2));
    assertEquals("Surface value - plus", value1, surfPlus2.getMap().get(point1), TOLERANCE);
    assertEquals("Surface value - plus", value2, surfPlus2.getMap().get(point2), TOLERANCE);
    assertTrue("Surface value - plus", StringValue.compare(StringValue.plus(surfPlus2, surfPlus2), StringValue.multiplyBy(surfPlus2, 2), TOLERANCE));
    String point3 = "Point 3";
    double value3 = 12.345;
    StringValue surf3 = StringValue.from(point3, value3);
    assertTrue("Surface value - plus", StringValue.compare(StringValue.plus(surfPlus2, point3, value3), StringValue.plus(surfPlus2, surf3), TOLERANCE));
  }

  @Test
  public void multipliedBy() {
    String point1 = "Point 1";
    double value1 = 2345.678;
    StringValue surf1 = StringValue.from(point1, value1);
    double factor = 3;
    StringValue surf2 = StringValue.multiplyBy(surf1, factor);
    StringValue surf3 = StringValue.from(point1, factor * value1);
    assertTrue("Surface value - multipliedBy", StringValue.compare(surf3, surf2, TOLERANCE));
  }

  @Test
  public void compare() {
    String point1 = "Point 1";
    double value1 = 2345.678;
    StringValue surf1 = StringValue.from(point1, value1);
    String point2 = "Point 2";
    double value2 = 10 * Math.E;
    StringValue surf2 = StringValue.from(point2, value2);
    StringValue surfPlus1 = StringValue.plus(surf1, surf2);
    StringValue surfPlus2 = StringValue.plus(surf2, surf1);
    assertTrue("Surface value - compare", StringValue.compare(surfPlus1, surfPlus2, TOLERANCE));
  }

}
