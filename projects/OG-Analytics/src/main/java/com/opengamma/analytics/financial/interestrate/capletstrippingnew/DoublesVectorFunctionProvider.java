/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class DoublesVectorFunctionProvider implements VectorFunctionProvider<Double> {

  @Override
  public VectorFunction from(final List<Double> x) {
    ArgumentChecker.notNull(x, "x");
    return from(x.toArray(new Double[0]));
  }

  @Override
  public VectorFunction from(final Double[] x) {
    ArgumentChecker.notNull(x, "x");
    return from(ArrayUtils.toPrimitive(x));
  }

  public abstract VectorFunction from(double[] x);

}
