/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.ArgumentChecker;

/**
 *
 * @param <T> The type of the data
 */
public class NormalLinearVaRCalculator<T> implements VaRCalculator<NormalVaRParameters, T> {
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;

  public NormalLinearVaRCalculator(final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator) {
    ArgumentChecker.notNull(meanCalculator, "mean calculator");
    ArgumentChecker.notNull(stdCalculator, "standard deviation calculator");
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStandardDeviationCalculator() {
    return _stdCalculator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public VaRCalculationResult evaluate(final NormalVaRParameters parameters, final T... data) {
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(data, "data");
    final double z = parameters.getZ();
    final double mult = parameters.getTimeScaling();
    final double mean = _meanCalculator.evaluate(data);
    final double stddev = _stdCalculator.evaluate(data);
    final double result = z * mult * stddev - mult * mult * mean;
    return new VaRCalculationResult(result, stddev);
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
