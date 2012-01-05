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
public class StudentTLinearVaRCalculator<T> implements VaRCalculator<StudentTVaRParameters, T> {
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;

  public StudentTLinearVaRCalculator(final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator) {
    Validate.notNull(meanCalculator, "mean calculator");
    Validate.notNull(stdCalculator, "standard deviation calculator");
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
  }

  @Override
  public Double evaluate(final StudentTVaRParameters parameters, final T... data) {
    Validate.notNull(parameters, "parameters");
    Validate.notNull(data, "data");
    return parameters.getMult() * _stdCalculator.evaluate(data) - parameters.getScale() * _meanCalculator.evaluate(data);
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStandardDeviationCalculator() {
    return _stdCalculator;
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
    final StudentTLinearVaRCalculator<?> other = (StudentTLinearVaRCalculator<?>) obj;
    return ObjectUtils.equals(_meanCalculator, other._meanCalculator) && ObjectUtils.equals(_stdCalculator, other._stdCalculator);
  }

}
