/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import java.util.Arrays;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.util.ArgumentChecker;

/**
 *  If a CDS is quoted with a standard premium (100 or 500bps in North America) then an upfront free (of either sign) is 
   * payable. If I buy protection (thus pay the premium) and the premium is greater than the par spread (the old way of quoting),
   * I am compensated by receiving a positive points-upfront. 
 */
public class PointsUpFrontConverter {

  private static final ISDACompliantCreditCurveBuilder BUILDER = new FastCreditCurveBuilder();
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();

  /**
   * Get the points-upfront if the credit (hazard) curve is known.
   * @param cds The CDS to be traded
   * @param premium The standard premium of the CDS <b>expressed as a fraction</b>
   * @param yieldCurve the yield/discount curve 
   * @param creditCurve the credit/hazard curve 
   * @return  points-upfront - these are usually quoted as a percentage of the notional - here we return a fraction of notional,
   *  so 0.01 is 1(%) points-upfront  
   */
  final double pointsUpFront(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    return PRICER.pv(cds, yieldCurve, creditCurve, premium, PriceType.CLEAN);
  }

  /**
   * Get the points-upfront for a collection of CDSs if the credit (hazard) curve is known.
   * collection of CDSs 
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve the yield/discount curve 
   * @param creditCurve the credit/hazard curve 
   * @return points-upfront (as fractions)
   */
  final double[] pointsUpFront(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pointsUpFront(cds[i], premium, yieldCurve, creditCurve);
    }
    return res;
  }

  /**
   * Get the points-upfront for a collection of CDSs if the credit (hazard) curve is known.
   * collection of CDSs 
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve the yield/discount curve 
   * @param creditCurve the credit/hazard curve 
   * @return points-upfront (as fractions)
   */
  final double[] pointsUpFront(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(premiums, "premiums");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pointsUpFront(cds[i], premiums[i], yieldCurve, creditCurve);
    }
    return res;
  }

  /**
   * Convert from a CDS quote as a par spread (the old way of quoting) to points upfront.
   * @param cds The CDS to be traded
   * @param premium The standard premium of the CDS <b>expressed as a fraction</b>
   * @param yieldCurve the yield/discount curve 
   * @param parSpread The par spread (<b>as a fraction</b>)
   * @return points-upfront - these are usually quoted as a percentage of the notional - here we return a fraction of notional,
   *  so 0.01 is 1(%) points-upfront  
   */
  final double pointsUpFront(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double parSpread) {
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(cds, parSpread, yieldCurve);
    return pointsUpFront(cds, premium, yieldCurve, creditCurve);
  }

  /**
   *  Convert from a set of CDSs quoted as a par spreads (the old way of quoting) to points up-front (PUF). 
   *  Each CDS is priced off a <b>separate</b> flat credit/hazard
   * curve - i.e. the CDSs are completely decoupled from each other. If the CDSs are priced off a single (bootstrapped) credit/hazard curve
   *   the actual difference (in numerical value) will be small
   * @param cds   collection of CDSs 
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve yieldCurve the yield/discount curve 
   * @param parSpreads The par-spreads
   * @return points up-front (expressed as fractions) 
   */
  final double[] pointsUpFrontFlat(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(parSpreads, "parSpreads");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == parSpreads.length, "parSpreads wrong length");
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pointsUpFront(cds[i], premium, yieldCurve, parSpreads[i]);
    }
    return res;
  }

  /**
   *  Convert from a set of CDSs quoted as a par spreads (the old way of quoting) to points up-front (PUF). 
   *  Each CDS is priced off a <b>separate</b> flat credit/hazard
   * curve - i.e. the CDSs are completely decoupled from each other. If the CDSs are priced off a single (bootstrapped) credit/hazard curve
   *   the actual difference (in numerical value) will be small
   * @param cds   collection of CDSs 
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve yieldCurve the yield/discount curve 
   * @param parSpreads The par-spreads
   * @return points up-front (expressed as fractions) 
   */
  final double[] pointsUpFrontFlat(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(premiums, "premiums");
    ArgumentChecker.notEmpty(parSpreads, "parSpreads");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == premiums.length, "premiums wrong length");
    ArgumentChecker.isTrue(n == parSpreads.length, "parSpreads wrong length");
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pointsUpFront(cds[i], premiums[i], yieldCurve, parSpreads[i]);
    }
    return res;
  }

  /**
   *  Convert from a set of CDSs quoted as a par spreads (the old way of quoting) to points up-front (PUF). 
   *  Each CDS is priced off a <b>single non-flat</b>credit/hazard curve -  If the CDSs are priced off separate (flat) credit/hazard curve
   *   the actual difference (in numerical value) will be small
   *   @see pointsUpFrontFlat
   * @param cds  collection of CDSs 
   * @param premiums The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve yieldCurve the yield/discount curve 
   * @param parSpreads The par-spreads
   * @return points up-front (expressed as fractions) 
   */
  final double[] pointsUpFront(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(cds, parSpreads, yieldCurve);
    return pointsUpFront(cds, premium, yieldCurve, creditCurve);
  }

  /**
   *  Convert from a set of CDSs quoted as a par spreads (the old way of quoting) to points up-front (PUF). 
   *  Each CDS is priced off a <b>single non-flat</b>credit/hazard curve -  If the CDSs are priced off separate (flat) credit/hazard curve
   *   the actual difference (in numerical value) will be small
   *   @see pointsUpFrontFlat
   * @param cds  collection of CDSs 
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve yieldCurve the yield/discount curve 
   * @param parSpreads The par-spreads
   * @return points up-front (expressed as fractions) 
   */
  final double[] pointsUpFront(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(cds, parSpreads, yieldCurve);
    return pointsUpFront(cds, premiums, yieldCurve, creditCurve);
  }

  /**
   * Convert from a CDS quote as points upfront (and a standard premium) to a quote as par spread (the old way of quoting) using
   * a flat credit/hazard curve. 
   * @param cds The CDS to be traded
   * @param premium The standard premium of the CDS <b>expressed as a fraction</b>
   * @param yieldCurve the yield/discount curve 
   * @param pointsUpfront points upfront
   * @return the par spread <b>expressed as a fraction</b>
   */
  final double parSpread(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double pointsUpfront) {
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(cds, premium, yieldCurve, pointsUpfront);
    return PRICER.parSpread(cds, yieldCurve, creditCurve);
  }

  /**
  * Get the equivalent par spreads for a collection of CDSs where each CDS is priced off a <b>separate</b> flat credit/hazard
   * curve - i.e. the CDSs are completely decoupled from each other. If the CDSs are priced off a single (bootstrapped) credit/hazard curve
   *   the actual difference (in numerical value) will be small
   * @param cds collection of CDSs 
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve the yield/discount curve 
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see parSpreads
   * @return collection of CDSs 
   */
  final double[] parSpreadsFlat(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notEmpty(pointsUpfront, "pointsUpfront");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = parSpread(cds[i], premium, yieldCurve, pointsUpfront[i]);
    }
    return res;
  }

  /**
   * Get the equivalent par spreads for a collection of CDSs where each CDS is priced off a <b>separate</b> flat credit/hazard
   * curve - i.e. the CDSs are completely decoupled from each other. If the CDSs are priced off a single (bootstrapped) credit/hazard curve
   *   the actual difference (in numerical value) will be small
   * @param cds collection of CDSs 
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
   * @param yieldCurve the yield/discount curve 
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see parSpreads
   * @return equivalent par spreads
   */
  final double[] parSpreadFlat(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notEmpty(premiums, "premiums");
    ArgumentChecker.notEmpty(pointsUpfront, "pointsUpfront");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = parSpread(cds[i], premiums[i], yieldCurve, pointsUpfront[i]);
    }
    return res;
  }

  /**
   * the equivalent par spreads for a collection of CDSs where a single, non-flat, credit/hazard curve is bootstrapped to
   * reprice all the given CDSs. Note: while this is more consistent (from a theoretical viewpoint), equivalent par spreads are
   * often quoted from (decoupled) individual flat credit/hazard curves   - the actual difference (in numerical value) will be small
     * @param cds collection of CDSs 
     * @param premiums The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
     * @param yieldCurve the yield/discount curve 
     * @param pointsUpfront The points up-front (expressed as fractions)
     * @see parSpreadFlat
   * @return equivalent par spreads
   */
  final double[] parSpreads(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "cds");
    final int n = cds.length;
    final double[] premiums = new double[n];
    Arrays.fill(premiums, premium);
    return parSpreads(cds, premiums, yieldCurve, pointsUpfront);
  }

  /**
   * the equivalent par spreads for a collection of CDSs where a single, non-flat, credit/hazard curve is bootstrapped to
   * reprice all the given CDSs. Note: while this is more consistent (from a theoretical viewpoint), equivalent par spreads are
   * often quoted from (decoupled) individual flat credit/hazard curves - the actual difference (in numerical value) will be small
     * @param cds collection of CDSs 
     * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05) 
     * @param yieldCurve the yield/discount curve 
     * @param pointsUpfront The points up-front (expressed as fractions)
     * @see parSpreadFlat
   * @return equivalent par spreads
   */
  final double[] parSpreads(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(cds, premiums, yieldCurve, pointsUpfront);
    return parSpreads(cds, yieldCurve, creditCurve);
  }

  /**
   * Get the par spreads for a collection of CDSs when the credit/hazard curve is known  
   * @param cds collection of CDSs 
   * @param yieldCurve the yield/discount curve 
   * @param creditCurve the credit/hazard curve 
   * @return par spreads 
   */
  final double[] parSpreads(final CDSAnalytic[] cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = PRICER.parSpread(cds[i], yieldCurve, creditCurve);
    }
    return res;
  }

}
