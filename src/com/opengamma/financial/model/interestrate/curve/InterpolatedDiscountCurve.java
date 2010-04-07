/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 * @author emcleod
 */
public class InterpolatedDiscountCurve extends DiscountCurve {
  private static final String INTERPOLATOR_FIELD_NAME = "interpolator";
  private static final String RATE_DATA_FIELD_NAME = "rateData";
  private static final String DF_DATA_FIELD_NAME = "dfData";
  private static final Logger s_Log = LoggerFactory.getLogger(InterpolatedDiscountCurve.class);
  private final SortedMap<Double, Double> _rateData;
  private final SortedMap<Double, Double> _dfData;
  private final SortedMap<Double, Interpolator1D> _interpolators;

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and interest rates
   *          in percent (e.g. 3% = 0.03)
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This cannot be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedDiscountCurve(final Map<Double, Double> data, final Interpolator1D interpolator) {
    this(data, Collections.<Double, Interpolator1D> singletonMap(Double.POSITIVE_INFINITY, interpolator));
  }

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and interest rates
   *          in percent (e.g. 3% = 0.03)
   * @param interpolators
   *          A map of times and interpolators. This allows different interpolators 
   *          to be used for different regions of the curve. The time value is the 
   *          maximum time in years for which an interpolator is valid.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedDiscountCurve(final Map<Double, Double> data, final Map<Double, Interpolator1D> interpolators) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (interpolators == null)
      throw new IllegalArgumentException("Interpolator was null");
    if (interpolators.size() == 0)
      throw new IllegalArgumentException("Interpolator map did not contain values");
    if (data.size() < 2)
      throw new IllegalArgumentException("Need to have at least two data points for an interpolated curve");
    for (final Map.Entry<Double, Interpolator1D> entry : interpolators.entrySet()) {
      if (entry.getValue() == null)
        throw new IllegalArgumentException("Interpolator for time " + entry.getKey() + " was null");
    }
    final SortedMap<Double, Double> sortedRates = new TreeMap<Double, Double>();
    final SortedMap<Double, Double> sortedDF = new TreeMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      if (entry.getKey() < 0)
        throw new IllegalArgumentException("Cannot have negative time in a discount curve");
      sortedRates.put(entry.getKey(), entry.getValue());
      sortedDF.put(entry.getKey(), Math.exp(-entry.getValue() * entry.getKey()));
    }
    _rateData = Collections.<Double, Double> unmodifiableSortedMap(sortedRates);
    _dfData = Collections.<Double, Double> unmodifiableSortedMap(sortedDF);
    _interpolators = Collections.<Double, Interpolator1D> unmodifiableSortedMap(new TreeMap<Double, Interpolator1D>(interpolators));
  }

  protected InterpolatedDiscountCurve(final SortedMap<Double, Double> sortedRates, final SortedMap<Double, Double> sortedDF, final Interpolator1D interpolator) {
    _rateData = Collections.<Double, Double> unmodifiableSortedMap(sortedRates);
    _dfData = Collections.<Double, Double> unmodifiableSortedMap(sortedDF);
    final SortedMap<Double, Interpolator1D> sorted = new TreeMap<Double, Interpolator1D>();
    sorted.put(Double.POSITIVE_INFINITY, interpolator);
    _interpolators = Collections.<Double, Interpolator1D> unmodifiableSortedMap(sorted);
  }

  protected InterpolatedDiscountCurve(final SortedMap<Double, Double> sortedRates, final SortedMap<Double, Double> sortedDF, final SortedMap<Double, Interpolator1D> interpolators) {
    _rateData = Collections.<Double, Double> unmodifiableSortedMap(sortedRates);
    _dfData = Collections.<Double, Double> unmodifiableSortedMap(sortedDF);
    _interpolators = Collections.<Double, Interpolator1D> unmodifiableSortedMap(interpolators);
  }

  /**
   * 
   * @return The data sorted by maturity. Note that these are discount factors,
   *         not rates.
   */
  public SortedMap<Double, Double> getData() {
    return _rateData;
  }

  /**
   * 
   * @return The interpolator for this curve.
   */
  public Map<Double, Interpolator1D> getInterpolators() {
    return _interpolators;
  }

  /**
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public double getInterestRate(final Double t) {
    if (t == null)
      throw new IllegalArgumentException("t was null");
    if (t < 0)
      throw new IllegalArgumentException("Cannot have a negative time in a DiscountCurve: provided " + t);
    return -Math.log(getDiscountFactor(t)) / t;
  }

  /**
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public double getDiscountFactor(final Double t) {
    if (t == null)
      throw new IllegalArgumentException("t was null");
    if (t < 0)
      throw new IllegalArgumentException("Cannot have a negative time in a DiscountCurve: provided " + t);
    final Double key = _interpolators.headMap(t).lastKey();
    return _interpolators.get(key).interpolate(_dfData, t).getResult();
  }

  @Override
  public Set<Double> getMaturities() {
    return getData().keySet();
  }

  @Override
  public DiscountCurve withParallelShift(final Double shift) {
    if (shift == null)
      throw new IllegalArgumentException("Shift was null");
    final Map<Double, Double> map = new HashMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : _rateData.entrySet()) {
      map.put(entry.getKey(), entry.getValue() + shift);
    }
    return new InterpolatedDiscountCurve(map, getInterpolators());
  }

  @Override
  public DiscountCurve withSingleShift(final Double t, final Double shift) {
    if (t == null)
      throw new IllegalArgumentException("t was null");
    if (t < 0)
      throw new IllegalArgumentException("t was negative");
    if (shift == null)
      throw new IllegalArgumentException("Shift was null");
    final Map<Double, Double> data = getData();
    final Map<Double, Double> map = new HashMap<Double, Double>(data);
    if (data.containsKey(t)) {
      map.put(t, data.get(t) + shift);
      return new InterpolatedDiscountCurve(map, getInterpolators());
    }
    map.put(t, getInterestRate(t) + shift);
    return new InterpolatedDiscountCurve(map, getInterpolators());
  }

  @Override
  public DiscountCurve withMultipleShifts(final Map<Double, Double> shifts) {
    if (shifts == null)
      throw new IllegalArgumentException("Shift map was null");
    if (shifts.isEmpty()) {
      s_Log.info("Shift map was empty; returning identical curve");
      return new InterpolatedDiscountCurve(getData(), getInterpolators());
    }
    final Map<Double, Double> data = getData();
    final Map<Double, Double> map = new HashMap<Double, Double>(data);
    for (final Map.Entry<Double, Double> entry : shifts.entrySet()) {
      if (entry.getValue() == null)
        throw new IllegalArgumentException("Null shift in shift map");
      if (entry.getKey() < 0)
        throw new IllegalArgumentException("Negative time in shift map");
      if (data.containsKey(entry.getKey())) {
        map.put(entry.getKey(), data.get(entry.getKey()) + entry.getValue());
      } else {
        map.put(entry.getKey(), getInterestRate(entry.getKey()) + entry.getValue());
      }
    }
    return new InterpolatedDiscountCurve(map, getInterpolators());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_rateData == null ? 0 : _rateData.hashCode());
    result = prime * result + (_interpolators == null ? 0 : _interpolators.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final InterpolatedDiscountCurve other = (InterpolatedDiscountCurve) obj;
    if (_rateData == null) {
      if (other._rateData != null)
        return false;
    } else if (!_rateData.equals(other._rateData))
      return false;
    if (_interpolators == null) {
      if (other._interpolators != null)
        return false;
    } else if (!_interpolators.equals(other._interpolators))
      return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("InterpolatedDiscountCurve[");
    sb.append("interpolators={");// .append(Interpolator1DFactory.getInterpolatorName(getInterpolators())).append(',');
    for (final Map.Entry<Double, Interpolator1D> e : _interpolators.entrySet()) {
      sb.append(e.getKey()).append('=').append(Interpolator1DFactory.getInterpolatorName(e.getValue())).append(',');
    }
    sb.append("},rate_data={");
    for (final Map.Entry<Double, Double> e : _rateData.entrySet()) {
      sb.append(e.getKey()).append('=').append(e.getValue()).append(',');
    }
    sb.append("},df_data={");
    for (final Map.Entry<Double, Double> e : _dfData.entrySet()) {
      sb.append(e.getKey()).append('=').append(e.getValue()).append(',');
    }
    sb.append("}]");
    return sb.toString();
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(null, 0, getClass().getName());
    message.add(INTERPOLATOR_FIELD_NAME, encodeDoubleInterpolator1DMap(context, _interpolators));
    message.add(RATE_DATA_FIELD_NAME, encodeDoubleDoubleMap(context, _rateData));
    message.add(DF_DATA_FIELD_NAME, encodeDoubleDoubleMap(context, _dfData));
    return message;
  }

  public static InterpolatedDiscountCurve fromFudgeMsg(final FudgeFieldContainer message) {
    final SortedMap<Double, Double> rateData = decodeSortedDoubleDoubleMap(message.getMessage(RATE_DATA_FIELD_NAME));
    final SortedMap<Double, Double> dfData = decodeSortedDoubleDoubleMap(message.getMessage(DF_DATA_FIELD_NAME));
    final SortedMap<Double, Interpolator1D> interpolators = decodeSortedDoubleInterpolator1DMap(message.getMessage(INTERPOLATOR_FIELD_NAME));
    return new InterpolatedDiscountCurve(rateData, dfData, interpolators);
  }

  // REVIEW kirk 2010-03-31 -- These probably belong in a utility class
  // methinks.
  // TODO 2010-04-06 Andrew -- Use the FSC/FDC methods for automatic map
  // encoding
  public static MutableFudgeFieldContainer encodeDoubleDoubleMap(final FudgeSerializationContext context, final Map<Double, Double> data) {
    final MutableFudgeFieldContainer message = context.newMessage();
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      message.add("key", entry.getKey());
      message.add("value", entry.getValue());
    }
    return message;
  }

  public static MutableFudgeFieldContainer encodeDoubleInterpolator1DMap(final FudgeSerializationContext context, final Map<Double, Interpolator1D> data) {
    final MutableFudgeFieldContainer message = context.newMessage();
    for (final Map.Entry<Double, Interpolator1D> entry : data.entrySet()) {
      message.add("key", entry.getKey());
      message.add("value", Interpolator1DFactory.getInterpolatorName(entry.getValue()));
    }
    return message;
  }

  public static SortedMap<Double, Double> decodeSortedDoubleDoubleMap(final FudgeFieldContainer msg) {
    final SortedMap<Double, Double> result = new TreeMap<Double, Double>();
    final Iterator<FudgeField> keyIter = msg.getAllByName("key").iterator();
    final Iterator<FudgeField> valueIter = msg.getAllByName("value").iterator();
    while (keyIter.hasNext()) {
      assert valueIter.hasNext();
      final FudgeField keyField = keyIter.next();
      final FudgeField valueField = valueIter.next();
      result.put((Double) keyField.getValue(), (Double) valueField.getValue());
    }
    return result;
  }

  public static SortedMap<Double, Interpolator1D> decodeSortedDoubleInterpolator1DMap(final FudgeFieldContainer msg) {
    final SortedMap<Double, Interpolator1D> result = new TreeMap<Double, Interpolator1D>();
    final Iterator<FudgeField> keyIter = msg.getAllByName("key").iterator();
    final Iterator<FudgeField> valueIter = msg.getAllByName("value").iterator();
    FudgeField keyField, valueField;
    while (keyIter.hasNext()) {
      assert valueIter.hasNext();
      keyField = keyIter.next();
      valueField = valueIter.next();
      result.put((Double) keyField.getValue(), Interpolator1DFactory.getInterpolator((String) valueField.getValue()));
    }
    return result;
  }
}
