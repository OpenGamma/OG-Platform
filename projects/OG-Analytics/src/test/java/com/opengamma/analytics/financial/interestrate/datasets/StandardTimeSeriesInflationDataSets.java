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
 * Time series of Price indexes used for testing purposes.
 */
public class StandardTimeSeriesInflationDataSets {

  /** ===== USD HICP Index ===== */ /** CPURNSA Index*/
//TODO : put the right value for 2005, 2006, 2007, 2008
  private static final double[] USCPI_VALUE_2005 = new double[] {
    211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949 };
  private static final double[] USCPI_VALUE_2006 = new double[] {
    211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949 };
  private static final double[] USCPI_VALUE_2007 = new double[] {
    211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949 };
  private static final double[] USCPI_VALUE_2008 = new double[] {
    211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949 };
  private static final double[] USCPI_VALUE_2009 = new double[] {
    211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949 };
  private static final double[] USCPI_VALUE_2010 = new double[] {
    216.687, 216.741, 217.631, 218.009, 218.178, 217.965, 218.011, 218.312, 218.439, 218.711, 218.803, 219.179 };
  private static final double[] USCPI_VALUE_2011 = new double[] {
    220.223, 221.309, 223.467, 224.906, 225.964, 225.722, 225.922, 226.545, 226.889, 226.421, 226.230, 225.672 };
  private static final double[] USCPI_VALUE_2012 = new double[] {
    226.655, 227.663, 229.392, 230.085, 229.815, 229.478, 229.104, 230.379, 231.407, 231.317, 230.221, 229.601 };
  private static final double[] USCPI_VALUE_2013 = new double[] {
    230.280, 232.166, 232.773, 232.531, 232.945, 233.504, 233.596, 233.877, 234.149, 233.546, 233.069, 233.049 };
  private static final double[] USCPI_VALUE_2014 = new double[] {
    233.916, 234.781, 236.293, 237.072, 237.900, 238.343, 238.250, 237.852 };
  private static final double[] USCPI_VALUE = new double[9 * 12 + USCPI_VALUE_2014.length];
  static {
    System.arraycopy(USCPI_VALUE_2005, 0, USCPI_VALUE, 0, 12);
    System.arraycopy(USCPI_VALUE_2006, 0, USCPI_VALUE, 12, 12);
    System.arraycopy(USCPI_VALUE_2007, 0, USCPI_VALUE, 24, 12);
    System.arraycopy(USCPI_VALUE_2008, 0, USCPI_VALUE, 36, 12);
    System.arraycopy(USCPI_VALUE_2009, 0, USCPI_VALUE, 48, 12);
    System.arraycopy(USCPI_VALUE_2010, 0, USCPI_VALUE, 60, 12);
    System.arraycopy(USCPI_VALUE_2011, 0, USCPI_VALUE, 72, 12);
    System.arraycopy(USCPI_VALUE_2012, 0, USCPI_VALUE, 84, 12);
    System.arraycopy(USCPI_VALUE_2013, 0, USCPI_VALUE, 96, 12);
    System.arraycopy(USCPI_VALUE_2014, 0, USCPI_VALUE, 108, USCPI_VALUE_2014.length);
  }
  private static final ZonedDateTime USCPI_START_DATE = DateUtils.getUTCDate(2005, 1, 31);
  private static final ZonedDateTime[] USCPI_DATES = new ZonedDateTime[USCPI_VALUE.length];
  static
  {
    for (int i=0; i<USCPI_VALUE.length; i++) {
      USCPI_DATES[i] = USCPI_START_DATE.plusMonths(i);
    }
  }
  
  private static final ZonedDateTimeDoubleTimeSeries USCPI_TIME_SERIES = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(USCPI_DATES, USCPI_VALUE);
  
  /**
   * Returns the US CPU index time series from January 2005 up to the endDate (exclusive).
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesUsCpi(ZonedDateTime endDate) {
    return USCPI_TIME_SERIES.subSeries(DateUtils.getUTCDate(2005, 1, 1), endDate);
  }
  
  /**
   * Returns the US CPU index time series from startDate (inclusive) up to the endDate (exclusive).
   * @param startDate The start date.
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesUsCpi(ZonedDateTime startDate, ZonedDateTime endDate) {
    return USCPI_TIME_SERIES.subSeries(startDate, endDate);
  }

  /** ===== BRL IPCA Index ===== */ /** BZPIIPCA Index */
  private static final double[] BRIPCA_VALUE_2013 = new double[] {
    3633.44, 3655.24, 3672.42, 3692.62, 3706.28, 3715.92, 3717.03, 3725.95, 3738.99, 3760.3, 3780.61, 3815.39 };
  private static final double[] BRIPCA_VALUE_2014 = new double[] {
    3836.38, 3862.84, 3898.38, 3924.50, 3655.24, 3942.55, 3958.32, 3958.72, 3968.62, 3991.24 };
  private static final double[] BRIPCA_VALUE = new double[1 * 12 + BRIPCA_VALUE_2014.length];
  static {
    System.arraycopy(BRIPCA_VALUE_2013, 0, USCPI_VALUE, 0, 12);
    System.arraycopy(BRIPCA_VALUE_2014, 0, USCPI_VALUE, 12, BRIPCA_VALUE_2014.length);
  }
  private static final ZonedDateTime BRIPCA_START_DATE = DateUtils.getUTCDate(2013, 1, 31);
  private static final ZonedDateTime[] BRIPCA_DATES = new ZonedDateTime[BRIPCA_VALUE.length];
  static
  {
    for (int i=0; i<BRIPCA_VALUE.length; i++) {
      BRIPCA_DATES[i] = BRIPCA_START_DATE.plusMonths(i);
    }
  }
  
  private static final ZonedDateTimeDoubleTimeSeries BRIPCA_TIME_SERIES = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(BRIPCA_DATES, BRIPCA_VALUE);
  
  /**
   * Returns the Brazil IPCA index time series from January 2013 up to the endDate (exclusive).
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesBrCpi(ZonedDateTime endDate) {
    return BRIPCA_TIME_SERIES.subSeries(DateUtils.getUTCDate(2013, 1, 1), endDate);
  }
  
  /**
   * Returns the Brazil IPCA index time series from startDate (inclusive) up to the endDate (exclusive).
   * @param startDate The start date.
   * @param endDate The end date.
   * @return The time series.
   */
  public static ZonedDateTimeDoubleTimeSeries timeSeriesBrCpi(ZonedDateTime startDate, ZonedDateTime endDate) {
    return BRIPCA_TIME_SERIES.subSeries(startDate, endDate);
  }


}
