/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.time.DateUtils;

/**
 * Time series of Overnight and Ibor indexes used for testing purposes.
 */
public class StandardTimeSeriesOnIborDataSets {
  
  /** ===== GBP IBOR 3M INDEX ===== */
  
  private static final ZonedDateTimeDoubleTimeSeries GBPLIBOR3M_2014JAN = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {
        DateUtils.getUTCDate(2014, 1, 2), DateUtils.getUTCDate(2014, 1, 3), 
        DateUtils.getUTCDate(2014, 1, 6), DateUtils.getUTCDate(2014, 1, 7), DateUtils.getUTCDate(2014, 1, 8), DateUtils.getUTCDate(2014, 1, 9), DateUtils.getUTCDate(2014, 1, 10),
        DateUtils.getUTCDate(2014, 1, 13), DateUtils.getUTCDate(2014, 1, 14), DateUtils.getUTCDate(2014, 1, 15), DateUtils.getUTCDate(2014, 1, 16), DateUtils.getUTCDate(2014, 1, 17),
        DateUtils.getUTCDate(2014, 1, 20), DateUtils.getUTCDate(2014, 1, 21), DateUtils.getUTCDate(2014, 1, 22), DateUtils.getUTCDate(2014, 1, 23), DateUtils.getUTCDate(2014, 1, 24),
        DateUtils.getUTCDate(2014, 1, 27), DateUtils.getUTCDate(2014, 1, 28), DateUtils.getUTCDate(2014, 1, 29), DateUtils.getUTCDate(2014, 1, 30), DateUtils.getUTCDate(2014, 1, 31) },
      new double[] {0.0024285, 0.0023985,   
        0.0023935, 0.002421, 0.002404, 0.0024165, 0.0024165, 
        0.002389, 0.0023675, 0.0023785, 0.0023635, 0.002366, 
        0.002371, 0.002366, 0.002371, 0.002386, 0.0023535, 
        0.002361, 0.002361,  0.002356, 0.002376, 0.002366});
  
  /**
   * Returns the GBP Ibor 3M index time series for January 2014 up to the endDate (exclusive).
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesGbpIbor3M2014Jan(ZonedDateTime endDate) {
    return GBPLIBOR3M_2014JAN.subSeries(DateUtils.getUTCDate(2014, 1, 1), endDate);
  }
  
  /** ===== USD IBOR 3M INDEX ===== */
  
  private static final ZonedDateTimeDoubleTimeSeries USDLIBOR3M_2014JAN = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {
        DateUtils.getUTCDate(2014, 1, 2), DateUtils.getUTCDate(2014, 1, 3), 
        DateUtils.getUTCDate(2014, 1, 6), DateUtils.getUTCDate(2014, 1, 7), DateUtils.getUTCDate(2014, 1, 8), DateUtils.getUTCDate(2014, 1, 9), DateUtils.getUTCDate(2014, 1, 10),
        DateUtils.getUTCDate(2014, 1, 13), DateUtils.getUTCDate(2014, 1, 14), DateUtils.getUTCDate(2014, 1, 15), DateUtils.getUTCDate(2014, 1, 16), DateUtils.getUTCDate(2014, 1, 17),
        DateUtils.getUTCDate(2014, 1, 20), DateUtils.getUTCDate(2014, 1, 21), DateUtils.getUTCDate(2014, 1, 22), DateUtils.getUTCDate(2014, 1, 23), DateUtils.getUTCDate(2014, 1, 24),
        DateUtils.getUTCDate(2014, 1, 27), DateUtils.getUTCDate(2014, 1, 28), DateUtils.getUTCDate(2014, 1, 29), DateUtils.getUTCDate(2014, 1, 30), DateUtils.getUTCDate(2014, 1, 31) },
      new double[] {0.0024285, 0.0023985,   
        0.0023935, 0.002421, 0.002404, 0.0024165, 0.0024165, 
        0.002389, 0.0023675, 0.0023785, 0.0023635, 0.002366, 
        0.002371, 0.002366, 0.002371, 0.002386, 0.0023535, 
        0.002361, 0.002361,  0.002356, 0.002376, 0.002366});
  
  private static final ZonedDateTimeDoubleTimeSeries USDLIBOR3M_2014JUL = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {
        DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25),
        DateUtils.getUTCDate(2014, 7, 28), DateUtils.getUTCDate(2014, 7, 29), DateUtils.getUTCDate(2014, 7, 30), DateUtils.getUTCDate(2014, 7, 31) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341,
        0.002341, 0.002371, 0.002396, 0.002391 });
  
  /**
   * Returns the USD Ibor 3M index time series for January 2014 up to the endDate (exclusive).
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesUsdIbor3M2014Jan(ZonedDateTime endDate) {
    return USDLIBOR3M_2014JAN.subSeries(DateUtils.getUTCDate(2014, 1, 1), endDate);
  }
  
  /**
   * Returns the USD Ibor 3M index time series for July 2014 up to the endDate (exclusive).
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesUsdIbor3M2014Jul(ZonedDateTime endDate) {
    return USDLIBOR3M_2014JUL.subSeries(DateUtils.getUTCDate(2014, 7, 1), endDate);
  }
  
  /** ===== USD OVERNIGHT INDEX ===== */
  
  private static final ZonedDateTimeDoubleTimeSeries USDON_2014JAN = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {
        DateUtils.getUTCDate(2014, 1, 2), DateUtils.getUTCDate(2014, 1, 3), 
        DateUtils.getUTCDate(2014, 1, 6), DateUtils.getUTCDate(2014, 1, 7), DateUtils.getUTCDate(2014, 1, 8), DateUtils.getUTCDate(2014, 1, 9), DateUtils.getUTCDate(2014, 1, 10),
        DateUtils.getUTCDate(2014, 1, 13), DateUtils.getUTCDate(2014, 1, 14), DateUtils.getUTCDate(2014, 1, 15), DateUtils.getUTCDate(2014, 1, 16), DateUtils.getUTCDate(2014, 1, 17),
        DateUtils.getUTCDate(2014, 1, 20), DateUtils.getUTCDate(2014, 1, 21), DateUtils.getUTCDate(2014, 1, 22), DateUtils.getUTCDate(2014, 1, 23), DateUtils.getUTCDate(2014, 1, 24),
        DateUtils.getUTCDate(2014, 1, 27), DateUtils.getUTCDate(2014, 1, 28), DateUtils.getUTCDate(2014, 1, 29), DateUtils.getUTCDate(2014, 1, 30), DateUtils.getUTCDate(2014, 1, 31) },
      new double[] {0.0007, 0.0007,
        0.0007, 0.0007, 0.0007, 0.0007, 0.0007, 
        0.0007, 0.0007, 0.0007, 0.0007, 0.0007, 
        0.0007, 0.0007, 0.0007, 0.0007, 0.0007, 
        0.0007, 0.0007, 0.0008, 0.0008, 0.0008});
  
  /**
   * Returns the USD overnight index time series for January 2014 up to the endDate (exclusive).
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesUsdOn2014Jan(ZonedDateTime endDate) {
    return USDON_2014JAN.subSeries(DateUtils.getUTCDate(2014, 1, 1), endDate);
  }

}
