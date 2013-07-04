/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * /**
 * This is a bootstrapper for the credit curve that is consistent with ISDA in that it will produce the same curve from
 * the same inputs (up to numerical round-off) 
 * @deprecated Use the faster ISDACompliantCreditCurveBuild
 */
@Deprecated
public class ISDACompliantCreditCurveCalibrator {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private static final BracketRoot BRACKER = new BracketRoot();
  private static final RealSingleRootFinder ROOTFINDER = new BrentSingleRootFinder();
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();

  public ISDACompliantCreditCurve calibrateCreditCurve(final CDSAnalytic[] cds, final double[] fractionalSpreads, final ISDACompliantYieldCurve yieldCurve) {
    ArgumentChecker.noNulls(cds, "null CDSs");
    ArgumentChecker.notEmpty(fractionalSpreads, "empty fractionalSpreads");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    final int n = cds.length;
    ArgumentChecker.isTrue(n == fractionalSpreads.length, "Number of CDSs does not match number of spreads");
    double proStart = cds[0].getProtectionStart();
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(proStart == cds[i].getProtectionStart(), "all CDSs must has same protection start");
      ArgumentChecker.isTrue(cds[i].getProtectionEnd() > cds[i - 1].getProtectionEnd(), "protection end must be ascending");
    }

    // use continuous premiums as initial guess
    double[] guess = new double[n];
    double[] t = new double[n];
    for (int i = 0; i < n; i++) {
      guess[i] = fractionalSpreads[i] / cds[i].getLGD();
      t[i] = cds[i].getProtectionEnd();
    }

    ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(t, guess);
    for (int i = 0; i < n; i++) {
      CDSPricer func = new CDSPricer(i, cds[i], fractionalSpreads[i], creditCurve, yieldCurve);
      double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * guess[i], 1.25 * guess[i], 0.0, Double.POSITIVE_INFINITY);
      double zeroRate = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
      creditCurve = creditCurve.withRate(zeroRate, i);
    }

    return creditCurve;
  }

  public ISDACompliantCreditCurve calibrateCreditCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate[] endDates,
      final double[] couponRates, final boolean payAccOnDefault, final Period tenor, StubType stubType, final boolean protectStart, final ISDACompliantDateYieldCurve yieldCurve,
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

    public CDSPricer(final int index, final CDSAnalytic cds, final double fracSpread, final ISDACompliantCreditCurve creditCurve, final ISDACompliantYieldCurve yieldCurve) {

      _index = index;
      _cds = cds;
      _yieldCurve = yieldCurve;
      _creditCurve = creditCurve;
      _spread = fracSpread;
    }

    @Override
    public Double evaluate(Double x) {
      ISDACompliantCreditCurve cc = _creditCurve.withRate(x, _index);
      return PRICER.pv(_cds, _yieldCurve, cc, _spread);
    }
  }

}
