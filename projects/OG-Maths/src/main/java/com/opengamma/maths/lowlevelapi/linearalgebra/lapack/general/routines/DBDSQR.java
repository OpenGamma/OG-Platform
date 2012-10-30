/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.routines;

import java.util.Arrays;

import com.opengamma.maths.commonapi.MathsConstants;
import com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.auxiliary.DLARTG;
import com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.auxiliary.DLASV2;

/**
 * Computes the singular values and possibly the singular vectors of   a bi-digaonal matrix.
 * The algorithm used is a variant of the implicit zero-shift QR algorithm as described in 
 * "Matrix Computations, Third Edition" by G. Golub and C. F. Van Loan.
 * Some corrections are made as outlined in
 * "Computing  Small Singular Values of Bidiagonal Matrices With Guaranteed High Relative Accuracy"
 * by J. Demmel and W. Kahan, LAPACK Working Note #3 http://www.netlib.org/lapack/lawnspdf/lawn03.pdf
 * 
 * Input to this routine will generally come from calls made to DGEBRD().
 * 
 * TODO: Algorithmic improvements:
 * Perhaps add further corrections with regards to:
 * 1) The iteration direction
 * 2) Convergence acceleration
 * 3) Accumulating self-scaling products of Given's rotations as outlined in 
 * "Fast Plane Rotations with Dynamic Scaling" Andrew A. Anda and Haesun Park, SIAM. J. Matrix Anal. & Appl. 15, 162 (1994).
 *
 * TODO Computational improvements:
 * 1) Implement for lower bi-diagonal.
 * 2) Mess about with cache sizes regarding the speed at which rotations can be applied to columns/rows depending on matrix origentation
 *
 */
public class DBDSQR {

  /**
   * The full Golub Kahan computation, from an upper bi-diagonal decomposition, B, comprising diagonal d and super diagonal dp1 the routine will
   * compute the singular values \Sigma (on return located in d), and apply transforms as appropriate to U and V such that
   * B = U * \Sigma * V^T
   * @param d the diagonal of the bi-diagonal matrix, on return contains the singular values (\Sigma)
   * @param dp1 the super diagonal of the bi-diagonal matrix, on return contains zeros to machine precision
   * @param matrixU the left singular vectors
   * @param matrixV the right singular vectors
   * @param m the number of rows in the system 
   * @param n the number of columns in the system
   */
  public static void fullGolubKahan(double[] d, double[] dp1, double[] matrixU, double[] matrixV, final int m, final int n) {
    int iter = 0;
    int blockstart = 0;
    int blockestore = n + 1;
    int blocksstore = 0;
    int blockiter = 0;
    int blockend = n - 1; // stupid off by 1 offsets, grrrrrr
    final int nm2 = n - 2;
    boolean zerofound = false;
    boolean jump = false;

    double f, h, g;
    int killx, killy;
    double[] cosinesine = new double[2];
    double tau1, tau2, spare;
    double[] bd;
    double sigma1, sigma2;
    double y, z;
    int k, kp1;
    double csn, sn;
    final double epsSeperate = MathsConstants.eps;
    final double small = MathsConstants.eps;

    while (true) {

      iter = iter + 1;
      for (int kidx = 0; kidx < nm2; kidx++) {
        if (Math.abs(dp1[kidx]) < (epsSeperate + small * (Math.abs(d[kidx]) + Math.abs(d[kidx + 1]))))
        {
          dp1[kidx] = 0;
        }
      }
      for (int im = blockend; im > 0; im--) {
        if (Math.abs(dp1[im - 1]) > small) {
          blockend = im;
          break;
        }
      }
      blockstart = 0;
      for (int im = blockend; im > 0; im--) {
        if (Math.abs(dp1[im - 1]) < small) {
          blockstart = im;
          break;
        }
      }
      if (blockestore == blockend && blocksstore == blockstart) {
        blockiter = blockiter + 1;
      } else {
        blockiter = 0;
      }
      blocksstore = blockstart;
      blockestore = blockend;

      // we have converged
      if (blockstart == blockend) {
        break;
      }

      // we are bored trying to converge, this is a magic number
      if (iter == 35 * n * n) {
        System.out.println("SVD: Exit called as max iterations reached: n =" + n + " . Iteration count=" + iter);
        System.out.println("d=" + Arrays.toString(d) + "\n .dp1=" + Arrays.toString(dp1));
        break;
      }
      zerofound = false;
      jump = false;
      // Walk diagonal looking for zeros, if found rotate off matrix edge,
      // jump when we get to the edge and call the final rotation from a standard GK step.
      for (int im = blockstart; im < blockend; im++) {
        if (Math.abs(d[im]) < small) {
          zerofound = true;
          killy = im;
          // if we are at the edge of a block or matrix, jump for final rotation
          if (im + 1 > blockend || im + 1 >= n) {
            jump = true;
            break;
          }
          cosinesine = DLARTG.givens(d[killy + 1], dp1[killy]);
          csn = cosinesine[0];
          sn = cosinesine[1];
          tau1 = dp1[killy];
          tau2 = d[killy + 1];
          dp1[killy] = tau1 * csn - tau2 * sn;
          d[killy + 1] = tau2 * csn + tau1 * sn;
          spare = dp1[killy];
          for (killx = im; killx < n - 2; killx++) {
            tau1 = spare;
            tau2 = dp1[killx + 1];
            dp1[killx + 1] = tau2 * csn + tau1 * sn;
            spare = tau1 * csn - tau2 * sn;
            for (int j = 0; j < m; j++) {
              tau1 = matrixU[j * m + killx + 1];
              tau2 = matrixU[j * m + killy];
              matrixU[j * m + killx + 1] = tau1 * csn + tau2 * sn;
              matrixU[j * m + killy] = tau2 * csn - tau1 * sn;
            }

            cosinesine = DLARTG.givens(d[killx + 2], spare);
            csn = cosinesine[0];
            sn = cosinesine[1];
            tau1 = spare;
            tau2 = d[killx + 2];
            d[killx + 2] = tau2 * csn + tau1 * sn;
            spare = tau1 * csn - tau2 * sn;
          }

          for (int j = 0; j < m; j++) {
            tau1 = matrixU[j * m + n - 1];
            tau2 = matrixU[j * m + killy];
            matrixU[j * m + n - 1] = tau1 * csn + tau2 * sn;
            matrixU[j * m + killy] = tau2 * csn - tau1 * sn;
          }

          break;
        }
      }
      // Classic zigzag down diagonal to try and drop tail of diag(X,+1) out as zero so problem can be deflated
      if (!zerofound || jump) {
        // Special case, analytical solution for 2x2 SVD possible. LAWN gives code with max error of ~48ULP.
        // Also stops pathological 2x2 systems where the SVs are very close numerically and the bidiagonal term is
        // of a similar magnitude (convergence via standard rotations is poor ~ max(eig())/min(eig()) ).
        if (blockend - blockstart == 1) {
          // run Demmel-Kahan step on 
          // |f g|
          // |0 h|
          // For more information see:
          // lawn03.pdf pp20
          // Computing the Singular Values of 2-by-2 Complex Matrices, Sanzheng Qiao and Xiaohong Wang. For expansion of derivation.
          // lawn46.pdf
          // Alg from Bai and Demmel LAPACK implementation
          f = d[blockstart];
          h = d[blockend];
          g = dp1[blockstart];

          bd = DLASV2.baiDemmel(f, g, h);
          sigma1 = bd[0];
          sigma2 = bd[1];
          d[blockstart] = sigma1;
          d[blockend] = sigma2;
          dp1[blockstart] = 0.;

          // apply to V
          final double cosineright = bd[4];
          final double sineright = bd[5];
          for (int j = 0; j < n; j++) {
            tau1 = matrixV[blockstart * n + j];
            tau2 = matrixV[blockend * n + j];
            matrixV[blockstart * n + j] = tau1 * cosineright + tau2 * sineright;
            matrixV[blockend * n + j] = tau2 * cosineright - tau1 * sineright;
          }

          // apply to U
          final double cosineleft = bd[2];
          final double sineleft = bd[3];
          for (int j = 0; j < m; j++) {
            tau1 = matrixU[j * m + blockstart];
            tau2 = matrixU[j * m + blockend];
            matrixU[j * m + blockstart] = tau1 * cosineleft + tau2 * sineleft;
            matrixU[j * m + blockend] = tau2 * cosineleft - tau1 * sineleft;
          }
        } else {
          // Do a standard zigzag down the bidiag structure.
          // Var "spare" is used to hold the extra number as a result of rotations, it's shuffled around a lot!
          // Loop is peeled by 1 iteration and then this iteration skewed as 2 half iterations, saves a branch due to edge cases at the top and bottom of the bidiag.

          // Get lowest 2x2 block and compute SVs
          f = d[blockend - 1];
          h = d[blockend];
          g = dp1[blockend - 1];
          bd = DLASV2.baiDemmel(f, g, h);
          sigma2 = bd[1];

          // First rotation is based on eig(G^T*G), G="lowest 2x2"
          y = d[blockstart] * d[blockstart] - sigma2 * sigma2;
          z = d[blockstart] * dp1[blockstart];
          spare = 0;
          k = blockstart;
          // 1/2 loop skew
          cosinesine = DLARTG.givens(y, z);
          csn = cosinesine[0];
          sn = cosinesine[1];
          tau1 = d[k];
          tau2 = dp1[k];
          d[k] = tau1 * csn + tau2 * sn;
          dp1[k] = tau2 * csn - tau1 * sn;
          tau1 = spare;
          tau2 = d[k + 1];
          spare = tau1 * csn + tau2 * sn;
          d[k + 1] = tau2 * csn - tau1 * sn;
          for (int j = 0; j < n; j++) {
            tau1 = matrixV[k * n + j];
            tau2 = matrixV[(k + 1) * n + j];
            matrixV[k * n + j] = tau1 * csn + tau2 * sn;
            matrixV[(k + 1) * n + j] = tau2 * csn - tau1 * sn;
          }

          y = d[k];
          z = spare;
          for (k = blockstart; k < blockend - 1; k++) {
            cosinesine = DLARTG.givens(y, z);
            csn = cosinesine[0];
            sn = cosinesine[1];
            tau1 = d[k];
            tau2 = spare;
            d[k] = tau1 * csn + tau2 * sn;
            tau1 = dp1[k];
            tau2 = d[k + 1];
            dp1[k] = tau1 * csn + tau2 * sn;
            d[k + 1] = tau2 * csn - tau1 * sn;
            tau1 = 0;
            tau2 = dp1[k + 1];
            spare = tau1 * csn + tau2 * sn;
            dp1[k + 1] = tau2 * csn - tau1 * sn;

            for (int j = 0; j < m; j++) {
              tau1 = matrixU[j * m + k];
              tau2 = matrixU[j * m + k + 1];
              matrixU[j * m + k] = tau1 * csn + tau2 * sn;
              matrixU[j * m + k + 1] = tau2 * csn - tau1 * sn;
            }

            y = dp1[k];
            z = spare;

            // this is the loop skew
            kp1 = k + 1;
            cosinesine = DLARTG.givens(y, z);
            csn = cosinesine[0];
            sn = cosinesine[1];
            tau1 = dp1[kp1 - 1];
            tau2 = spare;
            dp1[kp1 - 1] = tau1 * csn + tau2 * sn;
            tau1 = d[kp1];
            tau2 = dp1[kp1];
            d[kp1] = tau1 * csn + tau2 * sn;
            dp1[kp1] = tau2 * csn - tau1 * sn;
            tau1 = 0;
            tau2 = d[kp1 + 1];
            spare = tau1 * csn + tau2 * sn;
            d[kp1 + 1] = tau2 * csn - tau1 * sn;
            //                   % we want to update V too
            for (int j = 0; j < n; j++) {
              tau1 = matrixV[kp1 * n + j];
              tau2 = matrixV[(kp1 + 1) * n + j];
              matrixV[kp1 * n + j] = tau1 * csn + tau2 * sn;
              matrixV[(kp1 + 1) * n + j] = tau2 * csn - tau1 * sn;
            }

            y = d[kp1];
            z = spare;
          }

          // loop clean, this is the peel -1, half skew
          k = blockend - 1;
          cosinesine = DLARTG.givens(y, z);
          csn = cosinesine[0];
          sn = cosinesine[1];
          tau1 = d[k];
          tau2 = spare;
          d[k] = tau1 * csn + tau2 * sn;
          tau1 = dp1[k];
          tau2 = d[k + 1];
          dp1[k] = tau1 * csn + tau2 * sn;
          d[k + 1] = tau2 * csn - tau1 * sn;
          //               % we want to update U too         
          for (int j = 0; j < m; j++) {
            tau1 = matrixU[j * m + k];
            tau2 = matrixU[j * m + k + 1];
            matrixU[j * m + k] = tau1 * csn + tau2 * sn;
            matrixU[j * m + k + 1] = tau2 * csn - tau1 * sn;
          }

        } //2*2 test
      } // if !zerofound||jump
    } // while q!=n
  }
}
