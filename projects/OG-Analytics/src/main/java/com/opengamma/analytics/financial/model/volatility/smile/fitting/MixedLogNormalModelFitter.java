/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
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
public class MixedLogNormalModelFitter extends SmileModelFitter<MixedLogNormalModelData> {

  //private static final double PI_BY_2 = Math.PI / 2.0;
  private final ParameterLimitsTransform[] _transforms;
  private final boolean _useShiftedMean;
  private final int _nNormals;

  public MixedLogNormalModelFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols, final double[] error,
      final VolatilityFunctionProvider<MixedLogNormalModelData> model,
      final int numNormals, final boolean useShiftedMeans) {
    super(forward, strikes, timeToExpiry, impliedVols, error, model);

    final int n = useShiftedMeans ? 3 * numNormals - 2 : 2 * numNormals - 1;
    _transforms = new ParameterLimitsTransform[n];
    //    for (int i = 0; i < numNormals; i++) {
    //      _transforms[i] = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    //    }
    //TODO investigate whether it is better to restrict the range of angles
    for (int i = 0; i < n; i++) {
      _transforms[i] = new NullTransform();
    }

    _useShiftedMean = useShiftedMeans;
    _nNormals = numNormals;
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start) {
    final BitSet fixed = new BitSet();
    return new UncoupledParameterTransforms(start, _transforms, fixed);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start, final BitSet fixed) {
    return new UncoupledParameterTransforms(start, _transforms, fixed);
  }

  @Override
  public MixedLogNormalModelData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new MixedLogNormalModelData(modelParameters.getData(), _useShiftedMean);
  }

  @Override
  protected Function1D<DoubleMatrix1D, Boolean> getConstraintFunction(final NonLinearParameterTransforms t) {
    return new Function1D<DoubleMatrix1D, Boolean>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Boolean evaluate(final DoubleMatrix1D x) {
        if (x.getEntry(0) <= 1e-4) {
          return false;
        }
        for (int i = 1; i < _nNormals; i++) {
          if (x.getEntry(i) < 0.0) {
            return false;
          }
        }
        //Don't constrain angles
        //        for (int i = 0; i < _nNormals - 1; i++) {
        //          double temp = x.getEntry(i + _nNormals);
        //          if (temp < 0.0 || temp > PI_BY_2) {
        //            return true;
        //          }
        //          if (_useShiftedMean) {
        //            temp = x.getEntry(i + 2 * _nNormals - 1);
        //            if (temp < 0.0 || temp > PI_BY_2) {
        //              return true;
        //            }
        //          }
        //        }
        return true;
      }
    };
  }

  @Override
  protected DoubleMatrix1D getMaximumStep() {
    final int n = _useShiftedMean ? 3 * _nNormals - 2 : 2 * _nNormals - 1;
    return new DoubleMatrix1D(n, 0.1);
  }
}
