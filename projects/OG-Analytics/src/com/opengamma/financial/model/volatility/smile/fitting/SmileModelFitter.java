/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.math.minimization.NonLinearParameterTransforms;
import com.opengamma.math.minimization.NonLinearTransformFunction;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 * @param <T> The data for the smile model used
 */
public abstract class SmileModelFitter<T extends SmileModelData> {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare(DecompositionFactory.SV_COLT, MA, 1e-6);
  private static final Function1D<DoubleMatrix1D, Boolean> UNCONSTRAINED = new Function1D<DoubleMatrix1D, Boolean>() {
    @Override
    public Boolean evaluate(DoubleMatrix1D x) {
      return false;
    }
  };

  private final Function1D<T, double[]> _volFunc;
  private final Function1D<T, double[][]> _volAdjointFunc;
  private final DoubleMatrix1D _marketValues;
  private final DoubleMatrix1D _errors;
  private final int _nOptions;

  /**
   * Attempts to calibrate a model to the implied volatilities of European vanilla options, by minimising the sum of squares between the
   * market and model implied volatilities. All the options must be for the same expiry and (implicitly) on the same underlying.
   * @param forward The forward value of the underlying
   * @param strikes ordered values of strikes
   * @param timeToExpiry The time-to-expiry
   * @param impliedVols The market implied volatilities
   * @param error The 'measurement' error to apply to the market volatility of a particular option TODO: Review should this be part of  EuropeanOptionMarketData?
   * @param model VolatilityFunctionProvider
   */
  public SmileModelFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols,
      final double[] error, final VolatilityFunctionProvider<T> model) {
    Validate.notNull(strikes, "null strikes");
    Validate.notNull(impliedVols, "null implied vols");
    Validate.notNull(error, "null errors");
    Validate.notNull(model, "null model");
    final int n = strikes.length;
    Validate.isTrue(n == impliedVols.length, "vols not the same length as strikes");
    Validate.isTrue(n == error.length, "errors not the same length as strikes");

    _nOptions = n;
    _marketValues = new DoubleMatrix1D(impliedVols);
    _errors = new DoubleMatrix1D(error);

    _volFunc = model.getVolatilityFunction(forward, strikes, timeToExpiry);
    _volAdjointFunc = model.getModelAdjointFunction(forward, strikes, timeToExpiry);
  }

  /**
   * Solve using the default NonLinearParameterTransforms for the concrete implementation
   * @param start The first guess at the parameter values
   * @return The LeastSquareResults
   */
  public LeastSquareResultsWithTransform solve(final DoubleMatrix1D start) {
    return solve(start, new BitSet());
  }

  /**
   * Solve using the default NonLinearParameterTransforms for the concrete implementation, but with some parameters fixed to their initial
   * values (indicated by fixed)
   * @param start The first guess at the parameter values
   * @param fixed Indicates which parameters are fixed
   * @return The LeastSquareResults
   */
  public LeastSquareResultsWithTransform solve(final DoubleMatrix1D start, final BitSet fixed) {
    NonLinearParameterTransforms transform = getTransform(start, fixed);
    return solve(start, transform);
  }

  /**
   * Solve using a user supplied NonLinearParameterTransforms
   * @param start The first guess at the parameter values
   * @param transform Transform from model parameters to fitting parameters, and vice versa
   * @return The LeastSquareResults
   */
  public LeastSquareResultsWithTransform solve(final DoubleMatrix1D start, final NonLinearParameterTransforms transform) {
    NonLinearTransformFunction transFunc = new NonLinearTransformFunction(getModelValueFunction(), getModelJacobianFunction(), transform);

    LeastSquareResults solRes = SOLVER.solve(_marketValues, _errors, transFunc.getFittingFunction(), transFunc.getFittingJacobian(),
        transform.transform(start), getConstraintFunction(transform));
    return new LeastSquareResultsWithTransform(solRes, transform);
  }

  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> getModelValueFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        T data = toSmileModelData(x);
        double[] res = _volFunc.evaluate(data);
        return new DoubleMatrix1D(res);
      }
    };
  }

  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> getModelJacobianFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
        T data = toSmileModelData(x);
        //this thing will be (#strikes/vols) x (# model Params)
        double[][] volAdjoint = _volAdjointFunc.evaluate(data);
        return new DoubleMatrix2D(volAdjoint);
      }
    };
  }

  protected abstract NonLinearParameterTransforms getTransform(final DoubleMatrix1D start);

  protected abstract NonLinearParameterTransforms getTransform(final DoubleMatrix1D start, final BitSet fixed);

  protected abstract T toSmileModelData(final DoubleMatrix1D modelParameters);

  protected Function1D<DoubleMatrix1D, Boolean> getConstraintFunction(final  NonLinearParameterTransforms t) {
    return UNCONSTRAINED;
  }

}
