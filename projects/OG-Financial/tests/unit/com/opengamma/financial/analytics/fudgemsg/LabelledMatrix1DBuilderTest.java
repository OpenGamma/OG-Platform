/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;

/**
 * 
 */
public class LabelledMatrix1DBuilderTest extends AnalyticsTestBase {

  @Test
  public void testDouble() {
    final Double[] keys = new Double[] {1., 2., 3., 4., 5.};
    final Object[] labels = new Object[] {"1y", "2y", "3y", "4y", "5y"};
    final double[] values = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
    final DoubleLabelledMatrix1D m1 = new DoubleLabelledMatrix1D(keys, labels, values);
    final DoubleLabelledMatrix1D m2 = cycleObject(DoubleLabelledMatrix1D.class, m1);
    assertEquals(m1, m2);
    final DoubleLabelledMatrix1D m3 = new DoubleLabelledMatrix1D(keys, values);
    final DoubleLabelledMatrix1D m4 = cycleObject(DoubleLabelledMatrix1D.class, m3);
    assertEquals(m3, m4);
    final DoubleLabelledMatrix1D m5 = new DoubleLabelledMatrix1D(keys, keys, values);
    final DoubleLabelledMatrix1D m6 = cycleObject(DoubleLabelledMatrix1D.class, m5);
    assertEquals(m5, m6);
    assertEquals(m3, m6);
  }
}
