/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.slatec.fnlib;

/**
 *
 */
public class DERF {
  //  *DECK DERF
  //  DOUBLE PRECISION FUNCTION DERF (X)
  //C***BEGIN PROLOGUE  DERF
  //C***PURPOSE  COMPUTE THE ERROR FUNCTION.
  //C***LIBRARY   SLATEC (FNLIB)
  //C***CATEGORY  C8A, L5A1E
  //C***TYPE      DOUBLE PRECISION (ERF-S, DERF-D)
  //C***KEYWORDS  ERF, ERROR FUNCTION, FNLIB, SPECIAL FUNCTIONS
  //C***AUTHOR  FULLERTON, W., (LANL)
  //C***DESCRIPTION
  //C
  //C DERF(X) CALCULATES THE DOUBLE PRECISION ERROR FUNCTION FOR DOUBLE
  //C PRECISION ARGUMENT X.
  //C
  //C SERIES FOR ERF        ON THE INTERVAL  0.          TO  1.00000E+00
  //C                                        WITH WEIGHTED ERROR   1.28E-32
  //C                                         LOG WEIGHTED ERROR  31.89
  //C                               SIGNIFICANT FIGURES REQUIRED  31.05
  //C                                    DECIMAL PLACES REQUIRED  32.55
  //C
  //C***REFERENCES  (NONE)
  //C***ROUTINES CALLED  D1MACH, DCSEVL, DERFC, INITDS
  //C***REVISION HISTORY  (YYMMDD)
  //C   770701  DATE WRITTEN
  //C   890531  CHANGED ALL SPECIFIC INTRINSICS TO GENERIC.  (WRB)
  //C   890531  REVISION DATE FROM VERSION 3.2
  //C   891214  PROLOGUE CONVERTED TO VERSION 4.0 FORMAT.  (BAB)
  //C   900727  ADDED EXTERNAL STATEMENT.  (WRB)
  //C   920618  REMOVED SPACE FROM VARIABLE NAME.  (RWC, WRB)
  //C***END PROLOGUE  DERF
  //  DOUBLE PRECISION X, ERFCS(21), SQEPS, SQRTPI, XBIG, Y, D1MACH,
  // 1  DCSEVL, DERFC
  //  LOGICAL FIRST
  //  EXTERNAL DERFC
  //  SAVE ERFCS, SQRTPI, NTERF, XBIG, SQEPS, FIRST

  static final double[] ERFCS = {
    -0.49046121234691808039984544033376d - 1,
    -0.14226120510371364237824741899631d + 0,
    +0.10035582187599795575754676712933d - 1,
    -0.57687646997674847650827025509167d - 3,
    +0.27419931252196061034422160791471d - 4,
    -0.11043175507344507604135381295905d - 5,
    +0.38488755420345036949961311498174d - 7,
    -0.11808582533875466969631751801581d - 8,
    +0.32334215826050909646402930953354d - 10,
    -0.79910159470045487581607374708595d - 12,
    +0.17990725113961455611967245486634d - 13,
    -0.37186354878186926382316828209493d - 15,
    +0.71035990037142529711689908394666d - 17,
    -0.12612455119155225832495424853333d - 18,
    +0.20916406941769294369170500266666d - 20,
    -0.32539731029314072982364160000000d - 22,
    +0.47668672097976748332373333333333d - 24,
    -0.65980120782851343155199999999999d - 26,
    +0.86550114699637626197333333333333d - 28,
    -0.10788925177498064213333333333333d - 29,
    +0.12811883993017002666666666666666d - 31
  };
  static final double SQRTPI = 1.77245385090551602729816748334115;

  private static double s_xbig;
  private static double s_sqeps;
  private static int s_nterf;
  private static double s_d1MACH3;
  private static double s_d1MACH4;
  private static double s_onepl;
  static {
    //C***FIRST EXECUTABLE STATEMENT  DERF
    //  IF (FIRST) THEN
    //     NTERF = INITDS (ERFCS, 21, 0.1*REAL(D1MACH(3)))
    //     XBIG = SQRT(-LOG(SQRTPI*D1MACH(3)))
    //     SQEPS = SQRT(2.0D0*D1MACH(3))
    //  ENDIF
    //    NTERF = INITDS (ERFCS, 21, 0.1*REAL(D1MACH(3)))
    s_d1MACH3 = 0.1 * Double.MIN_VALUE / 2;
    s_nterf = INITDS.getInitds(ERFCS, 21, s_d1MACH3);
    s_xbig = Math.sqrt(-Math.log(SQRTPI) * s_d1MACH3);
    s_sqeps = Math.sqrt(2.0d * s_d1MACH3);
    s_d1MACH4 = Double.MIN_VALUE;
    s_onepl = 1.0 + s_d1MACH4;
  }

  double getErf(final double x) {
    double y = Math.abs(x);
    double derf = 0;
    if (y <= 1) {
      if (y <= s_sqeps) {
        derf = 2 * x * x / SQRTPI;
      }
      if (y > s_sqeps) {
        derf = x * (1 + DCSEVL.getDCSEVL(2 * x * x, ERFCS, s_nterf));
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
