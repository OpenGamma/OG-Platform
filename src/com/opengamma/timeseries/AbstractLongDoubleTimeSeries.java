/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.opengamma.timeseries.DoubleTimeSeriesOperations.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperations.UnaryOperator;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public abstract class AbstractLongDoubleTimeSeries<DATE_TYPE> implements FastBackedDoubleTimeSeries<DATE_TYPE> {

  final DateTimeConverter<DATE_TYPE> _converter;
  private final FastLongDoubleTimeSeries _timeSeries;

  public AbstractLongDoubleTimeSeries(final DateTimeConverter<DATE_TYPE> converter, final FastLongDoubleTimeSeries timeSeries) {
    _converter = converter;
    _timeSeries = timeSeries;
  }

  public DateTimeConverter<DATE_TYPE> getConverter() {
    return _converter;
  }

  public FastLongDoubleTimeSeries getFastSeries() {
    return _timeSeries;
  }

  //
  // protected abstract MutableDoubleTimeSeries<DATE_TYPE>
  // makeMutableListTimeSeries();
  //
  // protected abstract FastMutableLongDoubleTimeSeries
  // makePrimitiveMutableListTimeSeries();

  @Override
  public DATE_TYPE getEarliestTime() {
    return _converter.convertFromLong(getFastSeries().getEarliestTimeFast());
  }

  @Override
  public Double getEarliestValue() {
    return getFastSeries().getEarliestValueFast();
  }

  @Override
  public DATE_TYPE getLatestTime() {
    return _converter.convertFromLong(getFastSeries().getLatestTimeFast());
  }

  @Override
  public Double getLatestValue() {
    return getFastSeries().getLatestValueFast();
  }

  @Override
  public DATE_TYPE getTime(final int index) {
    return _converter.convertFromLong(getFastSeries().getTimeFast(index));
  }

  @Override
  public Double getValue(final DATE_TYPE dateTime) {
    return getFastSeries().getValueFast(_converter.convertToLong(dateTime));
  }

  @Override
  public Double getValueAt(final int index) {
    return getFastSeries().getValueAtFast(index);
  }

  @Override
  public DoubleTimeSeries<DATE_TYPE> head(final int numItems) {
    return _converter.convertFromLong(this, getFastSeries().headFast(numItems));
  }

  @Override
  public boolean isEmpty() {
    return getFastSeries().isEmpty();
  }

  protected class IteratorAdapter implements Iterator<Entry<DATE_TYPE, Double>> {

    private final Iterator<Entry<Long, Double>> _iterator;

    public IteratorAdapter(final Iterator<Entry<Long, Double>> iterator) {
      _iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return _iterator.hasNext();
    }

    @Override
    public Entry<DATE_TYPE, Double> next() {
      final Entry<Long, Double> next = _iterator.next();
      return _converter.makePair(_converter.convertFromLong(next.getKey()), next.getValue());
    }

    @Override
    public void remove() {
      _iterator.next();
    }

  }

  @Override
  public Iterator<Entry<DATE_TYPE, Double>> iterator() {
    return new IteratorAdapter(getFastSeries().iterator());
  }

  @Override
  public int size() {
    return getFastSeries().size();
  }

  @Override
  public TimeSeries<DATE_TYPE, Double> subSeries(final DATE_TYPE startTime, final DATE_TYPE endTime) {
    return _converter.convertFromLong(this, getFastSeries().subSeriesFast(_converter.convertToLong(startTime), _converter.convertToLong(endTime)));
  }

  @Override
  public TimeSeries<DATE_TYPE, Double> tail(final int numItems) {
    return _converter.convertFromLong(this, getFastSeries().tailFast(numItems));
  }

  class TimeIteratorAdapter implements Iterator<DATE_TYPE> {
    private final Iterator<Long> _iterator;

    public TimeIteratorAdapter(final Iterator<Long> iterator) {
      _iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return _iterator.hasNext();
    }

    @Override
    public DATE_TYPE next() {
      return _converter.convertFromLong(_iterator.next());
    }

    @Override
    public void remove() {
      _iterator.remove();
    }
  }

  @Override
  public Iterator<DATE_TYPE> timeIterator() {
    return new TimeIteratorAdapter(getFastSeries().timeIterator());
  }

  @Override
  public Iterator<Double> valuesIterator() {
    return getFastSeries().valuesIterator();
  }

  @Override
  public List<DATE_TYPE> times() {
    return _converter.convertFromLong(getFastSeries().timesFast());
  }

  @Override
  public List<Double> values() {
    return getFastSeries().values();
  }

  @Override
  public DATE_TYPE[] timesArray() {
    return _converter.convertFromLong(getFastSeries().timesArrayFast());
  }

  @Override
  public Double[] valuesArray() {
    return getFastSeries().valuesArray();
  }
  
  public TimeSeries<DATE_TYPE, Double> operate(final UnaryOperator operator) {
    FastTimeSeries<Long> fastResult = getFastSeries().operate(operator);
    return getConverter().convertFromLong(this, (FastLongDoubleTimeSeries) fastResult);
  }
  
  public TimeSeries<DATE_TYPE, Double> operate(final double other, final BinaryOperator operator) {
    FastTimeSeries<Long> fastResult = getFastSeries().operate(other, operator);
    return getConverter().convertFromLong(this, (FastLongDoubleTimeSeries) fastResult);
  }
  
  public TimeSeries<DATE_TYPE, Double> operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    FastLongDoubleTimeSeries longDoubleTimeSeries;
    if (fastSeries instanceof FastIntDoubleTimeSeries) {
      longDoubleTimeSeries = getFastSeries().operate((FastIntDoubleTimeSeries)fastSeries, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      longDoubleTimeSeries = getFastSeries().operate((FastLongDoubleTimeSeries)fastSeries, operator);
    }
    return getConverter().convertFromLong(this, longDoubleTimeSeries);
  }

  @Override
  public boolean equals(final Object obj) {
    return getFastSeries().equals(obj);
  }

  @Override
  public int hashCode() {
    return getFastSeries().hashCode();
  }
}
