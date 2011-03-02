/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public interface LeastSquareSmileFitter<T> {

  LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, T data, double[] blackVols, double[] errors, final double[] initialValues, final BitSet fixed);

}
