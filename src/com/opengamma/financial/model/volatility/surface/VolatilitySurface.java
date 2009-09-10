package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.FirstThenSecondPairComparator;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */

public class VolatilitySurface implements VolatilityModel<Double, Double> {
  private final Date _date;
  private final SortedMap<Pair<Double, Double>, Double> _data;
  private final Interpolator2D _interpolator;

  public VolatilitySurface(Date date, Map<Pair<Double, Double>, Double> data, Interpolator2D interpolator) {
    SortedMap<Pair<Double, Double>, Double> sorted = new TreeMap<Pair<Double, Double>, Double>(new FirstThenSecondPairComparator<Double, Double>());
    sorted.putAll(data);
    _date = date;
    _data = Collections.<Pair<Double, Double>, Double> unmodifiableSortedMap(sorted);
    _interpolator = interpolator;
  }

  public SortedMap<Pair<Double, Double>, Double> getData() {
    return _data;
  }

  public Date getDate() {
    return _date;
  }

  public Interpolator2D getInterpolator() {
    return _interpolator;
  }

  @Override
  public double getVolatility(Double x, Double y) throws InterpolationException {
    return _interpolator.interpolate(_data, new Pair<Double, Double>(x, y)).getResult();
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
    VolatilitySurface other = (VolatilitySurface) obj;
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
