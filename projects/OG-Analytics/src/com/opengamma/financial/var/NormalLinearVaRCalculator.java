/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;

/**
 * 
 * @param <T> The type of the data
 */
public class NormalLinearVaRCalculator<T> implements VaRCalculator<NormalVaRParameters, T> {
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;

  public NormalLinearVaRCalculator(final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator) {
    Validate.notNull(meanCalculator, "mean calculator");
    Validate.notNull(stdCalculator, "standard deviation calculator");
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStandardDeviationCalculator() {
    return _stdCalculator;
  }

  @Override
  public Double evaluate(final NormalVaRParameters parameters, final T... data) {
    Validate.notNull(parameters, "parameters");
    Validate.notNull(data, "data");
    double z = parameters.getZ();
    double mult = parameters.getTimeScaling();
    return z * mult * _stdCalculator.evaluate(data) - mult * mult * _meanCalculator.evaluate(data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _meanCalculator.hashCode();
    result = prime * result + _stdCalculator.hashCode();
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
    final NormalLinearVaRCalculator<?> other = (NormalLinearVaRCalculator<?>) obj;
    if (!ObjectUtils.equals(_meanCalculator, other._meanCalculator)) {
      return false;
    }
    if (!ObjectUtils.equals(_stdCalculator, other._stdCalculator)) {
      return false;
    }
    return true;
  }

}
