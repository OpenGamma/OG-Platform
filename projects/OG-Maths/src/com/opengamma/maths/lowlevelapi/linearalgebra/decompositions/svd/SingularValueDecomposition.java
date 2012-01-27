/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.decompositions.svd;

import java.util.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS1;

/**
 * Does svd, this is currently clunky as its being translated from a prototype I wrote in a rather nice vector language :)
 * TODO: Clean up and review. There are a load of possibilities and branches/algs to try along with the usual cache/fake SSE considerations.    
 */
public class SingularValueDecomposition {

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
      // so we need to swap/mangle u&v them
      SingularValueDecompositionFullUSV tmp = svdVector(data);
      return new SingularValueDecompositionFullUSV(tmp.getMatrixV(), tmp.getMatrixS(), tmp.getMatrixV());
    }

    // Main SVD code. Decide if we need to transpose A and make a memcpy that we can destroy
    double[] matrixAnew;
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

    // stu - implement these branches
    //    if (m >= 5 * n / 3) { // we have a system that is ripe for QR first
    //
    //    } else { // hit standard bi-diag
    // bidiag

    double[] d = new double[n];
    double[] dp1 = new double[n - 1];
    biDiag(data, m, n, d, dp1);

    // then chase

    //    }

    return null;

  }

  private static void biDiag(double[] data, int m, int n, double d[], double dp1[]) {
    reduceColSpace(data, m, n);
    reduceRowSpace(data, m, n);
    for (int i = 0; i < n - 1; i++) {
      d[i] = data[i * n + i];
      dp1[i] = data[i * n + i + 1];
    }
    d[n - 1] = data[n * n - 1];
    System.out.println("Diag = " + Arrays.toString(d));
    System.out.println("Super Diag = " + Arrays.toString(dp1));
  }

  /*
   * Reflect the row space about the diag to get an bidiag matrix, should be called after reduceColSpace,
   * QR form is assumed such that the current data structure is upper triangular, we can therefore ignore the lower part
   */
  private static void reduceRowSpace(double[] data, int m, int n) {
    double ip;
    double nrm2;
    int ini;
    int inj;
    double data0;
    double housesq;
    double beta;
    double house0;
    double[] house = new double[n]; // this is slightly wasteful but saves a lot of indexing pain
    double[] omega = new double[m];

    //    if(j<=n-2)
    //      [v,beta]=houseHolder(Anew(j,j+1:n)');
    //      Anew(j:m,j+1:n)=Anew(j:m,j+1:n)*(eye(n-j)-beta*v*v');
    //   %   Anew(j,j+2:n)=v(2:n-j)';
    //    end

    // walk in row space, as our data upper triangular we can just trample around in 0..n
    // we also want to be offset by 1 so that the Householder transforms in the row space don't mess up the column space
    // also conveniently, the data layout is row major so we walk in the direction of the data
    for (int i = 0; i < n - 1; i++) {
      // house is our aptly named Householder vector, which is formed and normalised here...
      Arrays.fill(house, 0d);
      ip = 0d;
      // form inner product of row i
      for (int j = i + 2; j < n; j++) {
        inj = i * n + j;
        ip += data[inj] * data[inj];
        house[j] = data[inj];
      }
      ini = i * n + i + 1;
      data0 = data[ini];
      // branch on first value, this is the Partlett idea to stop catastrophic cancellation.
      nrm2 = Math.sqrt(data0 * data0 + ip);
      if (data0 <= 0) {
        house[i + 1] = data0 - nrm2;
      } else {
        house[i + 1] = -ip / (data0 + nrm2);
      }
      housesq = house[i + 1] * house[i + 1];
      beta = (2 * housesq) / (ip + housesq); // beta stores 2/(house^T*house).
      // normalise the householder     
      house0 = house[i + 1];
      for (int j = i + 1; j < n; j++) {
        house[j] = house[j] / house0;
      }

      // We need to do this: P = (I - beta*house*house^T)
      // then A*P is the transform for the row space reduction
      // So we can form up a nice row major matrix vector product and an outer product update:
      // P*A = A - omega*house^T; where omega = beta*A*house
      // *but* we also note that house becomes shorter by 1 each time, so the work to do decreases
      // TODO: This is a BLAS2 offset DGEMV call write it!
      Arrays.fill(omega, 0d);
      for (int j = i; j < m; j++) { // start on row i
        int idx0 = j * n; // step into the jth row 
        for (int k = j; k < n; k++) { // walk triangle
          omega[j] += data[idx0 + k] * house[k];
        }
      }
      for (int k = i; k < n; k++) {
        omega[k] *= beta;
      }

      // We want to do A - omega*house^T, but *only* for the bit of A that hasn't been already reflected
      // *and* is in the upper triangle
      // do block first
      for (int j = i; j < n - 1; j++) {
        for (int k = i; k < n; k++) {
          System.out.println("Rowspace: Hitting =" + (j * n + k) + ". house[k]=" + house[k] + ". omega[j]=" + omega[j] + ". prod=" + (house[k] * omega[j]) + ". data=" + data[j * n + k]);
          data[j * n + k] -= house[k] * omega[j];
        }

      }

      System.out.println("house=" + Arrays.toString(house));
      //      System.out.println("omega=" + Arrays.toString(omega));
      //      System.out.println("beta=" + beta);
      //      System.out.println("d=" + Arrays.toString(d));
      System.out.println("row space: A=");
      printToMatrix(data, m, n);
    }
  }

  /*
   * Reflect the column space about the diag to get an upper triangular matrix
   */
  private static void reduceColSpace(double[] data, int m, int n) {
    double ip;
    double nrm2;
    int ini;
    double data0;
    double housesq;
    double beta;
    double house0;
    double[] house = new double[m]; // this is slightly wasteful but saves a lot of indexing pain
    double[] omega = new double[n];
    int jni;

    // walk in col space, TODO: it might be better cache wise to walk backwards, investigate 
    for (int i = 0; i < n; i++) {

      // house is our aptly named Householder vector, which is formed and normalised here...
      Arrays.fill(house, 0d);
      ip = 0d;
      // form inner product of column i
      for (int j = i + 1; j < m; j++) {
        jni = j * n + i;
        ip += data[jni] * data[jni];
        house[j] = data[jni];
      }
      ini = i * n + i;
      data0 = data[ini];
      // branch on first value, this is the Partlett idea to stop catastrophic cancellation.
      nrm2 = Math.sqrt(data0 * data0 + ip);
      if (data0 <= 0) {
        house[i] = data0 - nrm2;
      } else {
        house[i] = -ip / (data0 + nrm2);
      }
      housesq = house[i] * house[i];
      beta = (2 * housesq) / (ip + housesq); // beta stores 2/(house^T*house).
      // normalise the householder     
      house0 = house[i];
      for (int j = i; j < m; j++) {
        house[j] = house[j] / house0;
      }

      // We need to do this: P = (I - beta*house*house^T)
      // then P*A is the transform. Column entries left of and under the diag indexed by "i" can be set to 0 or ignored
      // the rest has to be crunched.
      // So we can form up a lovely matrix vector product and an outer product update:
      // P*A = A - house*omega^T; where omega = beta*A^T*house
      // *but* we also note that house becomes shorted by 1 each time, so the work to do decreases
      // TODO: This is a BLAS2 offset DGEMV call write it!
      Arrays.fill(omega, 0d);
      for (int j = i; j < m; j++) { // start on row i
        int idx0 = j * n; // step into the jth row 
        final double rhs = house[j];
        for (int k = i; k < n; k++) {
          omega[k] += data[idx0 + k] * rhs;
        }
      }
      for (int k = i; k < n; k++) {
        omega[k] *= beta;
      }

      // TODO: This is a BLAS2 DGER (if scaled by beta) make it so? Depends on crunch cost of zeros.
      // We want to do A - house*omega^T, but *only* for the bit of A that hasn't been already reflected
      for (int j = i; j < i + 1; j++) { // first walk in the triangulary bit
        for (int k = j; k < n; k++) {
          //          System.out.println("Tringle: Hitting ="+(j * n + k)+". house[j]="+house[j]+". omega[k]="+omega[k]);
          data[j * n + k] -= house[j] * omega[k];
        }
      }
      for (int j = i + 1; j < m; j++) { // next walk the square bit
        for (int k = i + 1; k < n; k++) {
          //        System.out.println("Block: Hitting ="+(j * n + k)+". house[j]="+house[j]+". omega[k]="+omega[k]+". prod="+(house[j] * omega[k])+". data="+data[j * n + k]);
          data[j * n + k] -= house[j] * omega[k];
        }
      }

      //      System.out.println("house=" + Arrays.toString(house));
      //      System.out.println("omega=" + Arrays.toString(omega));
      //      System.out.println("beta=" + beta);
      //      System.out.println("d=" + Arrays.toString(d));
      System.out.println("col space: A=");
      printToMatrix(data, m, n);
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
    System.out.println("houseV=" + Arrays.toString(h));
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

  // gets a column
  /**
   * 
   * @param data the data
   * @param n the number of columns in a matrix
   * @param colNumber the column number we want
   * @param startRow the row on which we want to start grabbing column entires
   * @param endRow the row on which we want to stop grabbing column entires
   * @return the column
   */
  private double[] getcol(double[] data, int n, int colNumber, int startRow, int endRow) {
    final int walklen = endRow - startRow;
    final int jmp = startRow * n;

    double[] tmp = new double[walklen];
    for (int i = 0; i < walklen; i++) {
      tmp[i] = data[jmp + i * n + colNumber];
    }
    return tmp;
  }

  // prints a matrix
  private static void printToMatrix(double[] x, int m, int n) {
    int in;
    for (int i = 0; i < m; i++) {
      in = i * n;
      for (int j = 0; j < n; j++) {
        System.out.print(x[in + j] + " ");
      }
      System.out.println("\n");
    }
  }

  /**
   * overload to stop memcpy
   * @param x
   * @return
   */
  public static HouseholderVector householder(double[] x) {
    return householder(x, 0, x.length);
  }

  /**
   * Does householder reflections
   * @param x a
   * @return a house holder vector
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
