/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.NonLinearTransformFunction;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 * @param <T> The data for the smile model used
 */
public abstract class SmileModelFitter<T extends SmileModelData> {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare(DecompositionFactory.SV_COLT, MA, 1e-12);
  private static final Function1D<DoubleMatrix1D, Boolean> UNCONSTRAINED = new Function1D<DoubleMatrix1D, Boolean>() {
    @Override
    public Boolean evaluate(final DoubleMatrix1D x) {
      return true;
    }
  };

  private final VolatilityFunctionProvider<T> _model;
  private final Function1D<T, double[]> _volFunc;
  private final Function1D<T, double[][]> _volAdjointFunc;
  private final DoubleMatrix1D _marketValues;
  private final DoubleMatrix1D _errors;

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

    _marketValues = new DoubleMatrix1D(impliedVols);
    _errors = new DoubleMatrix1D(error);

    _volFunc = model.getVolatilityFunction(forward, strikes, timeToExpiry);
    _volAdjointFunc = model.getModelAdjointFunction(forward, strikes, timeToExpiry);
    _model = model;
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
    final NonLinearParameterTransforms transform = getTransform(start, fixed);
    return solve(start, transform);
  }

  /**
   * Solve using a user supplied NonLinearParameterTransforms
   * @param start The first guess at the parameter values
   * @param transform Transform from model parameters to fitting parameters, and vice versa
   * @return The LeastSquareResults
   */
  public LeastSquareResultsWithTransform solve(final DoubleMatrix1D start, final NonLinearParameterTransforms transform) {
    final NonLinearTransformFunction transFunc = new NonLinearTransformFunction(getModelValueFunction(), getModelJacobianFunction(), transform);

    final LeastSquareResults solRes = SOLVER.solve(_marketValues, _errors, transFunc.getFittingFunction(), transFunc.getFittingJacobian(),
        transform.transform(start), getConstraintFunction(transform), getMaximumStep());
    return new LeastSquareResultsWithTransform(solRes, transform);
  }

  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> getModelValueFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final T data = toSmileModelData(x);
        final double[] res = _volFunc.evaluate(data);
        return new DoubleMatrix1D(res);
      }
    };
  }

  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> getModelJacobianFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final T data = toSmileModelData(x);
        //this thing will be (#strikes/vols) x (# model Params)
        final double[][] volAdjoint = _volAdjointFunc.evaluate(data);
        return new DoubleMatrix2D(volAdjoint);
      }
    };
  }

  protected abstract DoubleMatrix1D getMaximumStep();

  protected abstract NonLinearParameterTransforms getTransform(final DoubleMatrix1D start);

  protected abstract NonLinearParameterTransforms getTransform(final DoubleMatrix1D start, final BitSet fixed);

  public abstract T toSmileModelData(final DoubleMatrix1D modelParameters);

  protected Function1D<DoubleMatrix1D, Boolean> getConstraintFunction(@SuppressWarnings("unused") final NonLinearParameterTransforms t) {
    return UNCONSTRAINED;
  }

  public VolatilityFunctionProvider<T> getModel() {
    return _model;
  }

}
