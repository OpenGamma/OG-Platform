/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import com.opengamma.math.function.Function;

/**
 * 
 */
public interface CurveSpreadFunction extends Function<Curve<Double, Double>, Function<Double, Double>> {

  String getOperationName();
}
