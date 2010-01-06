/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.AutocorrelationFunctionCalculator;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class MovingAverageTimeSeriesModelTest {
  private static final double MEAN = 0;
  private static final double STD = 0.25;
  private static final MovingAverageTimeSeriesModel MODEL = new MovingAverageTimeSeriesModel(new NormalDistribution(MEAN, STD));
  private static final int ORDER = 2;
  private static final DoubleTimeSeries MA;
  private static final Double[] THETA;
  private static double LIMIT = 3;

  static {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final int n = 20000;
    for (int i = 0; i < n; i++) {
      dates.add(ZonedDateTime.fromInstant(Instant.instant(i), TimeZone.UTC));
    }
    THETA = new Double[ORDER + 1];
    for (int i = 0; i <= ORDER; i++) {
      THETA[i] = (i + 1) / 10.;
    }
    MA = MODEL.getSeries(THETA, ORDER, dates);
    LIMIT /= Math.sqrt(n);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadConstructor() {
    new MovingAverageTimeSeriesModel(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullThetas() {
    MODEL.getSeries(null, 2, Arrays.asList(ZonedDateTime.fromInstant(Instant.instant(1), TimeZone.UTC)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyThetas() {
    MODEL.getSeries(new Double[0], 2, Arrays.asList(ZonedDateTime.fromInstant(Instant.instant(1), TimeZone.UTC)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeOrder() {
    MODEL.getSeries(new Double[] { 0.2 }, -3, Arrays.asList(ZonedDateTime.fromInstant(Instant.instant(1), TimeZone.UTC)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientThetas() {
    MODEL.getSeries(new Double[] { 0.2 }, 4, Arrays.asList(ZonedDateTime.fromInstant(Instant.instant(1), TimeZone.UTC)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDates() {
    MODEL.getSeries(new Double[] { 0.3 }, 1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDates() {
    MODEL.getSeries(new Double[] { 0.3 }, 1, new ArrayList<ZonedDateTime>());
  }

  @Test
  public void testACF() {
    final double eps = 1e-2;
    final Double[] rho = new AutocorrelationFunctionCalculator().evaluate(MA);
    assertEquals(rho[0], 1, 1e-16);
    final double denom = 1 + THETA[1] * THETA[1] + THETA[2] * THETA[2];
    assertEquals(rho[1], (THETA[1] * THETA[2] + THETA[1]) / denom, eps);
    assertEquals(rho[2], THETA[2] / denom, eps);
    for (int i = 1; i <= 20; i++) {
      if (i < ORDER + 1) {
        assertTrue(rho[i] > LIMIT);
      } else {
        assertTrue(rho[i] < LIMIT);
      }
    }
    final Double mean = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()).evaluate(MA);
    assertEquals(mean, THETA[0], eps);
    final Double variance = new DoubleTimeSeriesStatisticsCalculator(new SampleVarianceCalculator()).evaluate(MA);
    double sum = 1;
    for (int i = 1; i <= ORDER; i++) {
      sum += THETA[i] * THETA[i];
    }
    assertEquals(variance, sum * STD * STD, eps);
  }
}
