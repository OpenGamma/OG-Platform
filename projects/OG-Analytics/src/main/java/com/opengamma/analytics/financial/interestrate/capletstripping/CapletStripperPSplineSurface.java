/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * This represents the (caplet) volatility surface by 2D basis-splines, then applies a penalty on the curvature of this
 * surface (in both directions) and solves for the market cap values in a (penalised) least-square sense.  
 * @deprecated This currently does not work properly 
 */
@Deprecated
public class CapletStripperPSplineSurface implements CapletStripper {
  private static final Function1D<DoubleMatrix1D, Boolean> POSITIVE = new PositiveOrZero();
  private final CapletStrippingImp _imp;
  private final DoubleMatrix2D _penalty;
  private final int _size;

  /**
   *
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param k1 The low strike value
   * @param k2 The upper strike value
   * @param nStrikeKnots Number of knots in strike direction
   * @param strikeDegree The degree of the basis-spline in the strike direction
   * @param t1 The low time
   * @param t2 The upper time
   * @param nTimeKnots Number of knots in time direction
   * @param timeDegree The degree of the basis-spline in the strike direction
   * @param lambda Strength of the penalty 
   */
  public CapletStripperPSplineSurface(final MultiCapFloorPricer pricer, final double k1, final double k2, final int nStrikeKnots, final int strikeDegree, final double t1, final double t2,
      final int nTimeKnots, final int timeDegree, final double lambda) {
    final BasisSplineVolatilitySurfaceProvider vtsp = new BasisSplineVolatilitySurfaceProvider(k1, k2, nStrikeKnots, strikeDegree, t1, t2, nTimeKnots, timeDegree);
    final DiscreteVolatilityFunctionProvider volFuncPro = new DiscreateVolatilityFunctionProviderFromVolSurface(vtsp);
    _imp = new CapletStrippingImp(pricer, volFuncPro);
    final int tSize = nTimeKnots + timeDegree - 1;
    final int kSize = nStrikeKnots + strikeDegree - 1;
    _size = vtsp.getNumModelParameters();
    ArgumentChecker.isTrue(_size == tSize * kSize, "something wrong");
    _penalty = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {kSize, tSize }, new int[] {strikeDegree, timeDegree }, new double[] {lambda, lambda });
    //  _penalty = (DoubleMatrix2D) MA.scale(PenaltyMatrixGenerator.getPenaltyMatrix(_size, 2), lambda);

  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type) {
    ArgumentChecker.notNull(marketValues, "marketValues");
    final int n = marketValues.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1.0);
    final DoubleMatrix1D guess = new DoubleMatrix1D(_size, 0.4);
    return solve(marketValues, type, errors, guess);
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final double[] errors) {
    final DoubleMatrix1D guess = new DoubleMatrix1D(_size, 0.4);
    return solve(marketValues, type, errors, guess);
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final DoubleMatrix1D guess) {
    ArgumentChecker.notNull(marketValues, "marketValues");
    final int n = marketValues.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1.0);
    return solve(marketValues, type, errors, guess);
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final double[] errors, final DoubleMatrix1D guess) {
    if (type == MarketDataType.PRICE) {
      return _imp.solveForCapPrices(marketValues, errors, guess, _penalty, POSITIVE);
    } else if (type == MarketDataType.VOL) {
      return _imp.solveForCapVols(marketValues, errors, guess, _penalty, POSITIVE);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

}
