package com.opengamma.timeseries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;

@SuppressWarnings("synthetic-access")
public class DoubleTimeSeriesOperations {
  /**
   * @author jim
   * Interface that should be satisfied by all binary operators.  Public so that it's possible
   * to provide custom operations to @method operate and @method unionOperate
   */
  public interface BinaryOperator {
    public double operate(double a, double b);
  }

  private static class AddOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a + b;
    }
  }

  private static class SubtractOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a - b;
    }
  }

  private static class MultiplyOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a * b;
    }
  }

  private static class DivideOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a / b;
    }
  }

  private static class PowerOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return Math.pow(a, b);
    }
  }

  private static class MinimumOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return Math.min(a, b);
    }
  }

  private static class MaximumOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return Math.max(a, b);
    }
  }
  
  private static class AverageOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return (a + b) / 2;
    }
  }
  
  private static class FirstOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a;
    }
  }

  private static class SecondOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return b;
    }
  }
  
  /**
   * @author jim
   * Interface to be implemented by any unary operation on time series.  Public so
   * that custom implementations can be passed to @method operate.
   */
  public interface UnaryOperator {
    public double operate(double a);
  }

  private static class ReciprocalOperator implements UnaryOperator {
    public double operate(double a) {
      return 1 / a;
    }
  }

  private static class NegateOperator implements UnaryOperator {
    public double operate(double a) {
      return -a;
    }
  }

  private static class LogOperator implements UnaryOperator {
    public double operate(double a) {
      return Math.log(a);
    }
  }

  private static class Log10Operator implements UnaryOperator {
    public double operate(double a) {
      return Math.log10(a);
    }
  }

  private static class AbsOperator implements UnaryOperator {
    public double operate(double a) {
      return Math.abs(a);
    }
  }

  private static final AddOperator s_addOperator = new AddOperator();
  private static final SubtractOperator s_subtractOperator = new SubtractOperator();
  private static final MultiplyOperator s_multiplyOperator = new MultiplyOperator();
  private static final DivideOperator s_divideOperator = new DivideOperator();
  private static final PowerOperator s_powerOperator = new PowerOperator();
  private static final MinimumOperator s_minimumOperator = new MinimumOperator();
  private static final MaximumOperator s_maximumOperator = new MaximumOperator();
  private static final AverageOperator s_averageOperator = new AverageOperator();
  private static final FirstOperator s_firstOperator = new FirstOperator();
  private static final SecondOperator s_secondOperator = new SecondOperator();

  private static final ReciprocalOperator s_reciprocalOperator = new ReciprocalOperator();
  private static final NegateOperator s_negateOperator = new NegateOperator();
  private static final LogOperator s_logOperator = new LogOperator();
  private static final Log10Operator s_log10Operator = new Log10Operator();
  private static final AbsOperator s_absOperator = new AbsOperator();

  /**
   * Takes the union operation of the two time series applying the given binary operator to each common element.
   * Items present in a and not in b and vice versa are copied unchanged to the resulting time series.
   * Normally it would be more convenient to use a pre-packaged operator elsewhere in this
   * class, it's public to allow implementation of unusual operations.
   * @param a time series
   * @param b time series
   * @param operator operation to perform on each common element
   * @return the resulting union of both time series, with the common elements combined using the supplied binary operator
   */
  public static DoubleTimeSeries unionOperate(DoubleTimeSeries a, DoubleTimeSeries b, BinaryOperator operator) {
    Set<ZonedDateTime> unionTimes = new TreeSet<ZonedDateTime>(a.times());
    unionTimes.addAll(b.times());
    int max = unionTimes.size();
    long[] times = new long[max];
    double[] values = new double[max];
    TimeZone[] zones = new TimeZone[max];
    int pos=0;
    for (ZonedDateTime dateTime : unionTimes) {
      Double valueA = a.getDataPoint(dateTime);
      Double valueB = b.getDataPoint(dateTime);
      times[pos] = dateTime.toInstant().toEpochMillis();
      zones[pos] = dateTime.getZone();
      if (valueB != null && valueA != null) {
        double newValue = operator.operate(valueA, valueB);
        values[pos] = newValue;
      } else if (valueA != null) {
        values[pos] = valueA;
      } else { // valueB must be non-null.
        assert valueB != null;
        values[pos] = valueB;
      }
      pos++;
    }
    return new ArrayDoubleTimeSeries(times, values, zones);
  }
  
  /**
   * Take the intersection of the two time series, a and b, i.e. only data points with dates present in both end up in the 
   * resulting time series.  Each common date has the data points combined using the operator object passed in.
   * Normally it would be more convenient to use a pre-packaged operator elsewhere in this
   * class, it's public to allow implementation of unusual operations.   
   * @param a time series
   * @param b time series
   * @param operator operation to perform on each common element
   * @return the resulting intersection of both time series with the common elements combined using the supplied binary operator
   */
  public static DoubleTimeSeries operate(DoubleTimeSeries a, DoubleTimeSeries b, BinaryOperator operator) {
    int max = Math.max(a.size(), b.size());
    long[] times = new long[max];
    double[] values = new double[max];
    TimeZone[] zones = new TimeZone[max];
    int pos = 0;
    for (Entry<ZonedDateTime, Double> entry : a) {
      Double valueB = b.getDataPoint(entry.getKey());
      if (valueB != null) {
        double newValue = operator.operate(entry.getValue(), valueB);
        times[pos] = entry.getKey().toInstant().toEpochMillis();
        values[pos] = newValue;
        zones[pos] = entry.getKey().getZone();
        pos++;
      }
    }
    long[] trimmedTimes = new long[pos];
    double[] trimmedValues = new double[pos];
    TimeZone[] trimmedZones = new TimeZone[pos];
    System.arraycopy(times, 0, trimmedTimes, 0, pos);
    System.arraycopy(values, 0, trimmedValues, 0, pos);
    System.arraycopy(zones, 0, trimmedZones, 0, pos);
    return new ArrayDoubleTimeSeries(trimmedTimes, trimmedValues, trimmedZones);
  }

  /**
   * Apply the supplied unary operator to each element of the supplied time series.
   * Normally it would be more convenient to use a pre-packaged operator elsewhere in this
   * class, it's public to allow implementation of unusual operations 
   * @param a time series
   * @param operator operation to perform on each element
   * @return the resulting time series
   */
  public  static DoubleTimeSeries operate(DoubleTimeSeries a, UnaryOperator operator) {
    final int size = a.size();
    long[] times = new long[size];
    double[] values = new double[size];
    TimeZone[] zones = new TimeZone[size];
    int pos = 0;
    for (Entry<ZonedDateTime, Double> entry : a) {
      times[pos] = entry.getKey().toInstant().toEpochMillis();
      values[pos] = operator.operate(entry.getValue());
      zones[pos] = entry.getKey().getZone();
    }
    return new ArrayDoubleTimeSeries(times, values, zones);
  }
  
  /**
   * Apply the supplied binary operator to each element of the single supplied time series, the
   * second value for the operator coming from the same double parameter for each element of that 
   * time series.
   * @param a time series
   * @param b a scalar value
   * @param operator operation to perform on each element
   * @return
   */
  public static DoubleTimeSeries operate(DoubleTimeSeries a, double b, BinaryOperator operator) {
    final int size = a.size();
    long[] times = new long[size];
    double[] values = new double[size];
    TimeZone[] zones = new TimeZone[size];
    int pos = 0;
    for (Entry<ZonedDateTime, Double> entry : a) {
      times[pos] = entry.getKey().toInstant().toEpochMillis();
      values[pos] = operator.operate(entry.getValue(), b);
      zones[pos] = entry.getKey().getZone();
    }
    return new ArrayDoubleTimeSeries(times, values, zones);
  }
  
  /**
   * Perform union addition.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, they are summed.
   * @param a time series
   * @param b time series
   * @return union sum of both time series
   */
  public static DoubleTimeSeries unionAdd(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_addOperator);
  }

  /**
   * Perform intersection addition.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, they are summed.
   * @param a time series
   * @param b time series
   * @return intersection sum of both time series
   */
  public static DoubleTimeSeries add(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_addOperator);
  }

  /**
   * Add all values in the supplied time series (a) to a given value (b)
   *  a[i] + b | i <- 0..size[a]
   * @param a time series
   * @param b subtrahend
   * @return supplied time series with each element subtracted by a scalar value
   */
  public static DoubleTimeSeries add(DoubleTimeSeries a, double b) {
    return operate(a, b, s_addOperator);
  }

  /**
   * Perform union subtraction.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, they are subtracted.
   * @param a time series
   * @param b time series
   * @return union difference of both time series
   */
  public static DoubleTimeSeries unionSubtract(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_subtractOperator);
  }

  /**
   * Perform intersection subtraction.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, they are subtracted.
   * @param a time series
   * @param b time series
   * @return intersection difference of both time series
   */  
  public static DoubleTimeSeries subtract(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_subtractOperator);
  }

  /**
   * Subtract all values in the supplied time series (a) by a given value (b)
   *  a[i] - b | i <- 0..size[a]
   * @param a time series
   * @param b subtrahend
   * @return supplied time series with each element subtracted by a scalar value
   */
  public static DoubleTimeSeries subtract(DoubleTimeSeries a, double b) {
    return operate(a, b, s_subtractOperator);
  }

  /**
   * Perform union multiplication.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, they are multiplied.
   * @param a time series
   * @param b time series
   * @return union multiplication of both time series
   */
  public static DoubleTimeSeries unionMultiply(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_addOperator);
  }
  
  /**
   * Perform intersection multiply.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, they are multiply.
   * @param a time series
   * @param b time series
   * @return intersection multiplication of both time series
   */
  public static DoubleTimeSeries multiply(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_multiplyOperator);
  }

  /**
   * Multiply all values in the supplied time series (a) by a given scalar value (b)
   *  a[i] * b | i <- 0..size[a]
   * @param a time series
   * @param b divisor
   * @return supplied time series with each element multiplied by scalar value
   */
  public static DoubleTimeSeries multiply(DoubleTimeSeries a, double b) {
    return operate(a, b, s_multiplyOperator);
  }

  /**
   * Perform union division.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, they are divided.
   * @param a time series
   * @param b time series
   * @return union division of both time series
   */
  public static DoubleTimeSeries unionDivide(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_divideOperator);
  }
  
  /**
   * Perform intersection division.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, they are divided.
   * @param a time series
   * @param b time series
   * @return intersection division of both time series
   */
  public static DoubleTimeSeries divide(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_divideOperator);
  }
  
  /**
   * Divide all values in the supplied time series (a) by a given value (b)
   *  a[i] / b | i <- 0..size[a]
   * @param a time series
   * @param b divisor
   * @return supplied time series with each element divided by scalar value
   */
  public static DoubleTimeSeries divide(DoubleTimeSeries a, double b) {
    return operate(a, b, s_divideOperator);
  }

  /**
   * Perform union power.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, a[i] is raised to the power of b[i].
   * @param a time series
   * @param b time series
   * @return union power of both time series
   */
  public static DoubleTimeSeries unionPow(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_powerOperator);
  }
  
  /**
   * Perform intersection power.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, a is raised to the power of b.
   * @param a time series
   * @param b time series
   * @return intersection power of both time series
   */
  public static DoubleTimeSeries pow(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_powerOperator);
  }

  /**
   * Raise all values in the supplied time series (a) to the value of b.
   * @param a time series
   * @param b power value
   * @return a raised to b
   */
  public static DoubleTimeSeries pow(DoubleTimeSeries a, double b) {
    return operate(a, b, s_powerOperator);
  }
  
  /**
   * Perform union minimum.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, the smaller is chosen.
   * @param a time series
   * @param b time series
   * @return union minimum of both time series
   */
  public static DoubleTimeSeries unionMin(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_minimumOperator);
  }

  /**
   * Perform intersection minimum.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, the minimum is chosen.
   * @param a time series
   * @param b time series
   * @return intersection minimum of both time series
   */
  public static DoubleTimeSeries min(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_minimumOperator);
  }

  public static DoubleTimeSeries min(DoubleTimeSeries a, double b) {
    return operate(a, b, s_minimumOperator);
  }
  
  /**
   * Perform union maximum.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, the maximum is chosen.
   * @param a time series
   * @param b time series
   * @return union maximum of both time series
   */
  public static DoubleTimeSeries unionMax(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_maximumOperator);
  }

  /**
   * Perform intersection maximum.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, the maximum is chosen.
   * @param a time series
   * @param b time series
   * @return intersection maximum of both time series
   */
  public static DoubleTimeSeries max(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_maximumOperator);
  }

  public static DoubleTimeSeries max(DoubleTimeSeries a, double b) {
    return operate(a, b, s_maximumOperator);
  }
  
  /**
   * Perform union average.  If data points are only in one series or the other, those are copied
   * to the resulting series.  If data points are in both series, they are averaged: (a+b)/2.
   * @param a time series
   * @param b time series
   * @return union average of both time series
   */
  public static DoubleTimeSeries unionAverage(DoubleTimeSeries a, DoubleTimeSeries b) {
    return unionOperate(a, b, s_averageOperator);
  }
  
  /**
   * Perform intersection average.  If data points are only in one series or the other, those are excluded
   * from the resulting series.  If data points are in both series, they are averaged.
   * @param a time series
   * @param b time series
   * @return intersection average of both time series
   */
  public static DoubleTimeSeries average(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_averageOperator);
  }
  
  /**
   * Perform an intersection of date in a time series, taking the value from the first for each common date.
   * @param a time series
   * @param b time series
   * @return intersection first value
   */
  public static DoubleTimeSeries intersectionFirstValues(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_firstOperator);
  }

  /**
   * Perform an intersection of date in a time series, taking the value from the second series (b) for each common date.
   * @param a time series
   * @param b time series
   * @return intersection second value
   */
  public static DoubleTimeSeries intersectionSecondValues(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_secondOperator);
  }

  /**
   * Negate the value of each data point in the supplied time series
   * @param a time series
   * @return the negation of the time series
   */
  public static DoubleTimeSeries negate(DoubleTimeSeries a) {
    return operate(a, s_negateOperator);
  }

  /**
   * The reciprocal of each data point in the supplied time series
   * @param a time series
   * @return the reciprocal of the time series
   */
  public static DoubleTimeSeries reciprocal(DoubleTimeSeries a) {
    return operate(a, s_reciprocalOperator);
  }

  /**
   * The natural logarithm of each data point in the supplied time series
   * @param a time series
   * @return the natural log of the time series
   */
  public static DoubleTimeSeries log(DoubleTimeSeries a) {
    return operate(a, s_logOperator);
  }

  /**
   * The base-10 logarithm of each data point in the supplied time series
   * @param a time series
   * @return the base-10 log of the time series
   */
  public static DoubleTimeSeries log10(DoubleTimeSeries a) {
    return operate(a, s_log10Operator);
  }

  /**
   * The absolute (positive) value of each data point in the supplied time series
   * @param a time series
   * @return the absolute value of the time series
   */
  public static DoubleTimeSeries abs(DoubleTimeSeries a) {
    return operate(a, s_absOperator);
  }

  /**
   * Find the largest value in the supplied time series
   * @param a time series
   * @return the largest value
   */
  public static double maxValue(DoubleTimeSeries a) {
    if (a == null || a.isEmpty()) {
      throw new OpenGammaRuntimeException("cannot determine maximum value of null or empty DoubleTimeSeries");
    }
    double max = 0.0; // the compiler can't work out that there's at least one element so this is unnecessary... 
    for (Double value : a.values()) {
      max = Math.max(value, max);
    }
    return max;
  }

  /**
   * Find the smallest value in the supplied time series
   * @param a time series
   * @return the smallest value
   */
  public static double minValue(DoubleTimeSeries a) {
    if (a == null || a.isEmpty()) {
      throw new OpenGammaRuntimeException("cannot determine minimum value of null or empty DoubleTimeSeries");
    }
    double min = 0.0; // the compiler can't work out that there's at least one element so this is unnecessary... 
    for (Double value : a.values()) {
      min = Math.min(value, min);
    }
    return min;
  }

  /**
   * shift the values in a series to neighboring data points by a particular offset.
   * @param a time series
   * @param lag the number of points to shift along
   * @return the resulting time series
   */
  public static DoubleTimeSeries lag(DoubleTimeSeries a, int lag) {
    // TODO deal with going forward in time
    if (lag >= a.size())
      throw new IllegalArgumentException("Lag must be less than series size");
    if (lag == 0)
      return a;
    List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    List<Double> data = new ArrayList<Double>();
    Iterator<ZonedDateTime> timeIter = a.timeIterator();
    Iterator<Double> dataIter = a.valuesIterator();
    for (int i = 0; i < a.size(); i++) {
      if (i < lag) {
        timeIter.next();
      } else {
        dates.add(timeIter.next());
        data.add(dataIter.next());
      }
    }
    return new ArrayDoubleTimeSeries(dates, data);
  }
}
