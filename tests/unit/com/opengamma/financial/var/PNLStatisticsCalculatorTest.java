/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;

/**
 * @author emcleod
 * 
 */
public class PNLStatisticsCalculatorTest {

  @Test(expected = IllegalArgumentException.class)
  public void test() {
    new PNLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator())).evaluate((HistoricalVaRDataBundle) null);
  }
}
