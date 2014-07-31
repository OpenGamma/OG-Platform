/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.List;

import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.util.ArgumentChecker;

/**
 * Model the caplet volatilities directly, i.e. the model parameters are the caplet volatilities  
 */
public class DiscreteVolatilityFunctionProviderDirect implements DiscreteVolatilityFunctionProvider {

  private final DataCheckImp _imp = new DataCheckImp();
  private final int _size;
  private final IdentityMatrix _identity;

  public DiscreteVolatilityFunctionProviderDirect(final int size) {
    ArgumentChecker.notNegativeOrZero(size, "sise");
    _size = size;
    _identity = new IdentityMatrix(size);
  }

  @Override
  public DiscreteVolatilityFunction from(final double[] expiries, final double[][] strikes, final double[] forwards) {
    final int n = _imp.checkData(expiries, strikes, forwards);
    ArgumentChecker.isTrue(n == _size, "size of input data is incorrect. Expected {} option but {} passed in", _size, n);

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        return x;
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        return _identity;
      }

    };

  }

  @Override
  public DiscreteVolatilityFunction from(final List<SimpleOptionData> data) {
    ArgumentChecker.notNull(data, "data");
    final int n = data.size();
    ArgumentChecker.isTrue(n == _size, "size of input data is incorrect. Expected {} option but {} passed in", _size, n);
    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        return x;
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        return _identity;
      }

    };
  }

  @Override
  public int getNumModelParameters() {
    return _size;
  }

}
