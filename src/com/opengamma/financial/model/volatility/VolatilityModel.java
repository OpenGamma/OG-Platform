package com.opengamma.financial.model.volatility;

/**
 * 
 * @author emcleod
 * 
 */

public interface VolatilityModel<T, U> {

  public double getVolatility(T x, U y);
}
