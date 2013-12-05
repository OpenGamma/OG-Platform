/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 * This should be viewed as "proof of concept" code, since it used the code that has date logic mixed with the analytics (this was to
 * mimic the structure of the ISDA c code). This should <b>not</b> be used for production credit (hazard) curve calibration/bootstrapping,
 * ISDACompliantCreditCurveCalibrator should be used.
 *
 */
public class ISDACompliantCurveCalibrator {

  private static final DayCount ACT_365 = DayCounts.ACT_365;

  private static final BracketRoot BRACKER = new BracketRoot();
  private static final RealSingleRootFinder ROOTFINDER = new BrentSingleRootFinder();
  private static final ISDACompliantPresentValueCreditDefaultSwap PRICER = new ISDACompliantPresentValueCreditDefaultSwap();

  public ISDACompliantDateCreditCurve calibrateHazardCurve(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate[] endDates,
      final double[] couponRates, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final boolean protectStart, final ISDACompliantDateYieldCurve yieldCurve,
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

    // use continuous premiums as initial guess
    final double[] guess = new double[n];
    final double[] t = new double[n];
    final double lgd = 1 - recoveryRate;
    for (int i = 0; i < n; i++) {
      guess[i] = couponRates[i] / lgd;
      t[i] = ACT_365.getDayCountFraction(today, endDates[i]);
    }

    // HazardRateCurve hazardCurve = new HazardRateCurve(toZoneDateTime(endDates), t, guess, 0.0);
    ISDACompliantDateCreditCurve hazardCurve = new ISDACompliantDateCreditCurve(today, endDates, guess);
    for (int i = 0; i < n; i++) {
      final CDSPricer func = new CDSPricer(i, today, stepinDate, valueDate, startDate, endDates[i], couponRates[i], protectStart, payAccOnDefault,
          tenor, stubType, recoveryRate, yieldCurve, hazardCurve);
      final double[] bracket = BRACKER.getBracketedPoints(func, 0.9 * guess[i], 1.1 * guess[i], 0.0, Double.POSITIVE_INFINITY);
      final double zeroRate = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
      hazardCurve = hazardCurve.withRate(zeroRate, i);
    }

    return hazardCurve;
  }

  private class CDSPricer extends Function1D<Double, Double> {

    private final int _index;
    private final LocalDate _today;
    private final LocalDate _stepinDate;
    private final LocalDate _valueDate;
    private final LocalDate _startDate;
    private final LocalDate _endDate;
    private final double _couponRate;
    private final boolean _protectStart;

    private final boolean _payAccOnDefault;
    private final Period _tenor;
    private final StubType _stubType;
    private final double _rr;

    private final ISDACompliantDateYieldCurve _yieldCurve;
    private final ISDACompliantDateCreditCurve _hazardCurve;

    public CDSPricer(final int index, final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate, final LocalDate endDate, final double couponRate,
        final boolean protectStart, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final double rr, final ISDACompliantDateYieldCurve yieldCurve,
        final ISDACompliantDateCreditCurve hazardCurve) {

      _index = index;
      _today = today;
      _stepinDate = stepinDate;
      _valueDate = valueDate;
      _startDate = startDate;
      _endDate = endDate;
      _couponRate = couponRate;
      _protectStart = protectStart;
      _payAccOnDefault = payAccOnDefault;
      _tenor = tenor;
      _stubType = stubType;
      _rr = rr;
      _yieldCurve = yieldCurve;
      _hazardCurve = hazardCurve;

    }

    @Override
    public Double evaluate(final Double x) {
      // TODO this direct access is unpleasant
      final ISDACompliantDateCreditCurve hazardCurve = _hazardCurve.withRate(x, _index);
      final double rpv01 = PRICER.pvPremiumLegPerUnitSpread(_today, _stepinDate, _valueDate, _startDate, _endDate, _payAccOnDefault, _tenor, _stubType, _yieldCurve, hazardCurve, _protectStart,
          PriceType.CLEAN);
      final double protectLeg = PRICER.calculateProtectionLeg(_today, _stepinDate, _valueDate, _startDate, _endDate, _yieldCurve, hazardCurve, _rr, _protectStart);
      final double pv = protectLeg - _couponRate * rpv01;
      return pv;
    }
  }

}
