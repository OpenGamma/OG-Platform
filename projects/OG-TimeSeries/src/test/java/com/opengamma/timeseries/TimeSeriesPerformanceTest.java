/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;


import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Test to check performance.
 */
@Test(enabled = false)
public class TimeSeriesPerformanceTest {

  private static final int SMALL = 10;
  private static final int BIG = 10_000;

  public void test_addSimple_small() {
    for (int i = 0; i < 5; i++) {
      addSimple(SMALL, "addSimple-small");
    }
  }

  public void test_addSimple_big() {
    for (int i = 0; i < 5; i++) {
      addSimple(BIG, "addSimple-big");
    }
  }

  void addSimple(int size, String name) {
    int loop = 10_000_000 / size;
    LocalDate base = LocalDate.now().minusYears(2);
    LocalDate[] dates = new LocalDate[size];
    double[] values = new double[size];
    for (int i = 0; i < size; i++) {
      dates[i] = base.plusDays(i);
      values[i] = i * 1.2d;
    }
    DoubleTimeSeries<LocalDate> ts = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts = ts.add(1.5d);
    }
    long end = System.nanoTime();
    double diff = (((double) end) - ((double) start)) / 1_000_000L;
    System.out.println(diff + " " + ts.getEarliestValue() + " " + name);
  }

  //-------------------------------------------------------------------------
  public void test_addSeries_small() {
    for (int i = 0; i < 5; i++) {
      addSeries(SMALL, "addSeries-small");
    }
  }

  public void test_addSeries_big() {
    for (int i = 0; i < 5; i++) {
      addSeries(BIG, "addSeries-big");
    }
  }

  void addSeries(int size, String name) {
    int loop = 10_000_000 / size;
    LocalDate base = LocalDate.now().minusYears(2);
    LocalDate[] dates1 = new LocalDate[size];
    double[] values1 = new double[size];
    LocalDate[] dates2 = new LocalDate[size];
    double[] values2 = new double[size];
    for (int i = 0; i < size; i++) {
      dates1[i] = base.plusDays(i);
      values1[i] = i * 1.2d;
      dates2[i] = base.plusDays(i);
      values2[i] = i * 1.5d;
    }
    DoubleTimeSeries<LocalDate> ts1 = ImmutableLocalDateDoubleTimeSeries.of(dates1, values1);
    DoubleTimeSeries<LocalDate> ts2 = ImmutableLocalDateDoubleTimeSeries.of(dates2, values2);
    long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts1 = ts1.add(ts2);
    }
    long end = System.nanoTime();
    double diff = (((double) end) - ((double) start)) / 1_000_000L;
    System.out.println(diff + " " + ts1.getEarliestValue() + " " + name);
  }

  //-------------------------------------------------------------------------
  public void test_multiplySeries_small() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(SMALL, "multiplySeries-small");
    }
  }

  public void test_multiplySeries_big() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(BIG, "multiplySeries-big");
    }
  }

  void multiplySeries(int size, String name) {
    int loop = 10_000_000 / size;
    LocalDate base = LocalDate.now().minusYears(2);
    LocalDate[] dates1 = new LocalDate[size];
    double[] values1 = new double[size];
    LocalDate[] dates2 = new LocalDate[size];
    double[] values2 = new double[size];
    for (int i = 0; i < size; i++) {
      dates1[i] = base.plusDays(i);
      values1[i] = i * 1.2d;
      dates2[i] = base.plusDays(i);
      values2[i] = i * 1.5d;
    }
    DoubleTimeSeries<LocalDate> ts1 = ImmutableLocalDateDoubleTimeSeries.of(dates1, values1);
    DoubleTimeSeries<LocalDate> ts2 = ImmutableLocalDateDoubleTimeSeries.of(dates2, values2);
    long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts1 = ts1.multiply(ts2);
    }
    long end = System.nanoTime();
    double diff = (((double) end) - ((double) start)) / 1_000_000L;
    System.out.println(diff + " " + ts1.getEarliestValue() + " " + name);
  }

  //-------------------------------------------------------------------------
  public void test_reciprocal_small() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(SMALL, "reciprocal-small");
    }
  }

  public void test_reciprocal_big() {
    for (int i = 0; i < 5; i++) {
      multiplySeries(BIG, "reciprocal-big");
    }
  }

  void reciprocal(int size, String name) {
    int loop = 10_000_000 / size;
    LocalDate base = LocalDate.now().minusYears(2);
    LocalDate[] dates = new LocalDate[size];
    double[] values = new double[size];
    for (int i = 0; i < size; i++) {
      dates[i] = base.plusDays(i);
      values[i] = i * 1.2d;
    }
    DoubleTimeSeries<LocalDate> ts = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    long start = System.nanoTime();
    for (int j = 0; j < loop; j++) {
      ts = ts.reciprocal();
    }
    long end = System.nanoTime();
    double diff = (((double) end) - ((double) start)) / 1_000_000L;
    System.out.println(diff + " " + ts.getEarliestValue() + " " + name);
  }

}
