/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * Provides matrix algebra by calling the Colt library 
 */
public class ColtMatrixAlgebra extends MatrixAlgebra {
  private final Algebra _algebra = new Algebra();

  @Override
  public double getCondition(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      return _algebra.cond(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find condition of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public double getDeterminant(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      return _algebra.det(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find determinant of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public DoubleMatrix2D getInverse(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      return new DoubleMatrix2D(_algebra.inverse(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData())).toArray());
    }
    throw new IllegalArgumentException("Can only find inverse of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      return _algebra.mult(DoubleFactory1D.dense.make(((DoubleMatrix1D) m1).getData()), DoubleFactory1D.dense.make(((DoubleMatrix1D) m2).getData()));
    }
    throw new IllegalArgumentException("Cannot find the inner product of a " + m1.getClass() + " and " + m2.getClass());
  }

  @Override
  public Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D) {
      throw new IllegalArgumentException("Cannot have 1D matrix as first argument");
    }
    if (m1 instanceof DoubleMatrix2D) {
      final DoubleMatrix2D x = (DoubleMatrix2D) m1;
      if (m2 instanceof DoubleMatrix1D) {
        return new DoubleMatrix1D(_algebra.mult(DoubleFactory2D.dense.make(x.getData()), DoubleFactory1D.dense.make(((DoubleMatrix1D) m2).getData())).toArray());
      } else if (m2 instanceof DoubleMatrix2D) {
        return new DoubleMatrix2D(_algebra.mult(DoubleFactory2D.dense.make(x.getData()), DoubleFactory2D.dense.make(((DoubleMatrix2D) m2).getData())).toArray());
      } else {
        throw new IllegalArgumentException("Can only have 1D or 2D matrix as second argument");
      }
    }
    throw new IllegalArgumentException("Can only multiply 2D and 1D matrices");
  }

  @Override
  public DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final cern.colt.matrix.DoubleMatrix2D x = DoubleFactory2D.dense.make(m1.getNumberOfElements(), m2.getNumberOfElements());
      _algebra.multOuter(DoubleFactory1D.dense.make(((DoubleMatrix1D) m1).getData()), DoubleFactory1D.dense.make(((DoubleMatrix1D) m2).getData()), x);
      return new DoubleMatrix2D(x.toArray());
    }
    throw new IllegalArgumentException("Cannot find the outer product of a " + m1.getClass() + " and " + m2.getClass());
  }

  @Override
  public double getNorm1(final Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      return _algebra.norm1(DoubleFactory1D.dense.make(((DoubleMatrix1D) m).getData()));
    } else if (m instanceof DoubleMatrix2D) {
      return _algebra.norm1(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find norm1 of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public double getNorm2(final Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      return Math.sqrt(_algebra.norm2(DoubleFactory1D.dense.make(((DoubleMatrix1D) m).getData())));
    } else if (m instanceof DoubleMatrix2D) {
      return _algebra.norm2(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find norm2 of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public double getNormInfinity(final Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      return _algebra.normInfinity(DoubleFactory1D.dense.make(((DoubleMatrix1D) m).getData()));
    } else if (m instanceof DoubleMatrix2D) {
      return _algebra.normInfinity(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find normInfinity of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final int p) {
    if (m instanceof DoubleMatrix2D) {
      return new DoubleMatrix2D(_algebra.pow(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()), p).toArray());
    }
    throw new IllegalArgumentException("Can only find transpose of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final double p) {
    throw new NotImplementedException();
  }

  @Override
  public double getTrace(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      return _algebra.trace(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find trace of DoubleMatrix2D; have " + m.getClass());
  }

  @Override
  public DoubleMatrix2D getTranspose(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      return new DoubleMatrix2D(_algebra.transpose(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData())).toArray());
    }
    throw new IllegalArgumentException("Can only find transpose of DoubleMatrix2D; have " + m.getClass());
  }
}
