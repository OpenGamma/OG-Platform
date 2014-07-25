/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Arrays;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStripperInterpolatedTermStructure implements CapletStripper {

  private static final String DEFAULT_INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private static final String DEFAULT_EXTRAPOLATOR = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final ParameterLimitsTransform TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);

  private final Interpolator1D _interpolator;
  private final ParameterLimitsTransform _transform;
  private final double[] _knots;

  public CapletStripperInterpolatedTermStructure() {
    _knots = null;
    _transform = TRANSFORM;
    _interpolator = new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR), _transform);
  }

  //TODO provide constructors to set interpolator and knots 
  public CapletStripperInterpolatedTermStructure(final double[] knots) {
    ArgumentChecker.notEmpty(knots, "knots");
    _transform = TRANSFORM;
    _interpolator = new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR), _transform);
    _knots = knots;
  }

  @Override
  public CapletStrippingResult solveForPrice(final MultiCapFloorPricer pricer, final double[] capPrices) {
    //this also checks arguments 
    final CapletStrippingImp imp = getImp(pricer, capPrices);
    final double[] impliedVol = pricer.impliedVols(capPrices);
    final DoubleMatrix1D start = getStartValue(impliedVol, imp.getnModelParms());
    return imp.solveForCapPrices(capPrices, start);
  }

  @Override
  public CapletStrippingResult solveForPrice(final MultiCapFloorPricer pricer, final double[] capPrices, final double[] errors) {
    //this also checks arguments 
    final CapletStrippingImp imp = getImp(pricer, capPrices);
    final double[] impliedVol = pricer.impliedVols(capPrices);
    final DoubleMatrix1D start = getStartValue(impliedVol, imp.getnModelParms());
    return imp.solveForCapPrices(capPrices, start);
  }

  @Override
  public CapletStrippingResult solveForVol(final MultiCapFloorPricer pricer, final double[] capVol) {
    return null;
  }

  private CapletStrippingImp getImp(final MultiCapFloorPricer pricer, final double[] values) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notEmpty(values, "values");
    final int nCaps = pricer.getNumCaps();
    ArgumentChecker.isTrue(nCaps == values.length, "Expected {} cap prices, but only given {}", nCaps, values.length);
    final double[] knots = _knots == null ? getKnots(pricer.getCapStartTimes(), pricer.getCapEndTimes(), nCaps) : _knots;
    final DiscreteVolatilityFunctionProvider volPro = new InterpolatedDiscreteVolatilityFunctionProvider(knots, _interpolator);
    return new CapletStrippingImp(pricer, volPro);
  }

  //TODO might want to order the caps first
  public DoubleMatrix1D getStartValue(final double[] capVols, final int n) {
    final double[] temp = new double[n];
    System.arraycopy(capVols, 0, temp, 0, n);
    if (n > capVols.length) {
      Arrays.fill(temp, capVols.length, n, capVols[capVols.length - 1]);
    }
    for (int i = 0; i < n; i++) {
      temp[i] = _transform.transform(temp[i]);
    }
    return new DoubleMatrix1D(temp);
  }

  private double[] getKnots(final double[] s, final double[] e, final int nCaps) {

    final int ns = s.length;
    final int ne = e.length;
    final double[] temp = new double[ns + ne];
    System.arraycopy(s, 0, temp, 0, ns);
    System.arraycopy(e, 0, temp, ns, ne);
    final double[] times = FunctionUtils.unique(temp);

    ArgumentChecker.isTrue(times.length >= nCaps, "Cannot auto generate knots for this set of caps. Please supply knots");
    if (times.length == nCaps) {
      return times;
    }

    //use the first nCaps times 
    final double[] knots = new double[nCaps];
    System.arraycopy(times, 0, knots, 0, nCaps);

    return knots;
  }

}
