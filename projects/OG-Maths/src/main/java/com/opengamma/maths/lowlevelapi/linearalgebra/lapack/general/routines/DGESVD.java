/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.routines;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS1;
import com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.types.SingularValueDecomposition.SingularValueDecompositionFullUSV;

/**
 * Does svd, this is currently clunky as its being translated from a prototype I wrote in a rather nice vector language :)
 * TODO: Clean up and review. There are a load of possibilities and branches/algs to try along with the usual cache/fake SSE considerations.    
 */
public class DGESVD {

  /**
   * Alg ideas  
   * @param aMatrix a matrix
   * @return U,S,V
   */
  public static SingularValueDecompositionFullUSV full(DenseMatrix aMatrix) {
    int m = aMatrix.getNumberOfRows();
    int n = aMatrix.getNumberOfColumns();
    double[] data = aMatrix.getData();

    if (m == 1 && n == 1) { // quick return
      return new SingularValueDecompositionFullUSV(new double[] {1 }, aMatrix.getData(), new double[] {1 });
    }

    if (m == 1) { // short cut, we have a row vector
      // u is 1 by definition
      // s is the 2 norm of the vector
      // v is computed by householder
      return svdVector(data);
    }

    if (n == 1) { // short cut, we have a column vector
      // u is computed by householder
      // s is the 2 norm of the vector
      // v is 1 by definition
      // so we need to swap/mangle u&v
      SingularValueDecompositionFullUSV tmp = svdVector(data);
      return new SingularValueDecompositionFullUSV(tmp.getMatrixV(), tmp.getMatrixS(), tmp.getMatrixV());
    }

    // Main SVD code. Decide if we need to transpose A and make a memcpy that we can destroy
    double[] matrixAnew;
    double[] matrixU = new double[m * m];
    double[] matrixVT = new double[n * n];
    boolean transposed = false;
    if (m < n) {
      matrixAnew = new double[data.length];
      for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
          matrixAnew[i * n + j] = data[j * m + i];
        }
      }
      transposed = true;
      int tmp = m;
      m = n;
      n = tmp;
    } else {
      matrixAnew = new double[data.length];
      System.arraycopy(data, 0, matrixAnew, 0, data.length);
      transposed = false;
    }

    double[] d = new double[n];
    double[] dp1 = new double[n - 1];
    if (m >= 5 * n / 3) { // we have a system that is ripe for QR first
      // TODO: implement QR variant, we have 1/2 of it below in fn reduceColSpace()
      DGEBRD.fullGolubReinsch(matrixAnew, m, n, matrixU, d, dp1, matrixVT);
    } else { // hit standard bi-diag
      DGEBRD.fullGolubReinsch(matrixAnew, m, n, matrixU, d, dp1, matrixVT);
    }

    // TODO: Sort and swap and stick in struct for return
    if (transposed) {
      return null;
    } else {
      return null;
    }
  }

  // fn svd's the row vector, swap u and v to get column, obviously this is just a pointer in memory
  // well, actually it's not, it'd be a lot easier if it was, let's just pretend.
  private static SingularValueDecompositionFullUSV svdVector(double[] data) {
    int n = data.length;
    // u is 1 by definition
    // s is the 2 norm of the vector
    // v is computed by householder
    double[] u = new double[1];
    double[] s = new double[1];
    double[] v = new double[n * n];
    u[0] = 1;
    s[0] = BLAS1.dnrm2(data);
    HouseholderVector hv = householder(data);
    // outer product
    final double beta = hv.getBeta();
    final double[] h = hv.getH();
    int in;
    for (int i = 0; i < n; i++) {
      in = i * n;
      for (int j = 0; j < n; j++) {
        v[in + j] = -beta * h[i] * h[j];
      }
      v[i * n + i] += 1; // by defn P=eye(n)-beta*h'*h;
    }
    return new SingularValueDecompositionFullUSV(u, s, v);
  }

  //  // gets a column, for debug purposes
  //  /**
  //   * 
  //   * @param data the data
  //   * @param n the number of columns in a matrix
  //   * @param colNumber the column number we want
  //   * @param startRow the row on which we want to start grabbing column entires
  //   * @param endRow the row on which we want to stop grabbing column entires
  //   * @return the column
  //   */
  //  private double[] getcol(double[] data, int n, int colNumber, int startRow, int endRow) {
  //    final int walklen = endRow - startRow;
  //    final int jmp = startRow * n;
  //
  //    double[] tmp = new double[walklen];
  //    for (int i = 0; i < walklen; i++) {
  //      tmp[i] = data[jmp + i * n + colNumber];
  //    }
  //    return tmp;
  //  }

  // prints a matrix, for debug purposes
//  private static void printToMatrix(double[] x, int m, int n) {
//    int in;
//    for (int i = 0; i < m; i++) {
//      in = i * n;
//      for (int j = 0; j < n; j++) {
//        System.out.print(x[in + j] + " ");
//      }
//      System.out.println("\n");
//    }
//  }

  /**
   * overload to stop memcpy
   * @param x the vector from which a Householder vector is required
   * @return a Householder vector based on x
   */
  public static HouseholderVector householder(double[] x) {
    return householder(x, 0, x.length);
  }

  /**
   * Does householder reflections
   * @param x the vector from which a Householder vector is required
   * @param start the position in the vector which is considered the starting position (to allow subvector computation)
   * @param end the position in the vector which is considered the end position (to allow subvector computation)
   * @return a Householder vector based on x
   */
  public static HouseholderVector householder(double[] x, int start, int end) {
    // This is an implementation of the pseudocode from "Matrix Computations 3rd Edition" by G. H. Golub and C. F. Van Loan, pp 210.
    // The lack of catastrophic cancellation is thanks to "Analysis of Algorithms for Reflections in BiSectors" SIAM review 13, 197-208. by B. N. Partlett.
    // There are a number of possibilities for scaling and sign within the Householder vector, following Golub and Van Loan we produce: 
    // (I - beta*h*h')x =  norm2(x)*e if x(1) <= 0,
    // (I - beta*h*h')x = -norm2(x)*e if x(1) > 0
    // Where h is the Householder vector, beta is a scaling (2/(h'*h)), e is the canonical vector.

    int n = x.length;
    double ip = 0d; // inner product of elements x[2:n]
    double[] h = new double[n]; // will hold the HouseHolder vector
    double beta;
    double nrm2;
    double h0;

    // form inner product
    for (int i = 1; i < n; i++) {
      ip += x[i] * x[i];
    }

    // assign h
    h[0] = 1d;
    System.arraycopy(x, 1, h, 1, n - 1);
    if (ip == 0) {
      beta = 0;
    } else {
      // branch on x[0] value, this is the Partlett idea.
      nrm2 = Math.sqrt(x[0] * x[0] + ip);
      if (x[0] <= 0) {
        h[0] = x[0] - nrm2;
      } else {
        h[0] = -ip / (x[0] + nrm2);
      }
      double tmp = h[0] * h[0];
      beta = 2 * tmp / (ip + tmp);
      // normalise the householder     
      h0 = h[0];
      for (int i = 0; i < n; i++) {
        h[i] = h[i] / h0;
      }
    }
    return new HouseholderVector(h, beta);
  }

}
