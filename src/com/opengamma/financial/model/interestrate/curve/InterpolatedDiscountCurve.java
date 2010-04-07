/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 * @author emcleod
 */
public class InterpolatedDiscountCurve extends DiscountCurve implements Serializable {
  private static final String INTERPOLATOR_FIELD_NAME = "interpolator";
  private static final String RATE_DATA_FIELD_NAME = "rateData";
  private static final String DF_DATA_FIELD_NAME = "dfData";
  private static final Logger s_Log = LoggerFactory.getLogger(InterpolatedDiscountCurve.class);
  private final SortedMap<Double, Double> _rateData;
  private final SortedMap<Double, Double> _dfData;
  private final Interpolator1D _interpolator;

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and interest rates
   *          in percent (e.g. 3% = 0.03)
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This can be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedDiscountCurve(final Map<Double, Double> data, final Interpolator1D interpolator) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (interpolator == null)
      throw new IllegalArgumentException("Interpolator was null");
    if (data.size() < 2)
      throw new IllegalArgumentException("Need to have at least two data points for an interpolated curve");
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
    _interpolator = interpolator;
  }

  protected InterpolatedDiscountCurve(final SortedMap<Double, Double> sortedRates, final SortedMap<Double, Double> sortedDF, final Interpolator1D interpolator) {
    _rateData = Collections.<Double, Double> unmodifiableSortedMap(sortedRates);
    _dfData = Collections.<Double, Double> unmodifiableSortedMap(sortedDF);
    _interpolator = interpolator;
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
  public Interpolator1D getInterpolator() {
    return _interpolator;
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
    return _interpolator.interpolate(_dfData, t).getResult();
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
    return new InterpolatedDiscountCurve(map, getInterpolator());
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
      return new InterpolatedDiscountCurve(map, getInterpolator());
    }
    map.put(t, getInterestRate(t) + shift);
    return new InterpolatedDiscountCurve(map, getInterpolator());
  }

  @Override
  public DiscountCurve withMultipleShifts(final Map<Double, Double> shifts) {
    if (shifts == null)
      throw new IllegalArgumentException("Shift map was null");
    if (shifts.isEmpty()) {
      s_Log.info("Shift map was empty; returning identical curve");
      return new InterpolatedDiscountCurve(getData(), getInterpolator());
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
    return new InterpolatedDiscountCurve(map, getInterpolator());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_rateData == null ? 0 : _rateData.hashCode());
    result = prime * result + (_interpolator == null ? 0 : _interpolator.hashCode());
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
    if (_interpolator == null) {
      if (other._interpolator != null)
        return false;
    } else if (!_interpolator.equals(other._interpolator))
      return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("InterpolatedDiscountCurve[");
    sb.append("interpolator=").append(Interpolator1DFactory.getInterpolatorName(getInterpolator())).append(',');
    sb.append("rate_data={");
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
    message.add(INTERPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(getInterpolator()));
    context.objectToFudgeMsg(message, RATE_DATA_FIELD_NAME, null, _rateData);
    context.objectToFudgeMsg(message, DF_DATA_FIELD_NAME, null, _dfData);
    return message;
  }

  @SuppressWarnings("unchecked")
  public static InterpolatedDiscountCurve fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(message.getString(INTERPOLATOR_FIELD_NAME));
    final SortedMap<Double, Double> rateData = new TreeMap<Double, Double>(context.fieldValueToObject(Map.class, message.getByName(RATE_DATA_FIELD_NAME)));
    final SortedMap<Double, Double> dfData = new TreeMap<Double, Double>(context.fieldValueToObject(Map.class, message.getByName(DF_DATA_FIELD_NAME)));
    return new InterpolatedDiscountCurve(rateData, dfData, interpolator);
  }

}
