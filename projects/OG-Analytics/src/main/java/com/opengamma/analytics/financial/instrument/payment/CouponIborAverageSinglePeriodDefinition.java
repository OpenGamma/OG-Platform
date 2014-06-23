/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageSinglePeriod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an average coupon by weighted mean of index values with difference fixing dates
 */
public class CouponIborAverageSinglePeriodDefinition extends CouponDefinition {

  private final ZonedDateTime[] _fixingDate;
  private final IborIndex _index;
  private final double[] _weight;
  private final ZonedDateTime[] _fixingPeriodStartDate;
  private final ZonedDateTime[] _fixingPeriodEndDate;
  private final double[] _fixingPeriodAccrualFactor;

  /**
   * Constructor without start dates and end dates of fixing period
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param index The coupon Ibor index
   * @param fixingDate The coupon fixing dates
   * @param weight The weights for the index
   * @param iborCalendar The holiday calendar for the index
   */
  public CouponIborAverageSinglePeriodDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final Calendar iborCalendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);

    ArgumentChecker.notNull(fixingDate, "fixingDate");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(weight, "weight");
    ArgumentChecker.notNull(iborCalendar, "iborCalendar");

    final int nDates = fixingDate.length;
    ArgumentChecker.isTrue(nDates == weight.length, "weight length different from fixingDate length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");

    for (int i = 0; i < nDates; ++i) {
      ArgumentChecker.notNull(fixingDate[i], "element of fixingDate");
    }

    _fixingDate = Arrays.copyOf(fixingDate, nDates);
    _index = index;
    _weight = Arrays.copyOf(weight, nDates);

    _fixingPeriodStartDate = new ZonedDateTime[nDates];
    _fixingPeriodEndDate = new ZonedDateTime[nDates];
    _fixingPeriodAccrualFactor = new double[nDates];

    for (int i = 0; i < nDates; ++i) {
      _fixingPeriodStartDate[i] = ScheduleCalculator.getAdjustedDate(fixingDate[i], index.getSpotLag(), iborCalendar);
      _fixingPeriodEndDate[i] = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate[i], index.getTenor(), index.getBusinessDayConvention(), iborCalendar, index.isEndOfMonth());
      _fixingPeriodAccrualFactor[i] = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate[i], _fixingPeriodEndDate[i], iborCalendar);
    }
  }

  /**
   * Constructor with start dates and end dates fixing period 
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param fixingDate The coupon fixing dates
   * @param index The coupon Ibor indices
   * @param weight The weights for the indices
   * @param fixingPeriodStartDate The start date of the fixing periods
   * @param fixingPeriodEndDate The end date of the fixing periods
   * @param fixingPeriodAccrualFactor The accrual factor of the fixing periods
   */
  public CouponIborAverageSinglePeriodDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final ZonedDateTime[] fixingPeriodStartDate,
      final ZonedDateTime[] fixingPeriodEndDate, final double[] fixingPeriodAccrualFactor) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);

    ArgumentChecker.notNull(fixingDate, "fixingDate");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(weight, "weight");
    ArgumentChecker.notNull(fixingPeriodStartDate, "fixingPeriodStartDate");
    ArgumentChecker.notNull(fixingPeriodEndDate, "fixingPeriodEndDate");

    final int nRates = fixingDate.length;
    ArgumentChecker.isTrue(nRates == weight.length, "weight length different from fixingDate length");
    ArgumentChecker.isTrue(nRates == fixingPeriodStartDate.length, "fixingPeriodStartDate length different from fixingDate length");
    ArgumentChecker.isTrue(nRates == fixingPeriodEndDate.length, "fixingPeriodEndDate length different from fixingDate length");
    ArgumentChecker.isTrue(nRates == fixingPeriodAccrualFactor.length, "fixingPeriodAccrualFactor length different from fixingDate length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");

    for (int i = 0; i < nRates; ++i) {
      ArgumentChecker.notNull(fixingDate[i], "element of fixingDate");
      ArgumentChecker.notNull(fixingPeriodStartDate[i], "element of fixingPeriodStartDate");
      ArgumentChecker.notNull(fixingPeriodEndDate[i], "element of fixingPeriodEndDate");
    }

    _fixingDate = Arrays.copyOf(fixingDate, nRates);
    _index = index;
    _weight = Arrays.copyOf(weight, nRates);
    _fixingPeriodStartDate = Arrays.copyOf(fixingPeriodStartDate, nRates);
    _fixingPeriodEndDate = Arrays.copyOf(fixingPeriodEndDate, nRates);
    _fixingPeriodAccrualFactor = Arrays.copyOf(fixingPeriodAccrualFactor, nRates);
  }

  /**
   * Construct a coupon without start dates and end dates of fixing period
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param fixingDate The coupon fixing dates
   * @param index The coupon Ibor indices
   * @param weight The weights for the indices
   * @param iborCalendar The holiday calendars for the indices
   * @return The coupon
   */
  public static CouponIborAverageSinglePeriodDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final Calendar iborCalendar) {
    return new CouponIborAverageSinglePeriodDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, fixingDate, weight, iborCalendar);
  }

  /**
   * Construct a coupon with start dates and end dates fixing period 
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param fixingDate The coupon fixing dates
   * @param index The coupon Ibor indices
   * @param weight The weights for the indices
   * @param fixingPeriodStartDate The start date of the fixing periods
   * @param fixingPeriodEndDate The end date of the fixing periods
   * @param fixingPeriodAccrualFactor The accrual factor of the fixing periods
   * @return The coupon
   */
  public static CouponIborAverageSinglePeriodDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final ZonedDateTime[] fixingPeriodStartDate,
      final ZonedDateTime[] fixingPeriodEndDate, final double[] fixingPeriodAccrualFactor) {
    return new CouponIborAverageSinglePeriodDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, fixingDate, weight, fixingPeriodStartDate,
        fixingPeriodEndDate, fixingPeriodAccrualFactor);
  }

  /**
   * Construct a new coupon with the same detail except notional
   * @param notional The notional
   * @return The coupon
   */
  public CouponIborAverageSinglePeriodDefinition withNotional(final double notional) {
    return new CouponIborAverageSinglePeriodDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, getIndex(), getFixingDate(),
        getWeight(), getFixingPeriodStartDate(), getFixingPeriodEndDate(), getFixingPeriodAccrualFactor());
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated
  public CouponIborAverageSinglePeriod toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public CouponIborAverageSinglePeriod toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");

    final int nDates = _weight.length;
    final LocalDate dayConversion = date.toLocalDate();

    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    for (int i = 0; i < nDates; ++i) {
      ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDate()[i].toLocalDate()), "Do not have any fixing data but are asking for a derivative at " + date
          + " which is after fixing date " + getFixingDate()[i]);
    }

    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());

    final double[] fixingTime = new double[nDates];
    final double[] fixingPeriodStartTime = new double[nDates];
    final double[] fixingPeriodEndTime = new double[nDates];

    for (int i = 0; i < nDates; ++i) {
      fixingTime[i] = TimeCalculator.getTimeBetween(date, getFixingDate()[i]);
      fixingPeriodStartTime[i] = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDate()[i]);
      fixingPeriodEndTime[i] = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDate()[i]);
    }

    return new CouponIborAverageSinglePeriod(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getIndex(), fixingTime, getWeight(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageSinglePeriodDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageSinglePeriodDefinition(this);
  }

  /**
   * Gets the fixingDate.
   * @return the fixingDate
   */
  public ZonedDateTime[] getFixingDate() {
    return _fixingDate;
  }

  /**
   * Gets the index.
   * @return the index
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the weight.
   * @return the weight
   */
  public double[] getWeight() {
    return _weight;
  }

  /**
   * Gets the fixingPeriodStartDate.
   * @return the fixingPeriodStartDate
   */
  public ZonedDateTime[] getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixingPeriodEndDate.
   * @return the fixingPeriodEndDate
   */
  public ZonedDateTime[] getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixingPeriodAccrualFactor.
   * @return the fixingPeriodAccrualFactor
   */
  public double[] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingDate);
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndDate);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartDate);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + Arrays.hashCode(_weight);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof CouponIborAverageSinglePeriodDefinition)) {
      return false;
    }
    CouponIborAverageSinglePeriodDefinition other = (CouponIborAverageSinglePeriodDefinition) obj;
    if (!Arrays.equals(_fixingDate, other._fixingDate)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (!Arrays.equals(_weight, other._weight)) {
      return false;
    }
    return true;
  }

}
