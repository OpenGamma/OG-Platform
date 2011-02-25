/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;


/**
 * 
 */
public class CEVFunctionData extends BlackFunctionData {
  private double _beta;

  public CEVFunctionData(final double f, final double df, final double sigma, final double beta) {
    super(f, df, sigma);
    Validate.isTrue(beta >= 0.0, "beta less than zero not supported");
    _beta = beta;
  }

  public double getBeta() {
    return _beta;
  }

  public void setBeta(final double beta) {
    _beta = beta;
  }
}
