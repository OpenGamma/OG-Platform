/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Model the  volatilities directly, i.e. the model parameters are the volatilities.<p>
 *  This gives a {@link DiscreteVolatilityFunction} that is a one-to-one mapping from model parameters to volatilities,
 *  i.e. the model parameters are the volatilities.
 */
public class DiscreteVolatilityFunctionProviderDirect extends DiscreteVolatilityFunctionProvider {

  @Override
  public DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints) {
    ArgumentChecker.noNulls(expiryStrikePoints, "strikeExpiryPoints");
    final int n = expiryStrikePoints.length;
    final IdentityMatrix iM = new IdentityMatrix(n);
    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        ArgumentChecker.isTrue(x.getNumberOfElements() == n, "size of x");
        return new DoubleMatrix1D(x.getData());
      }

      @Override
      public DoubleMatrix2D calculateJacobian(final DoubleMatrix1D x) {
        return iM;
      }

      @Override
      public int getLengthOfDomain() {
        return n;
      }

      @Override
      public int getLengthOfRange() {
        return n;
      }

    };
  }

}
