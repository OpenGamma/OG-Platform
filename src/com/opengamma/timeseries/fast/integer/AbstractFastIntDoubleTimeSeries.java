/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.DateTimeResolution;
import com.opengamma.util.Primitives;

/**
 * @author jim
 *         Contains methods to make Primitive time series work with the normal
 *         non-primitive time series interface (where possible)
 */
public abstract class AbstractFastIntDoubleTimeSeries implements FastIntDoubleTimeSeries {

  private final DateTimeNumericEncoding _encoding;

  protected AbstractFastIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    _encoding = encoding;
  }

  @Override
  public Integer getEarliestTime() {
    return getEarliestTimeFast();
  }

  @Override
  public Double getEarliestValue() {
    return getEarliestValueFast();
  }

  @Override
  public Integer getLatestTime() {
    return getLatestTimeFast();
  }

  @Override
  public Double getLatestValue() {
    return getLatestValueFast();
  }

  @Override
  public Integer getTime(final int index) {
    return getTimeFast(index);
  }

  @Override
  public Double getValue(final Integer dateTime) {
    return getValueFast(dateTime);
  }

  @Override
  public Double getValueAt(final int index) {
    return getValueAtFast(index);
  }

  @Override
  public TimeSeries<Integer, Double> subSeries(final Integer startTime, final Integer endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public Iterator<Integer> timeIterator() {
    return timesIteratorFast();
  }

  @Override
  public List<Integer> times() {
    return timesFast();
  }

  @Override
  public Integer[] timesArray() {
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

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Map.Entry<Integer, Double>> iterator() {
    return (Iterator<Map.Entry<Integer, Double>>) (Iterator<? extends Map.Entry<Integer, Double>>) iteratorFast();
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
  public TimeSeries<Integer, Double> newInstance(final Integer[] times, final Double[] values) {
    return newInstanceFast(Primitives.unbox(times), Primitives.unbox(values));
  }

}
