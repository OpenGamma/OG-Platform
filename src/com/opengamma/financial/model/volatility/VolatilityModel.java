package com.opengamma.financial.model.volatility;

import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 * 
 */

public interface VolatilityModel<T, U> {

  public double getVolatility(T x, U y) throws InterpolationException;
}
