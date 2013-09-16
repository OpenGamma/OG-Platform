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
 * If a CDS is quoted with a standard premium (100 or 500bps in North America) then an up-front free (of either sign) is
 * payable. If I buy protection (thus pay the premium) and the premium is greater than the par spread (the old way of quoting),
 * then the up-front free is negative (i.e. I  pay a negative fee, or to put it another way, I am compensated by receiving a
 * positive amount). <br>
 * The free is quoted as Points Up-Front (PUF), which is a percentage of the notional (here was use a fractional amount).
 */
public class PointsUpFrontConverter {

  private final ISDACompliantCreditCurveBuilder _builder;
  private final AnalyticCDSPricer _pricer;

  public PointsUpFrontConverter() {
    _builder = new FastCreditCurveBuilder();
    _pricer = new AnalyticCDSPricer();
  }

  public PointsUpFrontConverter(final boolean useCorrectACCOnDefaultFormula) {
    _builder = new FastCreditCurveBuilder(useCorrectACCOnDefaultFormula);
    _pricer = new AnalyticCDSPricer(useCorrectACCOnDefaultFormula);
  }

  /**
   * The clean price as a fraction of notional (it is often expressed as a percentage of notional)
   * @param fractionalPUF The points up-front (as a fraction)
   * @return The clean price  (as a fraction)
   */
  public double cleanPrice(final double fractionalPUF) {
    return 1 - fractionalPUF;
  }

  /**
   * The clean price as a fraction of notional (it is often expressed as a percentage of notional)  - this requires that a
   * credit curve is bootstrapped first
   * @param cds The CDS to be traded
   * @param yieldCurve the yield/discount curve
   * @param creditCurve the credit/hazard curve
   * @param coupon The fractional quoted spread (coupon) of the CDS
   * @return the clean price  (as a fraction)
   */
  public double cleanPrice(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double coupon) {
    final double puf = pointsUpFront(cds, coupon, yieldCurve, creditCurve);
    return 1 - puf;
  }

  /**
   * The principal - this is the clean present value
   * @param notional The notional of the trade
   * @param cds The CDS to be traded
   * @param yieldCurve the yield/discount curve
   * @param creditCurve the credit/hazard curve
   * @param coupon The fractional quoted spread (coupon) of the CDS
   * @return The principle
   */
  public double principal(final double notional, final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double coupon) {
    return notional * _pricer.pv(cds, yieldCurve, creditCurve, coupon, PriceType.CLEAN);
  }

  /**
   * Get the points up-front  - this requires that a credit curve is bootstrapped first
   * @param cds The CDS to be traded
   * @param premium The standard premium of the CDS <b>expressed as a fraction</b>
   * @param yieldCurve the yield/discount curve
   * @param creditCurve the credit/hazard curve
   * @return  points up-front - these are usually quoted as a percentage of the notional - here we return a fraction of notional,
   *  so 0.01 is 1(%) points up-front
   */
  public double pointsUpFront(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    return _pricer.pv(cds, yieldCurve, creditCurve, premium, PriceType.CLEAN);
  }

  /**
   * Get the points up-front for a collection of CDSs - this requires that a credit curve is bootstrapped first. This will
   * give a slightly different answer to using a single (flat) credit curve for each CDS (the latter is the market standard)
   * @param  cds collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param creditCurve the credit/hazard curve
   * @return points up-front (as fractions)
   */
  public double[] pointsUpFront(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pointsUpFront(cds[i], premium, yieldCurve, creditCurve);
    }
    return res;
  }

  /**
   * Get the points up-front for a collection of CDSs - this requires that a credit curve is bootstrapped first. This will
   * give a slightly different answer to using a single (flat) credit curve for each CDS (the latter is the market standard)
   * @param  cds collection of CDSs
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param creditCurve the credit/hazard curve
   * @return points-upfront (as fractions)
   */
  public double[] pointsUpFront(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(premiums, "premiums");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pointsUpFront(cds[i], premiums[i], yieldCurve, creditCurve);
    }
    return res;
  }

  public PointsUpFront toPointsUpFront(final CDSAnalytic cds, final QuotedSpread qSpread, final ISDACompliantYieldCurve yieldCurve) {
    final double puf = quotedSpreadToPUF(cds, qSpread.getCoupon(), yieldCurve, qSpread.getQuotedSpread());
    return new PointsUpFront(qSpread.getCoupon(), puf);
  }

  /**
   * Convert from a CDS quoted spread to points up-front (PUF). <b>Note:</b> Quoted spread is not the same as par spread
   * (although they are numerically similar) -  it is simply an alternative quoting convention from PUF where the CDS is priced
   *  off a flat credit/hazard curve.
   * @param cds The CDS to be traded
   * @param premium The standard premium of the CDS <b>expressed as a fraction</b>
   * @param yieldCurve the yield/discount curve
   * @param quotedSpread The quoted spread (<b>as a fraction</b>).
   * @return points up-front - these are usually quoted as a percentage of the notional - here we return a fraction of notional,
   *  so 0.01 is 1(%) points up-front
   */
  public double quotedSpreadToPUF(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double quotedSpread) {
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, quotedSpread, yieldCurve);
    return pointsUpFront(cds, premium, yieldCurve, creditCurve);
  }

  public PointsUpFront[] toPointsUpFront(final CDSAnalytic[] cds, final QuotedSpread[] qSpreads, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.noNulls(qSpreads, "qSpreads");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == qSpreads.length, "numbe od CDSs does not match qSpreads");
    final PointsUpFront[] res = new PointsUpFront[n];
    for (int i = 0; i < n; i++) {
      final double coupon = qSpreads[i].getCoupon();
      final double puf = quotedSpreadToPUF(cds[i], coupon, yieldCurve, qSpreads[i].getQuotedSpread());
      res[i] = new PointsUpFront(coupon, puf);
    }
    return res;
  }

  /**
   *  Convert from a set of CDSs quoted spreads  to points up-front (PUF). <b>Note:</b> Quoted spread is not the same as par spread
   * (although they are numerically similar) - it is simply an alternative quoting convention from PUF where each CDS is priced
   *  off a <b>separate</b> flat credit/hazard curve - i.e. the CDSs are completely decoupled from each other.
   * @param cds   collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve yieldCurve the yield/discount curve
   * @param quotedSpreads The quoted spreads (<b>as a fractions</b>).
   * @return points up-front (expressed as fractions)
   */
  public double[] quotedSpreadsToPUF(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] quotedSpreads) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(quotedSpreads, "parSpreads");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == quotedSpreads.length, "parSpreads wrong length");
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = quotedSpreadToPUF(cds[i], premium, yieldCurve, quotedSpreads[i]);
    }
    return res;
  }

  /**
  *  Convert from a set of CDSs quoted spreads  to points up-front (PUF). <b>Note:</b> Quoted spread is not the same as par spread
   * (although they are numerically similar) - it is simply an alternative quoting convention from PUF where each CDS is priced
   *  off a <b>separate</b> flat credit/hazard curve - i.e. the CDSs are completely decoupled from each other.
   * @param cds   collection of CDSs
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve yieldCurve the yield/discount curve
   * @param  quotedSpreads The quoted spreads (<b>as a fractions</b>).
   * @return points up-front (expressed as fractions)
   */
  public double[] quotedSpreadsToPUF(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] quotedSpreads) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(premiums, "premiums");
    ArgumentChecker.notEmpty(quotedSpreads, "parSpreads");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == premiums.length, "premiums wrong length");
    ArgumentChecker.isTrue(n == quotedSpreads.length, "parSpreads wrong length");
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = quotedSpreadToPUF(cds[i], premiums[i], yieldCurve, quotedSpreads[i]);
    }
    return res;
  }

  /**
   *  Convert from a set of CDSs quoted as a par spreads (the old way of quoting) to points up-front (PUF).
   *  Each CDS is priced off a <b>single non-flat</b> credit/hazard curve. <br>
   *  If the CDS are quoted as <b>quoted</b> spreads one must use quotedSpreadsToPUF instead
   *  {@link #pointsUpFront}
   * @param cds  collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve yieldCurve the yield/discount curve
   * @param parSpreads The par-spreads (<b>as a fractions</b>).
   * @return points up-front (expressed as fractions)
   */
  public double[] parSpreadsToPUF(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, parSpreads, yieldCurve);
    return pointsUpFront(cds, premium, yieldCurve, creditCurve);
  }

  /**
   *  Convert from a set of CDSs quoted as a par spreads (the old way of quoting) to points up-front (PUF).
   *  Each CDS is priced off a <b>single non-flat</b> credit/hazard curve. <br>
   *  If the CDS are quoted as <b>quoted</b> spreads one must use quotedSpreadsToPUF instead
   * @param cds  collection of CDSs
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve yieldCurve the yield/discount curve
   * @param parSpreads The par-spreads (<b>as a fractions</b>).
   * @see PointsUpFrontConverter#pointsUpFront
   * @return points up-front (expressed as fractions)
   */
  public double[] parSpreadsToPUF(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, parSpreads, yieldCurve);
    return pointsUpFront(cds, premiums, yieldCurve, creditCurve);
  }

  /**
   * Convert from a CDS quote as points up-front (PUF) and a standard premium, to a <i>quoted</i> spread.
   * This is simply an alternative quoting convention from PUF where the CDS is priced off a flat credit/hazard curve.
   * @param cds The CDS to be traded
   * @param premium The standard premium of the CDS <b>expressed as a fraction</b>
   * @param yieldCurve the yield/discount curve
   * @param pointsUpfront points up-front
   * @return the par spread <b>expressed as a fraction</b>
   */
  public double pufToQuotedSpread(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double pointsUpfront) {
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, premium, yieldCurve, pointsUpfront);
    return _pricer.parSpread(cds, yieldCurve, creditCurve);
  }

  /**
   * Get the equivalent <i>quoted</i> spreads for a collection of CDSs. This is simply a quoting convention -each CDS is priced
   * off a <b>separate</b> flat credit/hazard curve - i.e. the CDSs are completely decoupled from each other.
   * @param cds collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see PointsUpFrontConverter#pufToQuotedSpread
   * @return collection of CDSs
   */
  public double[] pufToQuotedSpreads(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notEmpty(pointsUpfront, "pointsUpfront");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pufToQuotedSpread(cds[i], premium, yieldCurve, pointsUpfront[i]);
    }
    return res;
  }

  /**
   * Get the equivalent <i>quoted</i> spreads for a collection of CDSs. This is simply a quoting convention -each CDS is priced
   * off a <b>separate</b> flat credit/hazard curve - i.e. the CDSs are completely decoupled from each other.
   * @param cds collection of CDSs
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see PointsUpFrontConverter#pufToQuotedSpread
   * @return equivalent par spreads
   */
  public double[] pufToQuotedSpreads(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notEmpty(premiums, "premiums");
    ArgumentChecker.notEmpty(pointsUpfront, "pointsUpfront");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = pufToQuotedSpread(cds[i], premiums[i], yieldCurve, pointsUpfront[i]);
    }
    return res;
  }

  /**
   * The equivalent par spreads for a collection of CDSs where a single, non-flat, credit/hazard curve is bootstrapped to
   * reprice all the given CDSs.
   * @param cds collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see PointsUpFrontConverter#pufToParSpreads
   * @return equivalent par spreads
   */
  public double[] pufToParSpreads(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "cds");
    final int n = cds.length;
    final double[] premiums = new double[n];
    Arrays.fill(premiums, premium);
    return pufToParSpreads(cds, premiums, yieldCurve, pointsUpfront);
  }

  /**
   * The equivalent par spreads for a collection of CDSs where a single, non-flat, credit/hazard curve is bootstrapped to
   * reprice all the given CDSs.
   * @param cds collection of CDSs
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see PointsUpFrontConverter#parSpreads
   * @return equivalent par spreads
   */
  public double[] pufToParSpreads(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, premiums, yieldCurve, pointsUpfront);
    return parSpreads(cds, yieldCurve, creditCurve);
  }

  /**
  * Convert from par spreads to quoted spreads
   * @param cds collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param parSpreads par spreads
   * @return quoted spreads
   */
  public double[] parSpreadsToQuotedSpreads(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    final double[] puf = parSpreadsToPUF(cds, premium, yieldCurve, parSpreads);
    return pufToQuotedSpreads(cds, premium, yieldCurve, puf);
  }

  /**
   * Convert from par spreads to quoted spreads
   * @param cds collection of CDSs
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param parSpreads par spreads
   * @return quoted spreads
   */
  public double[] parSpreadsToQuotedSpreads(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    final double[] puf = parSpreadsToPUF(cds, premiums, yieldCurve, parSpreads);
    return pufToQuotedSpreads(cds, premiums, yieldCurve, puf);
  }

  /**
   * Convert from quoted spreads to par spreads
   * @param cds collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param quotedSpreads The quoted spreads
   * @return par spreads
   */
  public double[] quotedSpreadToParSpreads(final CDSAnalytic[] cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double[] quotedSpreads) {
    final double[] puf = quotedSpreadsToPUF(cds, premium, yieldCurve, quotedSpreads);
    return pufToParSpreads(cds, premium, yieldCurve, puf);
  }

  /**
   * Convert from quoted spreads to par spreads
   * @param cds collection of CDSs
   * @param premiums The premiums of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param quotedSpreads The quoted spreads
   * @return par spreads
   */
  public double[] quotedSpreadToParSpreads(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] quotedSpreads) {
    final double[] puf = quotedSpreadsToPUF(cds, premiums, yieldCurve, quotedSpreads);
    return pufToParSpreads(cds, premiums, yieldCurve, puf);
  }

  /**
   * The par spreads for a collection of CDSs where a single, non-flat, credit/hazard curve is known.
   * @param cds collection of CDSs
   * @param yieldCurve the yield/discount curve
   * @param creditCurve the credit/hazard curve
   * @return par spreads
   */
  public double[] parSpreads(final CDSAnalytic[] cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    final int n = cds.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = _pricer.parSpread(cds[i], yieldCurve, creditCurve);
    }
    return res;
  }

}
