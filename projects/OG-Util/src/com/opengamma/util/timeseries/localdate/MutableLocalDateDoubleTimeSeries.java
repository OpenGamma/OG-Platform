/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.AbstractMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.MutableDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * 
 */
public interface MutableLocalDateDoubleTimeSeries extends LocalDateDoubleTimeSeries, MutableDoubleTimeSeries<LocalDate> {

  /** */
  public abstract static class Integer extends AbstractMutableIntDoubleTimeSeries<LocalDate> implements MutableLocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Integer(final DateTimeConverter<LocalDate> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
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
    public LocalDateDoubleTimeSeries operate(final UnaryOperator operator) {
      return (LocalDateDoubleTimeSeries) super.operate(operator);
    }

    @Override
    public MutableLocalDateDoubleTimeSeries newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract MutableLocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
  }

  /** */
  public abstract static class Long extends AbstractMutableLongDoubleTimeSeries<LocalDate> implements MutableLocalDateDoubleTimeSeries {
    private static final long serialVersionUID = 1L;

    public Long(final DateTimeConverter<LocalDate> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
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
    public LocalDateDoubleTimeSeries operate(final UnaryOperator operator) {
      return (LocalDateDoubleTimeSeries) super.operate(operator);
    }

    @Override
    public MutableLocalDateDoubleTimeSeries newInstance(final LocalDate[] dateTimes, final Double[] values) {
      return newInstanceFast(dateTimes, ArrayUtils.toPrimitive(values));
    }

    public abstract MutableLocalDateDoubleTimeSeries newInstanceFast(LocalDate[] dateTimes, double[] values);
  }

}
