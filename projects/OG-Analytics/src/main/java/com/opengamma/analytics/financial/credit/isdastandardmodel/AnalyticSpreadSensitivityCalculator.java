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
public class AnalyticSpreadSensitivityCalculator {

  private final MarketQuoteConverter _pufConverter;
  private final ISDACompliantCreditCurveBuilder _curveBuilder;
  private final AnalyticCDSPricer _pricer;

  public AnalyticSpreadSensitivityCalculator() {
    _pufConverter = new MarketQuoteConverter();
    _curveBuilder = new FastCreditCurveBuilder();
    _pricer = new AnalyticCDSPricer();
  }

  public AnalyticSpreadSensitivityCalculator(final AccrualOnDefaultFormulae formula) {
    _pufConverter = new MarketQuoteConverter(formula);
    _curveBuilder = new FastCreditCurveBuilder(formula);
    _pricer = new AnalyticCDSPricer(formula);
  }

  //***************************************************************************************************************
  // parallel CS01 of a CDS from single market quote of that CDS
  //***************************************************************************************************************

  /**
   * The CS01 (or credit DV01)  of a CDS - the sensitivity of the PV to a finite increase of market spread (on NOT the CDS's
   * coupon). If the CDS is quoted as points up-front, this is first converted to a quoted spread, and <b>this</b> is bumped
   * @param cds analytic description of a CDS traded at a certain time - it is this CDS that we are calculation CDV01 for
   * @param quote The market quote for the CDS - these can be ParSpread, PointsUpFront or QuotedSpread
   * @param yieldCurve The yield (or discount) curve
   * @return the parallel CS01
   */
  public double parallelCS01(final CDSAnalytic cds, final CDSQuoteConvention quote, final ISDACompliantYieldCurve yieldCurve) {
    return parallelCS01(cds, quote.getCoupon(), new CDSAnalytic[] {cds }, new CDSQuoteConvention[] {quote }, yieldCurve);
  }

  /**
   *The analytic CS01 (or credit DV01)
   * @param cds  analytic description of a CDS traded at a certain time - it is this CDS that we are calculation CDV01 for
   * @param coupon  the of the traded CDS  (expressed as <b>fractions not basis points</b>)
   * @param yieldCurve The yield (or discount) curve
   * @param puf points up-front (as a fraction)
   * @return  The credit DV01
   */
  public double parallelCS01FromPUF(final CDSAnalytic cds, final double coupon, final ISDACompliantYieldCurve yieldCurve, final double puf) {

    final ISDACompliantCreditCurve cc = _curveBuilder.calibrateCreditCurve(cds, coupon, yieldCurve, puf);
    final double a = _pricer.protectionLeg(cds, yieldCurve, cc);
    final double b = _pricer.annuity(cds, yieldCurve, cc, PriceType.CLEAN);
    final double aPrime = _pricer.protectionLegCreditSensitivity(cds, yieldCurve, cc, 0);
    final double bPrime = _pricer.pvPremiumLegCreditSensitivity(cds, yieldCurve, cc, 0);
    final double s = a / b;
    final double dPVdh = aPrime - coupon * bPrime;
    final double dSdh = (aPrime - s * bPrime) / b;
    return dPVdh / dSdh;
  }

  /**
   * The analytic CS01 (or credit DV01)
   * @param cds analytic description of a CDS traded at a certain time - it is this CDS that we are calculation CDV01 for
   * @param coupon the of the traded CDS  (expressed as <b>fractions not basis points</b>)
   * @param yieldCurve  The yield (or discount) curve
   * @param marketSpread the market spread of the reference CDS (in this case it is irrelevant whether this is par or quoted spread)
   * @return The credit DV01
   */
  public double parallelCS01FromSpread(final CDSAnalytic cds, final double coupon, final ISDACompliantYieldCurve yieldCurve, final double marketSpread) {

    final ISDACompliantCreditCurve cc = _curveBuilder.calibrateCreditCurve(cds, marketSpread, yieldCurve);
    final double a = _pricer.protectionLeg(cds, yieldCurve, cc);
    final double b = a / marketSpread; //shortcut calculation of RPV01
    final double diff = marketSpread - coupon;
    if (diff == 0) {
      return b;
    }
    final double aPrime = _pricer.protectionLegCreditSensitivity(cds, yieldCurve, cc, 0);
    final double bPrime = _pricer.pvPremiumLegCreditSensitivity(cds, yieldCurve, cc, 0);
    final double dSdh = (aPrime - marketSpread * bPrime); //note - this has not been divided by b
    return b * (1 + diff * bPrime / dSdh);
  }

  public double parallelCS01(final CDSAnalytic cds, final double cdsCoupon, final CDSAnalytic[] pillarCDSs, final CDSQuoteConvention[] marketQuotes, final ISDACompliantYieldCurve yieldCurve) {
    final ISDACompliantCreditCurve creditCurve = _curveBuilder.calibrateCreditCurve(pillarCDSs, marketQuotes, yieldCurve);
    return parallelCS01FromCreditCurve(cds, cdsCoupon, pillarCDSs, yieldCurve, creditCurve);
  }

  public double parallelCS01FromCreditCurve(final CDSAnalytic cds, final double cdsCoupon, final CDSAnalytic[] bucketCDSs, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve creditCurve) {
    final double[] temp = bucketedCS01FromCreditCurve(cds, cdsCoupon, bucketCDSs, yieldCurve, creditCurve);
    double sum = 0;
    for (final double cs : temp) {
      sum += cs;
    }
    return sum;
  }

  //***************************************************************************************************************
  // bucketed CS01 of a CDS from single market quote of that CDS
  //***************************************************************************************************************

  public double[] bucketedCS01FromSpread(final CDSAnalytic cds, final double coupon, final ISDACompliantYieldCurve yieldCurve, final double marketSpread, final CDSAnalytic[] buckets) {
    final ISDACompliantCreditCurve cc = _curveBuilder.calibrateCreditCurve(cds, marketSpread, yieldCurve);
    return bucketedCS01FromCreditCurve(cds, coupon, buckets, yieldCurve, cc);
  }

  public double[] bucketedCS01(final CDSAnalytic cds, final double cdsCoupon, final CDSAnalytic[] pillarCDSs, final CDSQuoteConvention[] marketQuotes, final ISDACompliantYieldCurve yieldCurve) {
    final ISDACompliantCreditCurve creditCurve = _curveBuilder.calibrateCreditCurve(pillarCDSs, marketQuotes, yieldCurve);
    return bucketedCS01FromCreditCurve(cds, cdsCoupon, pillarCDSs, yieldCurve, creditCurve);
  }

  public double[][] bucketedCS01(final CDSAnalytic[] cds, final double[] cdsCoupons, final CDSAnalytic[] pillarCDSs, final CDSQuoteConvention[] marketQuotes,
      final ISDACompliantYieldCurve yieldCurve) {
    final ISDACompliantCreditCurve creditCurve = _curveBuilder.calibrateCreditCurve(pillarCDSs, marketQuotes, yieldCurve);
    return bucketedCS01FromCreditCurve(cds, cdsCoupons, pillarCDSs, yieldCurve, creditCurve);
  }

  public double[] bucketedCS01FromParSpreads(final CDSAnalytic cds, final double cdsCoupon, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic[] pillarCDSs, final double[] spreads) {
    final ISDACompliantCreditCurve creditCurve = _curveBuilder.calibrateCreditCurve(pillarCDSs, spreads, yieldCurve);
    return bucketedCS01FromCreditCurve(cds, cdsCoupon, pillarCDSs, yieldCurve, creditCurve);
  }

  public double[] bucketedCS01FromCreditCurve(final CDSAnalytic cds, final double cdsCoupon, final CDSAnalytic[] bucketCDSs, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.noNulls(bucketCDSs, "bucketCDSs");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    final LUDecompositionCommons decomp = new LUDecompositionCommons();
    final int n = bucketCDSs.length;
    final double[] temp = new double[n];
    final double[][] res = new double[n][n];
    for (int i = 0; i < n; i++) {
      temp[i] = _pricer.pvCreditSensitivity(cds, yieldCurve, creditCurve, cdsCoupon, i);
      for (int j = 0; j < n; j++) {
        res[j][i] = _pricer.parSpreadCreditSensitivity(bucketCDSs[i], yieldCurve, creditCurve, j);
      }
    }
    final DoubleMatrix1D vLambda = new DoubleMatrix1D(temp);
    final DoubleMatrix2D jacT = new DoubleMatrix2D(res);
    final LUDecompositionResult luRes = decomp.evaluate(jacT);
    final DoubleMatrix1D vS = luRes.solve(vLambda);
    return vS.getData();
  }

  public double[][] bucketedCS01FromCreditCurve(final CDSAnalytic[] cds, final double[] cdsCoupon, final CDSAnalytic[] bucketCDSs, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(cdsCoupon, "cdsCoupons");
    ArgumentChecker.noNulls(bucketCDSs, "bucketCDSs");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    final int m = cds.length;
    ArgumentChecker.isTrue(m == cdsCoupon.length, m + " CDSs but " + cdsCoupon.length + " coupons");
    final LUDecompositionCommons decomp = new LUDecompositionCommons();
    final int n = bucketCDSs.length;
    final DoubleMatrix2D jacT = new DoubleMatrix2D(n, n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        jacT.getData()[j][i] = _pricer.parSpreadCreditSensitivity(bucketCDSs[i], yieldCurve, creditCurve, j);
      }
    }

    final double[] vLambda = new double[n];
    final double[][] res = new double[m][];
    final LUDecompositionResult luRes = decomp.evaluate(jacT);
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        vLambda[j] = _pricer.pvCreditSensitivity(cds[i], yieldCurve, creditCurve, cdsCoupon[i], j);
      }
      res[i] = luRes.solve(vLambda);
    }
    return res;
  }

}
