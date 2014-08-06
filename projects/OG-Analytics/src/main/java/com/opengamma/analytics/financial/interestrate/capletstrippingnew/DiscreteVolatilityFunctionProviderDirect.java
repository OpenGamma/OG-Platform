/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Model the caplet volatilities directly, i.e. the model parameters are the caplet volatilities  
 */
public class DiscreteVolatilityFunctionProviderDirect extends DiscreteVolatilityFunctionProvider {

  private final int _size;
  private final IdentityMatrix _identity;

  public DiscreteVolatilityFunctionProviderDirect(final int size) {
    ArgumentChecker.notNegativeOrZero(size, "sise");
    _size = size;
    _identity = new IdentityMatrix(size);
  }

  @Override
  public int getNumModelParameters() {
    return _size;
  }

  @Override
  public DiscreteVolatilityFunction from(final DoublesPair[] strikeExpiryPoints) {
    ArgumentChecker.noNulls(strikeExpiryPoints, "strikeExpiryPoints");
    final int n = strikeExpiryPoints.length;
    ArgumentChecker.isTrue(n == _size, "size of input data is incorrect. Expected {} points but {} passed in", _size, n);
    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        ArgumentChecker.isTrue(x.getNumberOfElements() == _size, "size of x");
        return x;
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        return _identity;
      }

      @Override
      public int getSizeOfDomain() {
        return _size;
      }

      @Override
      public int getSizeOfRange() {
        return n;
      }

    };
  }

}
