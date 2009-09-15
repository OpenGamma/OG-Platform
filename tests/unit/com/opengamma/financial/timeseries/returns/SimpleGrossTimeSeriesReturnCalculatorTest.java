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

public class SimpleGrossTimeSeriesReturnCalculatorTest {
  private static final Function<DoubleTimeSeries, DoubleTimeSeries, TimeSeriesException> CALCULATOR = new SimpleGrossTimeSeriesReturnCalculator();

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
  public void testReturns() throws TimeSeriesException {
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
        returns[i - 1] = random / data[i - 1];
      }
    }
    DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS }).equals(returnTS));
  }
}
