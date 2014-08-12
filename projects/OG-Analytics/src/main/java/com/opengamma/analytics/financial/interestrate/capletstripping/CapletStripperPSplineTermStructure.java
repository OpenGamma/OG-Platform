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
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * This represents the (caplet) volatility surface using a curve (built from basis-splines) in the expiry direction 
 * only (i.e. there is no strike dependence). A penalty on the curvature of the curve is applied, and we solve for the 
 * market cap (or floor) values in a (penalised) least-squares sense. <p>
 *  This is mainly used to strip a single strike. If used with multiple strikes, the lack of strike
 * dependence will make it impossible to recover the market values. 
 * @see {@link CapletStripperInterpolatedTermStructure}
 */
public class CapletStripperPSplineTermStructure implements CapletStripper {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final Function1D<DoubleMatrix1D, Boolean> POSITIVE = new PositiveOrZero();
  private final CapletStrippingImp _imp;
  private final DoubleMatrix2D _penalty;
  private final int _size;

  /**
   * 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param vtsp provider of volatility term structures made from basis-spline 
   * @param lambda the strength of the penalty 
   */
  public CapletStripperPSplineTermStructure(final MultiCapFloorPricer pricer, final BasisSplineVolatilityTermStructureProvider vtsp, final double lambda) {
    final DiscreteVolatilityFunctionProvider volFuncPro = new DiscreateVolatilityFunctionProviderFromVolSurface(vtsp);
    _imp = new CapletStrippingImp(pricer, volFuncPro);
    _size = vtsp.getNumModelParameters();
    _penalty = (DoubleMatrix2D) MA.scale(PenaltyMatrixGenerator.getPenaltyMatrix(_size, 2), lambda);

    //     (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(_nWeights, DIFFERENCE_ORDER), LAMBDA);
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
