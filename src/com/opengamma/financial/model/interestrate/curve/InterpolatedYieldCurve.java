/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

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
public class InterpolatedYieldCurve extends InterpolatedYieldAndDiscountCurve {
  private static final Logger s_logger = LoggerFactory.getLogger(InterpolatedYieldCurve.class);

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and rates as decimals (i.e. 4% = 0.04)
   * @param interpolator
   *          An interpolator to get interest rates / discount factors for
   *          maturities that fall in between nodes. This cannot be null.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty, or if it contains a
   *           negative time to maturity.
   */
  public InterpolatedYieldCurve(final Map<Double, Double> data,
      final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    super(data, interpolator);
  }

  /**
   * 
   * @param data
   *          A map containing pairs of maturities in years and interest rates
   *          in percent (e.g. 3% = 0.03)
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
  public InterpolatedYieldCurve(final Map<Double, Double> data,
      final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>> interpolators) {
    super(data, interpolators);
  }

  /**
   * 
   * @param t Time in years
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @SuppressWarnings("unchecked")
  @Override
  public double getInterestRate(final Double t) {
    Validate.notNull(t);
    ArgumentChecker.notNegative(t, "time");
    if (getInterpolators().size() == 1) {
      final Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResult> interpolator =
          (Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResult>) getInterpolators().values().iterator().next();
      return interpolator.interpolate(getDataBundles().values().iterator().next(), t).getResult();
    }
    final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>> tail = getInterpolators().tailMap(t);
    final Double key = tail.isEmpty() ? getInterpolators().lastKey() : getInterpolators().tailMap(t).firstKey();
    final Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResult> interpolator =
        (Interpolator1D<Interpolator1DDataBundle, InterpolationResult>) getInterpolators().get(key);
    return interpolator.interpolate(getDataBundles().get(key), t).getResult();
  }

  /**
   * 
   * @param t The time in years
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public double getDiscountFactor(final Double t) {
    Validate.notNull(t);
    ArgumentChecker.notNegative(t, "time");
    return Math.exp(-getInterestRate(t) * t);
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
    return new InterpolatedYieldCurve(map, getInterpolators());
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
      return new InterpolatedYieldCurve(map, getInterpolators());
    }
    map.put(t, getInterestRate(t) + shift);
    return new InterpolatedYieldCurve(map, getInterpolators());
  }

  @Override
  public YieldAndDiscountCurve withMultipleShifts(final Map<Double, Double> shifts) {
    Validate.notNull(shifts);
    if (shifts.isEmpty()) {
      s_logger.info("Shift map was empty; returning identical curve");
      return new InterpolatedYieldCurve(getData(), getInterpolators());
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
        map.put(entry.getKey(), getInterestRate(entry.getKey()) + entry.getValue());
      }
    }
    return new InterpolatedYieldCurve(map, getInterpolators());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("InterpolatedYieldCurve[");
    sb.append("interpolators={");
    for (final Map.Entry<Double, Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>> e : getInterpolators().entrySet()) {
      sb.append(e.getKey()).append('=').append(Interpolator1DFactory.getInterpolatorName(e.getValue())).append(',');
    }
    sb.append("},rate_data={");
    for (final Map.Entry<Double, Double> e : getData().entrySet()) {
      sb.append(e.getKey()).append('=').append(e.getValue()).append(',');
    }
    sb.append("}]");
    return sb.toString();
  }
}
