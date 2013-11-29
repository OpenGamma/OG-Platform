/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class HedgeRatioCalculator {

  private final AnalyticCDSPricer _pricer;

  public HedgeRatioCalculator() {
    _pricer = new AnalyticCDSPricer();
  }

  /**
   * The sensitivity of the PV of a CDS to the zero hazard rates at the knots of the credit curve 
   *TODO this should be handled directly by the pricer  
   * @param cds The CDS 
   * @param creditCurve The credit Curve
   * @param yieldCurve the yield curve
   * @return vector of sensitivities 
   */
  public DoubleMatrix1D getCurveSensitivities(final CDSAnalytic cds, final double coupon, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");

    final int nKnots = creditCurve.getNumberOfKnots();
    final double[] sense = new double[nKnots];
    for (int i = 0; i < nKnots; i++) {
      sense[i] = _pricer.pvCreditSensitivity(cds, yieldCurve, creditCurve, coupon, i);
    }
    return new DoubleMatrix1D(sense);
  }

  /**
   * The sensitivity of a set of CDSs to the zero hazard rates at the knots of the credit curve. The element (i,j) is the sensitivity of the PV of the
   * jth CDS to the ith knot. 
   * @param cds Set of CDSs 
   * @param coupons The coupons of the CDSs 
   * @param creditCurve The credit Curve
   * @param yieldCurve the yield curve
   * @return matrix of sensitivities 
   */
  public DoubleMatrix2D getCurveSensitivities(final CDSAnalytic[] cds, final double[] coupons, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(coupons, "coupons");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    final int nCDS = cds.length;
    ArgumentChecker.isTrue(nCDS == coupons.length, "number of coupons not equal number of CDS");
    final int nKnots = creditCurve.getNumberOfKnots();
    final double[][] sense = new double[nKnots][nCDS];

    for (int i = 0; i < nCDS; i++) {
      for (int j = 0; j < nKnots; j++) {
        sense[j][i] = _pricer.pvCreditSensitivity(cds[i], yieldCurve, creditCurve, coupons[i], j);
      }
    }
    return new DoubleMatrix2D(sense);
  }

  public DoubleMatrix1D getHedgeRatios(final CDSAnalytic cds, final double coupon, final CDSAnalytic[] hedgeCDSs, final double[] hedgeCDSCoupons, final ISDACompliantCreditCurve creditCurve,
      final ISDACompliantYieldCurve yieldCurve) {
    final DoubleMatrix1D cdsSense = getCurveSensitivities(cds, coupon, creditCurve, yieldCurve);
    final DoubleMatrix2D hedgeSense = getCurveSensitivities(hedgeCDSs, hedgeCDSCoupons, creditCurve, yieldCurve);
    return getHedgeRatios(cdsSense, hedgeSense);
  }

  public DoubleMatrix1D getHedgeRatios(final DoubleMatrix1D cdsSensitivities, final DoubleMatrix2D hedgeCDSSensitivities) {
    ArgumentChecker.notNull(hedgeCDSSensitivities, "hedgeCDSSensitivities");
    final LUDecompositionCommons decomp = new LUDecompositionCommons();
    final LUDecompositionResult luRes = decomp.evaluate(hedgeCDSSensitivities);
    return getHedgeRatios(cdsSensitivities, luRes);
  }

  public DoubleMatrix1D getHedgeRatios(final DoubleMatrix1D cdsSensitivities, final LUDecompositionResult luRes) {
    ArgumentChecker.notNull(cdsSensitivities, "cdsSensitivities");
    ArgumentChecker.notNull(luRes, " luRes");
    final DoubleMatrix1D w = luRes.solve(cdsSensitivities);
    return w;
  }

}
