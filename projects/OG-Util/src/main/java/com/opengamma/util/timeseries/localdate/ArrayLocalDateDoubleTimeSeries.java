/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.ToStringHelper;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ArrayLocalDateDoubleTimeSeries extends LocalDateDoubleTimeSeries.Integer {
  /** Empty instance */
  public static final ArrayLocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();
  private static final DateTimeConverter<LocalDate> s_converter = new LocalDateEpochDaysConverter();

  public ArrayLocalDateDoubleTimeSeries() {
    super(new LocalDateEpochDaysConverter(), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ArrayLocalDateDoubleTimeSeries(final LocalDate[] dates, final double[] values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDate[] dates, final double[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayLocalDateDoubleTimeSeries(final List<LocalDate> dates, final List<Double> values) {
    super(s_converter, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, s_converter.convertToInt(dates), values));
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final List<LocalDate> dates, final List<Double> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ArrayLocalDateDoubleTimeSeries(final DoubleTimeSeries<LocalDate> dts) {
    super(s_converter, s_converter.convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final LocalDateDoubleTimeSeries dts) {
    super(new LocalDateEpochDaysConverter(timeZone), new LocalDateEpochDaysConverter(timeZone).convertToInt(new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public ArrayLocalDateDoubleTimeSeries(final FastIntDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }
  
  public ArrayLocalDateDoubleTimeSeries(final DateTimeConverter<LocalDate> converter, final FastIntDoubleTimeSeries pidts) {
    super(converter, pidts);
  }

  public ArrayLocalDateDoubleTimeSeries(final TimeZone timeZone, final FastIntDoubleTimeSeries pidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pidts);
  }

  @Override
  public LocalDateDoubleTimeSeries newInstanceFast(final LocalDate[] dateTimes, final double[] values) {
    return new ArrayLocalDateDoubleTimeSeries(dateTimes, values);
  }

  @Override
  protected FastBackedDoubleTimeSeries<LocalDate> intersectionFirstValueFast(FastLongDoubleTimeSeries other) {
    //PLAT-1590
    int[] aTimes = getFastSeries().timesArrayFast();
    final double[] aValues = valuesArrayFast();
    int aCount = 0;
    final long[] bTimesLong = other.timesArrayFast();
    final int[] bTimes = new int[bTimesLong.length];
    
    DateTimeNumericEncoding aEncoding = getFastSeries().getEncoding();
    DateTimeNumericEncoding bEncoding = other.getEncoding();
    for (int i = 0; i < bTimesLong.length; i++) {
      bTimes[i] = bEncoding.convertToInt(bTimesLong[i], aEncoding);
    }
    
    
    int bCount = 0;
    final int[] resTimes = new int[Math.min(aTimes.length, bTimes.length)];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return new ArrayLocalDateDoubleTimeSeries(new FastArrayIntDoubleTimeSeries(getFastSeries().getEncoding(), trimmedTimes, trimmedValues));
  }

  @Override
  public String toString() {
    return ToStringHelper.toString(this);
  }

}
