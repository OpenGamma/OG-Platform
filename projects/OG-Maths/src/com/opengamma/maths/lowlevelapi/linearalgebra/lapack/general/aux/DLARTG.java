/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.aux;

/**
 * Computes planar rotations (often called Given's or Jacobi rotations)
 * | cos  sin  | . | a |  =  | r |   
 * | -sin cos  |   | b |     | 0 |
 * where cos^2 + sin^2 = 1.
 * 
 * Algorithm is taken from: 
 * "Discontinuous Plane Rotations and the Symmetric Eigenvalue Problem", Edward Anderson, Lockheed Martin Services Inc., Anderson.Edward@epa.gov
 * http://www.netlib.org/lapack/lawnspdf/lawn150.pdf
 * It forms part of the LAPACK library which is provided under a modified BSD licensed.
 * This algorithm is chosen as it produces a more continuous set of rotations whilst also avoiding under/overflow
 * (as outlined in "Matrix Computations, Third Edition" Golub and Van Loan p216, Alg. 5.1.3,
 *  which is essentially the same idea but with the discontinuity is the sign still present). 
 */
public class DLARTG {
  /**
   * A rather Fortran like call to compute givens rotations.
   * For parameter meaning see class headers.
   * @param a the distance in the first dimension
   * @param b the distance in the second dimension
   * @return a 3 long double [] containing {cos, sin, r} to match the definition in the headers.
   */
  public static double[] givens(double a, double b) {
    double c, s, u, r, t;
    if (b == 0) {
      c = Math.copySign(1, a);
      s = 0;
      r = Math.abs(a);
    } else if (a == 0) {
      c = 0;
      s = Math.signum(b);
      r = Math.abs(b);
    } else if (a > b) {
      t = b / a;
      u = Math.signum(a) * Math.sqrt(1 + t * t);
      c = 1 / u;
      s = t * c;
      r = a * u;
    } else {
      t = a / b;
      u = Math.signum(b) * Math.sqrt(1 + t * t);
      s = 1 / u;
      c = t * s;
      r = b * u;
    }
    double[] ret = {c, s, r };
    return ret;
  }

}
