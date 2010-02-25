package com.opengamma.timeseries;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

@SuppressWarnings("synthetic-access")
public class DoubleTimeSeriesOperations {
  /**
   * @author jim
   *         Interface that should be satisfied by all binary operators. Public
   *         so that it's possible
   *         to provide custom operations to @method operate and @method
   *         unionOperate
   */
  public interface BinaryOperator {
    public double operate(double a, double b);
  }

  private static class AddOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a + b;
    }
  }

  private static class SubtractOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a - b;
    }
  }

  private static class MultiplyOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a * b;
    }
  }

  private static class DivideOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a / b;
    }
  }

  private static class PowerOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return Math.pow(a, b);
    }
  }

  private static class MinimumOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return Math.min(a, b);
    }
  }

  private static class MaximumOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return Math.max(a, b);
    }
  }

  private static class AverageOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return (a + b) / 2;
    }
  }

  private static class FirstOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a;
    }
  }

  private static class SecondOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return b;
    }
  }

  /**
   * @author jim
   *         Interface to be implemented by any unary operation on time series.
   *         Public so
   *         that custom implementations can be passed to @method operate.
   */
  public interface UnaryOperator {
    public double operate(double a);
  }

  private static class ReciprocalOperator implements UnaryOperator {
    public double operate(final double a) {
      return 1 / a;
    }
  }

  private static class NegateOperator implements UnaryOperator {
    public double operate(final double a) {
      return -a;
    }
  }

  private static class LogOperator implements UnaryOperator {
    public double operate(final double a) {
      return Math.log(a);
    }
  }

  private static class Log10Operator implements UnaryOperator {
    public double operate(final double a) {
      return Math.log10(a);
    }
  }

  private static class AbsOperator implements UnaryOperator {
    public double operate(final double a) {
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
   * Takes the union operation of the two time series applying the given binary
   * operator to each common element.
   * Items present in a and not in b and vice versa are copied unchanged to the
   * resulting time series.
   * Normally it would be more convenient to use a pre-packaged operator
   * elsewhere in this
   * class, it's public to allow implementation of unusual operations.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @param operator
   *          operation to perform on each common element
   * @return the resulting union of both time series, with the common elements
   *         combined using the supplied binary operator
   */
  @SuppressWarnings("unchecked")
  public static <E> DoubleTimeSeries<E> unionOperate(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b, final BinaryOperator operator) {
    final Set<E> unionTimes = new TreeSet<E>(a.times());
    unionTimes.addAll(b.times());
    final int max = unionTimes.size();
    final E[] times = (E[]) new Object[max];
    final Double[] values = new Double[max];
    int pos = 0;
    for (final E dateTime : unionTimes) {
      final Double valueA = a.getValue(dateTime);
      final Double valueB = b.getValue(dateTime);
      times[pos] = dateTime;
      if (valueB != null && valueA != null) {
        final Double newValue = operator.operate(valueA, valueB);
        values[pos] = newValue;
      } else if (valueA != null) {
        values[pos] = valueA;
      } else { // valueB must be non-null.
        assert valueB != null;
        values[pos] = valueB;
      }
      pos++;
    }
    return (DoubleTimeSeries<E>) a.newInstance(times, values);
  }
  
  public static FastIntDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries a, final FastIntDoubleTimeSeries b, final BinaryOperator operator) { 
    final int[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final int[] bTimes = b.timesArrayFast();
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length){
        int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }
  
  public static FastIntDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries a, final FastLongDoubleTimeSeries b, final BinaryOperator operator) { 
    final int[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final long[] bTimesLong = b.timesArrayFast();
    final int[] bTimes = new int[bTimesLong.length];
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimesLong[i], aEncoding);
      }
    } else {
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = (int) bTimesLong[i];
      }
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length){
        int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }
  
  public static FastLongDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries a, final FastIntDoubleTimeSeries b, final BinaryOperator operator) { 
    final long[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final int[] bTimesInt = b.timesArrayFast();
    final long[] bTimes = new long[bTimesInt.length];
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimesInt[i], aEncoding);
      }
    } else {
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bTimesInt[i];
      }
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length){
        int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    long[] trimmedTimes = new long[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }  
  
  public static FastLongDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries a, final FastLongDoubleTimeSeries b, final BinaryOperator operator) { 
    final long[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final long[] bTimes = b.timesArrayFast();
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length){
        int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    long[] trimmedTimes = new long[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }  
  /**
   * Take the intersection of the two time series, a and b, i.e. only data
   * points with dates present in both end up in the
   * resulting time series. Each common date has the data points combined using
   * the operator object passed in.
   * Normally it would be more convenient to use a pre-packaged operator
   * elsewhere in this
   * class, it's public to allow implementation of unusual operations.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @param operator
   *          operation to perform on each common element
   * @return the resulting intersection of both time series with the common
   *         elements combined using the supplied binary operator
   */
  @SuppressWarnings("unchecked")
  public static <E> FastBackedDoubleTimeSeries<E> operate(final FastBackedDoubleTimeSeries<E> a, final FastBackedDoubleTimeSeries<E> b, final BinaryOperator operator) {
    FastTimeSeries<?> fastSeriesA = a.getFastSeries();
    FastTimeSeries<?> fastSeriesB = b.getFastSeries();
    if (fastSeriesA instanceof FastIntDoubleTimeSeries) {
      if (fastSeriesB instanceof FastIntDoubleTimeSeries) {
        FastIntDoubleTimeSeries result = operate((FastIntDoubleTimeSeries)fastSeriesA, (FastIntDoubleTimeSeries)fastSeriesB, operator);
        return (FastBackedDoubleTimeSeries<E>)a.newInstance(result.timesArrayFast(), result.valuesArrayFast());
      } else {
        FastIntDoubleTimeSeries result = operate((FastIntDoubleTimeSeries)fastSeriesA, (FastLongDoubleTimeSeries)fastSeriesB, operator);
      }
    }
  }
  
  public static FastLongDoubleTimeSeries operate(final FastLongDoubleTimeSeries a, final FastLongDoubleTimeSeries b, final BinaryOperator operator) { 
    final long[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final long[] bTimes = b.timesArrayFast();
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    long[] trimmedTimes = new long[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }
  
  public static FastLongDoubleTimeSeries operate(final FastLongDoubleTimeSeries a, final FastIntDoubleTimeSeries b, final BinaryOperator operator) { 
    final long[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final int[] bTimesInt = b.timesArrayFast();
    final long[] bTimes = new long[bTimesInt.length];
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimesInt.length; i++) {
        bTimes[i] = bEncoding.convertToLong(bTimesInt[i], aEncoding);
      }
    } else {
      for (int i=0; i<bTimesInt.length; i++) {
        bTimes[i] = bTimesInt[i];
      }      
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final long[] resTimes = new long[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    long[] trimmedTimes = new long[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }
  public static FastIntDoubleTimeSeries operate(final FastIntDoubleTimeSeries a, final FastLongDoubleTimeSeries b, final BinaryOperator operator) { 
    final int[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final long[] bTimesLong = b.timesArrayFast();
    final int[] bTimes = new int[bTimesLong.length];
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimesLong[i], aEncoding);
      }
    } else {
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = (int) bTimesLong[i];
      }      
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }
  
  public static FastIntDoubleTimeSeries operate(final FastIntDoubleTimeSeries a, final FastIntDoubleTimeSeries b, final BinaryOperator operator) { 
    final int[] aTimes = a.timesArrayFast();
    final double[] aValues = a.valuesArrayFast();
    int aCount = 0;
    final int[] bTimes = b.timesArrayFast();
    if (a.getEncoding() != b.getEncoding()) { // convert to a's format -- NOTE: if we switch to using an underlying array rather than a copy, we can't modify it in-place like we're doing here.
      DateTimeNumericEncoding aEncoding = a.getEncoding();
      DateTimeNumericEncoding bEncoding = b.getEncoding();
      for (int i=0; i<bTimes.length; i++) {
        bTimes[i] = bEncoding.convertToInt(bTimes[i], aEncoding);
      }
    }
    final double[] bValues = b.valuesArrayFast();
    int bCount = 0;
    final int[] resTimes = new int[aTimes.length + bTimes.length];
    final double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return a.newInstanceFast(trimmedTimes, trimmedValues);
  }

  /**
   * Apply the supplied unary operator to each element of the supplied time
   * series.
   * Normally it would be more convenient to use a pre-packaged operator
   * elsewhere in this
   * class, it's public to allow implementation of unusual operations
   * 
   * @param a
   *          time series
   * @param operator
   *          operation to perform on each element
   * @return the resulting time series
   */
  @SuppressWarnings("unchecked")
  public static <E> DoubleTimeSeries<E> operate(final DoubleTimeSeries<E> a, final UnaryOperator operator) {
    final int size = a.size();
    final E[] times = (E[]) new Object[size];
    final Double[] values = new Double[size];
    final int pos = 0;
    for (final Entry<E, Double> entry : a) {
      times[pos] = entry.getKey();
      values[pos] = operator.operate(entry.getValue());
    }
    return (DoubleTimeSeries<E>) a.newInstance(times, values);
  }

  /**
   * Apply the supplied binary operator to each element of the single supplied
   * time series, the
   * second value for the operator coming from the same double parameter for
   * each element of that
   * time series.
   * 
   * @param a
   *          time series
   * @param b
   *          a scalar value
   * @param operator
   *          operation to perform on each element
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <E> DoubleTimeSeries<E> operate(final DoubleTimeSeries<E> a, final double b, final BinaryOperator operator) {
    final int size = a.size();
    final E[] times = (E[]) new Object[size];
    final Double[] values = new Double[size];
    final int pos = 0;
    for (final Entry<E, Double> entry : a) {
      times[pos] = entry.getKey();
      values[pos] = operator.operate(entry.getValue(), b);
    }
    return (DoubleTimeSeries<E>) a.newInstance(times, values);
  }

  /**
   * Perform union addition. If data points are only in one series or the other,
   * those are copied
   * to the resulting series. If data points are in both series, they are
   * summed.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union sum of both time series
   */
  public static <E> DoubleTimeSeries<E> unionAdd(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_addOperator);
  }

  /**
   * Perform intersection addition. If data points are only in one series or the
   * other, those are excluded
   * from the resulting series. If data points are in both series, they are
   * summed.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection sum of both time series
   */
  public static <E> DoubleTimeSeries<E> add(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_addOperator);
  }

  /**
   * Add all values in the supplied time series (a) to a given value (b)
   * a[i] + b | i <- 0..size[a]
   * 
   * @param a
   *          time series
   * @param b
   *          subtrahend
   * @return supplied time series with each element subtracted by a scalar value
   */
  public static <E> DoubleTimeSeries<E> add(final DoubleTimeSeries<E> a, final double b) {
    return operate(a, b, s_addOperator);
  }

  /**
   * Perform union subtraction. If data points are only in one series or the
   * other, those are copied
   * to the resulting series. If data points are in both series, they are
   * subtracted.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union difference of both time series
   */
  public static <E> DoubleTimeSeries<E> unionSubtract(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_subtractOperator);
  }

  /**
   * Perform intersection subtraction. If data points are only in one series or
   * the other, those are excluded
   * from the resulting series. If data points are in both series, they are
   * subtracted.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection difference of both time series
   */
  public static <E> DoubleTimeSeries<E> subtract(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_subtractOperator);
  }

  /**
   * Subtract all values in the supplied time series (a) by a given value (b)
   * a[i] - b | i <- 0..size[a]
   * 
   * @param a
   *          time series
   * @param b
   *          subtrahend
   * @return supplied time series with each element subtracted by a scalar value
   */
  public static <E> DoubleTimeSeries<E> subtract(final DoubleTimeSeries<E> a, final double b) {
    return operate(a, b, s_subtractOperator);
  }

  /**
   * Perform union multiplication. If data points are only in one series or the
   * other, those are copied
   * to the resulting series. If data points are in both series, they are
   * multiplied.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union multiplication of both time series
   */
  public static <E> DoubleTimeSeries<E> unionMultiply(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_addOperator);
  }

  /**
   * Perform intersection multiply. If data points are only in one series or the
   * other, those are excluded
   * from the resulting series. If data points are in both series, they are
   * multiply.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection multiplication of both time series
   */
  public static <E> DoubleTimeSeries<E> multiply(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_multiplyOperator);
  }

  /**
   * Multiply all values in the supplied time series (a) by a given scalar value
   * (b)
   * a[i] * b | i <- 0..size[a]
   * 
   * @param a
   *          time series
   * @param b
   *          divisor
   * @return supplied time series with each element multiplied by scalar value
   */
  public static <E> DoubleTimeSeries<E> multiply(final DoubleTimeSeries<E> a, final double b) {
    return operate(a, b, s_multiplyOperator);
  }

  /**
   * Perform union division. If data points are only in one series or the other,
   * those are copied
   * to the resulting series. If data points are in both series, they are
   * divided.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union division of both time series
   */
  public static <E> DoubleTimeSeries<E> unionDivide(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_divideOperator);
  }

  /**
   * Perform intersection division. If data points are only in one series or the
   * other, those are excluded
   * from the resulting series. If data points are in both series, they are
   * divided.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection division of both time series
   */
  public static <E> DoubleTimeSeries<E> divide(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_divideOperator);
  }

  /**
   * Divide all values in the supplied time series (a) by a given value (b)
   * a[i] / b | i <- 0..size[a]
   * 
   * @param a
   *          time series
   * @param b
   *          divisor
   * @return supplied time series with each element divided by scalar value
   */
  public static <E> DoubleTimeSeries<E> divide(final DoubleTimeSeries<E> a, final double b) {
    return operate(a, b, s_divideOperator);
  }

  /**
   * Perform union power. If data points are only in one series or the other,
   * those are copied
   * to the resulting series. If data points are in both series, a[i] is raised
   * to the power of b[i].
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union power of both time series
   */
  public static <E> DoubleTimeSeries<E> unionPow(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_powerOperator);
  }

  /**
   * Perform intersection power. If data points are only in one series or the
   * other, those are excluded
   * from the resulting series. If data points are in both series, a is raised
   * to the power of b.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection power of both time series
   */
  public static <E> DoubleTimeSeries<E> pow(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_powerOperator);
  }

  /**
   * Raise all values in the supplied time series (a) to the value of b.
   * 
   * @param a
   *          time series
   * @param b
   *          power value
   * @return a raised to b
   */
  public static <E> DoubleTimeSeries<E> pow(final DoubleTimeSeries<E> a, final double b) {
    return operate(a, b, s_powerOperator);
  }

  /**
   * Perform union minimum. If data points are only in one series or the other,
   * those are copied
   * to the resulting series. If data points are in both series, the smaller is
   * chosen.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union minimum of both time series
   */
  public static <E> DoubleTimeSeries<E> unionMin(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_minimumOperator);
  }

  /**
   * Perform intersection minimum. If data points are only in one series or the
   * other, those are excluded
   * from the resulting series. If data points are in both series, the minimum
   * is chosen.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection minimum of both time series
   */
  public static <E> DoubleTimeSeries<E> min(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_minimumOperator);
  }

  public static <E> DoubleTimeSeries<E> min(final DoubleTimeSeries<E> a, final double b) {
    return operate(a, b, s_minimumOperator);
  }

  /**
   * Perform union maximum. If data points are only in one series or the other,
   * those are copied
   * to the resulting series. If data points are in both series, the maximum is
   * chosen.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union maximum of both time series
   */
  public static <E> DoubleTimeSeries<E> unionMax(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_maximumOperator);
  }

  /**
   * Perform intersection maximum. If data points are only in one series or the
   * other, those are excluded
   * from the resulting series. If data points are in both series, the maximum
   * is chosen.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection maximum of both time series
   */
  public static <E> DoubleTimeSeries<E> max(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_maximumOperator);
  }

  public static <E> DoubleTimeSeries<E> max(final DoubleTimeSeries<E> a, final double b) {
    return operate(a, b, s_maximumOperator);
  }

  /**
   * Perform union average. If data points are only in one series or the other,
   * those are copied
   * to the resulting series. If data points are in both series, they are
   * averaged: (a+b)/2.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return union average of both time series
   */
  public static <E> DoubleTimeSeries<E> unionAverage(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return unionOperate(a, b, s_averageOperator);
  }

  /**
   * Perform intersection average. If data points are only in one series or the
   * other, those are excluded
   * from the resulting series. If data points are in both series, they are
   * averaged.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection average of both time series
   */
  public static <E> DoubleTimeSeries<E> average(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_averageOperator);
  }

  /**
   * Perform an intersection of date in a time series, taking the value from the
   * first for each common date.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection first value
   */
  public static <E> DoubleTimeSeries<E> intersectionFirstValues(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_firstOperator);
  }

  /**
   * Perform an intersection of date in a time series, taking the value from the
   * second series (b) for each common date.
   * 
   * @param a
   *          time series
   * @param b
   *          time series
   * @return intersection second value
   */
  public static <E> DoubleTimeSeries<E> intersectionSecondValues(final DoubleTimeSeries<E> a, final DoubleTimeSeries<E> b) {
    return operate(a, b, s_secondOperator);
  }

  /**
   * Negate the value of each data point in the supplied time series
   * 
   * @param a
   *          time series
   * @return the negation of the time series
   */
  public static <E> DoubleTimeSeries<E> negate(final DoubleTimeSeries<E> a) {
    return operate(a, s_negateOperator);
  }

  /**
   * The reciprocal of each data point in the supplied time series
   * 
   * @param a
   *          time series
   * @return the reciprocal of the time series
   */
  public static <E> DoubleTimeSeries<E> reciprocal(final DoubleTimeSeries<E> a) {
    return operate(a, s_reciprocalOperator);
  }

  /**
   * The natural logarithm of each data point in the supplied time series
   * 
   * @param a
   *          time series
   * @return the natural log of the time series
   */
  public static <E> DoubleTimeSeries<E> log(final DoubleTimeSeries<E> a) {
    return operate(a, s_logOperator);
  }

  /**
   * The base-10 logarithm of each data point in the supplied time series
   * 
   * @param a
   *          time series
   * @return the base-10 log of the time series
   */
  public static <E> DoubleTimeSeries<E> log10(final DoubleTimeSeries<E> a) {
    return operate(a, s_log10Operator);
  }

  /**
   * The absolute (positive) value of each data point in the supplied time
   * series
   * 
   * @param a
   *          time series
   * @return the absolute value of the time series
   */
  public static <E> DoubleTimeSeries<E> abs(final DoubleTimeSeries<E> a) {
    return operate(a, s_absOperator);
  }

  /**
   * Find the largest value in the supplied time series
   * 
   * @param a
   *          time series
   * @return the largest value
   */
  public static <E> double maxValue(final DoubleTimeSeries<E> a) {
    if (a == null || a.isEmpty()) {
      throw new OpenGammaRuntimeException("cannot determine maximum value of null or empty DoubleTimeSeries");
    }
    double max = 0.0; // the compiler can't work out that there's at least one
    // element so this is unnecessary...
    for (final Double value : a.values()) {
      max = Math.max(value, max);
    }
    return max;
  }

  /**
   * Find the smallest value in the supplied time series
   * 
   * @param a
   *          time series
   * @return the smallest value
   */
  public static <E> double minValue(final DoubleTimeSeries<E> a) {
    if (a == null || a.isEmpty()) {
      throw new OpenGammaRuntimeException("cannot determine minimum value of null or empty DoubleTimeSeries");
    }
    double min = 0.0; // the compiler can't work out that there's at least one
    // element so this is unnecessary...
    for (final Double value : a.values()) {
      min = Math.min(value, min);
    }
    return min;
  }

  /**
   * shift the values in a series to neighboring data points by a particular
   * offset.
   * 
   * @param a
   *          time series
   * @param lag
   *          the number of points to shift along
   * @return the resulting time series
   */
  @SuppressWarnings("unchecked")
  public static <E> DoubleTimeSeries<E> lag(final DoubleTimeSeries<E> a, final int lag) {
    // TODO deal with going forward in time
    if (lag >= a.size())
      throw new IllegalArgumentException("Lag must be less than series size");
    if (lag == 0)
      return a;
    final List<E> dates = (List<E>) new ArrayList<Object>();
    final List<Double> data = new ArrayList<Double>();
    final Iterator<E> timeIter = a.timeIterator();
    final Iterator<Double> dataIter = a.valuesIterator();
    for (int i = 0; i < a.size(); i++) {
      if (i < lag) {
        timeIter.next();
      } else {
        dates.add(timeIter.next());
        data.add(dataIter.next());
      }
    }
    return (DoubleTimeSeries<E>) a.newInstance((E[]) dates.toArray(), (Double[]) data.toArray());
  }
}
