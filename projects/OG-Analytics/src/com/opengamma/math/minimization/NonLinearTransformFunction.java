/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;

/**
 * 
 */
public class NonLinearTransformFunction {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  private final NonLinearParameterTransforms _transform;
  private final Function1D<DoubleMatrix1D, DoubleMatrix1D> _func;
  private final Function1D<DoubleMatrix1D, DoubleMatrix2D> _jac;

  public NonLinearTransformFunction(final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac,
      final NonLinearParameterTransforms transform) {

    _transform = transform;

    _func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D yStar) {
        DoubleMatrix1D y = _transform.inverseTransform(yStar);
        return func.evaluate(y);
      }
    };

    _jac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D yStar) {
        DoubleMatrix1D y = _transform.inverseTransform(yStar);
        DoubleMatrix2D h = jac.evaluate(y);
        DoubleMatrix2D invJ = _transform.inverseJacobian(yStar);
        return (DoubleMatrix2D) MA.multiply(h, invJ);
      }
    };

  }

  public Function1D<DoubleMatrix1D, DoubleMatrix1D> getFittingFunction() {
    return _func;
  }

  public Function1D<DoubleMatrix1D, DoubleMatrix2D> getFittingJacobian() {
    return _jac;
  }

}
