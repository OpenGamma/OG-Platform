/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * /**
 * This is a bootstrapper for the credit curve that is consistent with ISDA in that it will produce the same curve from
 * the same inputs (up to numerical round-off) 
 * @deprecated Use the faster ISDACompliantCreditCurveBuild
 */
@Deprecated
public class SimpleCreditCurveBuilder extends ISDACompliantCreditCurveBuilder {

  private static final BracketRoot BRACKER = new BracketRoot();
  private static final RealSingleRootFinder ROOTFINDER = new BrentSingleRootFinder();
  // private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();

  private final AnalyticCDSPricer _pricer;

  public SimpleCreditCurveBuilder() {
    _pricer = new AnalyticCDSPricer();
  }

  public SimpleCreditCurveBuilder(final AccrualOnDefaultFormulae formula) {
    super(formula);
    _pricer = new AnalyticCDSPricer(formula);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double premium, final ISDACompliantYieldCurve yieldCurve, final double pointsUpfront) {
    return calibrateCreditCurve(new CDSAnalytic[] {cds }, new double[] {premium }, yieldCurve, new double[] {pointsUpfront });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic cds, final double marketFractionalSpread, final ISDACompliantYieldCurve yieldCurve) {
    return calibrateCreditCurve(new CDSAnalytic[] {cds }, new double[] {marketFractionalSpread }, yieldCurve, new double[1]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] cds, final double[] fractionalSpreads, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.notNull(cds, "cds");
    final int n = cds.length;
    return calibrateCreditCurve(cds, fractionalSpreads, yieldCurve, new double[n]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] cds, final double[] premiums, final ISDACompliantYieldCurve yieldCurve, final double[] pointsUpfront) {
    ArgumentChecker.noNulls(cds, "cds");
    ArgumentChecker.notEmpty(premiums, "empty fractionalSpreads");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == premiums.length, "Number of CDSs does not match number of spreads");
    final double proStart = cds[0].getEffectiveProtectionStart();
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(proStart == cds[i].getEffectiveProtectionStart(), "all CDSs must has same protection start");
      ArgumentChecker.isTrue(cds[i].getProtectionEnd() > cds[i - 1].getProtectionEnd(), "protection end must be ascending");
    }

    // use continuous premiums as initial guess
    final double[] guess = new double[n];
    final double[] t = new double[n];
    for (int i = 0; i < n; i++) {
      guess[i] = premiums[i] / cds[i].getLGD();
      t[i] = cds[i].getProtectionEnd();
    }

    ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(t, guess);
    for (int i = 0; i < n; i++) {
      final CDSPricer func = new CDSPricer(i, cds[i], premiums[i], creditCurve, yieldCurve, pointsUpfront[i]);
      final double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * guess[i], 1.25 * guess[i], 0.0, Double.POSITIVE_INFINITY);
      final double zeroRate = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
      creditCurve = creditCurve.withRate(zeroRate, i);
    }

    return creditCurve;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate,
      final double fractionalParSpread, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
      final double recoveryRate) {
    return calibrateCreditCurve(today, stepinDate, valueDate, startDate, new LocalDate[] {endDate }, new double[] {fractionalParSpread }, payAccOnDefault, tenor, stubType, protectStart, yieldCurve,
        recoveryRate);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate[] endDates,
      final double[] couponRates, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final boolean protectStart, final ISDACompliantYieldCurve yieldCurve,
      final double recoveryRate) {

    ArgumentChecker.notNull(today, "null today");
    ArgumentChecker.notNull(stepinDate, "null stepinDate");
    ArgumentChecker.notNull(valueDate, "null valueDate");
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.noNulls(endDates, "null endDates");
    ArgumentChecker.notEmpty(couponRates, "no or null couponRates");
    ArgumentChecker.notNull(tenor, "null tenor");
    ArgumentChecker.notNull(stubType, "null stubType");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.isInRangeExcludingHigh(0, 1.0, recoveryRate);
    ArgumentChecker.isFalse(valueDate.isBefore(today), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(today), "Require stepin >= today");

    final int n = endDates.length;
    ArgumentChecker.isTrue(n == couponRates.length, "length of couponRates does not match endDates");

    final CDSAnalytic[] cds = new CDSAnalytic[n];
    for (int i = 0; i < n; i++) {
      cds[i] = new CDSAnalytic(today, stepinDate, valueDate, startDate, endDates[i], payAccOnDefault, tenor, stubType, protectStart, recoveryRate);
    }

    return calibrateCreditCurve(cds, couponRates, yieldCurve);
  }

  private class CDSPricer extends Function1D<Double, Double> {

    private final int _index;
    private final CDSAnalytic _cds;
    private final ISDACompliantCreditCurve _creditCurve;
    private final ISDACompliantYieldCurve _yieldCurve;
    private final double _spread;
    private final double _pointsUpfront;

    public CDSPricer(final int index, final CDSAnalytic cds, final double fracSpread, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurve yieldCurve, final double pointsUpfront) {

      _index = index;
      _cds = cds;
      _yieldCurve = yieldCurve;
      _creditCurve = creditCurve;
      _spread = fracSpread;
      _pointsUpfront = pointsUpfront;
    }

    @Override
    public Double evaluate(final Double x) {
      final ISDACompliantCreditCurve cc = _creditCurve.withRate(x, _index);
      return _pricer.pv(_cds, _yieldCurve, cc, _spread) - _pointsUpfront;
    }
  }

  //TODO
  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic calibrationCDS, final CDSQuoteConvention marketQuote, final ISDACompliantYieldCurve yieldCurve) {
    double puf;
    double coupon;
    if (marketQuote instanceof ParSpread) {
      puf = 0.0;
      coupon = marketQuote.getCoupon();
    } else if (marketQuote instanceof QuotedSpread) {
      puf = 0.0;
      coupon = ((QuotedSpread) marketQuote).getQuotedSpread();
    } else if (marketQuote instanceof PointsUpFront) {
      final PointsUpFront temp = (PointsUpFront) marketQuote;
      puf = temp.getPointsUpFront();
      coupon = temp.getCoupon();
    } else {
      throw new IllegalArgumentException("Unknown CDSQuoteConvention type " + marketQuote.getClass());
    }

    return calibrateCreditCurve(new CDSAnalytic[] {calibrationCDS }, new double[] {coupon }, yieldCurve, new double[] {puf });
  }

  @Override
  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] calibrationCDSs, final CDSQuoteConvention[] marketQuotes, final ISDACompliantYieldCurve yieldCurve) {
    throw new NotImplementedException();
  }

}
