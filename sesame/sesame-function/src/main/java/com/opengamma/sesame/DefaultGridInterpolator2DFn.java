/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.result.Result;

/**
 * Create GridInterpolator2D based on Y and Y extrapolator and interpolator.
 */
public class DefaultGridInterpolator2DFn implements GridInterpolator2DFn {

  private final String _xInterpolatorName;
  private final String _xLeftExtrapolatorName;
  private final String _xRightExtrapolatorName;
  private final String _yInterpolatorName;
  private final String _yLeftExtrapolatorName;
  private final String _yRightExtrapolatorName;

  /**
   * Default implementation of GridInterpolator2DFn
   *
   * @param xInterpolatorName name of the X interpolator
   * @param xLeftExtrapolatorName name of the X left extrapolator
   * @param xRightExtrapolatorName name of the X right extrapolator
   * @param yInterpolatorName name of the Y interpolator
   * @param yLeftExtrapolatorName name of the Y left extrapolator
   * @param yRightExtrapolatorName name of the Y right extrapolator
   */
  public DefaultGridInterpolator2DFn(String xInterpolatorName,
                                     String xLeftExtrapolatorName,
                                     String xRightExtrapolatorName,
                                     String yInterpolatorName,
                                     String yLeftExtrapolatorName,
                                     String yRightExtrapolatorName) {
    _xInterpolatorName = xInterpolatorName;
    _xLeftExtrapolatorName = xLeftExtrapolatorName;
    _xRightExtrapolatorName = xRightExtrapolatorName;
    _yInterpolatorName = yInterpolatorName;
    _yLeftExtrapolatorName = yLeftExtrapolatorName;
    _yRightExtrapolatorName = yRightExtrapolatorName;
  }

  @Override
  public Result<GridInterpolator2D> createGridInterpolator2DFn(Environment env) {
    Interpolator1D combinedX =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(_xInterpolatorName,
                                                                _xLeftExtrapolatorName,
                                                                _xRightExtrapolatorName);

    Interpolator1D combinedY =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(_yInterpolatorName,
                                                                _yLeftExtrapolatorName,
                                                                _yRightExtrapolatorName);

    return Result.success(new GridInterpolator2D(combinedX, combinedY));
  }
}
