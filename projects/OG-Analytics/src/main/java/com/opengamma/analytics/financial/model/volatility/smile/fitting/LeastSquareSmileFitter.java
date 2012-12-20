/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;

/**
 * An interface for least-square fitting of option data to smile models
 * 
 */
public abstract class LeastSquareSmileFitter {

  public abstract LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, BlackFunctionData[] data,
      double[] errors, final double[] initialFitParameters, final BitSet fixed);

  public abstract LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed);

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

    final double t = options[0].getTimeToExpiry();
    final double fwd = data[0].getForward();
    final double df = data[0].getDiscountFactor();

    for (int i = 1; i < n; i++) {
      Validate.isTrue(Double.doubleToLongBits(options[i].getTimeToExpiry()) == Double.doubleToLongBits(t), "options not all at same time horizon");
      Validate.isTrue(Double.doubleToLongBits(data[i].getForward()) == Double.doubleToLongBits(fwd), "options don't all have same forward");
      Validate.isTrue(Double.doubleToLongBits(data[i].getDiscountFactor()) == Double.doubleToLongBits(df), "options don't all have same discount factors");
    }
  }
}
