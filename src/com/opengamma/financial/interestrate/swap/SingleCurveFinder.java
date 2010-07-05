/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */

public class SingleCurveFinder extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
  private final List<InterestRateDerivative> _derivatives;
  private final double[] _marketRates;
  private final double _spotRate;
  private final double[] _timeGrid;
  private final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> _interpolator;
  private final int _n;
  private final SwapRateCalculator _swapRateCalculator = new SwapRateCalculator();

  public SingleCurveFinder(final List<InterestRateDerivative> derivatives, final double[] marketRates, final double spotRate, final double[] timeGrid,
      final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> interpolator) {
    Validate.notNull(derivatives);
    Validate.notNull(marketRates);
    Validate.notNull(timeGrid);
    Validate.notNull(interpolator);
    Validate.notEmpty(derivatives);
    ArgumentChecker.notEmpty(marketRates, "market rates");
    ArgumentChecker.notEmpty(timeGrid, "time grid");
    _derivatives = derivatives;
    _marketRates = marketRates;
    _spotRate = spotRate;
    _timeGrid = timeGrid;
    _interpolator = interpolator;
    _n = _derivatives.size();

  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    Validate.notNull(x);
    final TreeMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(0.0, _spotRate);
    for (int i = 0; i < _timeGrid.length; i++) {
      data.put(_timeGrid[i], x.getEntry(i));
    }
    final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(data, _interpolator);
    final double[] res = new double[_n];
    for (int i = 0; i < _n; i++) {
      res[i] = _swapRateCalculator.getRate(curve, curve, _derivatives.get(i)) - _marketRates[i];
    }
    return new DoubleMatrix1D(res);
  }
}
