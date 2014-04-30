/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TimeSeriesOperatorTest {

  @Test
  public void testWeightedVol() {
    DateDoubleTimeSeries<?> priceSeries = getTestPriceSeries();
    TimeSeriesWeightedVolatilityOperator weightedVolOperator = TimeSeriesWeightedVolatilityOperator.relative(0.94);
    DateDoubleTimeSeries<?> weightedVolSeries = weightedVolOperator.evaluate(priceSeries);
    DateDoubleTimeSeries<?> expectedWeightedVolSeries = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {ld(2), ld(3), ld(4), ld(5), ld(6), ld(7), ld(8), ld(9), ld(10)}, new double[] {0.010840108, 0.012469726, 0.012089848, 0.011794118, 0.011732656, 0.012375053, 0.013438035, 0.013028659, 0.013433833});
    TimeSeriesDataTestUtils.testCloseEquals(expectedWeightedVolSeries, weightedVolSeries, 0.000000001);
  }
  
  @Test
  public void testRelativeVolatilityWeighting() {
    DateDoubleTimeSeries<?> priceSeries = getTestPriceSeries();
    TimeSeriesWeightedVolatilityOperator weightedVolOperator = TimeSeriesWeightedVolatilityOperator.relative(0.94);
    DateDoubleTimeSeries<?> weightedVolSeries = weightedVolOperator.evaluate(priceSeries);
    TimeSeriesRelativeWeightedDifferenceOperator relativeWeightedDifferenceOperator = new TimeSeriesRelativeWeightedDifferenceOperator();
    DateDoubleTimeSeries<?> relativeWeightedDifferenceSeries = relativeWeightedDifferenceOperator.evaluate(priceSeries, weightedVolSeries);
    DateDoubleTimeSeries<?> expectedRelativeWeightedDifferenceSeries = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {ld(2), ld(3), ld(4), ld(5), ld(6), ld(7), ld(8), ld(9), ld(10)}, new double[] {-9.914168489, 21.546315757, 0, -4.556112623, 9.159959999, 16.283363544, -18.994057616, 0, 14});
    TimeSeriesDataTestUtils.testCloseEquals(expectedRelativeWeightedDifferenceSeries, relativeWeightedDifferenceSeries, 0.000000001);
  }
  
  private static DateDoubleTimeSeries<?> getTestPriceSeries() {
    DateDoubleTimeSeries<?> returnSeries = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {ld(1), ld(2), ld(3), ld(4), ld(5), ld(6), ld(7), ld(8), ld(9), ld(10)}, new double[] {738, 730, 750, 750, 746, 754, 769, 750, 750, 764});
    return returnSeries;
  }

  private static LocalDate ld(long day) {
    return LocalDate.ofEpochDay(day);
  }
  
}
