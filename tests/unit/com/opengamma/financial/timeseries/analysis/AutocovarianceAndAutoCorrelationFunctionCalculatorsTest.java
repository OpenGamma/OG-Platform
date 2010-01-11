/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class AutocovarianceAndAutoCorrelationFunctionCalculatorsTest {
  private static final Function1D<DoubleTimeSeries, Double[]> COVARIANCE = new AutocovarianceFunctionCalculator();
  private static final Function1D<DoubleTimeSeries, Double[]> CORRELATION = new AutocorrelationFunctionCalculator();

  @Test(expected = IllegalArgumentException.class)
  public void testCovarianceWithNull() {
    COVARIANCE.evaluate((DoubleTimeSeries) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCovarianceWithEmpty() {
    COVARIANCE.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCorrelationWithNull() {
    CORRELATION.evaluate((DoubleTimeSeries) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCorrelationWithEmpty() {
    CORRELATION.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void test() {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final List<Double> data = new ArrayList<Double>();
    final int n = 20000;
    for (int i = 0; i < n; i++) {
      dates.add(ZonedDateTime.fromInstant(Instant.instant(i), TimeZone.UTC));
      data.add(Math.random());
    }
    final Double[] result = CORRELATION.evaluate(new ArrayDoubleTimeSeries(dates, data));
    assertEquals(result[0], 1, 1e-16);
    final double level = 0.05;
    final double criticalValue = new NormalDistribution(0, 1).getInverseCDF(1 - level / 2.) / Math.sqrt(n);
    final int m = 500;
    final double expectedViolations = level * m;
    int sum = 0;
    for (int i = 1; i < m; i++) {
      if (Math.abs(result[i]) > criticalValue) {
        sum++;
      }
    }
    assertTrue(sum > expectedViolations - 10);
    assertTrue(sum < expectedViolations + 10);
  }
}
