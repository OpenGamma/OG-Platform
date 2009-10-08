package com.opengamma.timeseries;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.util.CompareUtils;
import com.opengamma.util.KeyValuePair;

public class ArrayDoubleTimeSeries extends DoubleTimeSeries {
  public static final DoubleTimeSeries EMPTY_SERIES = new ArrayDoubleTimeSeries();

  protected final long[] _times;
  protected final double[] _values;

  private ArrayDoubleTimeSeries() {
    _times = new long[0];
    _values = new double[0];
  }

  public ArrayDoubleTimeSeries(long[] times, double[] values) {
    _times = new long[times.length];
    System.arraycopy(times, 0, _times, 0, times.length);
    _values = new double[values.length];
    System.arraycopy(values, 0, _values, 0, values.length);
    // check dates are ordered
    long maxTime = 0L;
    for (long time : _times) {
      if (time < maxTime) {
        throw new IllegalArgumentException("dates must be ordered");
      }
      maxTime = time;
    }
  }

  public ArrayDoubleTimeSeries(List<InstantProvider> times, List<Double> values) {
    if (times.size() != values.size()) {
      throw new IllegalArgumentException("lists are of different sizes");
    }
    _times = new long[times.size()];
    _values = new double[values.size()];
    Iterator<Double> iter = values.iterator();
    int i = 0;
    long maxTime = 0L; // for checking the dates are sorted.
    for (InstantProvider time : times) {
      Double value = iter.next();
      long epochMillis = time.toInstant().toEpochMillis();
      if (maxTime < epochMillis) {
        _times[i] = epochMillis;
        _values[i] = value;
        maxTime = epochMillis;
      } else {
        throw new IllegalArgumentException("dates must be ordered");
      }
      i++;
    }
  }

  public ArrayDoubleTimeSeries(DoubleTimeSeries dts) {
    final int size = dts.size();
    _times = new long[size];
    _values = new double[size];
    if (dts instanceof ArrayDoubleTimeSeries) { // interesting to know if this
      // is worth it
      ArrayDoubleTimeSeries adts = (ArrayDoubleTimeSeries) dts;
      System.arraycopy(adts._times, 0, _times, 0, size);
      System.arraycopy(adts._values, 0, _values, 0, size);
    } else {
      int pos = 0;
      for (Map.Entry<InstantProvider, Double> entry : dts) {
        _times[pos] = entry.getKey().toInstant().toEpochMillis();
        _values[pos] = entry.getValue();
        pos++;
      }
    }
  }

  public ArrayDoubleTimeSeries(SortedMap<InstantProvider, Double> initialMap) {
    final int size = initialMap.size();
    _times = new long[size];
    _values = new double[size];
    Iterator<Entry<InstantProvider, Double>> iterator = initialMap.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      Entry<InstantProvider, Double> entry = iterator.next();
      _times[i] = entry.getKey().toInstant().toEpochMillis();
      _values[i] = entry.getValue().doubleValue();
      i++;
    }
  }

  @Override
  public DoubleTimeSeries subSeries(InstantProvider startTime, InstantProvider endTime) {
    if (isEmpty()) {
      return EMPTY_SERIES;
      // throw new NoSuchElementException("Series is empty");
    }
    final long startMillis = startTime.toInstant().toEpochMillis();
    final long endMillis = endTime.toInstant().toEpochMillis();
    int startPos = Arrays.binarySearch(_times, startMillis);
    int endPos = Arrays.binarySearch(_times, endMillis);
    startPos = (startPos >= 0) ? startPos : (-startPos) - 1;
    endPos = (endPos >= 0) ? endPos : (-endPos) - 1; // if either is -1, make
    // it zero
    /*
     * Commented out because Maps turn out to let you use nearest element. if
     * (_times[startPos] == startMillis && _times[endPos] == endMillis) {
     */
    // endPos is odd, because it may or may not be inclusive
    if (startPos == endPos) { // the result series is empty
      // TODO: change to Empty time series when we've defined it.
      return new ArrayDoubleTimeSeries(new long[0], new double[0]);
    }
    // final long endEpochMillis = endTime.toInstant().toEpochMillis();
    if (endPos >= _times.length) {
      endPos--;
    }
    // if (_times[endPos] > endEpochMillis) { // should implicitly be >0
    // beacuse of startPos != endPos
    // endPos--;
    // }
    final int length = (endPos - startPos) + 1;
    long[] resultTimes = new long[length];
    double[] resultValues = new double[length];
    System.arraycopy(_times, startPos, resultTimes, 0, length);
    System.arraycopy(_values, startPos, resultValues, 0, length);
    return new ArrayDoubleTimeSeries(resultTimes, resultValues);
    /*
     * Also commented out because Maps are more tolerant than I initially
     * thought. } else { if (_times[startPos] != startMillis && _times[endPos]
     * != endMillis) { throw new
     * NoSuchElementException("Neither start time or end time are in the series"
     * ); } else if (_times[startPos] != startMillis) { throw new
     * NoSuchElementException("Start time is not in the series"); } else { //
     * by process of elimination, it must be only the end time not in the
     * series throw new
     * NoSuchElementException("End time is not in the series"); } }
     */
  }

  @Override
  public Double getDataPoint(InstantProvider instant) {
    return _values[Arrays.binarySearch(_times, instant.toInstant().toEpochMillis())];
  }

  @Override
  public InstantProvider getEarliestTime() {
    if (_times.length > 0) {
      return Instant.millisInstant(_times[0]);
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public Double getEarliestValue() {
    if (_values.length > 0) {
      return _values[0];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public InstantProvider getLatestTime() {
    if (_times.length > 0) {
      return Instant.millisInstant(_times[_times.length - 1]);
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public Double getLatestValue() {
    if (_values.length > 0) {
      return _values[_values.length - 1];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  /*package*/ class ArrayDoubleTimeSeriesIterator implements Iterator<Entry<InstantProvider, Double>> {
    private int _current = 0;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public Entry<InstantProvider, Double> next() {
      if (hasNext()) {
        KeyValuePair<InstantProvider, Double> keyValuePair = new KeyValuePair<InstantProvider, Double>(Instant.millisInstant(_times[_current]), _values[_current]);
        _current++;
        return keyValuePair;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<Entry<InstantProvider, Double>> iterator() {
    return new ArrayDoubleTimeSeriesIterator();
  }

  @Override
  public int size() {
    return _times.length;
  }

  @Override
  public boolean isEmpty() {
    return _times.length == 0;
  }

  /*package*/ class ArrayDoubleTimeSeriesTimeIterator implements Iterator<InstantProvider> {
    private int _current = 0;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public InstantProvider next() {
      if (hasNext()) {
        InstantProvider instant = Instant.millisInstant(_times[_current]);
        _current++;
        return instant;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<InstantProvider> timeIterator() {
    return new ArrayDoubleTimeSeriesTimeIterator();
  }

  /*package*/ class ArrayDoubleTimeSeriesValuesIterator implements Iterator<Double> {
    private int _current = 0;

    @Override
    public boolean hasNext() {
      return _current < _values.length;
    }

    @Override
    public Double next() {
      if (hasNext()) {
        Double value = _values[_current];
        _current++;
        return value;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<Double> valuesIterator() {
    return new ArrayDoubleTimeSeriesValuesIterator();
  }

  @Override
  public Double getValue(int index) {
    return _values[index];
  }
  
  @Override
  public InstantProvider getTime(int index) {
    return Instant.instant(_times[index]);
  }

  @Override
  public DoubleTimeSeries tail(int numItems) {
    if (numItems <= _times.length) {
      long[] times = new long[numItems];
      double[] values = new double[numItems];
      System.arraycopy(_times, _times.length - numItems, times, 0, numItems);
      System.arraycopy(_values, _values.length - numItems, values, 0, numItems);
      return new ArrayDoubleTimeSeries(times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @Override
  public DoubleTimeSeries head(int numItems) {
    if (numItems <= _times.length) {
      long[] times = new long[numItems];
      double[] values = new double[numItems];
      System.arraycopy(_times, 0, times, 0, numItems);
      System.arraycopy(_values, 0, values, 0, numItems);
      return new ArrayDoubleTimeSeries(times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    // the aim here is to always minimize the number of comparisons.
    if (!(o instanceof DoubleTimeSeries)) {
      return false;
    }
    if (!(o instanceof ArrayDoubleTimeSeries)) {
      ArrayDoubleTimeSeries otherADTS = (ArrayDoubleTimeSeries) o;
      if (otherADTS._times.length != _times.length) {
        return false;
      }
      if (!Arrays.equals(otherADTS._times, _times)) {
        return false;
      }
      return Arrays.equals(otherADTS._values, _values);
    } else {
      DoubleTimeSeries dts = (DoubleTimeSeries) o;
      if (dts.size() != size()) {
        return false;
      }
      if (isEmpty()) {
        return true;
      }
      Iterator<Entry<InstantProvider, Double>> otherIter = dts.iterator();
      // would be neater if we just had two iterators, but it will use
      // quite a bit more memory creating objects for no point. Of course
      // this probably is the _definition_ of premature optimization.
      int i = 0;
      while (otherIter.hasNext()) {
        Entry<InstantProvider, Double> entry = otherIter.next();
        if (entry.getKey().toInstant().toEpochMillis() != _times[i]) {
          return false;
        }
        if (!CompareUtils.closeEquals(entry.getValue(), _values[i])) {
          return false;
        }
        i++;
      }
      return true;
    }

  }
  
  public List<InstantProvider> times() {
    InstantProvider[] times = new InstantProvider[_times.length];
    for (int i=0; i<_times.length; i++) {
      times[i] = Instant.instant(_times[i]);
    }
    return Arrays.asList(times);
  }
  
  public List<Double> values() {
    Double[] copy = new Double[_values.length];
    System.arraycopy(_values, 0, copy, 0, _values.length);
    return Arrays.asList(copy);
  }

  @Override
  public int hashCode() {
    int value = 0;
    for (int i = 0; i < ((_times.length > 0) ? 1 : 0); i++) {
      final long bits = Double.doubleToLongBits(_values[i]);
      value += _times[i] ^ (bits ^ (bits >>> 32));
    }
    return value;
  }

  @Override
  public Double getValue(InstantProvider instant) {
    Instant time = instant.toInstant();
    long epochMillis = time.toEpochMillis();
    int binarySearch = Arrays.binarySearch(_times, epochMillis);
    if (_times[binarySearch] == epochMillis) {
      return _values[binarySearch];
    } else {
      throw new NoSuchElementException();
    }
  }

}