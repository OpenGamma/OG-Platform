/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public interface Interpolator1DNodeSensitivityCalculator {

  double[] calculate(Interpolator1DDataBundle data, double value);

}
