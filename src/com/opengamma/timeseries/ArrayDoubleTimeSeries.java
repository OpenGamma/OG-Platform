package com.opengamma.timeseries;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.CompareUtils;
import com.opengamma.util.KeyValuePair;

public class ArrayDoubleTimeSeries extends DoubleTimeSeries {
  public static final DoubleTimeSeries EMPTY_SERIES = new ArrayDoubleTimeSeries();

  protected final long[] _times;
  protected final double[] _values;
  protected final TimeZone[] _zones;

  private ArrayDoubleTimeSeries() {
    _times = new long[0];
    _values = new double[0];
    _zones = new TimeZone[0];
  }

  public ArrayDoubleTimeSeries(final long[] times, final double[] values, final TimeZone[] zones) {
    if (times.length != values.length || times.length != zones.length)
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length + ", " + zones.length);
    _times = new long[times.length];
    System.arraycopy(times, 0, _times, 0, times.length);
    _values = new double[values.length];
    System.arraycopy(values, 0, _values, 0, values.length);
    _zones = new TimeZone[zones.length];
    System.arraycopy(zones, 0, _zones, 0, zones.length);
    // check dates are ordered
    long maxTime = Long.MIN_VALUE;
    for (final long time : _times) {
      if (time < maxTime)
        throw new IllegalArgumentException("dates must be ordered");
      maxTime = time;
    }
  }

  public ArrayDoubleTimeSeries(final List<ZonedDateTime> times, final List<Double> values) {
    if (times.size() != values.size())
      throw new IllegalArgumentException("lists are of different sizes");
    _times = new long[times.size()];
    _values = new double[values.size()];
    _zones = new TimeZone[times.size()];
    final Iterator<Double> iter = values.iterator();
    int i = 0;
    long maxTime = Long.MIN_VALUE; // for checking the dates are sorted.
    for (final ZonedDateTime time : times) {
      final Double value = iter.next();
      final long epochMillis = time.toInstant().toEpochMillis();
      if (maxTime < epochMillis) {
        _times[i] = epochMillis;
        _values[i] = value;
        _zones[i] = time.getZone();
        maxTime = epochMillis;
      } else {
        throw new IllegalArgumentException("dates must be ordered");
      }
      i++;
    }
  }

  public ArrayDoubleTimeSeries(final DoubleTimeSeries dts) {
    final int size = dts.size();
    _times = new long[size];
    _values = new double[size];
    _zones = new TimeZone[size];
    if (dts instanceof ArrayDoubleTimeSeries) { // interesting to know if this
      // is worth it
      final ArrayDoubleTimeSeries adts = (ArrayDoubleTimeSeries) dts;
      System.arraycopy(adts._times, 0, _times, 0, size);
      System.arraycopy(adts._values, 0, _values, 0, size);
      System.arraycopy(adts._zones, 0, _zones, 0, size);
    } else {
      int pos = 0;
      for (final Map.Entry<ZonedDateTime, Double> entry : dts) {
        _times[pos] = entry.getKey().toInstant().toEpochMillis();
        _values[pos] = entry.getValue();
        _zones[pos] = entry.getKey().getZone();
        pos++;
      }
    }
  }

  public ArrayDoubleTimeSeries(final SortedMap<ZonedDateTime, Double> initialMap) {
    final int size = initialMap.size();
    _times = new long[size];
    _values = new double[size];
    _zones = new TimeZone[size];
    final Iterator<Entry<ZonedDateTime, Double>> iterator = initialMap.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<ZonedDateTime, Double> entry = iterator.next();
      _times[i] = entry.getKey().toInstant().toEpochMillis();
      _values[i] = entry.getValue().doubleValue();
      i++;
    }
  }

  @Override
  public DoubleTimeSeries subSeries(final ZonedDateTime startTime, final ZonedDateTime endTime) {
    if (isEmpty())
      return EMPTY_SERIES;
    // throw new NoSuchElementException("Series is empty");
    final long startMillis = startTime.toInstant().toEpochMillis();
    final long endMillis = endTime.toInstant().toEpochMillis();
    int startPos = Arrays.binarySearch(_times, startMillis);
    int endPos = Arrays.binarySearch(_times, endMillis);
    // if either is -1, make it zero
    startPos = startPos >= 0 ? startPos : -startPos - 1;
    endPos = endPos >= 0 ? endPos : -endPos - 1; 
    if (endPos >= _times.length) {
      endPos--;
    }
    final int length = endPos - startPos + 1;
    final long[] resultTimes = new long[length];
    final double[] resultValues = new double[length];
    final TimeZone[] resultTimeZones = new TimeZone[length];
    System.arraycopy(_times, startPos, resultTimes, 0, length);
    System.arraycopy(_values, startPos, resultValues, 0, length);
    System.arraycopy(_zones, startPos, resultTimeZones, 0, length);
    return new ArrayDoubleTimeSeries(resultTimes, resultValues, resultTimeZones);
  }

  @Override
  public Double getDataPoint(final ZonedDateTime instant) {
    return _values[Arrays.binarySearch(_times, instant.toInstant().toEpochMillis())];
  }

  @Override
  public ZonedDateTime getEarliestTime() {
    if (_times.length > 0)
      return ZonedDateTime.fromInstant(Instant.millisInstant(_times[0]), _zones[0]);
    else
      throw new NoSuchElementException("Series is empty");
  }

  @Override
  public Double getEarliestValue() {
    if (_values.length > 0)
      return _values[0];
    else
      throw new NoSuchElementException("Series is empty");
  }

  @Override
  public ZonedDateTime getLatestTime() {
    if (_times.length > 0) {
      final int index = _times.length - 1;
      return ZonedDateTime.fromInstant(Instant.millisInstant(_times[index]), _zones[index]);
    } else
      throw new NoSuchElementException("Series is empty");
  }

  @Override
  public Double getLatestValue() {
    if (_values.length > 0)
      return _values[_values.length - 1];
    else
      throw new NoSuchElementException("Series is empty");
  }

  /* package */class ArrayDoubleTimeSeriesIterator implements Iterator<Entry<ZonedDateTime, Double>> {
    private int _current = 0;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public Entry<ZonedDateTime, Double> next() {
      if (hasNext()) {
        final KeyValuePair<ZonedDateTime, Double> keyValuePair = new KeyValuePair<ZonedDateTime, Double>(ZonedDateTime.fromInstant(Instant.millisInstant(_times[_current]),
            _zones[_current]), _values[_current]);
        _current++;
        return keyValuePair;
      } else
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<Entry<ZonedDateTime, Double>> iterator() {
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

  /* package */class ArrayDoubleTimeSeriesTimeIterator implements Iterator<ZonedDateTime> {
    private int _current = 0;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public ZonedDateTime next() {
      if (hasNext()) {
        final ZonedDateTime time = ZonedDateTime.fromInstant(Instant.millisInstant(_times[_current]), _zones[_current]);
        _current++;
        return time;
      } else
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<ZonedDateTime> timeIterator() {
    return new ArrayDoubleTimeSeriesTimeIterator();
  }

  /* package */class ArrayDoubleTimeSeriesValuesIterator implements Iterator<Double> {
    private int _current = 0;

    @Override
    public boolean hasNext() {
      return _current < _values.length;
    }

    @Override
    public Double next() {
      if (hasNext()) {
        final Double value = _values[_current];
        _current++;
        return value;
      } else
        throw new NoSuchElementException();
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
  public Double getValue(final int index) {
    return _values[index];
  }

  //REVIEW Elaine - 2009-11-25 This is really horrible and it's my fault
  @Override
  public Double[] getValues() {
    Double[] values = new Double[_values.length];
    for(int i = 0; i < _values.length; i++) {
      values[i] = _values[i];
    }
    return values;
  }
  
  @Override
  public ZonedDateTime getTime(final int index) {
    return ZonedDateTime.fromInstant(Instant.millisInstant(_times[index]), _zones[index]);
  }

  @Override
  public DoubleTimeSeries tail(final int numItems) {
    if (numItems <= _times.length) {
      final long[] times = new long[numItems];
      final double[] values = new double[numItems];
      final TimeZone[] zones = new TimeZone[numItems];
      System.arraycopy(_times, _times.length - numItems, times, 0, numItems);
      System.arraycopy(_values, _values.length - numItems, values, 0, numItems);
      System.arraycopy(_zones, _zones.length - numItems, zones, 0, numItems);
      return new ArrayDoubleTimeSeries(times, values, zones);
    } else
      throw new NoSuchElementException("Not enough elements");
  }

  @Override
  public DoubleTimeSeries head(final int numItems) {
    if (numItems <= _times.length) {
      final long[] times = new long[numItems];
      final double[] values = new double[numItems];
      final TimeZone[] zones = new TimeZone[numItems];
      System.arraycopy(_times, 0, times, 0, numItems);
      System.arraycopy(_values, 0, values, 0, numItems);
      System.arraycopy(_zones, 0, zones, 0, numItems);
      return new ArrayDoubleTimeSeries(times, values, zones);
    } else
      throw new NoSuchElementException("Not enough elements");
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    // the aim here is to always minimize the number of comparisons.
    if (!(o instanceof DoubleTimeSeries))
      return false;
    if (o instanceof ArrayDoubleTimeSeries) {
      final ArrayDoubleTimeSeries otherADTS = (ArrayDoubleTimeSeries) o;
      if (otherADTS._times.length != _times.length)
        return false;
      if (!Arrays.equals(otherADTS._times, _times))
        return false;
      if (!Arrays.equals(otherADTS._values, _values))
        return false;
      return Arrays.equals(otherADTS._zones, _zones);
    } else {
      final DoubleTimeSeries dts = (DoubleTimeSeries) o;
      if (dts.size() != size())
        return false;
      if (isEmpty())
        return true;
      final Iterator<Entry<ZonedDateTime, Double>> otherIter = dts.iterator();
      // would be neater if we just had two iterators, but it will use
      // quite a bit more memory creating objects for no point. Of course
      // this probably is the _definition_ of premature optimization.
      int i = 0;
      while (otherIter.hasNext()) {
        final Entry<ZonedDateTime, Double> entry = otherIter.next();
        if (entry.getKey().toInstant().toEpochMillis() != _times[i])
          return false;
        if (!CompareUtils.closeEquals(entry.getValue(), _values[i]))
          return false;
        if (!entry.getKey().equals(_zones[i]))
          return false;
        i++;
      }
      return true;
    }

  }

  @Override
  public List<ZonedDateTime> times() {
    final ZonedDateTime[] times = new ZonedDateTime[_times.length];
    for (int i = 0; i < _times.length; i++) {
      times[i] = ZonedDateTime.fromInstant(Instant.instant(_times[i]), _zones[i]);
    }
    return Arrays.asList(times);
  }

  @Override
  public List<Double> values() {
    final Double[] copy = new Double[_values.length];
    System.arraycopy(_values, 0, copy, 0, _values.length);
    return Arrays.asList(copy);
  }

  @Override
  public int hashCode() {
    int value = 0;
    for (int i = 0; i < (_times.length > 0 ? 1 : 0); i++) {
      final long bits = Double.doubleToLongBits(_values[i]);
      value += _times[i] ^ bits ^ bits >>> 32;
    }
    return value;
  }

  @Override
  public Double getValue(final ZonedDateTime instant) {
    final Instant time = instant.toInstant();
    final long epochMillis = time.toEpochMillis();
    final int binarySearch = Arrays.binarySearch(_times, epochMillis);
    if (_times[binarySearch] == epochMillis)
      return _values[binarySearch];
    else
      throw new NoSuchElementException();
  }

}