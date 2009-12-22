/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.timeseries.model.AutoregressiveTimeSeriesModel;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class AutoregressiveTimeSeriesOrderIdentifierTest {
  private static final AutoregressiveTimeSeriesPACFOrderIdentifier PACF_IDENTIFIER = new AutoregressiveTimeSeriesPACFOrderIdentifier(10, 0.01);
  private static final AutoregressiveTimeSeriesModel AR_MODEL = new AutoregressiveTimeSeriesModel(new NormalProbabilityDistribution(0, 1));
  private static final DoubleTimeSeries RANDOM;
  private static final DoubleTimeSeries AR3;
  private static final DoubleTimeSeries AR5;

  static {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final List<Double> random = new ArrayList<Double>();
    final ProbabilityDistribution<Double> normal = new NormalProbabilityDistribution(2, 1);
    final int n = 20000;
    for (int i = 0; i < n; i++) {
      dates.add(ZonedDateTime.fromInstant(Instant.instant(i), TimeZone.UTC));
      random.add(normal.nextRandom());
    }
    final int order = 3;
    final Double[] coeffs = new Double[order + 1];
    coeffs[0] = 0.1;
    for (int i = 1; i <= order; i++) {
      coeffs[i] = 1. / (i + 5);
    }
    AR3 = AR_MODEL.getSeries(coeffs, order, dates);
    AR5 = AR_MODEL.getSeries(new Double[] { coeffs[0], coeffs[1], coeffs[2], 0., 0., 0.1 }, 5, dates);
    RANDOM = new ArrayDoubleTimeSeries(dates, random);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadOrder() {
    new AutoregressiveTimeSeriesPACFOrderIdentifier(-10, 0.05);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new AutoregressiveTimeSeriesPACFOrderIdentifier(20, -0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighLevel() {
    new AutoregressiveTimeSeriesPACFOrderIdentifier(20, 1.2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    PACF_IDENTIFIER.getOrder(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS() {
    PACF_IDENTIFIER.getOrder(ArrayDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientData() {
    PACF_IDENTIFIER.getOrder(new ArrayDoubleTimeSeries(Arrays.asList(ZonedDateTime.fromInstant(Instant.instant(1), TimeZone.UTC), ZonedDateTime.fromInstant(Instant.instant(2),
        TimeZone.UTC)), Arrays.asList(new Double[] { 0.1, 0.2 })));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRandomTS() {
    PACF_IDENTIFIER.getOrder(RANDOM);
  }

  @Test
  public void test() {
    assertEquals(PACF_IDENTIFIER.getOrder(AR3), 3);
    assertEquals(PACF_IDENTIFIER.getOrder(AR5), 5);
  }
}
