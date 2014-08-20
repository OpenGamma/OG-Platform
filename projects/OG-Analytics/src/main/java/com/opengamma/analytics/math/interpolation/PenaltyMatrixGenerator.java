/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.apache.commons.math.util.MathUtils.binomialCoefficient;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class PenaltyMatrixGenerator {

  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();

  /**
   * The penalty matrix, P, such the for weights vector, w, (w^T)*P*w is a quadratic penalty term. P is constructed as P = (D^T)*D, where
   * D is the kth order difference matrix such that D*x is the kth order difference vector of x (with first k terms zero) 
   * @param m Length of the weights vector.
   * @param k Difference order. Require m > k 
   * @return The penalty matrix P.
   */
  public static DoubleMatrix2D getPenaltyMatrix(final int m, final int k) {
    Validate.isTrue(k < m, "differce order too high");

    if (m == 0) {
      return DoubleMatrixUtils.getIdentityMatrix2D(m);
    }

    final double[][] data = new double[m][m];
    final int[] coeff = new int[k + 1];

    int sign = 1;
    for (int i = k; i >= 0; i--) {
      coeff[i] = (int) (sign * binomialCoefficient(k, i));
      sign *= -1;
    }

    for (int i = k; i < m; i++) {
      for (int j = 0; j < k + 1; j++) {
        data[i][j + i - k] = coeff[j];
      }
    }
    final DoubleMatrix2D d = new DoubleMatrix2D(data);

    final DoubleMatrix2D dt = MA.getTranspose(d);
    return (DoubleMatrix2D) MA.multiply(dt, d);
  }

  /**
   * get a kth order penalty matrix for a non-uniform grid. This is $d^Td$ where d is the kth order finite difference matrix 
   * @param x set of points
   * @param k order 
   * @return penalty matrix
   */
  public static DoubleMatrix2D getPenaltyMatrix(final double[] x, final int k) {
    final double range = x[x.length - 1] - x[0];
    final double scale = Math.pow(range, k);
    final DoubleMatrix2D d = (DoubleMatrix2D) MA.scale(getDiffMatrix(x, k), scale);
    final DoubleMatrix2D dt = MA.getTranspose(d);
    return (DoubleMatrix2D) MA.multiply(dt, d);
  }

  /**
   * get a kth order penalty matrix for a non-uniform grid who's values have been flattened to a vector 
   * @param x the grid positions in each dimension 
   * @param k the (finite difference) order
   * @param index which index to act on 
   * @return a penalty matrix 
   */
  public static DoubleMatrix2D getPenaltyMatrix(final double[][] x, final int k, final int index) {
    ArgumentChecker.noNulls(x, "x");
    final int dim = x.length;
    final int[] numElements = new int[dim];
    for (int i = 0; i < dim; i++) {
      numElements[i] = x[i].length;
    }
    final DoubleMatrix2D p = getPenaltyMatrix(x[index], k);

    return getMatrixForFlattened(numElements, p, index);
  }

  public static DoubleMatrix2D getPenaltyMatrix(final double[][] x, final int[] k, final double[] lambda) {
    ArgumentChecker.notEmpty(k, "k");
    ArgumentChecker.notEmpty(lambda, "lambda");
    final int dim = x.length;
    ArgumentChecker.isTrue(dim == k.length, "k different lenght to size");
    ArgumentChecker.isTrue(dim == lambda.length, "lambda different lenght to size");

    DoubleMatrix2D p = (DoubleMatrix2D) MA.scale(getPenaltyMatrix(x, k[0], 0), lambda[0]);
    for (int i = 1; i < dim; i++) {
      final DoubleMatrix2D temp = (DoubleMatrix2D) MA.scale(getPenaltyMatrix(x, k[i], i), lambda[i]);
      p = (DoubleMatrix2D) MA.add(p, temp);
    }
    return p;
  }

  /**
   * Assume a tensor has been flatten to a vector as $(x_{0,0}, x_{1,0}, x_{2,0}, \dots, x_{n,0}, x_{0,1}, x_{1,1}, \dots, x_{n,1}, \dots, x_{n,m})^T$
   * that is, the first index changes most rapidly. This produces a penalty matrix that acts on a given set of indexes only<P>
   * To produce a penalty matrix that acts on multiple indexes, produce one for each set of indexes and add them together (scaling if necessary)  
   * @param numElements The range of each index. In the example above, this would be {n,m} 
   * @param k Difference order. Require size[indices] > k
   * @param index Which set of indices does the matrix act on 
   * @return A penalty matrix 
   */
  public static DoubleMatrix2D getPenaltyMatrix(final int[] numElements, final int k, final int index) {
    ArgumentChecker.notEmpty(numElements, "size");
    final DoubleMatrix2D d = getPenaltyMatrix(numElements[index], k);

    return getMatrixForFlattened(numElements, d, index);
  }

  /**
   * for a matrix {{A_{0,0}, A_{0,1},...._A_{0,m},{A_{1,0}, A_{1,1},...._A_{1,m},...,{A_{n,0}, A_{n,1},...._A_{n,m}}
   * flatten to a vector {A_{0,0}, A_{0,1},...._A_{0,m}, A_{1,0}, A_{1,1},...._A_{1,m},...,A_{n,0}, A_{n,1},...._A_{n,m}}
   * @param aMatrix A matrix
   * @return a the flattened matrix 
   */
  public static DoubleMatrix1D flattenMatrix(final DoubleMatrix2D aMatrix) {
    final int elements = aMatrix.getNumberOfElements();
    final double[] data = new double[elements];
    final int nRows = aMatrix.getNumberOfRows();
    final int nCols = aMatrix.getNumberOfColumns();
    int pos = 0;
    for (int i = 0; i < nRows; i++) {
      System.arraycopy(aMatrix.getData()[i], 0, data, pos, nCols);
      pos += nCols;
    }
    return new DoubleMatrix1D(data);
  }

  /**
   * Assume a tensor has been flatten to a vector as {A_{0,0}, A_{0,1},...._A_{0,m}, A_{1,0}, A_{1,1},...._A_{1,m},...,A_{n,0}, A_{n,1},...._A_{n,m}}
   *  (see {@link flattenMatrix}) that is, the <b>last</b> index changes most rapidly. 
   * Given a matrix, M, that acts on the elements of one index only, i.e.
   * $$y_{i, i_1, i_2, \dots,i_{k-1}, i_{k+1},\dots, i_n} =  \sum_{i_k=0}^{N_k-1} M_{i,i_k}  x_{i_1, i_2, \dots,i_k,\dots, i_n} $$
   * form the larger matrix that acts on the flattened vector.  
   * @param numElements The number of elements in each index. In the example above, this would be {n,m} 
   * @param m the matrix M
   * @param index Which index does the matrix act on 
   * @return A (larger) matrix which acts on the flattened vector 
   */
  public static DoubleMatrix2D getMatrixForFlattened(final int[] numElements, final DoubleMatrix2D m, final int index) {
    ArgumentChecker.notEmpty(numElements, "numElements");
    final int dim = numElements.length;
    ArgumentChecker.notNull(m, "m");
    ArgumentChecker.isTrue(index >= 0 && index < dim, "indices outside range");
    ArgumentChecker.isTrue(m.getNumberOfColumns() == numElements[index], "columns in m ({}) do not match numElements for index ({})", m.getNumberOfColumns(), numElements[index]);
    int postProduct = 1;
    int preProduct = 1;
    for (int j = index + 1; j < dim; j++) {
      preProduct *= numElements[j];
    }
    for (int j = 0; j < index; j++) {
      postProduct *= numElements[j];
    }
    DoubleMatrix2D temp = m;
    if (preProduct != 1) {
      temp = (DoubleMatrix2D) MA.kroneckerProduct(temp, DoubleMatrixUtils.getIdentityMatrix2D(preProduct));
    }
    if (postProduct != 1) {
      temp = (DoubleMatrix2D) MA.kroneckerProduct(DoubleMatrixUtils.getIdentityMatrix2D(postProduct), temp);
    }

    return temp;
  }

  /**
   * Assume a tensor has been flatten to a vector as $(x_{0,0}, x_{1,0}, x_{2,0}, \dots, x_{n,0}, x_{0,1}, x_{1,1}, \dots, x_{n,1}, \dots, x_{n,m})^T$
   * that is, the first index changes most rapidly. This produces the sum of penalty matrices (or order given by k) with each scaled 
   * by lambda. 
   * @param size The range of each index. In the example above, this would be {n,m} 
   * @param k The difference order for each dimension 
   * @param lambda The scaling for each dimension 
   * @return  A penalty matrix 
   */
  public static DoubleMatrix2D getPenaltyMatrix(final int[] size, final int[] k, final double[] lambda) {
    ArgumentChecker.notEmpty(size, "size");
    ArgumentChecker.notEmpty(k, "k");
    ArgumentChecker.notEmpty(lambda, "lambda");
    final int dim = size.length;
    ArgumentChecker.isTrue(dim == k.length, "k different lenght to size");
    ArgumentChecker.isTrue(dim == lambda.length, "lambda different lenght to size");

    DoubleMatrix2D p = (DoubleMatrix2D) MA.scale(getPenaltyMatrix(size, k[0], 0), lambda[0]);
    for (int i = 1; i < dim; i++) {
      final DoubleMatrix2D temp = (DoubleMatrix2D) MA.scale(getPenaltyMatrix(size, k[i], i), lambda[i]);
      p = (DoubleMatrix2D) MA.add(p, temp);
    }

    return p;
  }

  /**
   * Get the kth order finite difference derivative matrix for a non-uniform set of points 
   * @param x A non-uniform set of points
   * @param k The order <b>Only first and second order are currently implemented</b>
   * @return derivative matrix
   */
  public static DoubleMatrix2D getDiffMatrix(final double[] x, final int k) {
    ArgumentChecker.notEmpty(x, "x");
    ArgumentChecker.notNegative(k, "k");
    final int size = x.length;
    ArgumentChecker.isTrue(k < size, "order order too high");
    if (k == 0) {
      return new IdentityMatrix(size);
    } else if (k > 2) {
      throw new NotImplementedException("cannot handle order (k) > 2");
      //      final int kd2 = k / 2;
      //      final DoubleMatrix2D d2 = getDiffMatrix(x, 2);
      //      final DoubleMatrix2D d = MA.getPower(d2, kd2);
      //      if (k % 2 == 0) {
      //        return d;
      //      } else {
      //        final DoubleMatrix2D rem = getDiffMatrix(x, 1);
      //        return (DoubleMatrix2D) MA.multiply(rem, d);
      //      }

    } else {
      final double[] dx = new double[size - 1];
      final double[] dx2 = new double[size - 1];
      for (int i = 0; i < (size - 1); i++) {
        final double temp = x[i + 1] - x[i];
        ArgumentChecker.isTrue(temp > 0.0, "x not in ascending order, or two identical points");
        dx[i] = temp;
        dx2[i] = temp * temp;
      }
      final double[] w = new double[size - 2];
      for (int i = 0; i < (size - 2); i++) {
        w[i] = 1.0 / dx[i] / dx[i + 1] / (dx[i] + dx[i + 1]);
      }

      final DoubleMatrix2D res = new DoubleMatrix2D(size, size);
      final double[][] data = res.getData();

      if (k == 1) {
        for (int i = 1; i < (size - 1); i++) {
          data[i][i - 1] = -w[i - 1] * dx2[i];
          data[i][i] = w[i - 1] * (dx2[i] - dx2[i - 1]);
          data[i][i + 1] = w[i - 1] * dx2[i - 1];
        }
        //ends 
        //        data[0][0] = -w[0] * dx[1] * (2 * dx[0] + dx[1]);
        //        data[0][1] = w[0] * (dx2[0] + dx2[1] + 2 * dx[0] * dx[1]);
        //        data[0][2] = -w[0] * dx2[0];
        //        data[size - 1][size - 3] = w[size - 3] * dx2[size - 2];
        //        data[size - 1][size - 2] = -w[size - 3] * (dx2[size - 3] + dx2[size - 2] + 2 * dx[size - 2] * dx[size - 3]);
        //        data[size - 1][size - 1] = w[size - 3] * dx[size - 3] * (2 * dx[size - 2] + dx[size - 3]);
        return res;
      } else {
        for (int i = 1; i < (size - 1); i++) {
          data[i][i - 1] = 2 * w[i - 1] * dx[i];
          data[i][i] = -2 * w[i - 1] * (dx[i] + dx[i - 1]);
          data[i][i + 1] = 2 * w[i - 1] * dx[i - 1];
        }
        //ends 
        //        data[0] = data[1];
        //        data[size - 1] = data[size - 2];
        return res;
      }
    }
  }

}
