/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.routines;

import java.util.Arrays;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionIllegalArgument;
import com.opengamma.maths.lowlevelapi.linearalgebra.lapack.auxiliary.SanityChecker;

/**
 * Bidiagonalises and m x n matrix A to upper bidiagonal matrix D through orthogonal transforms U and V such that
 * U^T * A * V = D 
 * TODO: implement the lower bidiagonal variant
 * TODO: strategy mangling depending on forms, dimensions, cache sizes etc
 */
public class DGEBRD {

  /**
   * Bidiagonalises matrix A as would be done in FORTRAN, direct manipulation and mallocs() assumed as with FORTRAN.
   * The matrix A is destroyed in the processes.
   * Classic Golub-Reinsch step, see "Matrix Computations, Third Edition" by G. Golub and C. F. Van Loan.
   * See commentary in code for further information, col space is fully commented, row space is essentially the same and so is not.
   * Operation: U^T * A * V = D 
   * @param matrixA an m by n matrix to be bidiagonalised
   * @param m the number of rows in matrixA
   * @param n the number of columns in matrixA
   * @param matrixUT See class headers, the transpose of an orthonogal m by m matrix 
   * @param vectorD An n by 1 vector of the diagonal of the required bidiagonal form
   * @param vectorDP1 An (n-1) by 1 vector of the superdiagonal of the required bidiagonal form
   * @param matrixV See class headers, an orthonogal n by n matrix 
   */
  public static void fullGolubReinsch(double[] matrixA, final int m, final int n, double[] matrixUT, double[] vectorD, double[] vectorDP1, double[] matrixV) {
    if (m < 1) {
      throw new MathsExceptionIllegalArgument("System dimension is < 1, given by variable \"m\" ");
    }
    if (n < 1) {
      throw new MathsExceptionIllegalArgument("System dimension is < 1, given by variable \"n\" ");
    }

    // Test A sanity
    SanityChecker.checkMatrixMallocIsAsExpected(matrixA, m, n, "matrixA");

    // Test UT sanity
    SanityChecker.checkMatrixMallocIsAsExpected(matrixUT, m, m, "matrixUT");

    // Test D sanity
    SanityChecker.checkVectorMallocIsAsExpected(vectorD, n, "vectorD");

    // Test DP! sanity
    SanityChecker.checkVectorMallocIsAsExpected(vectorDP1, (n - 1), "vectorDP1");

    // Test V sanity
    SanityChecker.checkMatrixMallocIsAsExpected(matrixV, n, n, "matrixV");

    // seems the mallocs are sane, proceed

    /*
     * Bidiagonalisation
     * Classic Golub-Reinsch step, see "Matrix Computations, Third Edition" by G. Golub and C. F. Van Loan.
     * Householder transforms are applied alternately to the colspace then the rowspace with a 1 offset to leave a bidiagonal matrix.
     * The essential part of the transforms are stored in the area of matrixA zeroed by the transform.
     */
    double[] data = matrixA; // to match dev space
    double ip;
    double nrm2;
    int ini, inj, jni;
    double data0;
    double housesq;
    double beta;
    double house0;
    double tmp;

    // holds 2/(v'*v) for extracting U and V later.
    double[] betaU = new double[m];
    double[] betaV = new double[n];

    // Full width stores of Householder vector/products, this is slightly wasteful but saves a lot of indexing pain and malloc/free
    double[] houseCol = new double[m];
    double[] omegaCol = new double[n];
    double[] houseRow = new double[n];
    double[] omegaRow = new double[m];

    // set U and V as identity
    for (int k = 0; k < m; k++) {
      matrixUT[k * m + k] = 1.;
    }
    for (int k = 0; k < n; k++) {
      matrixV[k * n + k] = 1.;
    }

    // walk in col space, TODO: it might be better cache wise to walk backwards, investigate 
    for (int i = 0; i < n; i++) {
      Arrays.fill(houseCol, 0d);
      houseCol[i] = 1;
      beta = 0;
      ip = 0d;
      // form inner product of column i
      for (int j = i + 1; j < m; j++) {
        jni = j * n + i;
        ip += data[jni] * data[jni];
        houseCol[j] = data[jni];
      }
      if (ip != 0) {
        ini = i * n + i;
        data0 = data[ini];
        // Form Householder vector, branch on first value to reduce catastrophic cancellation fun 
        // as described in "Analysis of Algorithms for Reflections in Bisectors", Parlett, B. N., SIAM Review 13, 197-208.
        // Implementation based on pseudo-code of Golub and Van Loan (see above for reference).
        nrm2 = Math.sqrt(data0 * data0 + ip);
        if (data0 <= 0) {
          houseCol[i] = data0 - nrm2;
        } else {
          houseCol[i] = -ip / (data0 + nrm2);
        }
        housesq = houseCol[i] * houseCol[i];
        beta = (2 * housesq) / (ip + housesq); // beta stores 2/(house^T*house).
        // normalise the householder     
        house0 = houseCol[i] != 0 ? houseCol[i] : 1;
        for (int j = i; j < m; j++) {
          houseCol[j] = houseCol[j] / house0;
        }

        // We need to do this: P = (I - beta*house*house^T)
        // then P*A is the transform. Column entries left of and under the diag indexed by "i" can be set to 0 or ignored
        // the rest has to be crunched.
        // So we can form up a lovely matrix vector product and an outer product update:
        // P*A = A - house*omega^T; where omega = beta*A^T*house
        // *but* we also note that house becomes shorted by 1 each time, so the work to do decreases
        // TODO: This is a BLAS2 offset DGEMV call write it!
        Arrays.fill(omegaCol, 0d);
        for (int j = i; j < m; j++) { // start on row i
          int idx0 = j * n; // step into the jth row 
          final double rhs = houseCol[j];
          for (int k = i; k < n; k++) {
            omegaCol[k] += data[idx0 + k] * rhs;
          }
        }
        for (int k = i; k < n; k++) {
          omegaCol[k] *= beta;
        }

        // TODO: This is a BLAS2 DGER (if scaled by beta) make it so? Depends on crunch cost of zeros.
        // We want to do A - house*omega^T, but *only* for the bit of A that hasn't been already reflected
        for (int j = i; j < i + 1; j++) { // first walk in the triangulary bit
          for (int k = j; k < n; k++) {
            data[j * n + k] -= houseCol[j] * omegaCol[k];
          }
        }
        for (int j = i + 1; j < m; j++) { // next walk the square bit
          for (int k = i + 1; k < n; k++) {
            data[j * n + k] -= houseCol[j] * omegaCol[k];
          }
        }
      }

      // store beta for unwind
      betaU[i] = beta;
      // store useful bit of householder for unwind
      for (int j = i + 1; j < m; j++) {
        data[j * n + i] = houseCol[j];
      }

      // now mangle the row from i+2:n so data becomes bidiag on row i
      if (i < n - 1) {
        // also conveniently, the data layout is row major so we walk in the direction of the data
        Arrays.fill(houseRow, 0d);
        houseRow[i + 1] = 1;
        beta = 0;
        ip = 0d;
        for (int j = i + 2; j < n; j++) {
          inj = i * n + j;
          ip += data[inj] * data[inj];
          houseRow[j] = data[inj];
        }
        if (ip != 0) {
          ini = i * n + i + 1;
          data0 = data[ini];
          nrm2 = Math.sqrt(data0 * data0 + ip);
          if (data0 <= 0) {
            houseRow[i + 1] = data0 - nrm2;
          } else {
            houseRow[i + 1] = -ip / (data0 + nrm2);
          }
          housesq = houseRow[i + 1] * houseRow[i + 1];
          beta = (2 * housesq) / (ip + housesq);
          house0 = houseRow[i + 1];
          for (int j = i + 1; j < n; j++) {
            houseRow[j] = houseRow[j] / house0;
          }
          Arrays.fill(omegaRow, 0d);
          for (int j = i; j < m; j++) { // start on row i
            int idx0 = j * n; // step into the jth row 
            for (int k = i; k < n; k++) { // walk triangle
              omegaRow[j] += data[idx0 + k] * houseRow[k];
            }
          }
          for (int k = i; k < m; k++) {
            omegaRow[k] *= beta;
          }

          // We want to do A - omega*house^T, but *only* for the bit of A that hasn't been already reflected
          // do bd first
          for (int j = i; j < i + 1; j++) {
            for (int k = i; k < i + 2; k++) {
              data[j * n + k] -= houseRow[k] * omegaRow[j];
            }
          }
          // do block second
          for (int j = i + 1; j < m; j++) {
            for (int k = 0; k < n; k++) {
              data[j * n + k] -= houseRow[k] * omegaRow[j];
            }
          }
        }
        // store beta and info
        betaV[i] = beta;

        // store useful bit of householder for unwind
        for (int j = i + 2; j < n; j++) {
          data[i * n + j] = houseRow[j];
        }

      }
    }

    // Unwind U, expand as DGEMV() going up from smallest sub-block (backwards accumulation)
    Arrays.fill(omegaRow, 0);
    for (int j = n - 1; j > -1; j--) {
      Arrays.fill(houseCol, 0);
      houseCol[j] = 1;
      for (int k = j + 1; k < m; k++) {
        houseCol[k] = data[k * n + j];
      }
      Arrays.fill(omegaRow, 0);
      for (int walkrows = j; walkrows < m; walkrows++) {
        tmp = houseCol[walkrows];
        for (int walkcols = j; walkcols < m; walkcols++) {
          omegaRow[walkcols] = omegaRow[walkcols] + matrixUT[walkrows * m + walkcols] * tmp;
        }
      }
      for (int walkrows = j; walkrows < m; walkrows++) {
        tmp = houseCol[walkrows] * betaU[j];
        for (int walkcols = j; walkcols < m; walkcols++) {
          matrixUT[walkrows * m + walkcols] -= tmp * omegaRow[walkcols];
        }
      }
    }

    // Unwind V, expand as DGEMV() going up from smallest sub-block (backwards accumulation)
    Arrays.fill(omegaCol, 0);
    for (int j = n - 2; j > 0; j--) {
      Arrays.fill(houseRow, 0);
      houseRow[j] = 1;
      for (int k = j + 1; k < n; k++) {
        houseRow[k] = data[(j - 1) * n + k];
      }
      Arrays.fill(omegaCol, 0);
      for (int walkrows = j; walkrows < n; walkrows++) {
        for (int walkcols = j; walkcols < n; walkcols++) {
          omegaCol[walkrows] += matrixV[walkrows * n + walkcols] * houseRow[walkcols];
        }
      }
      for (int walkrows = j; walkrows < n; walkrows++) {
        tmp = omegaCol[walkrows] * betaV[j - 1];
        for (int walkcols = j; walkcols < n; walkcols++) {
          matrixV[walkrows * n + walkcols] -= tmp * houseRow[walkcols];
        }
      }
    }
  } // method end    

}
