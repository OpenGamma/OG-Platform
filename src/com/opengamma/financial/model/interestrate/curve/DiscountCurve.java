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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_data == null) ? 0 : _data.hashCode());
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    result = prime * result + ((_interpolator == null) ? 0 : _interpolator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DiscountCurve other = (DiscountCurve) obj;
    if (_data == null) {
      if (other._data != null)
        return false;
    } else if (!_data.equals(other._data))
      return false;
    if (_date == null) {
      if (other._date != null)
        return false;
    } else if (!_date.equals(other._date))
      return false;
    if (_interpolator == null) {
      if (other._interpolator != null)
        return false;
    } else if (!_interpolator.equals(other._interpolator))
      return false;
    return true;
  }
}
