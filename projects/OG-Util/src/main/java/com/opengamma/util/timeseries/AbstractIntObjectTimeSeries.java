/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import static com.opengamma.util.tuple.TuplesUtil.pairToEntry;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.FastObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
/**
 * @param <DATE_TYPE> the type of object used to hold Dates/DateTimes in the wrpper
 * @param <T> the type of the objects being stored in the time series
 */
public abstract class AbstractIntObjectTimeSeries<DATE_TYPE, T> extends AbstractFastBackedObjectTimeSeries<DATE_TYPE, T> {

  private final DateTimeConverter<DATE_TYPE> _converter;
  private final FastIntObjectTimeSeries<T> _timeSeries;

  public AbstractIntObjectTimeSeries(final DateTimeConverter<DATE_TYPE> converter, final FastIntObjectTimeSeries<T> timeSeries) {
    _converter = converter;
    _timeSeries = timeSeries;
  }

  public DateTimeConverter<DATE_TYPE> getConverter() {
    return _converter;
  }

  public FastIntObjectTimeSeries<T> getFastSeries() {
    return _timeSeries;
  }

  //
  // protected abstract MutableDoubleTimeSeries<DATE_TYPE>
  // makeMutableListTimeSeries();
  //
  // protected abstract FastMutableIntDoubleTimeSeries
  // makePrimitiveMutableListTimeSeries();

  @Override
  public DATE_TYPE getEarliestTime() {
    return _converter.convertFromInt(getFastSeries().getEarliestTimeFast());
  }

  @Override
  public T getEarliestValue() {
    return getFastSeries().getEarliestValueFast();
  }

  @Override
  public DATE_TYPE getLatestTime() {
    return _converter.convertFromInt(getFastSeries().getLatestTimeFast());
  }

  @Override
  public T getLatestValue() {
    return getFastSeries().getLatestValueFast();
  }

  @Override
  public DATE_TYPE getTimeAt(final int index) {
    return _converter.convertFromInt(getFastSeries().getTimeFast(index));
  }

  @Override
  public T getValue(final DATE_TYPE dateTime) {
    try {
      return getFastSeries().getValueFast(_converter.convertToInt(dateTime));
    } catch (NoSuchElementException nsee) {
      return null;
    }
  }

  @Override
  public T getValueAt(final int index) {
    return getFastSeries().getValueAtFast(index);
  }

  @Override
  public ObjectTimeSeries<DATE_TYPE, T> head(final int numItems) {
    return _converter.convertFromInt(this, getFastSeries().headFast(numItems));
  }

  @Override
  public boolean isEmpty() {
    return getFastSeries().isEmpty();
  }
  
  /**
   * Converts from the underlying iterator into the wrapped type.
   */
  protected class IteratorAdapter implements Iterator<Entry<DATE_TYPE, T>> {

    private final Iterator<Entry<Integer, T>> _iterator;

    public IteratorAdapter(final Iterator<Entry<Integer, T>> iterator) {
      _iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return _iterator.hasNext();
    }

    @Override
    public Entry<DATE_TYPE, T> next() {
      final Entry<Integer, T> next = _iterator.next();
      return pairToEntry(_converter.<T>makePair(_converter.convertFromInt(next.getKey()), next.getValue()));
    }

    @Override
    public void remove() {
      _iterator.next();
    }

  }

  @Override
  public Iterator<Entry<DATE_TYPE, T>> iterator() {
    return new IteratorAdapter(getFastSeries().iterator());
  }

  @Override
  public int size() {
    return getFastSeries().size();
  }

  @Override
  public ObjectTimeSeries<DATE_TYPE, T> subSeries(final DATE_TYPE startTime, final DATE_TYPE endTime) {
    return _converter.convertFromInt(this, getFastSeries().subSeriesFast(_converter.convertToInt(startTime), _converter.convertToInt(endTime)));
  }
  
  @Override
  public ObjectTimeSeries<DATE_TYPE, T> subSeries(final DATE_TYPE startTime, final boolean includeStart, final DATE_TYPE endTime, final boolean includeEnd) { 
    return _converter.convertFromInt(this, getFastSeries().subSeriesFast(_converter.convertToInt(startTime), includeStart, _converter.convertToInt(endTime), includeEnd));
  }

  @Override
  public ObjectTimeSeries<DATE_TYPE, T> tail(final int numItems) {
    return _converter.convertFromInt(this, getFastSeries().tailFast(numItems));
  }

  class TimeIteratorAdapter implements Iterator<DATE_TYPE> {
    private final Iterator<Integer> _iterator;

    public TimeIteratorAdapter(final Iterator<Integer> iterator) {
      _iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return _iterator.hasNext();
    }

    @Override
    public DATE_TYPE next() {
      return _converter.convertFromInt(_iterator.next());
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
  public Iterator<T> valuesIterator() {
    return getFastSeries().valuesIterator();
  }

  @Override
  public List<DATE_TYPE> times() {
    return _converter.convertFromInt(getFastSeries().timesFast());
  }

  @Override
  public List<T> values() {
    return getFastSeries().valuesFast();
  }

  @Override
  public DATE_TYPE[] timesArray() {
    return _converter.convertFromInt(getFastSeries().timesArrayFast());
  }

  @Override
  public T[] valuesArray() {
    return getFastSeries().valuesArrayFast();
  }
  
  public FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final UnaryOperator<T> operator) {
    FastObjectTimeSeries<Integer, T> fastResult = getFastSeries().operate(operator);
    return (FastBackedObjectTimeSeries<DATE_TYPE, T>) getConverter().convertFromInt(this, (FastIntObjectTimeSeries<T>) fastResult);
  }
  
  public FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final T other, final BinaryOperator<T> operator) {
    FastObjectTimeSeries<Integer, T> fastResult = getFastSeries().operate(other, operator);
    return (FastBackedObjectTimeSeries<DATE_TYPE, T>) getConverter().convertFromInt(this, (FastIntObjectTimeSeries<T>) fastResult);
  }

  public FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator) {
    FastObjectTimeSeries<?, T> fastSeries = other.getFastSeries();
    return operate(fastSeries, operator);
  }
  
  public FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator) {
    FastObjectTimeSeries<?, T> fastSeries = other.getFastSeries();
    return unionOperate(fastSeries, operator);
  }
    
  @SuppressWarnings("unchecked")
  public FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final FastObjectTimeSeries<?, T> other, final BinaryOperator<T> operator) {  
    FastIntObjectTimeSeries<T> intDoubleTimeSeries;
    if (other instanceof FastIntObjectTimeSeries<?>) {
      intDoubleTimeSeries = getFastSeries().operate((FastIntObjectTimeSeries<T>) other, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      intDoubleTimeSeries = getFastSeries().operate((FastLongObjectTimeSeries<T>) other, operator);
    }
    return (FastBackedObjectTimeSeries<DATE_TYPE, T>) getConverter().convertFromInt(this, intDoubleTimeSeries);
  }
  
  @SuppressWarnings("unchecked")
  public FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(final FastObjectTimeSeries<?, T> other, final BinaryOperator<T> operator) {  
    FastIntObjectTimeSeries<T> intDoubleTimeSeries;
    if (other instanceof FastIntObjectTimeSeries<?>) {
      intDoubleTimeSeries = getFastSeries().unionOperate((FastIntObjectTimeSeries<T>) other, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      intDoubleTimeSeries = getFastSeries().unionOperate((FastLongObjectTimeSeries<T>) other, operator);
    }
    return (FastBackedObjectTimeSeries<DATE_TYPE, T>) getConverter().convertFromInt(this, intDoubleTimeSeries);
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof FastBackedObjectTimeSeries<?, ?>) {
      FastBackedObjectTimeSeries<?, ?> fbots = (FastBackedObjectTimeSeries<?, ?>) obj; 
      return getFastSeries().equals(fbots.getFastSeries());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getFastSeries().hashCode();
  }
}
