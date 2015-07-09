/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.util.result.Result;

/**
 * Function implementation that provides a GridInterpolator2D.
 */
public interface GridInterpolator2DFn {

  /**
   * Creates a GridInterpolator2D.
   *
   * @param env evaluation environment
   * @return the GridInterpolator2D
   */
  Result<GridInterpolator2D> createGridInterpolator2DFn(Environment env);

}
