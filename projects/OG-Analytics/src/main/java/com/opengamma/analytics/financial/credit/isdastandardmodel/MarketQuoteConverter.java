/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * Standard CDS have a standard premium (100 or 500bps in North America) and are quoted as either as Points Up-Front (PUF) or quoted 
 * spread - these method allow conversion via a single constant hazard rate. <p>
 * A credit curve can be built that is consistent with market quotes; this can then be used to imply equivalent par spreads, and vice-versa.  
 */
public class MarketQuoteConverter {

  private final ISDACompliantCreditCurveBuilder _builder;
  private final AnalyticCDSPricer _pricer;

  public MarketQuoteConverter() {
    _builder = new FastCreditCurveBuilder();
    _pricer = new AnalyticCDSPricer();
  }

  public MarketQuoteConverter(final AccrualOnDefaultFormulae formula) {
    _builder = new FastCreditCurveBuilder(formula);
    _pricer = new AnalyticCDSPricer(formula);
  }

  //**************************************************************************************************************
  // Various ways of quoting the price for a given credit curve 
  //**************************************************************************************************************

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

  //**************************************************************************************************************
  // Single Converters. These convert between a single quote as PUF or quoted spread by building a 
  // constant hazard rate curve.
  //**************************************************************************************************************

  /**
   * Convert from a CDS quoted spread to points up-front (PUF).
   * @param cds The CDS to be traded
   * @param qSpread The quoted spread
   * @param yieldCurve the yield/discount curve
   * @return the PUF
   */
  public PointsUpFront convert(final CDSAnalytic cds, final QuotedSpread qSpread, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(qSpread, "qSpread");
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
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, quotedSpread, yieldCurve);
    return pointsUpFront(cds, premium, yieldCurve, creditCurve);
  }

  /**
   * Convert from a CDS points up-front (PUF) to a quoted spread.
   * @param cds The CDS to be traded
   * @param puf point-up-front (this contains the premium) 
   * @param yieldCurve the yield/discount curve
   * @return the par spread
   */
  public QuotedSpread convert(final CDSAnalytic cds, final PointsUpFront puf, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(puf, "puf");
    final double qs = pufToQuotedSpread(cds, puf.getCoupon(), yieldCurve, puf.getPointsUpFront());
    return new QuotedSpread(puf.getCoupon(), qs);
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
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, premium, yieldCurve, pointsUpfront);
    return _pricer.parSpread(cds, yieldCurve, creditCurve);
  }

  //**************************************************************************************************************
  // multiple Converters. These convert between N quotes as PUF or quoted spread by building N independent  
  // constant hazard rate curves. 
  //**************************************************************************************************************

  public PointsUpFront[] convert(final CDSAnalytic[] cds, final QuotedSpread[] qSpreads, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(qSpreads, "qSpreads");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == qSpreads.length, "numbe of CDSs does not match qSpreads");
    final PointsUpFront[] res = new PointsUpFront[n];
    for (int i = 0; i < n; i++) {
      res[i] = convert(cds[i], qSpreads[i], yieldCurve);
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
    ArgumentChecker.notNull(cds, "cds");
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
    ArgumentChecker.notNull(cds, "cds");
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

  public QuotedSpread[] convert(final CDSAnalytic[] cds, final PointsUpFront[] puf, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(puf, "puf");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == puf.length, "numbe of CDSs does not match puf");
    final QuotedSpread[] res = new QuotedSpread[n];
    for (int i = 0; i < n; i++) {
      res[i] = convert(cds[i], puf[i], yieldCurve);
    }
    return res;
  }

  /**
   * Get the equivalent <i>quoted</i> spreads for a collection of CDSs. This is simply a quoting convention -each CDS is priced
   * off a <b>separate</b> flat credit/hazard curve - i.e. the CDSs are completely decoupled from each other.
   * @param cds collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see MarketQuoteConverter#pufToQuotedSpread
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
   * @see MarketQuoteConverter#pufToQuotedSpread
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

  //**************************************************************************************************************
  // Full Curve Converters. These convert between par spread quotes and PUF by constructing a single 
  // credit curve consistent with the quotes.
  //**************************************************************************************************************

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
   * @see MarketQuoteConverter#pointsUpFront
   * @return points up-front (expressed as fractions)
   */
  public double[] parSpreadsToPUF(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] parSpreads) {
    final ISDACompliantCreditCurve creditCurve = _builder.calibrateCreditCurve(cds, parSpreads, yieldCurve);
    return pointsUpFront(cds, premiums, yieldCurve, creditCurve);
  }

  /**
   * The equivalent par spreads for a collection of CDSs where a single, non-flat, credit/hazard curve is bootstrapped to
   * reprice all the given CDSs.
   * @param cds collection of CDSs
   * @param premium The single common premium of the CDSs expressed as fractions (these are usually 0.01 or 0.05)
   * @param yieldCurve the yield/discount curve
   * @param pointsUpfront The points up-front (expressed as fractions)
   * @see MarketQuoteConverter#pufToParSpreads
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
   * @see MarketQuoteConverter#parSpreads
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

}
