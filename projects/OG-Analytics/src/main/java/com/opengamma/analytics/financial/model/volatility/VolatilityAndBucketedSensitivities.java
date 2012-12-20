/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * Class containing a volatility calculated at a particular x and y and the bucketed sensitivities of this value to the volatility data points that were used to
 * construct the surface.
 */
public class VolatilityAndBucketedSensitivities {
  private final double _volatility;
  private final double[][] _sensitivities;

  /**
   * @param volatility The volatility
   * @param sensitivities The bucketed sensitivities, not null
   */
  public VolatilityAndBucketedSensitivities(final double volatility, final double[][] sensitivities) {
    ArgumentChecker.notNull(sensitivities, "sensitivities");
    _volatility = volatility;
    _sensitivities = sensitivities;
  }

  /**
   * Gets the volatility
   * @return The volatility
   */
  public double getVolatility() {
    return _volatility;
  }

  /**
   * Gets the bucketed sensitivities
   * @return The bucketed sensitivities
   */
  public double[][] getBucketedSensitivities() {
    return _sensitivities;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(_sensitivities);
    long temp;
    temp = Double.doubleToLongBits(_volatility);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VolatilityAndBucketedSensitivities other = (VolatilityAndBucketedSensitivities) obj;
    if (!Arrays.deepEquals(_sensitivities, other._sensitivities)) {
      return false;
    }
    if (Double.doubleToLongBits(_volatility) != Double.doubleToLongBits(other._volatility)) {
      return false;
    }
    return true;
  }

}
