/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution.fnlib;

/**
 * DERF(X) provides the ability to calculate the error function at position 'x'. 
 * It does this using Chebychev approximation mixed with calls to erfc() provided by {@link DERF}.
 * This code is an approximate translation of the equivalent function in the "Public Domain" code from SLATEC, see:
 * See http://www.netlib.org/slatec/fnlib/derf.f
 */
public class DERF {
  static final double[] ERFCS = {//CSOFF
  -0.49046121234691808039984544033376e-1,
      -0.14226120510371364237824741899631e+0,
      +0.10035582187599795575754676712933e-1,
      -0.57687646997674847650827025509167e-3,
      +0.27419931252196061034422160791471e-4,
      -0.11043175507344507604135381295905e-5,
      +0.38488755420345036949961311498174e-7,
      -0.11808582533875466969631751801581e-8,
      +0.32334215826050909646402930953354e-10,
      -0.79910159470045487581607374708595e-12,
      +0.17990725113961455611967245486634e-13,
      -0.37186354878186926382316828209493e-15,
      +0.71035990037142529711689908394666e-17,
      -0.12612455119155225832495424853333e-18,
      +0.20916406941769294369170500266666e-20,
      -0.32539731029314072982364160000000e-22,
      +0.47668672097976748332373333333333e-24,
      -0.65980120782851343155199999999999e-26,
      +0.86550114699637626197333333333333e-28,
      -0.10788925177498064213333333333333e-29,
      +0.12811883993017002666666666666666e-31
      //CSON
  };

  static final double SQRTPI = 1.77245385090551602729816748334115;
  private static double s_xbig;
  private static double s_sqeps;
  private static int s_nterf;
  static {
    s_nterf = INITDS.getInitds(ERFCS, 21, 0.1 * D1MACH.three());
    s_xbig = Math.sqrt(-Math.log(SQRTPI * D1MACH.three()));
    s_sqeps = Math.sqrt(2.0d * D1MACH.three());
  }

  /**
   * Gets the error function at position 'x'
   * @param x the position at which to evaluate the error function
   * @return the error function value at position 'x'
   */
  public static double getErf(final double x) {
    double y = Math.abs(x);
    double derf = 0;
    if (y <= 1) {
      if (y <= s_sqeps) {
        derf = 2 * x * x / SQRTPI;
      }
      if (y > s_sqeps) {
        derf = x * (1 + DCSEVL.getDCSEVL(2 * x * x - 1d, ERFCS, s_nterf));
      }
    } else {
      if (y <= s_xbig) {
        derf = Math.copySign(1 - DERFC.getErfc(y), x);
      }
      if (y > s_xbig) {
        derf = Math.copySign(1, x);
      }
    }
    return derf;
  }

} //end class
