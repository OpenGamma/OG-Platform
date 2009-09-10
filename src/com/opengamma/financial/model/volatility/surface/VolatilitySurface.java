package com.opengamma.financial.model.volatility.surface;

import java.util.Date;
import java.util.Map;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */

public class VolatilitySurface implements VolatilityModel<Double, Double> {
  private final Date _date;
  private final Map<Pair<Double, Double>, Double> _data;
  private final Interpolator2D _interpolator;

  public VolatilitySurface(Date date, Map<Pair<Double, Double>, Double> data, Interpolator2D interpolator) {
    _date = date;
    _data = data;
    _interpolator = interpolator;
  }

  public Map<Pair<Double, Double>, Double> getData() {
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

}
