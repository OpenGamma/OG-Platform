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
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.util.tuple.Pair;

/**
 * @param <T> 
 */
public class DoubleCurveJacobian<T extends Interpolator1DDataBundle> implements JacobianCalculator {
  private final ForwardCurveSensitivityCalculator _forwardSensitivities = new ForwardCurveSensitivityCalculator();
  private final FundingCurveSensitivityCalculator _fundingSensitivities = new FundingCurveSensitivityCalculator();
  private final List<Swap> _irds;
  private final double _spotRate;
  private final double[] _fwdTimeGrid;
  private final double[] _fundTimeGrid;
  private final Interpolator1DWithSensitivities<T> _fwdInterpolator;
  private final Interpolator1DWithSensitivities<T> _fundInterpolator;
  private final int _nSwaps, _nFwdNodes, _nFundNodes;

  public DoubleCurveJacobian(final List<Swap> irds, final double spotRate, final double[] forwardTimeGrid, final double[] fundingTimeGrid, final Interpolator1DWithSensitivities<T> fwdInterpolator,
      final Interpolator1DWithSensitivities<T> fundingInterpolator) {
    _irds = irds;
    _spotRate = spotRate;
    _fwdTimeGrid = forwardTimeGrid;
    _fundTimeGrid = fundingTimeGrid;
    _fwdInterpolator = fwdInterpolator;
    _fundInterpolator = fundingInterpolator;
    _nSwaps = _irds.size();
    _nFwdNodes = (forwardTimeGrid == null ? 0 : forwardTimeGrid.length);
    _nFundNodes = (fundingTimeGrid == null ? 0 : fundingTimeGrid.length);
    if (_nSwaps != _nFwdNodes + _nFundNodes) {
      throw new IllegalArgumentException("total number of nodes does not much number of instruments");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x, final Function1D<DoubleMatrix1D, DoubleMatrix1D>... functions) {
    if (x.getNumberOfElements() != (_nFwdNodes + _nFundNodes)) {
      throw new IllegalArgumentException("fitting vector not same length as number of nodes");
    }

    final TreeMap<Double, Double> fwdData = new TreeMap<Double, Double>();
    fwdData.put(0.0, _spotRate);
    for (int i = 0; i < _nFwdNodes; i++) {
      fwdData.put(_fwdTimeGrid[i], x.getEntry(i));
    }
    final InterpolatedYieldAndDiscountCurve fwdCurve = new InterpolatedYieldCurve(fwdData, _fwdInterpolator);
    final TreeMap<Double, Double> fundData = new TreeMap<Double, Double>();
    fundData.put(-0., _spotRate);
    for (int i = 0; i < _nFundNodes; i++) {
      fundData.put(_fundTimeGrid[i], x.getEntry(i + _nFwdNodes));
    }

    final InterpolatedYieldAndDiscountCurve fundCurve = new InterpolatedYieldCurve(fundData, _fundInterpolator);
    final T fwdModel = (T) fwdCurve.getDataBundles().values().iterator().next();
    final T fundModel = (T) fundCurve.getDataBundles().values().iterator().next();
    final int n = _nFundNodes + _nFwdNodes;
    final double[][] res = new double[n][n];
    for (int i = 0; i < n; i++) {
      final Swap swap = _irds.get(i);
      final List<Pair<Double, Double>> fwdSensitivity = _forwardSensitivities.getForwardCurveSensitivities(fwdCurve, fundCurve, swap);

      final List<Pair<Double, Double>> fundSensitivity = _fundingSensitivities.getFundingCurveSensitivities(fwdCurve, fundCurve, swap);

      double[][] sensitivity = new double[fwdSensitivity.size()][];
      int k = 0;
      for (final Pair<Double, Double> timeAndDF : fwdSensitivity) {
        sensitivity[k++] = _fwdInterpolator.interpolate(fwdModel, timeAndDF.getFirst()).getSensitivities();
      }

      for (int j = 0; j < _nFwdNodes; j++) {
        double temp = 0.0;
        k = 0;
        for (final Pair<Double, Double> timeAndDF : fwdSensitivity) {
          temp += timeAndDF.getSecond() * sensitivity[k++][j + 1];
        }
        res[i][j] = temp;
      }
      sensitivity = new double[fundSensitivity.size()][];
      k = 0;
      for (final Pair<Double, Double> timeAndDF : fundSensitivity) {
        sensitivity[k++] = _fundInterpolator.interpolate(fundModel, timeAndDF.getFirst()).getSensitivities();
      }

      for (int j = 0; j < _nFundNodes; j++) {
        double temp = 0.0;
        k = 0;
        for (final Pair<Double, Double> timeAndDF : fundSensitivity) {
          temp += timeAndDF.getSecond() * sensitivity[k++][j + 1];
        }
        res[i][j + _nFwdNodes] = temp;
      }

    }
    return new DoubleMatrix2D(res);
  }

}
