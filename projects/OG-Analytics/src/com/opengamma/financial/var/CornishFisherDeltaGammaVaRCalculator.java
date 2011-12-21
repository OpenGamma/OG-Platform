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
public class CornishFisherDeltaGammaVaRCalculator<T> implements VaRCalculator<NormalVaRParameters, T> {
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;
  private final Function<T, Double> _skewCalculator;
  private final Function<T, Double> _kurtosisCalculator;

  public CornishFisherDeltaGammaVaRCalculator(final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator,
      final Function<T, Double> skewCalculator, final Function<T, Double> kurtosisCalculator) {
    Validate.notNull(meanCalculator, "mean calculator");
    Validate.notNull(stdCalculator, "standard deviation calculator");
    Validate.notNull(skewCalculator, "skew calculator");
    Validate.notNull(kurtosisCalculator, "kurtosis calculator");
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
    _skewCalculator = skewCalculator;
    _kurtosisCalculator = kurtosisCalculator;
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStandardDeviationCalculator() {
    return _stdCalculator;
  }

  public Function<T, Double> getSkewCalculator() {
    return _skewCalculator;
  }

  public Function<T, Double> getKurtosisCalculator() {
    return _kurtosisCalculator;
  }

  @Override
  public Double evaluate(final NormalVaRParameters parameters, final T... data) {
    Validate.notNull(parameters, "parameters");
    Validate.notNull(data, "data");
    final double z = parameters.getZ();
    final double mult = parameters.getTimeScaling();
    final double zSq = z * z;
    final double mean = _meanCalculator.evaluate(data);
    final double std = _stdCalculator.evaluate(data);
    final double skew = _skewCalculator.evaluate(data);
    final double kurtosis = _kurtosisCalculator.evaluate(data);
    final double x = z + skew * (zSq - 1) / 6. + kurtosis * z * (zSq - 3) / 24. - skew * skew * z * (2 * zSq - 5) / 36.;
    return x * std * mult + mean * mult * mult;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _kurtosisCalculator.hashCode();
    result = prime * result + _meanCalculator.hashCode();
    result = prime * result + _skewCalculator.hashCode();
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
    final CornishFisherDeltaGammaVaRCalculator<?> other = (CornishFisherDeltaGammaVaRCalculator<?>) obj;
    if (!ObjectUtils.equals(_kurtosisCalculator, other._kurtosisCalculator)) {
      return false;
    }
    if (!ObjectUtils.equals(_meanCalculator, other._meanCalculator)) {
      return false;
    }
    if (!ObjectUtils.equals(_skewCalculator, other._skewCalculator)) {
      return false;
    }
    return ObjectUtils.equals(_stdCalculator, other._stdCalculator);
  }

}
