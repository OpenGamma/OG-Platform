/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.surface.Surface;

/**
 *  Class describing the Black volatility surface used in inflation year on year cap/floor modeling. The inflation year on year rate (\{CPI(T_{i+1})}{CPI(T_{i})})rate is assumed to be normal.
 */
public class BlackSmileCapInflationYearOnYearParameters implements VolatilityModel<double[]> {

  /**
   * The volatility surface. The dimensions are the expiration and the strike. Not null.
   */
  private final Surface<Double, Double, Double> _volatility;
  /**
   * The index price for which the volatility is valid. Not null.
   */
  private final IndexPrice _index;

  /**
   * Constructor from the parameter surfaces. 
   * @param volatility The Black volatility curve.
   * @param index The index price for which the volatility is valid.
   */
  public BlackSmileCapInflationYearOnYearParameters(final Surface<Double, Double, Double> volatility, final IndexPrice index) {
    Validate.notNull(volatility, "volatility curve");
    Validate.notNull(index, "index price");
    _volatility = volatility;
    _index = index;
  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @param expiration The time to expiration.
   * @param strike The strike.
   * @return The volatility.
   */
  public double getVolatility(final double expiration, final double strike) {
    return _volatility.getZValue(expiration, strike);
  }

  @Override
  /**
   * Return the volatility for a expiration tenor array.
   * @param data An array of one doubles with the expiration.
   * @return The volatility.
   */
  public Double getVolatility(final double[] data) {
    Validate.notNull(data, "data");
    Validate.isTrue(data.length == 2, "data should have two components (expiration and strike)");
    return getVolatility(data[0], data[1]);
  }

  /**
   * Gets the Ibor index for which the volatility is valid.
   * @return The index.
   */
  public IndexPrice getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _index.hashCode();
    result = prime * result + _volatility.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BlackSmileCapInflationYearOnYearParameters other = (BlackSmileCapInflationYearOnYearParameters) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
