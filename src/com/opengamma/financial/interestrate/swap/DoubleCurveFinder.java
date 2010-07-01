/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import java.util.List;
import java.util.TreeMap;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class DoubleCurveFinder extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
  private final List<Swap> _swaps;
  private final double[] _swapValues;
  private final double _spotRate;
  private final double[] _forwardTimeGrid;
  private final double[] _fundingTimeGrid;
  private YieldAndDiscountCurve _fwdCurve;
  private YieldAndDiscountCurve _fundCurve;
  private final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> _forwardInterpolator;
  private final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> _fundingInterpolator;
  private final int _nSwaps, _nFwdNodes, _nFundNodes;
  private final SwapRateCalculator _swapRateCalculator = new SwapRateCalculator();

  public DoubleCurveFinder(final List<Swap> swaps, final double[] swapValues, final double spotRate, final double[] forwardTimeGrid, final double[] fundingTimeGrid,
      final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundCurve, final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> forwardInterpolator,
      final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> fundingInterpolator) {
    _swaps = swaps;
    _swapValues = swapValues;
    _spotRate = spotRate;
    _forwardTimeGrid = forwardTimeGrid;
    _fundingTimeGrid = fundingTimeGrid;
    _forwardInterpolator = forwardInterpolator;
    _fundingInterpolator = fundingInterpolator;
    _nSwaps = _swaps.size();
    _nFwdNodes = (forwardTimeGrid == null ? 0 : forwardTimeGrid.length);
    _nFundNodes = (fundingTimeGrid == null ? 0 : fundingTimeGrid.length);
    _fwdCurve = forwardCurve;
    _fundCurve = fundCurve;
    if (_nSwaps != _nFwdNodes + _nFundNodes) {
      throw new IllegalArgumentException("total number of nodes does not much number of instruments");
    }
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    if (x.getNumberOfElements() != (_nFwdNodes + _nFundNodes)) {
      throw new IllegalArgumentException("fitting vector not same length as number of nodes");
    }

    if (_nFwdNodes == 0) {
      if (_fwdCurve == null) {
        _fwdCurve = new ConstantYieldCurve(_spotRate);
      }
    } else {
      final TreeMap<Double, Double> fwdData = new TreeMap<Double, Double>();
      fwdData.put(0.0, _spotRate);
      for (int i = 0; i < _nFwdNodes; i++) {
        fwdData.put(_forwardTimeGrid[i], x.getEntry(i));
      }
      _fwdCurve = new InterpolatedYieldCurve(fwdData, _forwardInterpolator);
    }

    if (_nFundNodes == 0) {
      if (_fundCurve == null) {
        _fundCurve = new ConstantYieldCurve(_spotRate);
      }
    } else {
      final TreeMap<Double, Double> fundData = new TreeMap<Double, Double>();
      fundData.put(0.0, _spotRate);
      for (int i = 0; i < _nFundNodes; i++) {
        fundData.put(_fundingTimeGrid[i], x.getEntry(i + _nFwdNodes));
      }
      _fundCurve = new InterpolatedYieldCurve(fundData, _fundingInterpolator);
    }

    final double[] res = new double[_nSwaps];
    for (int i = 0; i < _nSwaps; i++) {
      res[i] = _swapRateCalculator.getRate(_fwdCurve, _fundCurve, _swaps.get(i)) - _swapValues[i];
    }
    return new DoubleMatrix1D(res);
  }
}
