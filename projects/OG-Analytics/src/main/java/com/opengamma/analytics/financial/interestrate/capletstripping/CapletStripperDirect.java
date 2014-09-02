/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProviderDirect;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.minimization.PositiveOrZero;
import com.opengamma.util.ArgumentChecker;

/**
 * Fit directly for caplet volatilities using a (penalty) matrix which imposes a penalty for curvature of the volatility
 * 'surface' (actually the strike-expiry grid of caplet volatilities)
 */
public class CapletStripperDirect implements CapletStripper {

  private static final Function1D<DoubleMatrix1D, Boolean> POSITIVE = new PositiveOrZero();

  private final MultiCapFloorPricerGrid _pricer;
  private final double _lambdaT;
  private final double _lambdaK;

  /**
   * Set up the stripper
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param lambda Use the same curvature penalty in both strike and expiry directions. A lower value will impose less
   * constraint, allowing better recovery of cap values, but a less smooth volatility surface
   */
  public CapletStripperDirect(MultiCapFloorPricerGrid pricer, double lambda) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notNegative(lambda, "lambda");
    _pricer = pricer;
    _lambdaT = lambda;
    _lambdaK = lambda;
  }

  /**
   * Set up the stripper
   * @param pricer The pricer
   * @param lambdaK the curvature penalty in the expiry direction
   * @param lambdaT the curvature penalty in the expiry direction
   */
  public CapletStripperDirect(MultiCapFloorPricerGrid pricer, double lambdaK, double lambdaT) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notNegative(lambdaT, "lambdaT");
    ArgumentChecker.notNegative(lambdaK, "lambdaK");
    _pricer = pricer;
    _lambdaT = lambdaT;
    _lambdaK = lambdaK;
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type) {
    ArgumentChecker.notNull(marketValues, "marketValues");
    int n = marketValues.length;
    double[] errors = new double[n];
    Arrays.fill(errors, 1.0);
    DoubleMatrix1D guess = new DoubleMatrix1D(_pricer.getNumCaplets(), 0.4);
    return solve(marketValues, type, errors, guess);
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type, double[] errors) {
    DoubleMatrix1D guess = new DoubleMatrix1D(_pricer.getNumCaplets(), 0.4);
    return solve(marketValues, type, errors, guess);
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type, DoubleMatrix1D guess) {
    ArgumentChecker.notNull(marketValues, "marketValues");
    int n = marketValues.length;
    double[] errors = new double[n];
    Arrays.fill(errors, 1.0);
    return solve(marketValues, type, errors, guess);
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type, double[] errors, DoubleMatrix1D guess) {
    DoubleMatrix2D p = getPenaltyMatrix(_pricer.getStrikes(), _pricer.getCapletExpiries(), _lambdaK, _lambdaT);
    CapletStrippingImp imp = getImp(marketValues);
    if (type == MarketDataType.PRICE) {
      return imp.solveForCapPrices(marketValues, errors, guess, p, POSITIVE);
    } else if (type == MarketDataType.VOL) {
      return imp.solveForCapVols(marketValues, errors, guess, p, POSITIVE);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

  private CapletStrippingImp getImp(double[] values) {

    ArgumentChecker.notEmpty(values, "values");
    int nCaps = _pricer.getNumCaps();
    ArgumentChecker.isTrue(nCaps == values.length, "Expected {} cap prices, but only given {}", nCaps, values.length);
    DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderDirect();
    return new CapletStrippingImp(_pricer, volPro);
  }

  protected DoubleMatrix2D getPenaltyMatrix(double[] strikes, double[] expiries, double lambdaK, double lambdaT) {

    // use second order difference unless too few points
    int diffOrderK = Math.min(2, strikes.length - 1);
    int diffOrderT = Math.min(2, expiries.length - 1);
    double effLambdaK = diffOrderK == 0 ? 0.0 : lambdaK;
    double effLambdaT = diffOrderT == 0 ? 0.0 : lambdaT;
    return PenaltyMatrixGenerator.getPenaltyMatrix(new double[][] {strikes, expiries }, new int[] {diffOrderK, diffOrderT }, new double[] {effLambdaK, effLambdaT });
  }

}
