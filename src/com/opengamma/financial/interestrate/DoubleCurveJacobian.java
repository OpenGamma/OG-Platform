/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
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
public class DoubleCurveJacobian<T extends Interpolator1DDataBundle> implements JacobianCalculator {
  private final ForwardCurveSensitivityCalculator _forwardSensitivities = new ForwardCurveSensitivityCalculator();
  private final FundingCurveSensitivityCalculator _fundingSensitivities = new FundingCurveSensitivityCalculator();
  private final List<InterestRateDerivative> _derivatives;
  private final double[] _fwdTimeGrid;
  private final double[] _fundTimeGrid;
  private YieldAndDiscountCurve _fwdCurve;
  private YieldAndDiscountCurve _fundCurve;
  private final Interpolator1D<T, InterpolationResultWithSensitivities> _fwdInterpolator;
  private final Interpolator1D<T, InterpolationResultWithSensitivities> _fundInterpolator;
  private final int _nSwaps, _nFwdNodes, _nFundNodes;

  public DoubleCurveJacobian(final List<InterestRateDerivative> derivatives, final double[] forwardTimeGrid, final double[] fundingTimeGrid,
      final Interpolator1D<T, InterpolationResultWithSensitivities> forwardInterpolator, final Interpolator1D<T, InterpolationResultWithSensitivities> fundingInterpolator) {
    this(derivatives, forwardTimeGrid, fundingTimeGrid, null, null, forwardInterpolator, fundingInterpolator);
  }

  public DoubleCurveJacobian(final List<InterestRateDerivative> derivatives, final double[] forwardTimeGrid, final double[] fundingTimeGrid, final YieldAndDiscountCurve forwardCurve,
      final YieldAndDiscountCurve fundCurve, final Interpolator1D<T, InterpolationResultWithSensitivities> forwardInterpolator,
      final Interpolator1D<T, InterpolationResultWithSensitivities> fundingInterpolator) {
    Validate.notNull(derivatives);
    Validate.notEmpty(derivatives);
    if (forwardTimeGrid != null) {
      Validate.notNull(forwardInterpolator);
      ArgumentChecker.notEmpty(forwardTimeGrid, "forward time grid");
    } else {
      Validate.notNull(forwardCurve); // if you are not fitting this curve it must be supplied
    }
    if (fundingTimeGrid != null) {
      Validate.notNull(fundingInterpolator);
      ArgumentChecker.notEmpty(fundingTimeGrid, "funding time grid");
    } else {
      Validate.notNull(fundCurve);
    }
    _nSwaps = derivatives.size();
    _nFwdNodes = (forwardTimeGrid == null ? 0 : forwardTimeGrid.length);
    _nFundNodes = (fundingTimeGrid == null ? 0 : fundingTimeGrid.length);
    if (_nSwaps != _nFwdNodes + _nFundNodes) {
      throw new IllegalArgumentException("total number of nodes does not match number of instruments");
    }
    _derivatives = derivatives;
    _fwdTimeGrid = forwardTimeGrid;
    _fundTimeGrid = fundingTimeGrid;
    _fwdCurve = forwardCurve;
    _fundCurve = fundCurve;
    _fwdInterpolator = forwardInterpolator;
    _fundInterpolator = fundingInterpolator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x, final Function1D<DoubleMatrix1D, DoubleMatrix1D>... functions) {
    Validate.notNull(x);
    if (x.getNumberOfElements() != (_nFwdNodes + _nFundNodes)) {
      throw new IllegalArgumentException("fitting vector not same length as number of nodes");
    }
    T fwdModel = null;
    T fundModel = null;

    final int n = _nFundNodes + _nFwdNodes;
    final double[][] res = new double[n][n];

    if (_nFwdNodes > 0) {
      double[] yields = Arrays.copyOfRange(x.getData(), 0, _nFwdNodes);
      InterpolatedYieldCurve curve = new InterpolatedYieldCurve(_fwdTimeGrid, yields, _fwdInterpolator);
      fwdModel = (T) curve.getDataBundles().values().iterator().next();
      _fwdCurve = curve;
    }
    if (_nFundNodes > 0) {
      double[] yields = Arrays.copyOfRange(x.getData(), _nFwdNodes, x.getNumberOfElements());
      InterpolatedYieldCurve curve = new InterpolatedYieldCurve(_fundTimeGrid, yields, _fundInterpolator);
      fundModel = (T) curve.getDataBundles().values().iterator().next();
      _fundCurve = curve;
    }

    for (int i = 0; i < n; i++) {
      final InterestRateDerivative derivative = _derivatives.get(i);
      if (_nFwdNodes > 0) {
        final List<Pair<Double, Double>> fwdSensitivity = _forwardSensitivities.getForwardCurveSensitivities(_fwdCurve, _fundCurve, derivative);
        double[][] sensitivity = new double[fwdSensitivity.size()][];
        int k = 0;
        for (final Pair<Double, Double> timeAndDF : fwdSensitivity) {
          sensitivity[k++] = _fwdInterpolator.interpolate(fwdModel, timeAndDF.getFirst()).getSensitivities();
        }
        for (int j = 0; j < _nFwdNodes; j++) {
          double temp = 0.0;
          k = 0;
          for (final Pair<Double, Double> timeAndDF : fwdSensitivity) {
            temp += timeAndDF.getSecond() * sensitivity[k++][j];
          }
          res[i][j] = temp;
        }
      }
      if (_nFundNodes > 0) {
        final List<Pair<Double, Double>> fundSensitivity = _fundingSensitivities.getFundingCurveSensitivities(_fwdCurve, _fundCurve, derivative);
        double[][] sensitivity = new double[fundSensitivity.size()][];
        int k = 0;
        for (final Pair<Double, Double> timeAndDF : fundSensitivity) {
          sensitivity[k++] = _fundInterpolator.interpolate(fundModel, timeAndDF.getFirst()).getSensitivities();
        }
        for (int j = 0; j < _nFundNodes; j++) {
          double temp = 0.0;
          k = 0;
          for (final Pair<Double, Double> timeAndDF : fundSensitivity) {
            temp += timeAndDF.getSecond() * sensitivity[k++][j];
          }
          res[i][j + _nFwdNodes] = temp;
        }
      }
    }

    return new DoubleMatrix2D(res);
  }
}
