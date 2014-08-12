/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.util.ArgumentChecker;

/**
 * This represents the (caplet) volatility surface using an interpolated curve in the expiry direction only (i.e. 
 * there is no strike dependence). This is mainly used to strip a single strike, in which case one can have as many knots 
 * as caps (of a single strike) and root find for the knot values.
 * <P> If used with multiple strikes, the lack of strike
 * dependence will make it impossible to recover the market values. 
 *@see {@link CapletStripperPSplineTermStructure}
 */
public class CapletStripperInterpolatedTermStructure implements CapletStripper {

  private static final String DEFAULT_INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private static final String DEFAULT_EXTRAPOLATOR = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final ParameterLimitsTransform TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);

  private final MultiCapFloorPricer _pricer;
  private final Interpolator1D _interpolator;
  private final ParameterLimitsTransform _transform;
  private final double[] _knots;

  /**
   * Set up the stripper with a double-quadratic interpolator ({@link DoubleQuadraticInterpolator1D}) and a linear extrapolator ({@link LinearExtrapolator1D}).
   * The transformation (using {@link SingleRangeLimitTransform}) ensures the caplet volatility is always positive (regardless of the value of the knots).
   * Here the knots positions are auto generated. 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   */
  public CapletStripperInterpolatedTermStructure(final MultiCapFloorPricer pricer) {
    ArgumentChecker.notNull(pricer, "pricer");
    _pricer = pricer;
    _knots = null;
    _transform = TRANSFORM;
    _interpolator = new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR), _transform);
  }

  //TODO provide constructors to set interpolator and knots   
  /**
   * Set up the stripper with a transformed double-quadratic interpolator ({@link DoubleQuadraticInterpolator1D}) and a linear extrapolator ({@link LinearExtrapolator1D}).
   * The transformation (using {@link SingleRangeLimitTransform}) ensures the caplet volatility is always positive (regardless of the value of the knots).
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param knots The knots. The knot positions will have a large effect on the shape of the term structure, and hence
   * the quality of the solution. The number of knots must not exceed the number of caps.  
   */
  public CapletStripperInterpolatedTermStructure(final MultiCapFloorPricer pricer, final double[] knots) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notEmpty(knots, "knots");
    ArgumentChecker.isTrue(pricer.getNumCaps() >= knots.length, "#knots ({}) is greater than number of caps ({}). Please reduce the number of knots", knots.length, pricer.getNumCaps());
    _pricer = pricer;
    _transform = TRANSFORM;

    _interpolator = new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR), _transform);
    _knots = knots;
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type) {
    final CapletStrippingImp imp = getImp(marketValues);
    final double[] impliedVol = type == MarketDataType.PRICE ? _pricer.impliedVols(marketValues) : marketValues;
    final DoubleMatrix1D start = getStartValue(impliedVol, imp.getnModelParms());
    if (type == MarketDataType.PRICE) {
      return imp.solveForCapPrices(marketValues, start);
    } else if (type == MarketDataType.VOL) {
      return imp.solveForCapVols(marketValues, start);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final double[] errors) {
    final CapletStrippingImp imp = getImp(marketValues);
    final double[] impliedVol = type == MarketDataType.PRICE ? _pricer.impliedVols(marketValues) : marketValues;
    final DoubleMatrix1D start = getStartValue(impliedVol, imp.getnModelParms());
    if (type == MarketDataType.PRICE) {
      return imp.solveForCapPrices(marketValues, errors, start);
    } else if (type == MarketDataType.VOL) {
      return imp.solveForCapVols(marketValues, errors, start);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final DoubleMatrix1D guess) {

    final CapletStrippingImp imp = getImp(marketValues);
    if (type == MarketDataType.PRICE) {
      return imp.solveForCapPrices(marketValues, guess);
    } else if (type == MarketDataType.VOL) {
      return imp.solveForCapVols(marketValues, guess);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final double[] errors, final DoubleMatrix1D guess) {

    final CapletStrippingImp imp = getImp(marketValues);
    if (type == MarketDataType.PRICE) {
      return imp.solveForCapPrices(marketValues, errors, guess);
    } else if (type == MarketDataType.VOL) {
      return imp.solveForCapVols(marketValues, errors, guess);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

  private CapletStrippingImp getImp(final double[] values) {

    ArgumentChecker.notEmpty(values, "values");
    final int nCaps = _pricer.getNumCaps();
    ArgumentChecker.isTrue(nCaps == values.length, "Expected {} cap prices, but only given {}", nCaps, values.length);
    final double[] knots = _knots == null ? getKnots(_pricer.getCapStartTimes(), _pricer.getCapEndTimes(), nCaps) : _knots;
    final DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderFromInterpolatedTermStructure(knots, _interpolator);
    return new CapletStrippingImp(_pricer, volPro);
  }

  /**
   * get a rough starting value of model parameters from cap volatilities. 
   * @param capVols cap volatilities 
   * @param n number of model parameters 
   * @return starting guess 
   */
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
