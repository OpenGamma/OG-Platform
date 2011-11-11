/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;

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
    final DoubleLabelledMatrix1D m7 = new DoubleLabelledMatrix1D(keys, labels, "labels", values, "values");
    final DoubleLabelledMatrix1D m8 = cycleObject(DoubleLabelledMatrix1D.class, m7);
    assertEquals(m7, m8);
  }

  @Test
  public void testLocalDate() {
    final LocalDate[] keys = new LocalDate[] {LocalDate.of(2012, 1, 1), LocalDate.of(2013, 1, 1), LocalDate.of(2014, 1, 1), LocalDate.of(2015, 1, 1), LocalDate.of(2016, 1, 1)};
    final Object[] labels = new Object[] {"1y", "2y", "3y", "4y", "5y"};
    final double[] values = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
    final LocalDateLabelledMatrix1D m1 = new LocalDateLabelledMatrix1D(keys, labels, values);
    final LocalDateLabelledMatrix1D m2 = cycleObject(LocalDateLabelledMatrix1D.class, m1);
    assertEquals(m1, m2);
    final LocalDateLabelledMatrix1D m3 = new LocalDateLabelledMatrix1D(keys, values);
    final LocalDateLabelledMatrix1D m4 = cycleObject(LocalDateLabelledMatrix1D.class, m3);
    assertEquals(m3, m4);
    final LocalDateLabelledMatrix1D m5 = new LocalDateLabelledMatrix1D(keys, keys, values);
    final LocalDateLabelledMatrix1D m6 = cycleObject(LocalDateLabelledMatrix1D.class, m5);
    assertEquals(m5, m6);
    final LocalDateLabelledMatrix1D m7 = new LocalDateLabelledMatrix1D(keys, labels, "labels", values, "values");
    final LocalDateLabelledMatrix1D m8 = cycleObject(LocalDateLabelledMatrix1D.class, m7);
    assertEquals(m7, m8);
  }

  @Test
  public void testString() {
    final String[] keys = new String[] {"A", "B", "C", "D", "E"};
    final double[] values = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
    final StringLabelledMatrix1D m1 = new StringLabelledMatrix1D(keys, values);
    final StringLabelledMatrix1D m2 = cycleObject(StringLabelledMatrix1D.class, m1);
    assertEquals(m1, m2);
    final StringLabelledMatrix1D m3 = new StringLabelledMatrix1D(keys, "labels", values, "values");
    final StringLabelledMatrix1D m4 = cycleObject(StringLabelledMatrix1D.class, m3);
    assertEquals(m3, m4);
  }
}
