/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.model;

import static org.junit.Assert.assertEquals;

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
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class AutoregressiveTimeSeriesModelTest {
  private static final double MEAN = 0;
  private static final double STD = 0.25;
  private static final AutoregressiveTimeSeriesModel MODEL = new AutoregressiveTimeSeriesModel(new NormalProbabilityDistribution(MEAN, STD));
  private static final int ORDER = 2;
  private static final DoubleTimeSeries AR;
  private static final Double[] PHI;
  private static double LIMIT = 3;

  static {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final int n = 20000;
    for (int i = 0; i < n; i++) {
      dates.add(ZonedDateTime.fromInstant(Instant.instant(i), TimeZone.UTC));
    }
    PHI = new Double[ORDER + 1];
    for (int i = 0; i <= ORDER; i++) {
      PHI[i] = (i + 1) / 10.;
    }
    AR = MODEL.getSeries(PHI, ORDER, dates);
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
    final Double[] rho = new AutocorrelationFunctionCalculator().evaluate(AR);
    final double rho1 = PHI[1] / (1 - PHI[2]);
    assertEquals(rho[0], 1, 1e-16);
    assertEquals(rho[1], rho1, eps);
    assertEquals(rho[2], rho1 * PHI[1] + PHI[2], eps);
    final Double mean = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()).evaluate(AR);
    assertEquals(mean, PHI[0] / (1 - PHI[1] - PHI[2]), eps);
  }
}
