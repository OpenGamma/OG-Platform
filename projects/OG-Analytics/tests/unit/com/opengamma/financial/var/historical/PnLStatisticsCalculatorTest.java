/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;

/**
 * 
 */
public class PnLStatisticsCalculatorTest {
  private static final DoubleTimeSeriesStatisticsCalculator MEAN = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());
  private static final PnLStatisticsCalculator CALCULATOR = new PnLStatisticsCalculator(MEAN);

  @Test(expected = IllegalArgumentException.class)
  public void testNullConstructor() {
    new PnLStatisticsCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.evaluate((HistoricalVaRDataBundle) null);
  }

  @Test
  public void testHashCodeAndEquals() {
    PnLStatisticsCalculator calculator1 = new PnLStatisticsCalculator(MEAN);
    PnLStatisticsCalculator calculator2 = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new SampleStandardDeviationCalculator()));
    assertEquals(CALCULATOR, calculator1);
    assertEquals(CALCULATOR.hashCode(), calculator1.hashCode());
    assertFalse(CALCULATOR.equals(calculator2));
  }
}
