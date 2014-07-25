/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Iterator;
import java.util.List;

import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DiscreateVolatilityFunctionProviderFromVolSurface implements DiscreteVolatilityFunctionProvider {

  private final DataCheckImp _imp = new DataCheckImp();
  private final VolatilitySurfaceProvider _volatilitySurfacePro;

  public DiscreateVolatilityFunctionProviderFromVolSurface(final VolatilitySurfaceProvider volSurfacePro) {
    ArgumentChecker.notNull(volSurfacePro, "volSurfacePro");
    _volatilitySurfacePro = volSurfacePro;
  }

  @Override
  public DiscreteVolatilityFunction from(final double[] expiries, final double[][] strikes, final double[] forwards) {
    final int size = _imp.checkData(expiries, strikes, forwards);

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] res = new double[size];
        final VolatilitySurface vs = _volatilitySurfacePro.getVolSurface(x);
        int count = 0;
        final int nExp = expiries.length;

        for (int i = 0; i < nExp; i++) {
          final double t = expiries[i];
          final double[] s = strikes[i];
          final int nStrikes = s.length;
          for (int j = 0; j < nStrikes; j++) {
            res[count++] = vs.getVolatility(t, s[j]);
          }
        }

        return new DoubleMatrix1D(res);
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        //TODO use the vol adjoint surface  
        return evaluateJacobianViaFD(x);
      }
    };
  }

  @Override
  public DiscreteVolatilityFunction from(final List<SimpleOptionData> data) {
    ArgumentChecker.notNull(data, "data");
    final int n = data.size();

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] res = new double[n];
        final VolatilitySurface vs = _volatilitySurfacePro.getVolSurface(x);
        final Iterator<SimpleOptionData> inter = data.iterator();
        int count = 0;
        while (inter.hasNext()) {
          final SimpleOptionData option = inter.next();
          res[count++] = vs.getVolatility(option.getTimeToExpiry(), option.getStrike());
        }
        return new DoubleMatrix1D(res);
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        final Surface<Double, Double, DoubleMatrix1D> volSurfaceAdjoint = _volatilitySurfacePro.getVolSurfaceAdjoint(x);
        final double[][] res = new double[n][];
        final Iterator<SimpleOptionData> inter = data.iterator();
        int count = 0;
        while (inter.hasNext()) {
          final SimpleOptionData option = inter.next();
          res[count++] = volSurfaceAdjoint.getZValue(option.getTimeToExpiry(), option.getStrike()).getData();
        }

        return new DoubleMatrix2D(res);
      }

    };

  }

  @Override
  public int getNumModelParameters() {
    return _volatilitySurfacePro.getNumModelParameters();
  }

}
