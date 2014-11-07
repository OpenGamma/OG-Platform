/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Ibor-like coupon with compounding and spread. There are three ISDA versions of compounding with spread.
 * The one referred in this class is the "compounding" (not "Flat Compounding" and not "Compounding treating spread as simple interest").
 * The Ibor fixing are compounded over several sub-periods.
 * The amount paid is equal to
 * $$
 * \begin{equation*}
 * \left(\prod_{i=1}^n (1+\delta_i r_i + s) \right)-1
 * \end{equation*}
 * $$
 * where the $\delta_i$ are the accrual factors of the sub periods, the $r_i$ the fixing for the same periods and $s$ the common spread.
 * The fixing have their own start dates, end dates and accrual factors. In general they are close to the accrual
 * dates used to compute the coupon accrual factors.
 * <p> Reference: Mengle, D. (2009). Alternative compounding methods for over-the-counter derivative transactions. ISDA.
 */
public class CouponIborCompoundingSpreadDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * All the coupon sub-periods fix on the same index.
   */
  private final IborIndex _index;
  /**
   * The start dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _accrualStartDates;
  /**
   * The end dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _accrualEndDates;
  /**
   * The accrual factors (or year fraction) associated to the sub-periods.
   */
  private final double[] _paymentAccrualFactors;
  /**
   * The coupon fixing dates.
   */
  private final ZonedDateTime[] _fixingDates;
  /**
   * The start dates of the fixing periods.
   */
  private final ZonedDateTime[] _fixingPeriodStartDates;
  /**
   * The end dates of the fixing periods.
   */
  private final ZonedDateTime[] _fixingPeriodEndDates;
  /**
   * The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;
  /**
   * The spread paid above the Ibor rate.
   */
  private final double _spread;
  
  /**
   * The rate of the first compounded period.
   */
  private final double _initialRate;

  /**
   * Constructor.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @param fixingDates The coupon fixing dates.
   * @param fixingPeriodStartDates The start dates of the fixing periods.
   * @param fixingPeriodEndDates The end dates of the fixing periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   * @param spread The spread paid above the Ibor rate.
   * @param initialRate The rate of the first compounded period.
   */
  protected CouponIborCompoundingSpreadDefinition(
      final Currency currency,
      final ZonedDateTime paymentDate,
      final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor,
      final double notional,
      final IborIndex index,
      final ZonedDateTime[] accrualStartDates,
      final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors,
      final ZonedDateTime[] fixingDates,
      final ZonedDateTime[] fixingPeriodStartDates,
      final ZonedDateTime[] fixingPeriodEndDates,
      final double[] fixingPeriodAccrualFactors,
      final double spread,
      final double initialRate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.isTrue(accrualStartDates.length == accrualEndDates.length, "Accrual start and end dates should have same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodStartDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodEndDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodAccrualFactors.length, "Same length");
    _index = index;
    _accrualStartDates = accrualStartDates;
    _accrualEndDates = accrualEndDates;
    _paymentAccrualFactors = paymentAccrualFactors;
    _fixingDates = fixingDates;
    _fixingPeriodStartDates = fixingPeriodStartDates;
    _fixingPeriodEndDates = fixingPeriodEndDates;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _spread = spread;
    _initialRate = initialRate;
  }

  /**
   * Builds an Ibor compounded coupon from all the details.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @param fixingDates The coupon fixing dates.
   * @param fixingPeriodStartDates The start dates of the fixing periods.
   * @param fixingPeriodEndDates The end dates of the fixing periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   * @param spread The spread paid above the Ibor rate.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSpreadDefinition from(
      final ZonedDateTime paymentDate,
      final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor,
      final double notional,
      final IborIndex index,
      final ZonedDateTime[] accrualStartDates,
      final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors,
      final ZonedDateTime[] fixingDates,
      final ZonedDateTime[] fixingPeriodStartDates,
      final ZonedDateTime[] fixingPeriodEndDates,
      final double[] fixingPeriodAccrualFactors,
      final double spread) {
    return new CouponIborCompoundingSpreadDefinition(
        index.getCurrency(),
        paymentDate,
        accrualStartDate,
        accrualEndDate,
        paymentAccrualFactor,
        notional,
        index,
        accrualStartDates,
        accrualEndDates,
        paymentAccrualFactors,
        fixingDates,
        fixingPeriodStartDates,
        fixingPeriodEndDates,
        fixingPeriodAccrualFactors,
        spread,
        Double.NaN);
  }

  /**
   * Builds an Ibor compounded coupon from all the details.
   * @param currency The currency of the notional.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @param fixingDates The coupon fixing dates.
   * @param fixingPeriodStartDates The start dates of the fixing periods.
   * @param fixingPeriodEndDates The end dates of the fixing periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   * @param spread The spread paid above the Ibor rate.
   * @param initialRate The rate of the first compounded period.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSpreadDefinition from(
      final Currency currency,
      final ZonedDateTime paymentDate,
      final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor,
      final double notional,
      final IborIndex index,
      final ZonedDateTime[] accrualStartDates,
      final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors,
      final ZonedDateTime[] fixingDates,
      final ZonedDateTime[] fixingPeriodStartDates,
      final ZonedDateTime[] fixingPeriodEndDates,
      final double[] fixingPeriodAccrualFactors,
      final double spread,
      final double initialRate) {
    return new CouponIborCompoundingSpreadDefinition(
        currency,
        paymentDate,
        accrualStartDate,
        accrualEndDate,
        paymentAccrualFactor,
        notional,
        index,
        accrualStartDates,
        accrualEndDates,
        paymentAccrualFactors,
        fixingDates,
        fixingPeriodStartDates,
        fixingPeriodEndDates,
        fixingPeriodAccrualFactors,
        spread,
        initialRate);
  }
  
  /**
   * Builds an Ibor compounded coupon from the accrual and payment details. The fixing dates and fixing accrual periods are computed from those dates using the index conventions.
   * @param paymentDate The coupon payment date.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSpreadDefinition from(final ZonedDateTime paymentDate, final double notional, final IborIndex index,
      final ZonedDateTime[] accrualStartDates, final ZonedDateTime[] accrualEndDates, final double[] paymentAccrualFactors, final double spread,
      final Calendar calendar) {
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime accrualStartDate = accrualStartDates[0];
    final ZonedDateTime accrualEndDate = accrualEndDates[nbSubPeriod - 1];
    double paymentAccrualFactor = 0.0;
    final ZonedDateTime[] fixingDates = new ZonedDateTime[nbSubPeriod];
    final ZonedDateTime[] fixingPeriodEndDates = new ZonedDateTime[nbSubPeriod];
    final double[] fixingPeriodAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactor += paymentAccrualFactors[loopsub];
      fixingDates[loopsub] = ScheduleCalculator.getAdjustedDate(accrualStartDates[loopsub], -index.getSpotLag(), calendar);
      fixingPeriodEndDates[loopsub] = ScheduleCalculator.getAdjustedDate(accrualStartDates[loopsub], index, calendar);
      fixingPeriodAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], fixingPeriodEndDates[loopsub], calendar);
    }
    return new CouponIborCompoundingSpreadDefinition(
        index.getCurrency(),
        paymentDate,
        accrualStartDate,
        accrualEndDate,
        paymentAccrualFactor,
        notional,
        index,
        accrualStartDates,
        accrualEndDates,
        paymentAccrualFactors,
        fixingDates,
        accrualStartDates,
        fixingPeriodEndDates,
        fixingPeriodAccrualFactors,
        spread,
        Double.NaN);
  }

  /**
   * Builds an Ibor compounded coupon from a total period and the Ibor index. The Ibor day count is used to compute the accrual factors.
   * If required the stub of the sub-periods will be short and last. The payment date is the start accrual date plus the tenor in the index conventions.
   * The payment accrual factors are in the day count of the index.
   * @param notional The coupon notional.
   * @param accrualStartDate The first accrual date. The other one are computed from that one and the index conventions.
   * @param tenor The total coupon tenor.
   * @param index The underlying Ibor index.
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSpreadDefinition from(final double notional, final ZonedDateTime accrualStartDate, final Period tenor, final IborIndex index,
      final double spread, final Calendar calendar) {
    final ZonedDateTime[] accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(accrualStartDate, tenor, true, false, index, calendar);
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime[] accrualStartDates = new ZonedDateTime[nbSubPeriod];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, nbSubPeriod - 1);
    final double[] paymentAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], accrualEndDates[loopsub], calendar);
    }
    return from(accrualEndDates[nbSubPeriod - 1], notional, index, accrualStartDates, accrualEndDates, paymentAccrualFactors, spread, calendar);
  }

  /**
   * Builds an Ibor compounded coupon from a total period and the Ibor index. The Ibor day count is used to compute the accrual factors.
   * If required the stub of the sub-periods will be short and last. The payment date is the adjusted end accrual date.
   * The payment accrual factors are in the day count of the index. 
   * @param notional The coupon notional.
   * @param accrualStartDate The first accrual date. 
   * @param accrualEndDate The end accrual date.
   * @param index The underlying Ibor index.
   * @param spread The spread paid above the Ibor rate.
   * @param stub The stub type used for the compounding sub-periods. Not null.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSpreadDefinition from(final double notional, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final IborIndex index,
      final double spread, final StubType stub, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar) {
    ArgumentChecker.notNull(accrualStartDate, "Accrual start date");
    ArgumentChecker.notNull(accrualEndDate, "Accrual end date");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(calendar, "Calendar");
    final boolean isStubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START);
    final boolean isStubStart = stub.equals(StubType.LONG_START) || stub.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(accrualStartDate, accrualEndDate, index.getTenor(), isStubShort, isStubStart,
        businessDayConvention, calendar, endOfMonth);
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime[] accrualStartDates = new ZonedDateTime[nbSubPeriod];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, nbSubPeriod - 1);
    final double[] paymentAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], accrualEndDates[loopsub], calendar);
    }
    return from(accrualEndDates[nbSubPeriod - 1], notional, index, accrualStartDates, accrualEndDates, paymentAccrualFactors, spread, calendar);
  }

  public static CouponIborCompoundingSpreadDefinition from(final double notional, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final IborIndex index,
                                                           final double spread, final StubType stub, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar, final
                                                           RollDateAdjuster adjuster) {
    ArgumentChecker.notNull(accrualStartDate, "Accrual start date");
    ArgumentChecker.notNull(accrualEndDate, "Accrual end date");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(calendar, "Calendar");
    final boolean isStubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START);
    final boolean isStubStart = stub.equals(StubType.LONG_START) || stub.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(accrualStartDate, accrualEndDate, index.getTenor(), isStubShort, isStubStart,
                                                                                       businessDayConvention, calendar, endOfMonth, adjuster);
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime[] accrualStartDates = new ZonedDateTime[nbSubPeriod];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, nbSubPeriod - 1);
    final double[] paymentAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], accrualEndDates[loopsub], calendar);
    }
    return from(accrualEndDates[nbSubPeriod - 1], notional, index, accrualStartDates, accrualEndDates, paymentAccrualFactors, spread, calendar);
  }

  public static CouponIborCompoundingSpreadDefinition from(
      double notional,
      ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate,
      IborIndex index, double spread, StubType stubType,
      Calendar accrualCalendar,
      BusinessDayConvention accrualBusinessDayConvention,
      Calendar fixingCalendar,
      BusinessDayConvention fixingBusinessDayConvention,
      Calendar resetCalendar,
      DateRelativeTo resetRelativeTo,
      DateRelativeTo paymentRelativeTo,
      RollDateAdjuster rollDateAdjuster) {
    boolean isEOM = true;
    
    ZonedDateTime paymentDate = DateRelativeTo.START == paymentRelativeTo ? accrualStartDate : accrualEndDate;
    double paymentAccrualFactor = index.getDayCount().getDayCountFraction(accrualStartDate, paymentDate, accrualCalendar);
    
    // TODO this should not be calculating adjusted payment date from unadjusted accrual dates
    paymentDate = accrualBusinessDayConvention.adjustDate(accrualCalendar, paymentDate);
    
    ZonedDateTime[] accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(
          accrualStartDate,
          accrualEndDate,
          index.getTenor(),
          stubType,
          accrualBusinessDayConvention,
          accrualCalendar,
          isEOM,
          rollDateAdjuster);
    ZonedDateTime[] accrualStartDates = new ZonedDateTime[accrualEndDates.length];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, accrualEndDates.length - 1);
    accrualStartDates = ScheduleCalculator.getAdjustedDateSchedule(accrualStartDates, accrualBusinessDayConvention, accrualCalendar);
    
    double[] paymentAccrualFactors = new double[accrualEndDates.length];
    for (int i = 0; i < paymentAccrualFactors.length; i++) {
      paymentAccrualFactors[i] = index.getDayCount().getDayCountFraction(accrualStartDates[i], accrualEndDates[i], accrualCalendar);
    }
    
    ZonedDateTime[] fixingStartDates = accrualStartDates;
    ZonedDateTime[] fixingEndDates = ScheduleCalculator.getAdjustedDateSchedule(
        fixingStartDates,
        index.getTenor(),
        fixingBusinessDayConvention,
        fixingCalendar,
        null);
    
    ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedDate(
        DateRelativeTo.START == resetRelativeTo ? accrualStartDates : accrualEndDates, -index.getSpotLag(), resetCalendar);
    
    double[] fixingAccrualFactors = new double[accrualEndDates.length];
    for (int i = 0; i < fixingAccrualFactors.length; i++) {
      fixingAccrualFactors[i] = index.getDayCount().getDayCountFraction(fixingStartDates[i], fixingEndDates[i], fixingCalendar);
    }

    return new CouponIborCompoundingSpreadDefinition(
        index.getCurrency(),
        paymentDate,
        accrualStartDate,
        accrualEndDate,
        paymentAccrualFactor,
        notional,
        index,
        accrualStartDates,
        accrualEndDates,
        paymentAccrualFactors,
        resetDates,
        fixingStartDates,
        fixingEndDates,
        fixingAccrualFactors,
        spread,
        Double.NaN);
  }  

  /**
   * Returns the Ibor index underlying the coupon.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Returns the accrual start dates of each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getAccrualStartDates() {
    return _accrualStartDates;
  }

  /**
   * Returns the accrual end dates of each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getAccrualEndDates() {
    return _accrualEndDates;
  }

  /**
   * Returns the payment accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getPaymentAccrualFactors() {
    return _paymentAccrualFactors;
  }

  /**
   * Returns the fixing dates for each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingDates() {
    return _fixingDates;
  }

  /**
   * Returns the fixing period start dates for each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingPeriodStartDates() {
    return _fixingPeriodStartDates;
  }

  /**
   * Returns the fixing period end dates for each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingPeriodEndDates() {
    return _fixingPeriodEndDates;
  }

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * Returns the spread.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }
  
  /**
   * Returns the rate of the first compounded period. This is an optional field.
   * @return the rate of the first compounded period.
   */
  public double getInitialRate() {
    return _initialRate;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CouponIborCompoundingSpread toDerivative(final ZonedDateTime dateTime) {
    final LocalDate dateConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dateConversion.isAfter(_fixingDates[0].toLocalDate()), "toDerivative without time series should have a date before the first fixing date.");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double[] fixingTimes = TimeCalculator.getTimeBetween(dateTime, _fixingDates);
    final double[] fixingPeriodStartTimes = TimeCalculator.getTimeBetween(dateTime, _fixingPeriodStartDates);
    final double[] fixingPeriodEndTimes = TimeCalculator.getTimeBetween(dateTime, _fixingPeriodEndDates);
    return new CouponIborCompoundingSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getNotional(), _index, _paymentAccrualFactors,
        fixingTimes, fixingPeriodStartTimes, fixingPeriodEndTimes, _fixingPeriodAccrualFactors, _spread);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    final LocalDate dateConversion = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.isTrue(!dateConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final int nbSubPeriods = _fixingDates.length;
    int nbFixed = 0;
    double ratioAccrued = 1.0;

    // We're assuming a default stub type of short start so only do this at nbFixed = 0
    if (!Double.isNaN(_initialRate)) {
      ratioAccrued *= 1.0 + _paymentAccrualFactors[0] * (_initialRate + _spread);
      nbFixed++;
    }
    
    while ((nbFixed < nbSubPeriods) && (dateConversion.isAfter(_fixingDates[nbFixed].toLocalDate()))) {
      final ZonedDateTime rezonedFixingDate = ZonedDateTime.of(LocalDateTime.of(_fixingDates[nbFixed].toLocalDate(), LocalTime.of(0, 0)), ZoneOffset.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate);
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + rezonedFixingDate);
      }
      ratioAccrued *= 1.0 + _paymentAccrualFactors[nbFixed] * (fixedRate + _spread);
      nbFixed++;
    }
    if ((nbFixed < nbSubPeriods) && dateConversion.equals(_fixingDates[nbFixed].toLocalDate())) {
      final ZonedDateTime rezonedFixingDate = ZonedDateTime.of(LocalDateTime.of(_fixingDates[nbFixed].toLocalDate(), LocalTime.of(0, 0)), ZoneOffset.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate);
      if (fixedRate != null) {
        // Implementation note: on the fixing date and fixing already known.
        ratioAccrued *= 1.0 + _paymentAccrualFactors[nbFixed] * (fixedRate + _spread);
        nbFixed++;
      }
    }
    if (nbFixed == nbSubPeriods) {
      // Implementation note: all dates already fixed: CouponFixed
      final double rate = (ratioAccrued - 1.0) / getPaymentYearFraction();
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
    }
    final double notionalAccrued = getNotional() * ratioAccrued;
    final int nbSubPeriodLeft = nbSubPeriods - nbFixed;
    final double[] paymentAccrualFactorsLeft = new double[nbSubPeriodLeft];
    System.arraycopy(_paymentAccrualFactors, nbFixed, paymentAccrualFactorsLeft, 0, nbSubPeriodLeft);
    final double[] fixingTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingDates), nbFixed, fixingTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodStartTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingPeriodStartDates), nbFixed, fixingPeriodStartTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodEndTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingPeriodEndDates), nbFixed, fixingPeriodEndTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodAccrualFactorsLeft = new double[nbSubPeriodLeft];
    System.arraycopy(_fixingPeriodAccrualFactors, nbFixed, fixingPeriodAccrualFactorsLeft, 0, nbSubPeriodLeft);
    return new CouponIborCompoundingSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), notionalAccrued, _index, paymentAccrualFactorsLeft,
        fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, _spread);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborCompoundingSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborCompoundingSpreadDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_accrualEndDates);
    result = prime * result + Arrays.hashCode(_accrualStartDates);
    result = prime * result + Arrays.hashCode(_fixingDates);
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndDates);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartDates);
    result = prime * result + _index.hashCode();
    result = prime * result + Arrays.hashCode(_paymentAccrualFactors);
    long temp;
    temp = Double.doubleToLongBits(_initialRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponIborCompoundingSpreadDefinition other = (CouponIborCompoundingSpreadDefinition) obj;
    if (!Arrays.equals(_accrualEndDates, other._accrualEndDates)) {
      return false;
    }
    if (!Arrays.equals(_accrualStartDates, other._accrualStartDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingDates, other._fixingDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodEndDates, other._fixingPeriodEndDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartDates, other._fixingPeriodStartDates)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_initialRate) != Double.doubleToLongBits(other._initialRate)) {
      return false;
    }
    return true;
  }

}
