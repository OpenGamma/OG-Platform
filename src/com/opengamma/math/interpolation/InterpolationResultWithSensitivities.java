/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Arrays;

/**
 * 
 */
public class InterpolationResultWithSensitivities extends InterpolationResult {
  private final double[] _sensitivities;

  public InterpolationResultWithSensitivities(final double result, final double[] sensitivities) {
    super(result);
    _sensitivities = sensitivities;
  }

  public double[] getSensitivities() {
    return _sensitivities;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_sensitivities);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterpolationResultWithSensitivities other = (InterpolationResultWithSensitivities) obj;
    if (!Arrays.equals(_sensitivities, other._sensitivities)) {
      return false;
    }
    return true;
  }

}
