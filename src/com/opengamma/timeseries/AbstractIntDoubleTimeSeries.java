/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.Primitives;
/**
 * @author jim
 * 
 */
public abstract class AbstractIntDoubleTimeSeries<DATE_TYPE> extends AbstractFastBackedDoubleTimeSeries<DATE_TYPE> {

  final DateTimeConverter<DATE_TYPE> _converter;
  private final FastIntDoubleTimeSeries _timeSeries;

  public AbstractIntDoubleTimeSeries(final DateTimeConverter<DATE_TYPE> converter, final FastIntDoubleTimeSeries timeSeries) {
    _converter = converter;
    _timeSeries = timeSeries;
  }

  public DateTimeConverter<DATE_TYPE> getConverter() {
    return _converter;
  }

  public FastIntDoubleTimeSeries getFastSeries() {
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
  public Double getEarliestValue() {
    return getFastSeries().getEarliestValueFast();
  }

  @Override
  public DATE_TYPE getLatestTime() {
    return _converter.convertFromInt(getFastSeries().getLatestTimeFast());
  }

  @Override
  public Double getLatestValue() {
    return getFastSeries().getLatestValueFast();
  }

  @Override
  public DATE_TYPE getTime(final int index) {
    return _converter.convertFromInt(getFastSeries().getTimeFast(index));
  }

  @Override
  public Double getValue(final DATE_TYPE dateTime) {
    return getFastSeries().getValueFast(_converter.convertToInt(dateTime));
  }

  @Override
  public Double getValueAt(final int index) {
    return getFastSeries().getValueAtFast(index);
  }

  @Override
  public DoubleTimeSeries<DATE_TYPE> head(final int numItems) {
    return _converter.convertFromInt(this, getFastSeries().headFast(numItems));
  }

  @Override
  public boolean isEmpty() {
    return getFastSeries().isEmpty();
  }

  protected class IteratorAdapter implements Iterator<Entry<DATE_TYPE, Double>> {

    private final Iterator<Entry<Integer, Double>> _iterator;

    public IteratorAdapter(final Iterator<Entry<Integer, Double>> iterator) {
      _iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return _iterator.hasNext();
    }

    @Override
    public Entry<DATE_TYPE, Double> next() {
      final Entry<Integer, Double> next = _iterator.next();
      return _converter.makePair(_converter.convertFromInt(next.getKey()), next.getValue());
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
    return _converter.convertFromInt(this, getFastSeries().subSeriesFast(_converter.convertToInt(startTime), _converter.convertToInt(endTime)));
  }
  
  @Override
  public TimeSeries<DATE_TYPE, Double> subSeries(final DATE_TYPE startTime, final boolean includeStart, final DATE_TYPE endTime, final boolean includeEnd) { 
    return _converter.convertFromInt(this, getFastSeries().subSeriesFast(_converter.convertToInt(startTime), includeStart, _converter.convertToInt(endTime), includeEnd));
  }

  @Override
  public TimeSeries<DATE_TYPE, Double> tail(final int numItems) {
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
  public Iterator<Double> valuesIterator() {
    return getFastSeries().valuesIterator();
  }

  @Override
  public List<DATE_TYPE> times() {
    return _converter.convertFromInt(getFastSeries().timesFast());
  }

  @Override
  public List<Double> values() {
    return getFastSeries().valuesFast();
  }

  @Override
  public DATE_TYPE[] timesArray() {
    return _converter.convertFromInt(getFastSeries().timesArrayFast());
  }

  @Override
  public Double[] valuesArray() {
    return Primitives.box(getFastSeries().valuesArrayFast());
  }
  
  @SuppressWarnings("unchecked")
  public FastBackedDoubleTimeSeries<DATE_TYPE> operate(final UnaryOperator operator) {
    FastTimeSeries<Integer> fastResult = getFastSeries().operate(operator);
    return (FastBackedDoubleTimeSeries<DATE_TYPE>) getConverter().convertFromInt(this, (FastIntDoubleTimeSeries) fastResult);
  }
  
  @SuppressWarnings("unchecked")
  public FastBackedDoubleTimeSeries<DATE_TYPE> operate(final double other, final BinaryOperator operator) {
    FastTimeSeries<Integer> fastResult = getFastSeries().operate(other, operator);
    return (FastBackedDoubleTimeSeries<DATE_TYPE>) getConverter().convertFromInt(this, (FastIntDoubleTimeSeries) fastResult);
  }

  public FastBackedDoubleTimeSeries<DATE_TYPE> operate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    return operate(fastSeries, operator);
  }
  
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(final FastBackedDoubleTimeSeries<?> other, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeries = other.getFastSeries();
    return unionOperate(fastSeries, operator);
  }
    
  @SuppressWarnings("unchecked")
  public FastBackedDoubleTimeSeries<DATE_TYPE> operate(final FastTimeSeries<?> other, final BinaryOperator operator) {  
    FastIntDoubleTimeSeries intDoubleTimeSeries;
    if (other instanceof FastIntDoubleTimeSeries) {
      intDoubleTimeSeries = getFastSeries().operate((FastIntDoubleTimeSeries)other, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      intDoubleTimeSeries = getFastSeries().operate((FastLongDoubleTimeSeries)other, operator);
    }
    return (FastBackedDoubleTimeSeries<DATE_TYPE>) getConverter().convertFromInt(this, intDoubleTimeSeries);
  }
  
  @SuppressWarnings("unchecked")
  public FastBackedDoubleTimeSeries<DATE_TYPE> unionOperate(final FastTimeSeries<?> other, final BinaryOperator operator) {  
    FastIntDoubleTimeSeries intDoubleTimeSeries;
    if (other instanceof FastIntDoubleTimeSeries) {
      intDoubleTimeSeries = getFastSeries().unionOperate((FastIntDoubleTimeSeries)other, operator);
    } else { // if (fastSeries instanceof FastLongDoubleTimeSeries
      intDoubleTimeSeries = getFastSeries().unionOperate((FastLongDoubleTimeSeries)other, operator);
    }
    return (FastBackedDoubleTimeSeries<DATE_TYPE>) getConverter().convertFromInt(this, intDoubleTimeSeries);
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
