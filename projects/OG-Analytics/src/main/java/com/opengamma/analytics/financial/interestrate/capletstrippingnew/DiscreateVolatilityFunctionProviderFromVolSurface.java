/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class DiscreateVolatilityFunctionProviderFromVolSurface extends DiscreteVolatilityFunctionProvider {

  private final VolatilitySurfaceProvider _volatilitySurfacePro;

  public DiscreateVolatilityFunctionProviderFromVolSurface(final VolatilitySurfaceProvider volSurfacePro) {
    ArgumentChecker.notNull(volSurfacePro, "volSurfacePro");
    _volatilitySurfacePro = volSurfacePro;
  }

  @Override
  public DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints) {
    ArgumentChecker.notNull(expiryStrikePoints, "strikeExpiryPoints");
    final int n = expiryStrikePoints.length;

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] res = new double[n];
        final VolatilitySurface vs = _volatilitySurfacePro.getVolSurface(x);
        for (int i = 0; i < n; i++) {
          res[i] = vs.getVolatility(expiryStrikePoints[i]);
        }
        return new DoubleMatrix1D(res);
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        final Surface<Double, Double, DoubleMatrix1D> volSurfaceAdjoint = _volatilitySurfacePro.getVolSurfaceAdjoint(x);
        final DoubleMatrix2D res = new DoubleMatrix2D(n, _volatilitySurfacePro.getNumModelParameters());
        for (int i = 0; i < n; i++) {
          res.getData()[i] = volSurfaceAdjoint.getZValue(expiryStrikePoints[i]).getData();
        }

        return res;
      }

      @Override
      public int getSizeOfDomain() {
        return _volatilitySurfacePro.getNumModelParameters();
      }

      @Override
      public int getSizeOfRange() {
        return n;
      }

    };
  }

}
