package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;
import java.util.Date;

import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */
public class ConstantVolatilitySurface extends VolatilitySurface {
  private final double _sigma;

  public ConstantVolatilitySurface(Date date, Double sigma) {
    super(date, Collections.<Pair<Double, Double>, Double> singletonMap(new Pair<Double, Double>(0., 0.), sigma), null);
    _sigma = sigma;
  }

  @Override
  public Interpolator2D getInterpolator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getVolatility(Double x, Double y) {
    return _sigma;
  }
}
