/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.LUDecomposition;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;

import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Provided matrix algebra by calling the Commons-math library 
 */
public class CommonsMatrixAlgebra extends MatrixAlgebra {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getCondition(com.opengamma.math
   * .matrix.Matrix)
   */
  @Override
  public double getCondition(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      final SingularValueDecomposition svd = new SingularValueDecompositionImpl(temp);
      return svd.getConditionNumber();
    }
    throw new IllegalArgumentException("Can only find condition number of DoubleMatrix2D; have " + m.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getDeterminant(com.opengamma.math
   * .matrix.Matrix)
   */
  @Override
  public double getDeterminant(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      final LUDecomposition lud = new LUDecompositionImpl(temp);
      return lud.getDeterminant();
    }
    throw new IllegalArgumentException("Can only find determinant of DoubleMatrix2D; have " + m.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getInnerProduct(com.opengamma.math
   * .matrix.Matrix, com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final RealVector t1 = CommonsMathWrapper.wrap((DoubleMatrix1D) m1);
      final RealVector t2 = CommonsMathWrapper.wrap((DoubleMatrix1D) m2);
      return t1.dotProduct(t2);
    }
    throw new IllegalArgumentException("Can only find inner product of DoubleMatrix1D; have " + m1.getClass() + " and " + m2.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getInverse(com.opengamma.math.matrix
   * .Matrix)
   */
  @Override
  public DoubleMatrix2D getInverse(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      final LUDecomposition lud = new LUDecompositionImpl(temp);
      final RealMatrix inv = lud.getSolver().getInverse();
      return CommonsMathWrapper.wrap(inv);
    }
    throw new IllegalArgumentException("Can only find inverse of DoubleMatrix2D; have " + m.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getNorm1(com.opengamma.math.matrix
   * .Matrix)
   */
  @Override
  public double getNorm1(final Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      final RealVector temp = CommonsMathWrapper.wrap((DoubleMatrix1D) m);
      return temp.getL1Norm();
    } else if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      // TODO find if commons implements this anywhere, so we are not doing it
      // by hand
      double max = 0.0;
      for (int col = temp.getColumnDimension() - 1; col >= 0; col--) {
        max = Math.max(max, temp.getColumnVector(col).getL1Norm());
      }
      return max;

    }
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getNorm2(com.opengamma.math.matrix
   * .Matrix)
   */
  @Override
  public double getNorm2(final Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      final RealVector temp = CommonsMathWrapper.wrap((DoubleMatrix1D) m);
      return temp.getNorm();
    } else if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      final SingularValueDecomposition svd = new SingularValueDecompositionImpl(temp);
      return svd.getNorm();
      // return svd.getSingularValues()[0];
    }
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getNormInfinity(com.opengamma.math
   * .matrix.Matrix)
   */
  @Override
  public double getNormInfinity(final Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      final RealVector temp = CommonsMathWrapper.wrap((DoubleMatrix1D) m);
      return temp.getLInfNorm();
    } else if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      //REVIEW Commons getNorm() is wrong - it returns the column norm
      // TODO find if commons implements this anywhere, so we are not doing it
      // by hand
      double max = 0.0;
      for (int row = temp.getRowDimension() - 1; row >= 0; row--) {
        max = Math.max(max, temp.getRowVector(row).getL1Norm());
      }
      return max;
      //return temp.getNorm();
    }
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getOuterProduct(com.opengamma.math
   * .matrix.Matrix, com.opengamma.math.matrix.Matrix)
   */
  @Override
  public DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final RealVector t1 = CommonsMathWrapper.wrap((DoubleMatrix1D) m1);
      final RealVector t2 = CommonsMathWrapper.wrap((DoubleMatrix1D) m2);
      return CommonsMathWrapper.wrap(t1.outerProduct(t2));
    }
    throw new IllegalArgumentException("Can only find outer product of DoubleMatrix1D; have " + m1.getClass() + " and " + m2.getClass());

  }

  /*
   * (non-Javadoc)
   * Since Commons appears to have no power function for matrices, we call the Colt version
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getPower(com.opengamma.math.matrix
   * .Matrix, int)
   */
  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final int p) {
    //TODO write a Power function that does not use Colt
    final MatrixAlgebra coltAlgebra = new ColtMatrixAlgebra();
    return coltAlgebra.getPower(m, p);
  }

  /* 
   * Returns a real matrix raised to some real power 
   * Currently this method is limited to symmetric matrices only as Commons Math does not support the diagonalization of asymmetric matrices  
   *@param The <strong>symmetric</strong> matrix to take the power of. 
   *@param The power to raise to matrix to
   *
   */
  public DoubleMatrix2D getPower(final Matrix<?> m, final double p) {
    if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      final EigenDecomposition eigen = new EigenDecompositionImpl(temp, 0.0);
      final double[] rEigenValues = eigen.getRealEigenvalues();
      final double[] iEigenValues = eigen.getImagEigenvalues();
      final int n = rEigenValues.length;
      final double[][] d = new double[n][n];
      for (int i = n - 1; i >= 0; --i) {
        d[i][i] = Math.pow(rEigenValues[i], p);
        if (iEigenValues[i] != 0.0) {
          throw new NotImplementedException("Cannot handle complex eigenvalues in getPower");
        }
      }
      final RealMatrix res = eigen.getV().multiply((new Array2DRowRealMatrix(d)).multiply(eigen.getVT()));
      return CommonsMathWrapper.wrap(res);
    }
    throw new IllegalArgumentException("Can only find pow of DoubleMatrix2D; have " + m.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getTrace(com.opengamma.math.matrix
   * .Matrix)
   */
  @Override
  public double getTrace(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      return temp.getTrace();
    }
    throw new IllegalArgumentException("Can only find trace of DoubleMatrix2D; have " + m.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#getTranspose(com.opengamma.math
   * .matrix.Matrix)
   */
  @Override
  public DoubleMatrix2D getTranspose(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      final RealMatrix temp = CommonsMathWrapper.wrap((DoubleMatrix2D) m);
      return CommonsMathWrapper.wrap(temp.transpose());
    }
    throw new IllegalArgumentException("Can only find transpose of DoubleMatrix2D; have " + m.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.matrix.MatrixAlgebra#multiply(com.opengamma.math.matrix
   * .Matrix, com.opengamma.math.matrix.Matrix)
   */
  @Override
  public Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      final RealMatrix t1 = CommonsMathWrapper.wrap((DoubleMatrix2D) m1);
      final RealMatrix t2 = CommonsMathWrapper.wrap((DoubleMatrix2D) m2);
      return CommonsMathWrapper.wrap(t1.multiply(t2));
    }
    throw new IllegalArgumentException("Can only find inner product of DoubleMatrix1D; have " + m1.getClass() + " and " + m2.getClass());
  }

}
