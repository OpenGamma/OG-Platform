/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.model.volatility.discrete.ParameterizedSmileModelDiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.math.function.VectorFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Smile model based caplet stripper. The parameters of the smile model (e.g. SABR, Heston, SVI) are themselves
 * represented as parameterised term structures (one for each smile model parameter), and this collection of parameters
 * are the <i>model parameters</i>. For a particular caplet, we can find the smile model parameters at its expiry,
 * then (using the smile model) find the (Black) volatility at its strike; hence the model parameters describe a caplet
 * volatility surface.
 * <p>
 * For a set of market cap values, we can find, in a least-square sense, the optimal set of model parameters to
 * reproduce the market values. Since the smiles are smooth functions of a few (4-5) parameters, it is generally not
 * possible to recover exactly market values using this method.
 * @param <T> The type of smile model data
 */
public class CapletStripperSmileModel<T extends SmileModelData> implements CapletStripper {

  private final CapletStrippingImp _imp;

  /**
   * Set up the stripper.
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param volFuncProvider A {@link VectorFunctionProvider} backed by a smile model
   */
  public CapletStripperSmileModel(MultiCapFloorPricer pricer,
      ParameterizedSmileModelDiscreteVolatilityFunctionProvider<T> volFuncProvider) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notNull(volFuncProvider, "volFuncProvider");
    _imp = new CapletStrippingImp(pricer, volFuncProvider);
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type) {
    throw new NotImplementedException("must provide a guess");
  }

  @Override
  public CapletStrippingResult solve(double[] marketValues, MarketDataType type, double[] errors) {
    throw new NotImplementedException("must provide a guess");
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
    if (type == MarketDataType.PRICE) {
      return _imp.solveForCapPrices(marketValues, errors, guess);
    } else if (type == MarketDataType.VOL) {
      return _imp.solveForCapVols(marketValues, errors, guess);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

}
