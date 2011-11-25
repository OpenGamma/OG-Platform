/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.NonLinearParameterTransforms;
import com.opengamma.math.minimization.NullTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.UncoupledParameterTransforms;

/**
 * 
 */
public class SABRModelFitterConstained extends SmileModelFitter<SABRFormulaData> {
  private static final ParameterLimitsTransform[] DEFAULT_TRANSFORMS;

  static {
    DEFAULT_TRANSFORMS = new ParameterLimitsTransform[4];
    DEFAULT_TRANSFORMS[0] = new NullTransform();
    DEFAULT_TRANSFORMS[1] = new NullTransform();
    DEFAULT_TRANSFORMS[2] = new NullTransform();
    DEFAULT_TRANSFORMS[3] = new NullTransform();
  }

  public SABRModelFitterConstained(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols,
      final double[] error, VolatilityFunctionProvider<SABRFormulaData> model) {
    super(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  protected SABRFormulaData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new SABRFormulaData(modelParameters.getData());
  }

  @Override
  protected NonLinearParameterTransforms getTransform(DoubleMatrix1D start) {
    BitSet fixed = new BitSet();
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(DoubleMatrix1D start, final BitSet fixed) {
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected Function1D<DoubleMatrix1D, Boolean> getConstaintFunction(final NonLinearParameterTransforms t) {
    return new Function1D<DoubleMatrix1D, Boolean>() {

      @Override
      public Boolean evaluate(DoubleMatrix1D y) {
        DoubleMatrix1D x = t.inverseTransform(y);
        final double alpha = x.getEntry(0);
        final double beta = x.getEntry(1);
        final double rho = x.getEntry(2);
        final double nu = x.getEntry(3);
        return (alpha <= 0 || beta < 0.0 || rho < -1.0 || rho > 1.0 || nu < 0.0);
      }
    };
  }

}
