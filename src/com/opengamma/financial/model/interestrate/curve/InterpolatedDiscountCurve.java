/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterpolatedDiscountCurve extends InterpolatedYieldAndDiscountCurve implements Serializable {
  private static final Logger s_logger = LoggerFactory.getLogger(InterpolatedDiscountCurve.class);

  /**
   * 
   * @param t
   *          An array containing the time in years to maturity
   * @param df
   *          An array containing the discount factors
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This cannot be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedDiscountCurve(final double[] t, final double[] df, final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    super(t, df, interpolator);
  }

  /**
   * 
   * @param t
   *          An array containing the time in years to maturity
   * @param df
   *          An array containing the discount factors
   * @param interpolators
   *          A map of times and interpolators. This allows different
   *          interpolators
   *          to be used for different regions of the curve. The time value is
   *          the
   *          maximum time in years for which an interpolator is valid.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedDiscountCurve(final double[] t, final double[] df, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>> interpolators) {
    super(t, df, interpolators);
  }

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and discount factors
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This cannot be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedDiscountCurve(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    super(data, interpolator);
  }

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and discount factors
   * @param interpolators
   *          A map of times and interpolators. This allows different
   *          interpolators
   *          to be used for different regions of the curve. The time value is
   *          the
   *          maximum time in years for which an interpolator is valid.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedDiscountCurve(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>> interpolators) {
    super(data, interpolators);
  }

  /**
   * 
   * @param t Time in years
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public double getInterestRate(final Double t) {
    Validate.notNull(t);
    ArgumentChecker.notNegative(t, "time");
    return -Math.log(getDiscountFactor(t)) / t;
  }

  /**
   * 
   * @param t The time in years
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @SuppressWarnings("unchecked")
  @Override
  public double getDiscountFactor(final Double t) {
    Validate.notNull(t);
    ArgumentChecker.notNegative(t, "time");
    if (getInterpolators().size() == 1) {
      final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> interpolator = (Interpolator1D<Interpolator1DDataBundle, InterpolationResult>) getInterpolators().values().iterator().next();
      return interpolator.interpolate(getDataBundles().values().iterator().next(), t).getResult();
    }
    final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>> tail = getInterpolators().tailMap(t);
    final Double key = tail.isEmpty() ? getInterpolators().lastKey() : getInterpolators().tailMap(t).firstKey();
    final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> interpolator = (Interpolator1D<Interpolator1DDataBundle, InterpolationResult>) getInterpolators().get(key);
    return interpolator.interpolate(getDataBundles().get(key), t).getResult();
  }

  @Override
  public Set<Double> getMaturities() {
    return getData().keySet();
  }

  @Override
  public YieldAndDiscountCurve withParallelShift(final Double shift) {
    Validate.notNull(shift);
    final Map<Double, Double> map = new HashMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : getData().entrySet()) {
      map.put(entry.getKey(), entry.getValue() + shift);
    }
    return new InterpolatedDiscountCurve(map, getInterpolators());
  }

  @Override
  public YieldAndDiscountCurve withSingleShift(final Double t, final Double shift) {
    Validate.notNull(t);
    Validate.notNull(shift);
    ArgumentChecker.notNegative(t, "time");
    final Map<Double, Double> data = getData();
    final Map<Double, Double> map = new HashMap<Double, Double>(data);
    if (data.containsKey(t)) {
      map.put(t, data.get(t) + shift);
      return new InterpolatedDiscountCurve(map, getInterpolators());
    }
    map.put(t, getDiscountFactor(t) + shift);
    return new InterpolatedDiscountCurve(map, getInterpolators());
  }

  @Override
  public YieldAndDiscountCurve withMultipleShifts(final Map<Double, Double> shifts) {
    Validate.notNull(shifts);
    if (shifts.isEmpty()) {
      s_logger.info("Shift map was empty; returning identical curve");
      return new InterpolatedDiscountCurve(getData(), getInterpolators());
    }
    final Map<Double, Double> data = getData();
    final Map<Double, Double> map = new HashMap<Double, Double>(data);
    for (final Map.Entry<Double, Double> entry : shifts.entrySet()) {
      Validate.notNull(entry.getKey());
      Validate.notNull(entry.getValue());
      ArgumentChecker.notNegative(entry.getKey(), "time");
      if (data.containsKey(entry.getKey())) {
        map.put(entry.getKey(), data.get(entry.getKey()) + entry.getValue());
      } else {
        map.put(entry.getKey(), getDiscountFactor(entry.getKey()) + entry.getValue());
      }
    }
    return new InterpolatedDiscountCurve(map, getInterpolators());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("InterpolatedDiscountCurve[");
    sb.append("interpolators={");
    for (final Map.Entry<Double, Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>> e : getInterpolators().entrySet()) {
      sb.append(e.getKey()).append('=').append(Interpolator1DFactory.getInterpolatorName(e.getValue())).append(',');
    }
    sb.append("},df_data={");
    for (final Map.Entry<Double, Double> e : getData().entrySet()) {
      sb.append(e.getKey()).append('=').append(e.getValue()).append(',');
    }
    sb.append("}]");
    return sb.toString();
  }

}
