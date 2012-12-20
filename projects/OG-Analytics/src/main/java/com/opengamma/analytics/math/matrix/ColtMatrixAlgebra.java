/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * Provides matrix algebra by using the <a href = "http://acs.lbl.gov/software/colt/api/cern/colt/matrix/linalg/Algebra.html">Colt matrix algebra library</a>. 
 */
public class ColtMatrixAlgebra extends MatrixAlgebra {
  private static final Algebra ALGEBRA = new Algebra();

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCondition(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      return ALGEBRA.cond(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find condition of DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getDeterminant(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      return ALGEBRA.det(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find determinant of DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getInverse(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      return new DoubleMatrix2D(ALGEBRA.inverse(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData())).toArray());
    }
    throw new IllegalArgumentException("Can only find inverse of DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2) {
    Validate.notNull(m1, "m1");
    Validate.notNull(m2, "m2");
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      return ALGEBRA.mult(DoubleFactory1D.dense.make(((DoubleMatrix1D) m1).getData()), DoubleFactory1D.dense.make(((DoubleMatrix1D) m2).getData()));
    }
    throw new IllegalArgumentException("Cannot find the inner product of a " + m1.getClass() + " and " + m2.getClass());
  }

  /**
   * {@inheritDoc}
   * The following combinations of input matrices m1 and m2 are allowed:
   * <ul>
   * <li> m1 = 2-D matrix, m2 = 2-D matrix, returns $\mathbf{C} = \mathbf{AB}$
   * <li> m1 = 2-D matrix, m2 = 1-D matrix, returns $\mathbf{C} = \mathbf{A}b$
   * </ul>
   */
  @Override
  public Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2) {
    Validate.notNull(m1, "m1");
    Validate.notNull(m2, "m2");
    if (m1 instanceof DoubleMatrix1D) {
      return new OGMatrixAlgebra().multiply(m1, m2);
    }
    if (m1 instanceof DoubleMatrix2D) {
      final DoubleMatrix2D x = (DoubleMatrix2D) m1;
      if (m2 instanceof DoubleMatrix1D) {
        return new DoubleMatrix1D(ALGEBRA.mult(DoubleFactory2D.dense.make(x.getData()), DoubleFactory1D.dense.make(((DoubleMatrix1D) m2).getData())).toArray());
      } else if (m2 instanceof DoubleMatrix2D) {
        return new DoubleMatrix2D(ALGEBRA.mult(DoubleFactory2D.dense.make(x.getData()), DoubleFactory2D.dense.make(((DoubleMatrix2D) m2).getData())).toArray());
      }
      throw new IllegalArgumentException("Can only have 1D or 2D matrix as second argument");
    }
    throw new IllegalArgumentException("Can only multiply 2D and 1D matrices");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2) {
    Validate.notNull(m1, "m1");
    Validate.notNull(m2, "m2");
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final cern.colt.matrix.DoubleMatrix2D x = DoubleFactory2D.dense.make(m1.getNumberOfElements(), m2.getNumberOfElements());
      ALGEBRA.multOuter(DoubleFactory1D.dense.make(((DoubleMatrix1D) m1).getData()), DoubleFactory1D.dense.make(((DoubleMatrix1D) m2).getData()), x);
      return new DoubleMatrix2D(x.toArray());
    }
    throw new IllegalArgumentException("Cannot find the outer product of a " + m1.getClass() + " and " + m2.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getNorm1(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix1D) {
      return ALGEBRA.norm1(DoubleFactory1D.dense.make(((DoubleMatrix1D) m).getData()));
    } else if (m instanceof DoubleMatrix2D) {
      return ALGEBRA.norm1(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find norm1 of DoubleMatrix1D or DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getNorm2(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix1D) {
      return Math.sqrt(ALGEBRA.norm2(DoubleFactory1D.dense.make(((DoubleMatrix1D) m).getData())));
    } else if (m instanceof DoubleMatrix2D) {
      return ALGEBRA.norm2(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find norm2 of DoubleMatrix1D or DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getNormInfinity(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix1D) {
      return ALGEBRA.normInfinity(DoubleFactory1D.dense.make(((DoubleMatrix1D) m).getData()));
    } else if (m instanceof DoubleMatrix2D) {
      return ALGEBRA.normInfinity(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find normInfinity of DoubleMatrix1D or DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final int p) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      return new DoubleMatrix2D(ALGEBRA.pow(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()), p).toArray());
    }
    throw new IllegalArgumentException("Can only find transpose of DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final double p) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getTrace(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      return ALGEBRA.trace(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData()));
    }
    throw new IllegalArgumentException("Can only find trace of DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getTranspose(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      return new DoubleMatrix2D(ALGEBRA.transpose(DoubleFactory2D.dense.make(((DoubleMatrix2D) m).getData())).toArray());
    }
    throw new IllegalArgumentException("Can only find transpose of DoubleMatrix2D; have " + m.getClass());
  }
}
