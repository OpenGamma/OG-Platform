/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Ibor-like floating coupon with a spread. The coupon payment is: notional * accrual factor * (Ibor + spread).
 */
public class CouponIborSpreadDefinition extends CouponFloatingDefinition {

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
   * The spread paid above the Ibor rate.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;
  /**
   * The holiday calendar for the ibor index.
   */
  private final Calendar _calendar;

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index.
   * The fixing dates and accrual factors are inferred from the index.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   */
  public CouponIborSpreadDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double spread, final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    ArgumentChecker.notNull(calendar, "calendar");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), calendar);
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate, calendar);
    _spread = spread;
    _spreadAmount = spread * getNotional() * getPaymentYearFraction();
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
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   */
  public CouponIborSpreadDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final ZonedDateTime fixingDate, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate,
      final double fixingPeriodAccrualFactor, final IborIndex index, final double spread, final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _fixingPeriodStartDate = fixingPeriodStartDate;
    _fixingPeriodEndDate = fixingPeriodEndDate;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _index = index;
    _spread = spread;
    _spreadAmount = spread * getNotional() * getPaymentYearFraction();
    _calendar = calendar;
  }

  /**
   * Builder of a coupon from the accrual dates and the index. The fixing dates are calculated using the index. The payment date is the end accrual date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   * @return The coupon.
   */
  public static CouponIborSpreadDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional, final IborIndex index,
      final double spread, final Calendar calendar) {
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), calendar);
    return new CouponIborSpreadDefinition(index.getCurrency(), accrualEndDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, spread, calendar);
  }

  /**
   * Builder from an Ibor coupon and the spread.
   * @param couponIbor An Ibor coupon.
   * @param spread The spread.
   * @return The Ibor coupon with spread.
   */
  public static CouponIborSpreadDefinition from(final CouponIborDefinition couponIbor, final double spread) {
    ArgumentChecker.notNull(couponIbor, "Ibor coupon");
    return new CouponIborSpreadDefinition(couponIbor.getCurrency(), couponIbor.getPaymentDate(), couponIbor.getAccrualStartDate(), couponIbor.getAccrualEndDate(), couponIbor.getPaymentYearFraction(),
        couponIbor.getNotional(), couponIbor.getFixingDate(), couponIbor.getIndex(), spread, couponIbor.getCalendar());
  }

  /**
   * Gets the spread.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the fixed amount related to the spread.
   * @return The spread amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
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

  @Override
  public String toString() {
    return super.toString() + ", spread = " + _spread + ", spread amount = " + _spreadAmount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    result = prime * result + _index.hashCode();
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spreadAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _calendar.hashCode();
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
    final CouponIborSpreadDefinition other = (CouponIborSpreadDefinition) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (Double.doubleToLongBits(_spreadAmount) != Double.doubleToLongBits(other._spreadAmount)) {
      return false;
    }
    return true;
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
    ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDate().toLocalDate()),
        "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + dateTime);
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIborSpread(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _spread, forwardCurveName);
  }

  /**
   * {@inheritDoc}
   * If the fixing date is strictly before the conversion date and the fixing rate is not available, an exception is thrown; if the fixing rate is available a fixed coupon is returned.
   * If the fixing date is equal to the conversion date, if the fixing rate is available a fixed coupon is returned, if not a coupon Ibor with spread is returned.
   * If the fixing date is strictly after the conversion date, a coupon Ibor with spread is returned.
   * All the comparisons are between dates without time.
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
        return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate + _spread);
      }
    }
    if (dayConversion.isAfter(dayFixing)) { // The fixing is required
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate().withHour(0)); // TODO: remove time from fixing date.
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + dayFixing);
      }
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate + _spread);
    }
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIborSpread(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _spread, forwardCurveName);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDate().toLocalDate()),
        "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + dateTime);
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIborSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _spread);
  }

  /**
   * {@inheritDoc}
   * If the fixing date is strictly before the conversion date and the fixing rate is not available, an exception is thrown; if the fixing rate is available a fixed coupon is returned.
   * If the fixing date is equal to the conversion date, if the fixing rate is available a fixed coupon is returned, if not a coupon Ibor with spread is returned.
   * If the fixing date is strictly after the conversion date, a coupon Ibor with spread is returned.
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
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate + _spread);
      }
    }
    if (dayConversion.isAfter(dayFixing)) { // The fixing is required
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate().truncatedTo(ChronoUnit.DAYS)); // TODO: remove time from fixing date.
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + dayFixing);
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate + _spread);
    }
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    return new CouponIborSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _spread);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborSpreadDefinition(this);
  }
}
