/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.var.historical.HistoricalVaRDataBundle;
import com.opengamma.financial.var.historical.PnLStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;

/**
 * @author emcleod
 * 
 */
public class PnLStatisticsCalculatorTest {

  @Test(expected = IllegalArgumentException.class)
  public void test() {
    new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator())).evaluate((HistoricalVaRDataBundle) null);
  }
}
