package com.opengamma.analytics.financial.timeseries.util;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * A data set of time series to be used in testing.
 */
public class TimeSeriesDataSet {
  
  private static final LocalDate[] DATES_2014JAN = new LocalDate[] {
      LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3), 
      LocalDate.of(2014, 1, 6), LocalDate.of(2014, 1, 7), LocalDate.of(2014, 1, 8), LocalDate.of(2014, 1, 9), LocalDate.of(2014, 1, 10),
      LocalDate.of(2014, 1, 13), LocalDate.of(2014, 1, 14), LocalDate.of(2014, 1, 15), LocalDate.of(2014, 1, 16), LocalDate.of(2014, 1, 17),
      LocalDate.of(2014, 1, 20), LocalDate.of(2014, 1, 21), LocalDate.of(2014, 1, 22), LocalDate.of(2014, 1, 23), LocalDate.of(2014, 1, 24),
      LocalDate.of(2014, 1, 27), LocalDate.of(2014, 1, 28), LocalDate.of(2014, 1, 29), LocalDate.of(2014, 1, 30), LocalDate.of(2014, 1, 31) };
  private static final Double[] VALUE_GBPLIBOR3M_2014JAN = new Double[] {0.0024285, 0.0023985,   
      0.0023935, 0.002421, 0.002404, 0.0024165, 0.0024165, 
      0.002389, 0.0023675, 0.0023785, 0.0023635, 0.002366, 
      0.002371, 0.002366, 0.002371, 0.002386, 0.0023535, 
      0.002361, 0.002361,  0.002356, 0.002376, 0.002366};
  
  /** The time series for GBPLIBOR3M in January 2014. */
  private static final ImmutableLocalDateDoubleTimeSeries GBPLIBOR3M_2014JAN = 
      ImmutableLocalDateDoubleTimeSeries.of(DATES_2014JAN, VALUE_GBPLIBOR3M_2014JAN);
  
  /**
   * Returns the GBP Ibor 3M index time series for January 2014 up to the endDate (exclusive).
   * @param endDate The end date.
   * @return The time series.
   */
  public static LocalDateDoubleTimeSeries timeSeriesGbpLibor3M2014Jan(LocalDate endDate) {
    return GBPLIBOR3M_2014JAN.subSeries(LocalDate.of(2014, 1, 1), endDate);
  }

}
