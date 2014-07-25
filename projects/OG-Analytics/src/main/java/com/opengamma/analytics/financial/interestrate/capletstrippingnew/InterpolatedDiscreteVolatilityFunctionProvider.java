/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterpolatedDiscreteVolatilityFunctionProvider implements DiscreteVolatilityFunctionProvider {

  private final double[] _knots;
  private final Interpolator1D _interpolator;
  private final DataCheckImp _imp = new DataCheckImp();

  public InterpolatedDiscreteVolatilityFunctionProvider(final double[] knotPoints, final Interpolator1D interpolator) {
    ArgumentChecker.notEmpty(knotPoints, "null or empty knotPoints");
    ArgumentChecker.notNull(interpolator, "null interpolator");
    final int n = knotPoints.length;

    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(knotPoints[i] > knotPoints[i - 1], "knot points must be strictly ascending");
    }
    _knots = knotPoints;
    _interpolator = interpolator;
  }

  @Override
  public DiscreteVolatilityFunction from(final double[] expiries, final double[][] strikes, final double[] forwards) {
    final int n = _imp.checkData(expiries, strikes, forwards);
    final int nExp = expiries.length;
    final int m = getNumModelParameters();

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, x.getData(), _interpolator, true);
        final double[] res = new double[n];
        int pos = 0;
        for (int i = 0; i < nExp; i++) {
          final double t = expiries[i];
          final double vol = curve.getYValue(t);
          final int nStrikes = strikes[i].length;
          final int newPos = pos + nStrikes;
          //there is no strike dependence in this model, so all options at the same expiry have the same volatility  
          Arrays.fill(res, pos, newPos, vol);
          pos = newPos;
        }

        return new DoubleMatrix1D(res);
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {

        final double[][] res = new double[n][m];
        final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, x.getData(), _interpolator, true);
        int pos = 0;
        for (int i = 0; i < nExp; i++) {
          final double t = expiries[i];
          final double[] volSense = ArrayUtils.toPrimitive(curve.getYValueParameterSensitivity(t));
          final int nStrikes = strikes[i].length;
          final int newPos = pos + nStrikes;
          //there is no strike dependence in this model, so all options at the same expiry have the same volatility, and
          //hence sensitivity to the knot values 
          Arrays.fill(res, pos, newPos, volSense);
          pos = newPos;
        }
        return new DoubleMatrix2D(res);
      }
    };
  }

  @Override
  public DiscreteVolatilityFunction from(final List<SimpleOptionData> data) {
    final int n = data.size();
    final int m = getNumModelParameters();

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        final double[][] res = new double[n][m];
        final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, x.getData(), _interpolator, true);
        int pos = 0;
        final Iterator<SimpleOptionData> iter = data.iterator();
        while (iter.hasNext()) {
          final SimpleOptionData option = iter.next();
          final double[] volSense = ArrayUtils.toPrimitive(curve.getYValueParameterSensitivity(option.getTimeToExpiry()));
          res[pos++] = volSense;
        }
        return new DoubleMatrix2D(res);
      }

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, x.getData(), _interpolator, true);
        final double[] res = new double[n];
        int pos = 0;
        final Iterator<SimpleOptionData> iter = data.iterator();
        while (iter.hasNext()) {
          final SimpleOptionData option = iter.next();
          final double vol = curve.getYValue(option.getTimeToExpiry());
          res[pos++] = vol;
        }
        return new DoubleMatrix1D(res);
      }
    };
  }

  @Override
  public int getNumModelParameters() {
    return _knots.length;
  }

}
