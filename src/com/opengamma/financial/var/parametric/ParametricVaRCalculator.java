/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author emcleod
 * 
 */
public abstract class ParametricVaRCalculator {
  // TODO extract out the horizon scaling so that we can treat static and
  // dynamic portfolios correctly

  // public abstract Double scaleStatic();

  // public abstract Double scaleDynamic();

  public abstract Double evaluate(DoubleMatrix2D m, DoubleMatrix1D v, double periods, double horizon, double quantile);
}
