package com.opengamma.financial.timeseries.returns;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;

/**
 * 
 * @author emcleod
 * 
 */

public class ContinuouslyCompoundedTimeSeriesReturnCalculatorTest {
  private static final Function<DoubleTimeSeries, DoubleTimeSeries, TimeSeriesException> CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);

  @Test
  public void testWithBadInputs() {
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
  public void testReturnsWithZeroesInSeries() throws TimeSeriesException {
    int n = 20;
    long[] times = new long[n];
    double[] data = new double[n];
    double[] returns = new double[n - 2];
    double random;
    for (int i = 0; i < n - 2; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    times[n - 2] = n - 2;
    data[n - 2] = 0;
    times[n - 1] = n - 1;
    data[n - 1] = Math.random();
    DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n - 2), returns);
    TimeSeriesReturnCalculator strict = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
    DoubleTimeSeries[] tsArray = new DoubleTimeSeries[] { priceTS };
    try {
      strict.evaluate(tsArray);
      fail();
    } catch (TimeSeriesException e) {
      // Expected
    }
    TimeSeriesReturnCalculator lenient = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
    assertTrue(lenient.evaluate(tsArray).equals(returnTS));
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
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS }).equals(returnTS));
  }

}
