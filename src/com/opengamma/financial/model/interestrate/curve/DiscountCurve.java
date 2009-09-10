package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.interpolation.Interpolator1D;

/**
 * 
 * @author emcleod
 * 
 */

public class DiscountCurve implements InterestRateModel<Double> {
  private final SortedMap<Double, Double> _data;
  private final Interpolator1D _interpolator;
  private final Date _date;

  public DiscountCurve(Date date, Map<Double, Double> data, Interpolator1D interpolator) {
    SortedMap<Double, Double> sorted = new TreeMap<Double, Double>(data);
    if (sorted.firstKey() < 0)
      throw new IllegalArgumentException("Cannot have negative time in a discount curve");
    _date = date;
    _data = Collections.<Double, Double> unmodifiableSortedMap(sorted);
    _interpolator = interpolator;
  }

  public SortedMap<Double, Double> getData() {
    return _data;
  }

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  public Date getDate() {
    return _date;
  }

  @Override
  public double getInterestRate(Double t) throws InterpolationException {
    return _interpolator.interpolate(_data, t).getResult();
  }

  public double getDiscountFactor(Double t) throws InterpolationException {
    return Math.exp(-getInterestRate(t) * t);
  }
}
