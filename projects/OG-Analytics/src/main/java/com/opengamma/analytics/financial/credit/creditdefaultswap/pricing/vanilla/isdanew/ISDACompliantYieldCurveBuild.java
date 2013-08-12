/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDACompliantYieldCurveBuild {
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder(); // new BrentSingleRootFinder(); // TODO get gradient and use Newton
  private static final BracketRoot BRACKETER = new BracketRoot();

  /**
   *  Build a ISDA-Compliant yield curve (i.e. one with piecewise flat forward rate) from money market rates and par swap rates.
   *  Note if today is different from spotDate, the curve is adjusted accordingly
   * @param today The 'observation' date
   * @param spotDate The spot date of the instruments
   * @param instrumentTypes List of instruments - these are  MoneyMarket or Swap
   * @param tenors The length of the instruments (e.g. a 5y swap would be  Period.ofYears(5))
   * @param rates the par rates (as fractions) of the instruments
   * @param moneyMarketDCC The day count convention for money market instruments
   * @param swapDCC The day count convention for swap fixed payments
   * @param swapInterval Time between fixed payments (e.g. 3M fixed is Period.ofMonths(3))
   * @param curveDCC The day count convention used by the yield/discount curve - normally this is ACT/365
   * @param convention Specification of non-business days
   * @return A yield curve observed from today
   */
  public ISDACompliantYieldCurve build(final LocalDate today, final LocalDate spotDate, final ISDAInstrumentTypes[] instrumentTypes, final Period[] tenors, final double[] rates,
      final DayCount moneyMarketDCC, final DayCount swapDCC, final Period swapInterval, final DayCount curveDCC, final BusinessDayConvention convention) {
    final ISDACompliantYieldCurve baseCurve = build(spotDate, instrumentTypes, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, convention);
    if (spotDate.isEqual(today)) {
      return baseCurve;
    }

    final double offset = today.isAfter(spotDate) ? -curveDCC.getDayCountFraction(spotDate, today) : curveDCC.getDayCountFraction(today, spotDate);
    return new ISDACompliantYieldCurve(baseCurve.getKnotTimes(), baseCurve.getKnotZeroRates(), offset);
  }

  /**
   * Build a ISDA-Compliant yield curve (i.e. one with piecewise flat forward rate) from money market rates and par swap rates.
   * @param spotDate The spot date of the instruments (note is curve is assumed to be observed from this date)
   * @param instrumentTypes List of instruments - these are  MoneyMarket or Swap
   * @param tenors The length of the instruments (e.g. a 5y swap would be  Period.ofYears(5))
   * @param rates the par rates (as fractions) of the instruments
   * @param moneyMarketDCC The day count convention for money market instruments
   * @param swapDCC The day count convention for swap fixed payments
   * @param swapInterval Time between fixed payments (e.g. 3M fixed is Period.ofMonths(3))
   * @param curveDCC The day count convention used by the yield/discount curve - normally this is ACT/365
   * @param convention Specification of non-business days
   * @return A yield curve
   */
  public ISDACompliantYieldCurve build(final LocalDate spotDate, final ISDAInstrumentTypes[] instrumentTypes, final Period[] tenors, final double[] rates, final DayCount moneyMarketDCC,
      final DayCount swapDCC, final Period swapInterval, final DayCount curveDCC, final BusinessDayConvention convention) {

    final int n = tenors.length;
    final LocalDate[] matDates = new LocalDate[n];
    final LocalDate[] adjMatDates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      matDates[i] = spotDate.plus(tenors[i]);
      if (i == 0) {
        ArgumentChecker.isTrue(matDates[0].isAfter(spotDate), "first tenor zero");
      } else {
        ArgumentChecker.isTrue(matDates[i].isAfter(matDates[i - 1]), "tenors are not assending");
      }
      adjMatDates[i] = convention.adjustDate(DEFAULT_CALENDAR, matDates[i]);
    }

    final double[] t = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = curveDCC.getDayCountFraction(spotDate, adjMatDates[i]);
    }

    // set up curve with best guess rates
    ISDACompliantCurve curve = new ISDACompliantCurve(t, rates);

    // loop over the instruments and adjust the curve to price each in turn
    for (int i = 0; i < n; i++) {
      if (instrumentTypes[i] == ISDAInstrumentTypes.MoneyMarket) {
        // TODO in ISDA code money market instruments of less than 21 days have special treatment
        final double dt = moneyMarketDCC.getDayCountFraction(spotDate, adjMatDates[i]);
        final double z = 1.0 / (1 + rates[i] * dt);
        curve = curve.withDiscountFactor(z, i);
      } else {
        final BasicFixedLeg swap = new BasicFixedLeg(spotDate, matDates[i], swapInterval, rates[i], swapDCC, curveDCC, convention);
        curve = fitSwap(i, swap, curve);
      }

    }
    return new ISDACompliantYieldCurve(curve);
  }

  private ISDACompliantCurve fitSwap(final int curveIndex, final BasicFixedLeg swap, final ISDACompliantCurve curve) {

    final int nPayments = swap.getNumPayments();
    final int nNodes = curve.getNumberOfKnots();
    final double t1 = curveIndex == 0 ? 0.0 : curve.getTimeAtIndex(curveIndex - 1);
    final double t2 = curveIndex == nNodes - 1 ? Double.POSITIVE_INFINITY : curve.getTimeAtIndex(curveIndex + 1);

    double temp = 0;
    double temp2 = 0;
    int i1 = 0;
    int i2 = nPayments;
    for (int i = 0; i < nPayments; i++) {
      final double t = swap.getPaymentTime(i);
      if (t <= t1) {
        final double c = swap.getPaymentAmounts(i);
        final double df = curve.getDiscountFactor(t);
        temp += c * df;
        temp2 -= c * curve.getSingleNodeDiscountFactorSensitivity(t, curveIndex);
        i1++;
      } else if (t >= t2) {
        final double c = swap.getPaymentAmounts(i);
        final double df = curve.getDiscountFactor(t);
        temp += c * df;
        temp2 += c * curve.getSingleNodeDiscountFactorSensitivity(t, curveIndex);
        i2--;
      }
    }
    final double cachedValues = temp;
    final double cachedSense = temp2;
    final int index1 = i1;
    final int index2 = i2;

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final ISDACompliantCurve tempCurve = curve.withRate(x, curveIndex);
        double sum = 1.0 - cachedValues; // Floating leg at par
        for (int i = index1; i < index2; i++) {
          final double t = swap.getPaymentTime(i);
          sum -= swap.getPaymentAmounts(i) * tempCurve.getDiscountFactor(t);
        }
        return sum;
      }
    };

    final Function1D<Double, Double> grad = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final ISDACompliantCurve tempCurve = curve.withRate(x, curveIndex);
        double sum = cachedSense;
        for (int i = index1; i < index2; i++) {
          final double t = swap.getPaymentTime(i);
          // TODO have two looks ups for the same time - could have a specialist function in ISDACompliantCurve
          sum -= swap.getPaymentAmounts(i) * tempCurve.getSingleNodeDiscountFactorSensitivity(t, curveIndex);
        }
        return sum;
      }

    };

    final double guess = curve.getZeroRateAtIndex(curveIndex);
    if (guess == 0.0 && func.evaluate(guess) == 0.0) {
      return curve;
    }
    final double[] bracket = BRACKETER.getBracketedPoints(func, 0.8 * guess, 1.25 * guess, 0, Double.POSITIVE_INFINITY);
    // final double r = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
    final double r = ROOTFINDER.getRoot(func, grad, bracket[0], bracket[1]);
    return curve.withRate(r, curveIndex);
  }

  /**
   * very crude swap fixed leg description. TODO modify to match ISDA <p>
   * So that the floating leg can be taken as having a value of 1.0, rather than the text book 1 - P(T) for LIBOR discounting,
   * we add 1.0 to the final payment, which is financially equivalent
   */
  private class BasicFixedLeg {
    private final int _nPayments;
    private final double[] _swapPaymentTimes;
    private final double[] _paymentAmounts;

    public BasicFixedLeg(final LocalDate spotDate, final LocalDate mat, final Period swapInterval, final double rate, final DayCount swapDCC, final DayCount curveDCC,
        final BusinessDayConvention convention) {
      ArgumentChecker.isFalse(swapInterval.getDays() > 0, "swap interval must be in months or years");

      final List<LocalDate> list = new ArrayList<>();
      LocalDate tDate = mat;
      list.add(tDate);
      int step = 1;
      while (tDate.isAfter(spotDate)) {
        tDate = mat.minus(swapInterval.multipliedBy(step++));
        list.add(tDate);
      }

      // remove spotDate from list, if it ends up there
      list.remove(spotDate);

      _nPayments = list.size();
      _swapPaymentTimes = new double[_nPayments];
      _paymentAmounts = new double[_nPayments];

      LocalDate prev = spotDate;
      int j = _nPayments - 1;
      for (int i = 0; i < _nPayments; i++, j--) {
        final LocalDate current = list.get(j);
        final LocalDate adjCurr = convention.adjustDate(DEFAULT_CALENDAR, current);
        _paymentAmounts[i] = rate * swapDCC.getDayCountFraction(prev, adjCurr);
        _swapPaymentTimes[i] = curveDCC.getDayCountFraction(spotDate, adjCurr); // Payment times always good business days
        prev = adjCurr;
      }
      _paymentAmounts[_nPayments - 1] += 1.0; // see Javadocs comment
    }

    public int getNumPayments() {
      return _nPayments;
    }

    public double getPaymentAmounts(final int index) {
      return _paymentAmounts[index];
    }

    public double getPaymentTime(final int index) {
      return _swapPaymentTimes[index];
    }

  }

}
