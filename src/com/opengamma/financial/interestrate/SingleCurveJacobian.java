/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationResultWithSensitivities;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * @param <T>
 */
public class SingleCurveJacobian<T extends Interpolator1DDataBundle> implements JacobianCalculator {
  private final ForwardCurveSensitivityCalculator _forwardSensitivities = new ForwardCurveSensitivityCalculator();
  private final FundingCurveSensitivityCalculator _fundingSensitivities = new FundingCurveSensitivityCalculator();
  private final List<InterestRateDerivative> _derivatives;
  private final double[] _timeGrid;
  private final Interpolator1D<T, ? extends InterpolationResultWithSensitivities> _interpolator;
  private final int _nRows, _nCols;

  public SingleCurveJacobian(final List<InterestRateDerivative> derivatives, final double[] timeGrid, final Interpolator1D<T, ? extends InterpolationResultWithSensitivities> interpolator) {
    Validate.notNull(derivatives);
    Validate.notNull(timeGrid);
    Validate.notNull(interpolator);
    Validate.notEmpty(derivatives);
    ArgumentChecker.notEmpty(timeGrid, "time grid");
    _derivatives = derivatives;
    _timeGrid = timeGrid;
    _interpolator = interpolator;
    _nRows = _derivatives.size();
    _nCols = _derivatives.size();
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x, final Function1D<DoubleMatrix1D, DoubleMatrix1D>... functions) {

    final InterpolatedYieldAndDiscountCurve curve = new InterpolatedYieldCurve(_timeGrid, x.getData(), _interpolator);
    final T model = (T) curve.getDataBundles().values().iterator().next();

    final double[][] res = new double[_nRows][_nCols];
    for (int i = 0; i < _nRows; i++) {
      final InterestRateDerivative swap = _derivatives.get(i);
      final List<Pair<Double, Double>> fwdSensitivity = _forwardSensitivities.getForwardCurveSensitivities(curve, curve, swap);
      final List<Pair<Double, Double>> fundSensitivity = _fundingSensitivities.getFundingCurveSensitivities(curve, curve, swap);
      final int n = fwdSensitivity.size() + fundSensitivity.size();
      final double[][] sensitivity = new double[n][];
      int k = 0;
      for (final Pair<Double, Double> timeAndDF : fwdSensitivity) {
        sensitivity[k++] = _interpolator.interpolate(model, timeAndDF.getFirst()).getSensitivities();
      }
      for (final Pair<Double, Double> timeAndDF : fundSensitivity) {
        sensitivity[k++] = _interpolator.interpolate(model, timeAndDF.getFirst()).getSensitivities();
      }
      for (int j = 0; j < _nCols; j++) {
        double temp = 0.0;
        k = 0;
        for (final Pair<Double, Double> timeAndDF : fwdSensitivity) {
          temp += timeAndDF.getSecond() * sensitivity[k++][j];
        }
        for (final Pair<Double, Double> timeAndDF : fundSensitivity) {
          temp += timeAndDF.getSecond() * sensitivity[k++][j];
        }
        res[i][j] = temp;
      }

    }
    return new DoubleMatrix2D(res);
  }

}
