/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.NullTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;

/**
 * 
 */
public class SABRModelFitterConstrained extends SmileModelFitter<SABRFormulaData> {
  private static final ParameterLimitsTransform[] DEFAULT_TRANSFORMS;

  static {
    DEFAULT_TRANSFORMS = new ParameterLimitsTransform[4];
    DEFAULT_TRANSFORMS[0] = new NullTransform();
    DEFAULT_TRANSFORMS[1] = new NullTransform();
    DEFAULT_TRANSFORMS[2] = new NullTransform();
    DEFAULT_TRANSFORMS[3] = new NullTransform();
  }

  public SABRModelFitterConstrained(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols,
      final double[] error, final VolatilityFunctionProvider<SABRFormulaData> model) {
    super(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  public SABRFormulaData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new SABRFormulaData(modelParameters.getData());
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start) {
    final BitSet fixed = new BitSet();
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start, final BitSet fixed) {
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected Function1D<DoubleMatrix1D, Boolean> getConstraintFunction(final NonLinearParameterTransforms t) {
    return new Function1D<DoubleMatrix1D, Boolean>() {

      @Override
      public Boolean evaluate(final DoubleMatrix1D y) {
        final DoubleMatrix1D x = t.inverseTransform(y);
        final double alpha = x.getEntry(0);
        final double beta = x.getEntry(1);
        final double rho = x.getEntry(2);
        final double nu = x.getEntry(3);
        return (alpha >= 0 && beta >= 0.0 && rho >= -1.0 && rho <= 1.0 && nu >= 0.0);
      }
    };
  }

  @Override
  protected DoubleMatrix1D getMaximumStep() {
    return null;
  }

}
