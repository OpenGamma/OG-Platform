/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import java.util.List;
import java.util.TreeMap;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SingleCurveJacobian implements JacobianCalculator {
  private final ForwardCurveSensitivityCalculator _forwardSensitivities = new ForwardCurveSensitivityCalculator();
  private final FundingCurveSensitivityCalculator _fundingSensitivities = new FundingCurveSensitivityCalculator();
  private final List<Swap> _irds;
  private final double _spotRate;
  private final double[] _timeGrid;
  private final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> _interpolator;
  private final int _nRows, _nCols;

  public SingleCurveJacobian(final List<Swap> irds, final double spotRate, final double[] timeGrid,
      final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> interpolator) {
    _irds = irds;
    _spotRate = spotRate;
    _timeGrid = timeGrid;
    _interpolator = interpolator;
    _nRows = _irds.size();
    _nCols = _irds.size();
  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x, Function1D<DoubleMatrix1D, DoubleMatrix1D>... functions) {
    final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(0.0, _spotRate);
    for (int i = 0; i < _timeGrid.length; i++) {
      data.put(_timeGrid[i], x.getEntry(i));
    }
    final InterpolatedYieldAndDiscountCurve curve = new InterpolatedYieldCurve(data, _interpolator);
    final Interpolator1DCubicSplineWithSensitivitiesDataBundle model =
        (Interpolator1DCubicSplineWithSensitivitiesDataBundle) curve.getDataBundles().values().iterator().next();

    final double[][] res = new double[_nRows][_nCols];
    for (int i = 0; i < _nRows; i++) {
      final Swap swap = _irds.get(i);
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
          temp += timeAndDF.getSecond() * sensitivity[k++][j + 1];
        }
        for (final Pair<Double, Double> timeAndDF : fundSensitivity) {
          temp += timeAndDF.getSecond() * sensitivity[k++][j + 1];
        }
        res[i][j] = temp;
      }

    }
    return new DoubleMatrix2D(res);
  }

}
