/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public abstract class SmileModelVolatilitySurfaceProvider<T extends SmileModelData> implements VolatilitySurfaceProvider {

  @Override
  public VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters) {
    return null;
  }

  @Override
  public Surface<Double, Double, DoubleMatrix1D> getVolSurfaceAdjoint(final DoubleMatrix1D modelParameters) {
    return null;
  }

  @Override
  public int getNumModelParameters() {
    return 0;
  }

}
