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

  /** ===== USD HICP Index ===== */
  private static final double[] USCPI_VALUE_2005 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2006 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2007 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2008 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };// TODO : put the right value for 2005, 2006, 2007, 2008
  private static final double[] USCPI_VALUE_2009 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2010 = new double[] {216.687, 216.741, 217.631, 218.009, 218.178, 217.965, 218.011, 218.312, 218.439, 218.711, 218.803, 219.179, 218.056 };
  private static final double[] USCPI_VALUE_2011 = new double[] {220.223, 221.309, 223.467, 224.906, 225.964, 225.722, 225.922, 226.545, 226.889, 226.421, 226.230, 225.672, 224.939 };
  private static final double[] USCPI_VALUE_2012 = new double[] {226.655, 227.663, 229.392, 230.085, 229.815, 229.478, 229.104, 230.379, 231.407, 231.317, 230.221, 229.601, 229.594 };
  private static final double[] USCPI_VALUE_2013 = new double[] {230.280 }; // TODO
  private static final double[] USCPI_VALUE_2014 = new double[] {230.280 };
  private static final double[] USCPI_VALUE = new double[8 * 12 + USCPI_VALUE_2013.length];
  static {
    System.arraycopy(USCPI_VALUE_2005, 0, USCPI_VALUE, 0, 12);
    System.arraycopy(USCPI_VALUE_2006, 0, USCPI_VALUE, 12, 12);
    System.arraycopy(USCPI_VALUE_2007, 0, USCPI_VALUE, 24, 12);
    System.arraycopy(USCPI_VALUE_2008, 0, USCPI_VALUE, 36, 12);
    System.arraycopy(USCPI_VALUE_2009, 0, USCPI_VALUE, 48, 12);
    System.arraycopy(USCPI_VALUE_2010, 0, USCPI_VALUE, 60, 12);
    System.arraycopy(USCPI_VALUE_2011, 0, USCPI_VALUE, 72, 12);
    System.arraycopy(USCPI_VALUE_2012, 0, USCPI_VALUE, 84, 12);
    System.arraycopy(USCPI_VALUE_2013, 0, USCPI_VALUE, 96, USCPI_VALUE_2013.length);

  }
  // TODO: generate the date automatically
  private static final ZonedDateTime[] USCPI_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2004, 12, 31), DateUtils.getUTCDate(2005, 1, 31), DateUtils.getUTCDate(2005, 2, 28),
    DateUtils.getUTCDate(2005, 3, 31), DateUtils.getUTCDate(2005, 4, 30), DateUtils.getUTCDate(2005, 5, 31), DateUtils.getUTCDate(2005, 6, 30), DateUtils.getUTCDate(2005, 7, 31),
    DateUtils.getUTCDate(2005, 8, 31), DateUtils.getUTCDate(2005, 9, 30), DateUtils.getUTCDate(2005, 10, 31), DateUtils.getUTCDate(2005, 11, 30), DateUtils.getUTCDate(2005, 12, 31),
    DateUtils.getUTCDate(2006, 1, 31), DateUtils.getUTCDate(2006, 2, 28), DateUtils.getUTCDate(2006, 3, 31), DateUtils.getUTCDate(2006, 4, 30), DateUtils.getUTCDate(2006, 5, 31),
    DateUtils.getUTCDate(2006, 6, 30), DateUtils.getUTCDate(2006, 7, 31), DateUtils.getUTCDate(2006, 8, 31), DateUtils.getUTCDate(2006, 9, 30), DateUtils.getUTCDate(2006, 10, 31),
    DateUtils.getUTCDate(2006, 11, 30), DateUtils.getUTCDate(2006, 12, 31), DateUtils.getUTCDate(2007, 1, 31), DateUtils.getUTCDate(2007, 2, 28), DateUtils.getUTCDate(2007, 3, 31),
    DateUtils.getUTCDate(2007, 4, 30), DateUtils.getUTCDate(2007, 5, 31), DateUtils.getUTCDate(2007, 6, 30), DateUtils.getUTCDate(2007, 7, 31), DateUtils.getUTCDate(2007, 8, 31),
    DateUtils.getUTCDate(2007, 9, 30), DateUtils.getUTCDate(2007, 10, 31), DateUtils.getUTCDate(2007, 11, 30), DateUtils.getUTCDate(2007, 12, 31), DateUtils.getUTCDate(2008, 1, 31),
    DateUtils.getUTCDate(2008, 2, 28), DateUtils.getUTCDate(2008, 3, 31), DateUtils.getUTCDate(2008, 4, 30), DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2008, 6, 30),
    DateUtils.getUTCDate(2008, 7, 31), DateUtils.getUTCDate(2008, 8, 31), DateUtils.getUTCDate(2008, 9, 30), DateUtils.getUTCDate(2008, 10, 31), DateUtils.getUTCDate(2008, 11, 30),
    DateUtils.getUTCDate(2008, 12, 31), DateUtils.getUTCDate(2009, 1, 31), DateUtils.getUTCDate(2009, 2, 28), DateUtils.getUTCDate(2009, 3, 31), DateUtils.getUTCDate(2009, 4, 30),
    DateUtils.getUTCDate(2009, 5, 31), DateUtils.getUTCDate(2009, 6, 30), DateUtils.getUTCDate(2009, 7, 31), DateUtils.getUTCDate(2009, 8, 31), DateUtils.getUTCDate(2009, 9, 30),
    DateUtils.getUTCDate(2009, 10, 31), DateUtils.getUTCDate(2009, 11, 30), DateUtils.getUTCDate(2009, 12, 31), DateUtils.getUTCDate(2010, 1, 31), DateUtils.getUTCDate(2010, 2, 28),
    DateUtils.getUTCDate(2010, 3, 31), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 31),
    DateUtils.getUTCDate(2010, 8, 31), DateUtils.getUTCDate(2010, 9, 30), DateUtils.getUTCDate(2010, 10, 31), DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31),
    DateUtils.getUTCDate(2011, 1, 31), DateUtils.getUTCDate(2011, 2, 28), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31),
    DateUtils.getUTCDate(2011, 6, 30), DateUtils.getUTCDate(2011, 7, 31), DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2011, 10, 31),
    DateUtils.getUTCDate(2011, 11, 30), DateUtils.getUTCDate(2011, 12, 31), DateUtils.getUTCDate(2012, 1, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 3, 31),
    DateUtils.getUTCDate(2012, 4, 30), DateUtils.getUTCDate(2012, 5, 31), DateUtils.getUTCDate(2012, 6, 30), DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2012, 8, 31),
    DateUtils.getUTCDate(2012, 9, 30), DateUtils.getUTCDate(2012, 10, 31), DateUtils.getUTCDate(2012, 11, 30), DateUtils.getUTCDate(2012, 12, 31) };
  private static final ZonedDateTimeDoubleTimeSeries USCPI_TIME_SERIES = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(USCPI_DATE, USCPI_VALUE);


}
