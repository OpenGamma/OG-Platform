package com.opengamma.financial.timeseries.returns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.time.InstantProvider;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 * 
 */

public class SimpleNetWithDividendTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {
  private final TimeSeriesReturnCalculator _noDividendCalculator = new SimpleNetTimeSeriesReturnCalculator();

  @Override
  public DoubleTimeSeries evaluate(DoubleTimeSeries... x) throws TimeSeriesException {
    if (x == null)
      throw new TimeSeriesException("Time series array was null");
    if (x.length < 2)
      return _noDividendCalculator.evaluate(x);
    DoubleTimeSeries ts = x[0];
    if (ts.size() < 2)
      throw new TimeSeriesException("Need at least two data points to calculate return series");
    DoubleTimeSeries d = x[1];
    if (d.size() == 0)
      return _noDividendCalculator.evaluate(x);
    List<InstantProvider> times = new ArrayList<InstantProvider>();
    List<Double> data = new ArrayList<Double>();
    Iterator<Map.Entry<InstantProvider, Double>> iter = ts.iterator();
    Map.Entry<InstantProvider, Double> previousEntry = iter.next();
    Map.Entry<InstantProvider, Double> entry;
    double dividend;
    while (iter.hasNext()) {
      entry = iter.next();
      times.add(entry.getKey());
      try {
        dividend = d.getDataPoint(entry.getKey());
        data.add((entry.getValue() + dividend) / previousEntry.getValue() - 1);
      } catch (ArrayIndexOutOfBoundsException e) {
        data.add(entry.getValue() / previousEntry.getValue() - 1);
      } catch (NoSuchElementException e) {
        data.add(entry.getValue() / previousEntry.getValue() - 1);
      }
      previousEntry = entry;
    }
    return new ArrayDoubleTimeSeries(times, data);
  }
}
