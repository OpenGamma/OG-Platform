/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDACompliantYieldCurveBuild {
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  private static final NewtonRaphsonSingleRootFinder ROOTFINDER = new NewtonRaphsonSingleRootFinder(); // new BrentSingleRootFinder(); // TODO get gradient and use Newton
  private static final BracketRoot BRACKETER = new BracketRoot();

  private final double _offset; //if curve spot date is not the same as CDS trade date
  private final double[] _t; //yieldCurve nodes
  private final double[] _mmYF; //money market year fractions
  private final BasicFixedLeg[] _swaps;
  private final ISDAInstrumentTypes[] _instrumentTypes;

  //************************************************************************************************************************
  // static curve builders 
  //************************************************************************************************************************

  /**
   *  Build a ISDA-Compliant yield curve (i.e. one with piecewise flat forward rate) from money market rates and par swap rates.
   *  Note if cdsTradeDate (today) is different from spotDate, the curve is adjusted accordingly
   * @param cdsTradeDate The 'observation' date
   * @param spotDate The spot date of the instruments
   * @param instrumentTypes List of instruments - these are  MoneyMarket or Swap
   * @param tenors The length of the instruments (e.g. a 5y swap would be  Period.ofYears(5))
   * @param rates the par rates (as fractions) of the instruments
   * @param moneyMarketDCC The day count convention for money market instruments
   * @param swapDCC The day count convention for swap fixed payments
   * @param swapInterval Time between fixed payments (e.g. 3M fixed is Period.ofMonths(3))
   * @param curveDCC The day count convention used by the yield/discount curve - normally this is ACT/365
   * @param convention Specification for the handling of  non-business days
   * @return A yield curve observed from today
   */
  public static ISDACompliantYieldCurve build(final LocalDate cdsTradeDate, final LocalDate spotDate, final ISDAInstrumentTypes[] instrumentTypes, final Period[] tenors, final double[] rates,
      final DayCount moneyMarketDCC, final DayCount swapDCC, final Period swapInterval, final DayCount curveDCC, final BusinessDayConvention convention) {
    final ISDACompliantYieldCurveBuild builder = new ISDACompliantYieldCurveBuild(cdsTradeDate, spotDate, instrumentTypes, tenors, moneyMarketDCC, swapDCC, swapInterval, curveDCC, convention);
    return builder.build(rates);
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
   * @param convention Specification for the handling of  non-business days
   * @return A yield curve
   */
  public static ISDACompliantYieldCurve build(final LocalDate spotDate, final ISDAInstrumentTypes[] instrumentTypes, final Period[] tenors, final double[] rates, final DayCount moneyMarketDCC,
      final DayCount swapDCC, final Period swapInterval, final BusinessDayConvention convention) {
    final ISDACompliantYieldCurveBuild builder = new ISDACompliantYieldCurveBuild(spotDate, instrumentTypes, tenors, moneyMarketDCC, swapDCC, swapInterval, convention);
    return builder.build(rates);
  }

  //************************************************************************************************************************
  // constructors 
  //************************************************************************************************************************

  /**
   * Set up a yield curve builder (run the method build, to build the curve). Note, the cds trade date is taken as the spot date;
   * a weekend only calendar is used.
   * @param spotDate The spot date for the instruments used to build the yield curve 
   * @param instrumentTypes  The instrument type for each curve node 
   * @param tenors The tenors of the instruments 
   * @param moneyMarketDCC The day-count-convention for money market (spot libor) instruments (accrual)
   * @param swapDCC The day-count-convention for swaps (accrual)  
   * @param swapInterval The payment interval for the swaps
   * @param convention Specification for the handling of  non-business days
   */
  public ISDACompliantYieldCurveBuild(final LocalDate spotDate, final ISDAInstrumentTypes[] instrumentTypes, final Period[] tenors, final DayCount moneyMarketDCC, final DayCount swapDCC,
      final Period swapInterval, final BusinessDayConvention convention) {
    this(spotDate, spotDate, instrumentTypes, tenors, moneyMarketDCC, swapDCC, swapInterval, ACT_365, convention);
  }

  /**
   * Set up a yield curve builder (run the method build, to build the curve). Note, a weekend only calendar is used.
   * @param cdsTradeDate The trade date (aka today)
   * @param spotDate The spot date for the instruments used to build the yield curve 
   * @param instrumentTypes  The instrument type for each curve node 
   * @param tenors The tenors of the instruments 
   * @param moneyMarketDCC The day-count-convention for money market (spot libor) instruments (accrual)
   * @param swapDCC The day-count-convention for swaps (accrual)  
   * @param swapInterval The payment interval for the swaps
   * @param curveDCC The day-count-convention used for the curve 
   * @param convention Specification for the handling of  non-business days
   */
  public ISDACompliantYieldCurveBuild(final LocalDate cdsTradeDate, final LocalDate spotDate, final ISDAInstrumentTypes[] instrumentTypes, final Period[] tenors, final DayCount moneyMarketDCC,
      final DayCount swapDCC, final Period swapInterval, final DayCount curveDCC, final BusinessDayConvention convention) {
    this(cdsTradeDate, spotDate, instrumentTypes, tenors, moneyMarketDCC, swapDCC, swapInterval, curveDCC, convention, DEFAULT_CALENDAR);
  }

  /**
   * Set up a yield curve builder (run the method build, to build the curve). 
   * @param cdsTradeDate The trade date (aka today)
   * @param spotDate The spot date for the instruments used to build the yield curve 
   * @param instrumentTypes  The instrument type for each curve node 
   * @param tenors The tenors of the instruments 
   * @param moneyMarketDCC The day-count-convention for money market (spot libor) instruments (accrual)
   * @param swapDCC The day-count-convention for swaps (accrual)  
   * @param swapInterval The payment interval for the swaps
   * @param curveDCC The day-count-convention used for the curve 
   * @param convention Specification for the handling of  non-business days
   * @param calendar Calendar defining what is a non-business day
   */
  public ISDACompliantYieldCurveBuild(final LocalDate cdsTradeDate, final LocalDate spotDate, final ISDAInstrumentTypes[] instrumentTypes, final Period[] tenors, final DayCount moneyMarketDCC,
      final DayCount swapDCC, final Period swapInterval, final DayCount curveDCC, final BusinessDayConvention convention, final Calendar calendar) {
    ArgumentChecker.notNull(spotDate, "spotDate");
    ArgumentChecker.noNulls(instrumentTypes, "instrumentTypes");
    ArgumentChecker.noNulls(tenors, "tenors");
    ArgumentChecker.notNull(moneyMarketDCC, "moneyMarketDCC");
    ArgumentChecker.notNull(swapDCC, "swapDCC");
    ArgumentChecker.notNull(swapInterval, "swapInterval");
    ArgumentChecker.notNull(curveDCC, "curveDCC");
    ArgumentChecker.notNull(convention, "convention");
    final int n = tenors.length;
    ArgumentChecker.isTrue(n == instrumentTypes.length, "{} tenors given, but {} instrumentTypes", n, instrumentTypes.length);

    final LocalDate[] matDates = new LocalDate[n];
    final LocalDate[] adjMatDates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      matDates[i] = spotDate.plus(tenors[i]);
      if (i == 0) {
        ArgumentChecker.isTrue(matDates[0].isAfter(spotDate), "first tenor zero");
      } else {
        ArgumentChecker.isTrue(matDates[i].isAfter(matDates[i - 1]), "tenors are not assending");
      }
      adjMatDates[i] = convention.adjustDate(calendar, matDates[i]);
    }

    _t = new double[n];
    _instrumentTypes = instrumentTypes;
    int nMM = 0;
    for (int i = 0; i < n; i++) {
      _t[i] = curveDCC.getDayCountFraction(spotDate, adjMatDates[i]);
      if (_instrumentTypes[i] == ISDAInstrumentTypes.MoneyMarket) {
        nMM++;
      }
    }
    final int nSwap = n - nMM;
    _mmYF = new double[nMM];
    _swaps = new BasicFixedLeg[nSwap];
    int mmCount = 0;
    int swapCount = 0;
    for (int i = 0; i < n; i++) {
      if (instrumentTypes[i] == ISDAInstrumentTypes.MoneyMarket) {
        // TODO in ISDA code money market instruments of less than 21 days have special treatment
        _mmYF[mmCount++] = moneyMarketDCC.getDayCountFraction(spotDate, adjMatDates[i]);
      } else {
        _swaps[swapCount++] = new BasicFixedLeg(spotDate, matDates[i], swapInterval, swapDCC, curveDCC, convention, calendar);
      }
    }
    _offset = cdsTradeDate.isAfter(spotDate) ? curveDCC.getDayCountFraction(spotDate, cdsTradeDate) : -curveDCC.getDayCountFraction(cdsTradeDate, spotDate);
  }

  /**
   * build a yield curve 
   * @param rates The par rates of the instruments (as fractions) 
   * @return a yield curve 
   */
  public ISDACompliantYieldCurve build(final double[] rates) {
    ArgumentChecker.notEmpty(rates, "rates");
    final int n = _instrumentTypes.length;
    ArgumentChecker.isTrue(n == rates.length, "expecting " + n + " rates, given " + rates.length);

    // set up curve with best guess rates
    ISDACompliantCurve curve = new ISDACompliantCurve(_t, rates);
    // loop over the instruments and adjust the curve to price each in turn
    int mmCount = 0;
    int swapCount = 0;
    for (int i = 0; i < n; i++) {
      if (_instrumentTypes[i] == ISDAInstrumentTypes.MoneyMarket) {
        // TODO in ISDA code money market instruments of less than 21 days have special treatment
        final double z = 1.0 / (1 + rates[i] * _mmYF[mmCount++]);
        curve = curve.withDiscountFactor(z, i);
      } else {
        curve = fitSwap(i, _swaps[swapCount++], curve, rates[i]);
      }
    }

    final ISDACompliantYieldCurve baseCurve = new ISDACompliantYieldCurve(curve);
    if (_offset == 0.0) {
      return baseCurve;
    }
    return baseCurve.withOffset(_offset);
    //  return new ISDACompliantYieldCurve(baseCurve.getKnotTimes(), baseCurve.getKnotZeroRates(), _offset);
  }

  private ISDACompliantCurve fitSwap(final int curveIndex, final BasicFixedLeg swap, final ISDACompliantCurve curve, final double swapRate) {

    final int nPayments = swap.getNumPayments();
    final int nNodes = curve.getNumberOfKnots();
    final double t1 = curveIndex == 0 ? 0.0 : curve.getTimeAtIndex(curveIndex - 1);
    final double t2 = curveIndex == nNodes - 1 ? Double.POSITIVE_INFINITY : curve.getTimeAtIndex(curveIndex + 1);

    double temp = 0;
    double temp2 = 0;
    int i1 = 0;
    int i2 = nPayments;
    final double[] paymentAmounts = new double[nPayments];
    for (int i = 0; i < nPayments; i++) {
      final double t = swap.getPaymentTime(i);
      paymentAmounts[i] = swap.getPaymentAmounts(i, swapRate);
      if (t <= t1) {
        final double df = curve.getDiscountFactor(t);
        temp += paymentAmounts[i] * df;
        temp2 -= paymentAmounts[i] * curve.getSingleNodeDiscountFactorSensitivity(t, curveIndex);
        i1++;
      } else if (t >= t2) {
        final double df = curve.getDiscountFactor(t);
        temp += paymentAmounts[i] * df;
        temp2 += paymentAmounts[i] * curve.getSingleNodeDiscountFactorSensitivity(t, curveIndex);
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
          sum -= paymentAmounts[i] * tempCurve.getDiscountFactor(t);
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
          sum -= swap.getPaymentAmounts(i, swapRate) * tempCurve.getSingleNodeDiscountFactorSensitivity(t, curveIndex);
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
    private final double[] _yearFraction;

    public BasicFixedLeg(final LocalDate spotDate, final LocalDate mat, final Period swapInterval, final DayCount swapDCC, final DayCount curveDCC, final BusinessDayConvention convention,
        final Calendar calendar) {
      ArgumentChecker.isFalse(swapInterval.getDays() > 0, "swap interval must be in months or years");

      final List<LocalDate> list = new ArrayList<>();
      LocalDate tDate = mat;
      int step = 1;
      while (tDate.isAfter(spotDate)) {
        list.add(tDate);
        tDate = mat.minus(swapInterval.multipliedBy(step++));
      }

      // remove spotDate from list, if it ends up there
      list.remove(spotDate);

      _nPayments = list.size();
      _swapPaymentTimes = new double[_nPayments];
      _yearFraction = new double[_nPayments];

      LocalDate prev = spotDate;
      int j = _nPayments - 1;
      for (int i = 0; i < _nPayments; i++, j--) {
        final LocalDate current = list.get(j);
        final LocalDate adjCurr = convention.adjustDate(calendar, current);
        _yearFraction[i] = swapDCC.getDayCountFraction(prev, adjCurr);
        _swapPaymentTimes[i] = curveDCC.getDayCountFraction(spotDate, adjCurr); // Payment times always good business days
        prev = adjCurr;
      }
      //  _paymentAmounts[_nPayments - 1] += 1.0; // see Javadocs comment
    }

    public int getNumPayments() {
      return _nPayments;
    }

    public double getPaymentAmounts(final int index, final double rate) {
      return index == _nPayments - 1 ? 1 + rate * _yearFraction[index] : rate * _yearFraction[index];
    }

    public double getPaymentTime(final int index) {
      return _swapPaymentTimes[index];
    }

  }

}
