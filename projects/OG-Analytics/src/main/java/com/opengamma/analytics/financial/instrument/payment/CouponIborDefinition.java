/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Ibor-like floating coupon.
 */
public class CouponIborDefinition extends CouponFloatingDefinition {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * The end date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * The holiday calendar for the ibor index.
   */
  private final Calendar _calendar;

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * The fixing dates and accrual factors are inferred from the index.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param calendar The holiday calendar for the ibor index.
   */
  public CouponIborDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentAccrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), calendar);
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate, calendar);
    _calendar = calendar;
  }

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * @param currency The coupn currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param fixingPeriodAccrualFactor The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param calendar The holiday calendar for the ibor index.
   */
  public CouponIborDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentAccrualFactor,
      final double notional, final ZonedDateTime fixingDate, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final double fixingPeriodAccrualFactor,
      final IborIndex index, final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _fixingPeriodStartDate = fixingPeriodStartDate;
    _fixingPeriodEndDate = fixingPeriodEndDate;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _index = index;
    _calendar = calendar;
  }

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   *
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @param calendar The holiday calendar for the ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional,
      final ZonedDateTime fixingDate, final IborIndex index, final Calendar calendar) {
    ArgumentChecker.notNull(index, "index");
    return new CouponIborDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, calendar);
  }

  /**
   * Builder of a coupon from the accrual dates and the index. The fixing dates are calculated using the index. The payment date is the end accrual date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param calendar The holiday calendar for the ibor index.
   * @return The coupon.
   */
  public static CouponIborDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional, final IborIndex index,
      final Calendar calendar) {
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), calendar);
    return new CouponIborDefinition(index.getCurrency(), accrualEndDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, calendar);
  }

  /**
   * Builder of a coupon from the accrual dates and the index. The fixing dates and accrual factor are calculated using the index. The payment date is the end accrual date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param notional The coupon notional.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param calendar The holiday calendar for the ibor index.
   * @return The coupon.
   */
  public static CouponIborDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional, final IborIndex index,
      final Calendar calendar) {
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), calendar);
    final double accrualFactor = index.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, calendar);
    return new CouponIborDefinition(index.getCurrency(), accrualEndDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, calendar);
  }

  /**
   * Builder of Ibor-like coupon from the fixing date and the index. The payment and accrual dates are the one of the fixing period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @param calendar The holiday calendar for the ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final double notional, final ZonedDateTime fixingDate, final IborIndex index, final Calendar calendar) {
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.notNull(index, "index");
    final ZonedDateTime fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, index.getSpotLag(), calendar);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
    final double fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate, calendar);
    return new CouponIborDefinition(index.getCurrency(), fixingPeriodEndDate, fixingPeriodStartDate, fixingPeriodEndDate, fixingPeriodAccrualFactor, notional, fixingDate, index,
        calendar);
  }

  /**
   * Builder of Ibor-like coupon from an underlying coupon, the fixing date and the index. The fixing period dates are deduced from the index and the fixing date.
   * @param coupon Underlying coupon.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @param calendar The holiday calendar for the ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final CouponDefinition coupon, final ZonedDateTime fixingDate, final IborIndex index, final Calendar calendar) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.notNull(index, "index");
    return new CouponIborDefinition(index.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        fixingDate, index, calendar);
  }

  /**
   * Builder from an Ibor coupon with spread. The spread is ignored.
   * @param coupon The coupon with spread.
   * @return The ibor coupon.
   */
  public static CouponIborDefinition from(final CouponIborSpreadDefinition coupon) {
    ArgumentChecker.notNull(coupon, "coupon");
    return new CouponIborDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), coupon.getIndex(), coupon.getCalendar());
  }

  /**
   * Gets the Ibor index of the instrument.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the start date of the fixing period.
   * @return The start date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the end date of the fixing period.
   * @return The end date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @return The accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the holiday calendar for the ibor index.
   * @return The holiday calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Creates a new coupon with all the details the same except the notional which is replaced by the notional provided.
   * @param notional The notional.
   * @return The coupon.
   */
  public CouponIborDefinition withNotional(final double notional) {
    return new CouponIborDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, getFixingDate(), _fixingPeriodStartDate,
        _fixingPeriodEndDate, _fixingPeriodAccrualFactor, _index, _calendar);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDate().toLocalDate()), "Do not have any fixing data but are asking for a derivative at " + dateTime + " which is after fixing date "
        + getFixingDate());
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIbor(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), forwardCurveName);
  }

  /**
   * If the fixing date is strictly before the conversion date and the fixing rate is not available, an exception is thrown; if the fixing rate is available a fixed coupon is returned.
   * If the fixing date is equal to the conversion date, if the fixing rate is available a fixed coupon is returned, if not a coupon Ibor with spread is returned.
   * If the fixing date is strictly after the conversion date, a coupon Ibor is returned.
   * All the comparisons are between dates without time.
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final LocalDate dayFixing = getFixingDate().toLocalDate();
    if (dayConversion.equals(dayFixing)) { // The fixing is on the reference date; if known the fixing is used and if not, the floating coupon is created.
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      if (fixedRate != null) {
        return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate);
      }
    }
    if (dayConversion.isAfter(dayFixing)) { // The fixing is required
      final ZonedDateTime rezonedFixingDate = getFixingDate().toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate); // TODO: remove time from fixing date.
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
      }
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate);
    }
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIbor(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), forwardCurveName);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDate().toLocalDate()), "Do not have any fixing data but are asking for a derivative at " + dateTime + " which is after fixing date "
        + getFixingDate());
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIbor(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor());
  }

  /**
   * {@inheritDoc}
   * If the fixing date is strictly before the conversion date and the fixing rate is not available, an exception is thrown; if the fixing rate is available a fixed coupon is returned.
   * If the fixing date is equal to the conversion date, if the fixing rate is available a fixed coupon is returned, if not a coupon Ibor with spread is returned.
   * If the fixing date is strictly after the conversion date, a coupon Ibor is returned.
   * All the comparisons are between dates without time.
   */
  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final LocalDate dayFixing = getFixingDate().toLocalDate();
    if (dayConversion.equals(dayFixing)) { // The fixing is on the reference date; if known the fixing is used and if not, the floating coupon is created.
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      if (fixedRate != null) {
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate);
      }
    }
    if (dayConversion.isAfter(dayFixing)) { // The fixing is required
      final ZonedDateTime rezonedFixingDate = getFixingDate().toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate); // TODO: remove time from fixing date.
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate);
    }
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIbor(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborDefinition(this);
  }

  @Override
  public String toString() {
    return super.toString() + " *Ibor coupon* Index = " + _index + ", Fixing period = [" + _fixingPeriodStartDate + " - " + _fixingPeriodEndDate + " - " + _fixingPeriodAccrualFactor + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _index.hashCode();
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
    final CouponIborDefinition other = (CouponIborDefinition) obj;
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }
}
