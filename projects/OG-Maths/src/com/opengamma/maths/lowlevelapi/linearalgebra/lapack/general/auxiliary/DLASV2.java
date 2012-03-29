/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.auxiliary;

import com.opengamma.maths.commonapi.MathsConstants;

/**
 * Computes the singular values and rotations required to achieve said values of a 2 x 2 upper bi-diagonal matrix.
 * |f g|
 * |0 h|
 * Such that sigma1 and sigma2 are respectively the maximum and minimum singular values of the 2 x 2 system.  
 * The rotations are given in terms of cos{r,l} and sin{r,l}, such that cos{r,l}^2+sin{r,l}^2=1.
 * | cosl sinl|. |f g| . |cosr -sinr|  =  |sigma1   0   |
 * |-sinl cosl|  |0 h|   |sinr  cosr|     |  0    sigma2|
 * 
 * This code is a direct translation of the LAPACK routine DLASV2.
 * http://www.netlib.org/lapack/double/dlasv2.f
 * It forms part of the LAPACK library which is provided under a modified BSD licensed.
 * 
 * A direct translation of LAPACK code was chosen as this algorithm is designed to be as accurate as possible and inaccuracy at in this
 * particular function when called by DBDSQR can cause very slow convergence.
 * 
 * For more information see:
 * 1) www.netlib.org/lapack/lawnspdf/lawn03.pdf pp20
 * 2) "Computing the Singular Values of 2-by-2 Complex Matrices", Sanzheng Qiao and Xiaohong Wang. www.cas.mcmaster.ca/~qiao/publications/zsvd2.pdf
 * For an expansion of the derivation (also extension to complex space).
 * 3) "Computing the Generalized Singular Value Decomposition", Z. Bai and J. W. Demmel, www.netlib.org/lapack/lawnspdf/lawn46.pdf 
 *
 * It is from 3) that this algorithm and indeed the LAPACK DLASV2() routine are taken hence the call below reflecting the author's names.
 */
public class DLASV2 {

  /**
   * Compute the singular values and right and left singular vectors of a 2x2 upper bi-diagonal matrix as outlined in
   * "Computing the Generalized Singular Value Decomposition" Z. Bai and J. W. Demmel, www.netlib.org/lapack/lawnspdf/lawn46.pdf
   * which in turn forms part of the LAPACK library which is provided under a modified BSD licensed.
   * 
   * For parameter meaning see class headers
   * @param f element (1,1) of the 2 x 2 system
   * @param g element (1,2) of the 2 x 2 system
   * @param h element (2,2) of the 2 x 2 system
   * @return a double vector of length 6 containing in order {sigma1, sigma2, cosl, sinl, cosr, sinr };
   */
  public static double[] baiDemmel(double f, double g, double h) {
    double ft, fa, ht, ha, gt, ga;
    double tmp;
    double debugSignal = 0xDEADBEEF;

    int pmax = 1;
    boolean swapped = false;
    boolean gasmall = false;
    double clt = debugSignal, crt = debugSignal, slt = debugSignal, srt = debugSignal;
    double sigma1 = debugSignal, sigma2 = debugSignal;
    double l, t, mm, s, d, a, m, tt, r;
    double csl, snl, csr, snr;
    double tsign = debugSignal;
    ft = f;
    fa = Math.abs(f);
    ht = h;
    ha = Math.abs(h);

    if (ha > fa) {
      swapped = true;
    }
    if (swapped) {
      pmax = 3;
      tmp = ft;
      ft = ht;
      ht = tmp;
      tmp = fa;
      fa = ha;
      ha = tmp;
    }
    gt = g;
    ga = Math.abs(g);
    if (ga == 0) {
      sigma1 = ha;
      sigma2 = fa;
      clt = 1.;
      crt = 1.;
      slt = 0.;
      srt = 0.;
    } else {
      gasmall = true;
      if (ga > fa) {
        pmax = 2;
        if (Math.abs(fa / ga) <= MathsConstants.eps) {
          gasmall = false;
          sigma1 = ga;
          if (ha > 1.) {
            sigma2 = fa / (ga / ha);
          } else {
            sigma2 = (fa / ga) * ha;
          }
          clt = 1.;
          slt = ht / gt;
          srt = 1.;
          crt = ft / gt;
        }
      }

      if (gasmall == true) {
        d = fa - ha;
        if (d == fa) {
          l = 1;
        } else {
          l = d / fa;
        }
        m = gt / ft;
        t = 2.0 - l;
        tt = t * t;
        mm = m * m;
        s = Math.sqrt(tt + mm);
        if (l == 0) {
          r = Math.abs(m);
        } else {
          r = Math.sqrt(l * l + mm);
        }
        a = 0.5 * (r + s);
        sigma1 = fa * a;
        sigma2 = ha / a;
        if (mm == 0) {
          if (l == 0) {
            t = 2 * Math.signum(ft) * Math.signum(gt);
          } else {
            t = gt / (Math.signum(d) * ft) + m / t;
          }
        } else {
          t = (m / (s + t) + m / (r + l)) * (1 + a);
        }
        l = Math.sqrt(t * t + 4.);
        crt = 2 / l;
        srt = t / l;
        clt = (crt + srt * m) / a;
        slt = (ht / ft) * srt / a;
      }
    }
    if (swapped == true) {
      csl = srt;
      snl = crt;
      csr = slt;
      snr = clt;
    } else {
      csl = clt;
      snl = slt;
      csr = crt;
      snr = srt;
    }
    // possibly want copysign here opposed to signum to prevent sign clobber?
    if (pmax == 1) {
      tsign = Math.signum(csr) * Math.signum(csl) * Math.signum(f);
    }
    if (pmax == 2) {
      tsign = Math.signum(snr) * Math.signum(csl) * Math.signum(g);
    }
    if (pmax == 3) {
      tsign = Math.signum(snr) * Math.signum(snl) * Math.signum(h);
    }
    sigma1 = tsign * sigma1;
    sigma2 = sigma2 * Math.signum(tsign * Math.signum(f) * Math.signum(h));
    double[] ret = {sigma1, sigma2, csl, snl, csr, snr };
    return ret;
  }

}
