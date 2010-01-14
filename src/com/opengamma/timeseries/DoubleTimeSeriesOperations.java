package com.opengamma.timeseries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;

@SuppressWarnings("synthetic-access")
public class DoubleTimeSeriesOperations {
  private interface BinaryOperator {
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

  private interface UnaryOperator {
    public double operate(double a);
  }

  public static class ReciprocalOperator implements UnaryOperator {
    public double operate(double a) {
      return 1 / a;
    }
  }

  public static class NegateOperator implements UnaryOperator {
    public double operate(double a) {
      return -a;
    }
  }

  public static class LogOperator implements UnaryOperator {
    public double operate(double a) {
      return Math.log(a);
    }
  }

  public static class Log10Operator implements UnaryOperator {
    public double operate(double a) {
      return Math.log10(a);
    }
  }

  public static class AbsOperator implements UnaryOperator {
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

  private static final ReciprocalOperator s_reciprocalOperator = new ReciprocalOperator();
  private static final NegateOperator s_negateOperator = new NegateOperator();
  private static final LogOperator s_logOperator = new LogOperator();
  private static final Log10Operator s_log10Operator = new Log10Operator();
  private static final AbsOperator s_absOperator = new AbsOperator();

  private static DoubleTimeSeries operate(DoubleTimeSeries a, DoubleTimeSeries b, BinaryOperator operator) {
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

  private static DoubleTimeSeries operate(DoubleTimeSeries a, UnaryOperator operator) {
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

  private static DoubleTimeSeries operate(DoubleTimeSeries a, double b, BinaryOperator operator) {
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

  public static DoubleTimeSeries add(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_addOperator);
  }

  public static DoubleTimeSeries add(DoubleTimeSeries a, double b) {
    return operate(a, b, s_addOperator);
  }

  public static DoubleTimeSeries subtract(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_subtractOperator);
  }

  public static DoubleTimeSeries subtract(DoubleTimeSeries a, double b) {
    return operate(a, b, s_subtractOperator);
  }

  public static DoubleTimeSeries multiply(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_multiplyOperator);
  }

  public static DoubleTimeSeries multiply(DoubleTimeSeries a, double b) {
    return operate(a, b, s_multiplyOperator);
  }

  public static DoubleTimeSeries divide(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_divideOperator);
  }

  public static DoubleTimeSeries divide(DoubleTimeSeries a, double b) {
    return operate(a, b, s_divideOperator);
  }

  public static DoubleTimeSeries pow(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_powerOperator);
  }

  public static DoubleTimeSeries pow(DoubleTimeSeries a, double b) {
    return operate(a, b, s_powerOperator);
  }

  public static DoubleTimeSeries min(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_minimumOperator);
  }

  public static DoubleTimeSeries min(DoubleTimeSeries a, double b) {
    return operate(a, b, s_minimumOperator);
  }

  public static DoubleTimeSeries max(DoubleTimeSeries a, DoubleTimeSeries b) {
    return operate(a, b, s_maximumOperator);
  }

  public static DoubleTimeSeries max(DoubleTimeSeries a, double b) {
    return operate(a, b, s_maximumOperator);
  }

  public static DoubleTimeSeries negate(DoubleTimeSeries a) {
    return operate(a, s_negateOperator);
  }

  public static DoubleTimeSeries reciprocal(DoubleTimeSeries a) {
    return operate(a, s_reciprocalOperator);
  }

  public static DoubleTimeSeries log(DoubleTimeSeries a) {
    return operate(a, s_logOperator);
  }

  public static DoubleTimeSeries log10(DoubleTimeSeries a) {
    return operate(a, s_log10Operator);
  }

  public static DoubleTimeSeries abs(DoubleTimeSeries a) {
    return operate(a, s_absOperator);
  }

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
