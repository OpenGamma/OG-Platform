/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;

/**
 * 
 */
public class LabelledMatrix2DBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final Double[] xKeys = new Double[] {1., 2., 3., 4., 5.};
    final Double[] yKeys = new Double[] {1., 2., 3.};
    final Object[] xLabels = new String[] {"A", "B", "C", "D", "E"};
    final Object[] yLabels = new String[] {"A", "B", "C"};
    final double[][] values = new double[][] {new double[] {1, 2, 3, 4, 5}, new double[] {2, 4, 6, 8, 10}, new double[] {3, 6, 9, 12, 15}};
    DoubleLabelledMatrix2D m1 = new DoubleLabelledMatrix2D(xKeys, xLabels, yKeys, yLabels, values);
    assertEquals(m1, cycleObject(DoubleLabelledMatrix2D.class, m1));
    DoubleLabelledMatrix2D m2 = new DoubleLabelledMatrix2D(xKeys, xLabels, "x", yKeys, yLabels, "y", values, "values");
    assertEquals(m2, cycleObject(DoubleLabelledMatrix2D.class, m2));
  }
  
  
  
}
