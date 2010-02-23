/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.longint;

import java.util.Iterator;
import java.util.List;

import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.DateTimeResolution;
import com.opengamma.util.Primitives;

/**
 * @author jim
 *         Contains methods to make Primitive time series work with the normal
 *         non-primitive time series interface (where possible)
 */
public abstract class AbstractFastLongDoubleTimeSeries implements FastLongDoubleTimeSeries {

  private final DateTimeNumericEncoding _encoding;

  protected AbstractFastLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    _encoding = encoding;
  }

  @Override
  public Long getEarliestTime() {
    return getEarliestTimeFast();
  }

  @Override
  public Double getEarliestValue() {
    return getEarliestValueFast();
  }

  @Override
  public Long getLatestTime() {
    return getLatestTimeFast();
  }

  @Override
  public Double getLatestValue() {
    return getLatestValueFast();
  }

  @Override
  public Long getTime(final int index) {
    return getTimeFast(index);
  }

  @Override
  public Double getValue(final Long dateTime) {
    return getValueFast(dateTime);
  }

  @Override
  public Double getValueAt(final int index) {
    return getValueAtFast(index);
  }

  @Override
  public TimeSeries<Long, Double> subSeries(final Long startTime, final Long endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public Iterator<Long> timeIterator() {
    return timesIteratorFast();
  }

  @Override
  public List<Long> times() {
    return timesFast();
  }

  @Override
  public Long[] timesArray() {
    return Primitives.box(timesArrayFast());
  }

  @Override
  public List<Double> values() {
    return valuesFast();
  }

  @Override
  public Double[] valuesArray() {
    return Primitives.box(valuesArrayFast());
  }

  @Override
  public Iterator<Double> valuesIterator() {
    return valuesIteratorFast();
  }

  @Override
  public DateTimeResolution getDateTimeResolution() {
    return getEncoding().getResolution();
  }

  @Override
  public DateTimeNumericEncoding getEncoding() {
    return _encoding;
  }

  @Override
  public TimeSeries<Long, Double> newInstance(final Long[] times, final Double[] values) {
    return newInstanceFast(Primitives.unbox(times), Primitives.unbox(values));
  }

}
