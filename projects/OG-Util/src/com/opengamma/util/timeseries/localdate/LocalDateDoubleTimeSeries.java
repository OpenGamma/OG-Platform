/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.PublicAPI;
import com.opengamma.util.timeseries.AbstractIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * Abstraction of a time series that stores {@code double} data values against {@link LocalDate} dates. 
 */
@PublicAPI
public interface LocalDateDoubleTimeSeries extends DoubleTimeSeries<LocalDate>, FastBackedDoubleTimeSeries<LocalDate> {

  @Override
  LocalDateDoubleTimeSeries subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd);

  @Override
  LocalDateDoubleTimeSeries subSeries(LocalDate startTime, LocalDate endTime);

  @Override
  LocalDateDoubleTimeSeries head(int numItems);

  @Override
  LocalDateDoubleTimeSeries tail(int numItems);

  @Override
  LocalDateDoubleTimeSeries lag(final int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Partial implementation of the {@link LocalDateDoubleTimeSeries} that uses an {@code integer} representation of the date.
   */
  public abstract static class Integer extends AbstractIntDoubleTimeSeries<LocalDate> implements LocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Integer(final DateTimeConverter<LocalDate> converter, final FastIntDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd) {
      return (LocalDateDoubleTimeSeries) super.subSeries(startTime, includeStart, endTime, includeEnd);
    }

    @Override
    public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, LocalDate endTime) {
      return (LocalDateDoubleTimeSeries) super.subSeries(startTime, endTime);
    }

    @Override
    public LocalDateDoubleTimeSeries head(int numItems) {
      return (LocalDateDoubleTimeSeries) super.head(numItems);
    }

    @Override
    public LocalDateDoubleTimeSeries tail(int numItems) {
      return (LocalDateDoubleTimeSeries) super.tail(numItems);
    }

    @Override
    public LocalDateDoubleTimeSeries lag(final int lagCount) {
      return (LocalDateDoubleTimeSeries) super.lag(lagCount);
    }

    @Override
    public LocalDateDoubleTimeSeries newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract LocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
    
    @Override
    public LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries() {
      return this;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Partial implementation of the {@link LocalDateDoubleTimeSeries} that uses a {@code long} representation of the date.
   */
  public abstract static class Long extends AbstractLongDoubleTimeSeries<LocalDate> implements LocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Long(final DateTimeConverter<LocalDate> converter, final FastLongDoubleTimeSeries timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd) {
      return (LocalDateDoubleTimeSeries) super.subSeries(startTime, includeStart, endTime, includeEnd);
    }

    @Override
    public LocalDateDoubleTimeSeries subSeries(LocalDate startTime, LocalDate endTime) {
      return (LocalDateDoubleTimeSeries) super.subSeries(startTime, endTime);
    }

    @Override
    public LocalDateDoubleTimeSeries head(int numItems) {
      return (LocalDateDoubleTimeSeries) super.head(numItems);
    }

    @Override
    public LocalDateDoubleTimeSeries tail(int numItems) {
      return (LocalDateDoubleTimeSeries) super.tail(numItems);
    }

    @Override
    public LocalDateDoubleTimeSeries lag(final int lagCount) {
      return (LocalDateDoubleTimeSeries) super.lag(lagCount);
    }

    @Override
    public LocalDateDoubleTimeSeries newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract LocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
  }

}
