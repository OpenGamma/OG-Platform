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
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class HedgeRatioCalculator {

  MatrixAlgebra MA = new OGMatrixAlgebra();

  private final AnalyticCDSPricer _pricer;
  private final ISDACompliantCreditCurveBuilder _builder;

  public HedgeRatioCalculator() {
    _pricer = new AnalyticCDSPricer();
    _builder = new FastCreditCurveBuilder();
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

  /**
   * Hedge a CDS with other CDSs on the same underlying (single-name or index) at different maturities. The hedge is such that the total portfolio (the CDS <b>minus</b>
   * the hedging CDSs, with notionals of the CDS notional times the computed hedge ratios) is insensitive to infinitesimal changes to the the credit curve. <br>
   * Here the credit curve is built using the hedging CDSs as pillars. 
   * @param cds  The CDS to be hedged 
   * @param coupon The coupon of the CDS to be hedged 
   * @param hedgeCDSs The CDSs to hedge with - these are also used to build the credit curve 
   * @param hedgeCDSCoupons The coupons of the CDSs to hedge with/build credit curve
   * @param hegdeCDSPUF The PUF of the CDSs to build credit curve
   * @param yieldCurve he yield curve 
   * @return The hedge ratios. Since we use a unit notional, there ratios should be multiplied by -notional to give the hedge notional amounts. 
   */
  public DoubleMatrix1D getHedgeRatios(final CDSAnalytic cds, final double coupon, final CDSAnalytic[] hedgeCDSs, final double[] hedgeCDSCoupons, final double[] hegdeCDSPUF,
      final ISDACompliantYieldCurve yieldCurve) {
    final ISDACompliantCreditCurve cc = _builder.calibrateCreditCurve(hedgeCDSs, hedgeCDSCoupons, yieldCurve, hegdeCDSPUF);
    return getHedgeRatios(cds, coupon, hedgeCDSs, hedgeCDSCoupons, cc, yieldCurve);
  }

  /**
   * Hedge a CDS with other CDSs on the same underlying (single-name or index) at different maturities. The hedge is such that the total portfolio (the CDS <b>minus</b>
   * the hedging CDSs, with notionals of the CDS notional times the computed hedge ratios) is insensitive to infinitesimal changes to the the credit curve. 
   * If the number of hedge-CDSs equals the number of credit-curve knots, the system is square and is solved exactly (see below).<br>
   * If the number of hedge-CDSs is less than the number of credit-curve knots, the system is solved in a least-square sense (i.e. is hedge is not exact).<br>
   * If the number of hedge-CDSs is greater than the number of credit-curve knots, the system cannot be solved. <br>
   * The system may not solve if the maturities if the hedging CDSs and very different from the knot times (i.e. the sensitivity matrix is singular). 
   * @param cds The CDS to be hedged 
   * @param coupon The coupon of the CDS to be hedged 
   * @param hedgeCDSs The CDSs to hedge with 
   * @param hedgeCDSCoupons The coupons of the CDSs to hedge with   
   * @param creditCurve The credit curve  
   * @param yieldCurve the yield curve 
   * @return The hedge ratios. Since we use a unit notional, there ratios should be multiplied by -notional to give the hedge notional amounts. 
   */
  public DoubleMatrix1D getHedgeRatios(final CDSAnalytic cds, final double coupon, final CDSAnalytic[] hedgeCDSs, final double[] hedgeCDSCoupons, final ISDACompliantCreditCurve creditCurve,
      final ISDACompliantYieldCurve yieldCurve) {
    final DoubleMatrix1D cdsSense = getCurveSensitivities(cds, coupon, creditCurve, yieldCurve);
    final DoubleMatrix2D hedgeSense = getCurveSensitivities(hedgeCDSs, hedgeCDSCoupons, creditCurve, yieldCurve);
    return getHedgeRatios(cdsSense, hedgeSense);
  }

  /**
   * Hedge a CDS with other CDSs on the same underlying (single-name or index) at different maturities. The hedge is such that the total portfolio (the CDS <b>minus</b>
   * the hedging CDSs, with notionals of the CDS notional times the computed hedge ratios) is insensitive to infinitesimal changes to the the credit curve. 
   * If the number of hedge-CDSs equals the number of credit-curve knots, the system is square and is solved exactly (see below).<br>
   * If the number of hedge-CDSs is less than the number of credit-curve knots, the system is solved in a least-square sense (i.e. is hedge is not exact).<br>
   * If the number of hedge-CDSs is greater than the number of credit-curve knots, the system cannot be solved. <br>
   * The system may not solve if the maturities if the hedging CDSs and very different from the knot times (i.e. the sensitivity matrix is singular). 
   * @param cdsSensitivities vector of sensitivities of the CDS to the zero hazard rates at the credit curve knots.
   * @param hedgeCDSSensitivities matrix of sensitivities of the hedging-CDSs to the zero hazard rates at the credit curve knots. The (i,j) element is the sensitivity
   * of the jth CDS to the ith knot. 
   * @return The hedge ratios. Since we use a unit notional, there ratios should be multiplied by -notional to give the hedge notional amounts.
   */
  public DoubleMatrix1D getHedgeRatios(final DoubleMatrix1D cdsSensitivities, final DoubleMatrix2D hedgeCDSSensitivities) {
    ArgumentChecker.notNull(hedgeCDSSensitivities, "hedgeCDSSensitivities");
    final int nRows = hedgeCDSSensitivities.getNumberOfRows();
    final int nCols = hedgeCDSSensitivities.getNumberOfColumns();
    ArgumentChecker.isTrue(nRows == cdsSensitivities.getNumberOfElements(), "Number of matrix rows does not match vector length");
    if (nCols == nRows) {
      final LUDecompositionCommons decomp = new LUDecompositionCommons();
      final LUDecompositionResult luRes = decomp.evaluate(hedgeCDSSensitivities);
      return getHedgeRatios(cdsSensitivities, luRes);
    } else {
      if (nRows < nCols) {
        //Under-specified. No unique solution exists. There are  curve knots but hedging instruments  
        throw new IllegalArgumentException("Under-specified. No unique solution exists. There are " + nRows + " curve knots but " + nCols + " hedging instruments.");
      } else {
        //over-specified. Solve in a least-square sense 
        final DoubleMatrix2D senseT = MA.getTranspose(hedgeCDSSensitivities);
        final DoubleMatrix2D a = (DoubleMatrix2D) MA.multiply(senseT, hedgeCDSSensitivities);
        final DoubleMatrix1D b = (DoubleMatrix1D) MA.multiply(senseT, cdsSensitivities);
        final LUDecompositionCommons decomp = new LUDecompositionCommons();
        final LUDecompositionResult luRes = decomp.evaluate(a);
        return getHedgeRatios(b, luRes);
      }
    }
  }

  public DoubleMatrix1D getHedgeRatios(final DoubleMatrix1D cdsSensitivities, final LUDecompositionResult luRes) {
    ArgumentChecker.notNull(cdsSensitivities, "cdsSensitivities");
    ArgumentChecker.notNull(luRes, " luRes");
    final DoubleMatrix1D w = luRes.solve(cdsSensitivities);
    return w;
  }

}
