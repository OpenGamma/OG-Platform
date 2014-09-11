package com.opengamma.analytics.financial.timeseries.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests for TimeSeriesRelativeWeightedDifferenceOperator.
 */
public class TimeSeriesRelativeWeightedDifferenceOperatorTest {

  private TimeSeriesRelativeWeightedDifferenceOperator _operator = new TimeSeriesRelativeWeightedDifferenceOperator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSeriesCausesException() {
    _operator.evaluate(null, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullWeightsCausesException() {
    _operator.evaluate(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void seriesMustHaveOneMoreElementThaWeights() {
    LocalDate[] dates = {LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3)};
    double[] values = {1.23, 2.34, 3.45};
    LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    LocalDateDoubleTimeSeries weights = series.tail(1);
    _operator.evaluate(series, weights);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void constantWeightsJustProducesSeriesDifference() {

    LocalDate[] dates = {LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3)};
    double[] values = {1.23, 2.34, 3.45};
    LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    LocalDate[] weightDates = {LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3)};
    double[] weightValues = {2, 2};
    LocalDateDoubleTimeSeries weights = ImmutableLocalDateDoubleTimeSeries.of(weightDates, weightValues);

    DateDoubleTimeSeries<LocalDate> result = (DateDoubleTimeSeries<LocalDate>) _operator.evaluate(series, weights);
    DateDoubleTimeSeries<LocalDate> expected = ImmutableLocalDateDoubleTimeSeries.of(
        new LocalDate[]{LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3)},
        new double[]{1.11, 1.11});
    compareTimeseries(result, expected);
  }

  /**
   * This tests the issue raised in PLT-426 where a 0 weight at the start
   * of a series causes NaNs to appear in the results as the value is
   * being multiplied by wt(T) / wt(t) where T is the final weight and
   * wt(t) = 0.
   * <p>
   * For the time being the fix is to set the calculated value to zero
   * which may want to be revisited in the future as it assumes that the
   * value we are multiplying is also 0.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void zeroWeightsAreHandled() {

    LocalDate[] dates = {LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3)};
    double[] values = {1.23, 2.34, 3.45};
    LocalDateDoubleTimeSeries series = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    LocalDate[] weightDates = {LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3)};
    double[] weightValues = {0, 2};
    LocalDateDoubleTimeSeries weights = ImmutableLocalDateDoubleTimeSeries.of(weightDates, weightValues);

    DateDoubleTimeSeries<LocalDate> result = (DateDoubleTimeSeries<LocalDate>) _operator.evaluate(series, weights);
    DateDoubleTimeSeries<LocalDate> expected = ImmutableLocalDateDoubleTimeSeries.of(
        new LocalDate[]{LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3)},
        new double[]{1.11, 1.11});
    compareTimeseries(result, expected);
  }

  private void compareTimeseries(DateDoubleTimeSeries<LocalDate> result, DateDoubleTimeSeries<LocalDate> expected) {
    assertThat(result.size(), is(expected.size()));
    for (int i = 0; i < result.size(); i++) {
      assertThat(result.getTimeAtIndex(i), is(expected.getTimeAtIndex(i)));
      assertThat("Expected values: " + expected.values() + " but got: " + result.values(),
                 result.getValueAtIndexFast(i) - expected.getValueAtIndexFast(i) < 1e-10, is(true));
    }
  }

}
