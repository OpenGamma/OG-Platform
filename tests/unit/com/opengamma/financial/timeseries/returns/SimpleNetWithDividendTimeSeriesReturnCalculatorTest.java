package com.opengamma.financial.timeseries.returns;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 * 
 */

public class SimpleNetWithDividendTimeSeriesReturnCalculatorTest {
  private static final Function<DoubleTimeSeries, DoubleTimeSeries, TimeSeriesException> CALCULATOR = new SimpleNetWithDividendTimeSeriesReturnCalculator();

  @Test
  public void testWithBadInputs() throws Exception {
    try {
      CALCULATOR.evaluate((DoubleTimeSeries[]) null);
      fail();
    } catch (TimeSeriesException e) {
      // Expected
    }
    try {
      CALCULATOR.evaluate(new DoubleTimeSeries[0]);
      fail();
    } catch (TimeSeriesException e) {
      // Expected
    }
    DoubleTimeSeries ts = new ArrayDoubleTimeSeries(new long[] { 1 }, new double[] { 4 });
    try {
      CALCULATOR.evaluate(new DoubleTimeSeries[] { ts });
      fail();
    } catch (TimeSeriesException e) {
      // Expected
    }
  }

  @Test
  public void testReturnsWithoutDividends() throws TimeSeriesException {
    int n = 20;
    long[] times = new long[n];
    double[] data = new double[n];
    double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = random / data[i - 1] - 1;
      }
    }
    DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS }).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividendsAtDifferentTimes() throws TimeSeriesException {
    int n = 20;
    long[] times = new long[n];
    double[] data = new double[n];
    double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = random / data[i - 1] - 1;
      }
    }
    DoubleTimeSeries dividendTS = new ArrayDoubleTimeSeries(new long[] { 300 }, new double[] { 3 });
    DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS, dividendTS }).equals(returnTS));
  }

  @Test
  public void testReturns() throws TimeSeriesException {
    int n = 20;
    long[] times = new long[n];
    double[] data = new double[n];
    double[] returns = new double[n - 1];
    long[] dividendTimes = new long[] { 1, 4 };
    double[] dividendData = new double[] { 0.4, 0.6 };
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        if (i == 1) {
          returns[i - 1] = (random + dividendData[0]) / data[i - 1] - 1;
        } else if (i == 4) {
          returns[i - 1] = (random + dividendData[1]) / data[i - 1] - 1;
        } else {
          returns[i - 1] = random / data[i - 1] - 1;
        }
      }
    }
    DoubleTimeSeries dividendTS = new ArrayDoubleTimeSeries(dividendTimes, dividendData);
    DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS, dividendTS }).equals(returnTS));
  }

}
