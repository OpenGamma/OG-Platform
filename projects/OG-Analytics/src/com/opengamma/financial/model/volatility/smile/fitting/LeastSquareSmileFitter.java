/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;

/**
 * An interface for least-square fitting of option data to smile models
 * 
 */
public abstract class LeastSquareSmileFitter {

  public abstract LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, BlackFunctionData[] data, double[] errors, final double[] initialFitParameters, final BitSet fixed);

  public abstract LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed);

  protected void testData(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters, final BitSet fixed, 
      final int nParameters) {
    Validate.notEmpty(options, "options");
    final int n = options.length;
    Validate.notNull(data, "data");
    Validate.isTrue(data.length == n, "Black function data array must be the same length as option array");
    if (errors != null) {
      Validate.isTrue(errors.length == n, "Error array length must be the same as the option array length");
    }
    Validate.notNull(initialFitParameters, "initial values");
    Validate.isTrue(initialFitParameters.length == nParameters, "must have length of initial values array equal to number of parameters");
    Validate.notNull(fixed, "fixed");
  }
}
