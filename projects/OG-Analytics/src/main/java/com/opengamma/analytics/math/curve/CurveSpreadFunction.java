/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import org.joda.convert.FromStringFactory;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.financial.convention.NamedInstance;

/**
 * Given an array of curves, returns a function {@link Function} that will apply a spread operation to
 * each of the curves.
 */
@FromStringFactory(factory = CurveSpreadFunctionFactory.class)
public interface CurveSpreadFunction extends Function<Curve<Double, Double>, Function<Double, Double>>, NamedInstance {

  /**
   * The string representing the spread operation
   * @return The operation name
   * @deprecated Use {@link #getName}
   */
  @Deprecated
  String getOperationName();
}
