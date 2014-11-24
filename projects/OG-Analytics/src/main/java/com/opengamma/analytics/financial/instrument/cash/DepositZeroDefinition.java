/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cash;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a deposit where the rate is expressed in a specific composition convention.
 * Used mainly for curve construction with rates directly provided (not calibrated).
 */
public class DepositZeroDefinition implements InstrumentDefinition<DepositZero> {

  /**
   * The deposit currency.
   */
  private final Currency _currency;
  /**
   * The deposit start date (a good business day, adjusted by the business day convention if required).
   */
  private final ZonedDateTime _startDate;
  /**
   * The deposit end (or maturity) date (a good business day, adjusted by the business day convention if required).
   */
  private final ZonedDateTime _endDate;
  /**
   * The deposit notional.
   */
  private final double _notional;
  /**
   * The accrual factor (or year fraction).
   */
  private final double _paymentAccrualFactor;
  /**
   * The interest rate and its composition type.
   */
  private final InterestRate _rate;
  /**
   * The interest amount to be paid at end date.
   */
  private final double _interestAmount;
  /**
   * The holiday calendar.
   */
  private final Calendar _calendar;
  /**
   * The day count.
   */
  private final DayCount _dayCount;

  /**
   * Constructor from all details.
   * @param currency The currency.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param notional The notional.
   * @param paymentAccrualFactor The accrual factor (or year fraction).
   * @param rate The interest rate and its composition type.
   * @param calendar The holiday calendar.
   * @param dayCount The day count.
   */
  public DepositZeroDefinition(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime endDate, final double notional, final double paymentAccrualFactor,
      final InterestRate rate, final Calendar calendar, final DayCount dayCount) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(endDate, "End date");
    ArgumentChecker.notNull(rate, "Rate");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    _currency = currency;
    _startDate = startDate;
    _endDate = endDate;
    _notional = notional;
    _paymentAccrualFactor = paymentAccrualFactor;
    _rate = rate;
    _interestAmount = (1.0 / rate.getDiscountFactor(paymentAccrualFactor) - 1.0) * notional;
    _calendar = calendar;
    _dayCount = dayCount;
  }

  /**
   * Builder. The day count is used to compute the accrual factor. The notional is 1.
   * @param currency The currency.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param daycount The day count.
   * @param rate The interest rate and its composition type.
   * @param calendar The holiday calendar.
   * @param dayCount The day count
   * @return The deposit.
   */
  public static DepositZeroDefinition from(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime endDate, final DayCount daycount, final InterestRate rate,
      final Calendar calendar, final DayCount dayCount) {
    ArgumentChecker.notNull(daycount, "day count");
    return new DepositZeroDefinition(currency, startDate, endDate, 1.0, daycount.getDayCountFraction(startDate, endDate, calendar), rate, calendar, dayCount);
  }

  /**
   * Builder. The day count is used to compute the accrual factor. The notional is 1.
   * @param currency The currency.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param daycount The day count.
   * @param rate The interest rate and its composition type.
   * @param calendar The holiday calendar.
   * @return The deposit.
   */
  public static DepositZeroDefinition withAdjustedRate(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime endDate, final DayCount daycount, final InterestRate rate,
      final Calendar calendar) {
    ArgumentChecker.notNull(daycount, "day count");
    double adjustedRate;
    if (currency.equals(Currency.BRL)) {
      adjustedRate = rate.getRate() * daycount.getDayCountFraction(startDate, endDate, calendar) / TimeCalculator.getTimeBetween(startDate, endDate);
    } else {
      adjustedRate = rate.getRate();
    }
    final InterestRate adjustedInterestRate = new PeriodicInterestRate(adjustedRate, 1);
    return new DepositZeroDefinition(currency, startDate, endDate, 1.0, daycount.getDayCountFraction(startDate, endDate, calendar), adjustedInterestRate, calendar, daycount);
  }

  /**
   * Gets the deposit currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the deposit start date
   * @return The date.
   */
  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  /**
   * Gets the deposit end date
   * @return The date.
   */
  public ZonedDateTime getEndDate() {
    return _endDate;
  }

  /**
   * Gets the deposit notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the accrual factor (or year fraction).
   * @return The factor.
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the rate (figure and composition rule).
   * @return The rate.
   */
  public InterestRate getRate() {
    return _rate;
  }

  /**
   * Gets the interest rate amount.
   * @return The amount.
   */
  public double getInterestAmount() {
    return _interestAmount;
  }

  @Override
  public String toString() {
    return "DepositZero " + _currency + " [" + _startDate + " - " + _endDate + "] - notional: " + _notional + " - rate: " + _rate;
  }

  @Override
  public DepositZero toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!date.isAfter(_endDate), "date is after end date");
    double startTime;
    if (date.toLocalDate().isBefore(_startDate.toLocalDate())) {
      startTime = _dayCount.getDayCountFraction(date.toLocalDate(), _startDate.toLocalDate(), _calendar);
    } else if (date.toLocalDate().equals(_startDate.toLocalDate())) {
      startTime = 0;
    } else {
      startTime = _dayCount.getDayCountFraction(_startDate.toLocalDate(), date.toLocalDate(), _calendar);
    }
    final double endTime = _dayCount.getDayCountFraction(date.toLocalDate(), _endDate.toLocalDate(), _calendar);
    if (startTime < 0) {
      return new DepositZero(_currency, 0, endTime, 0, _notional, _paymentAccrualFactor, _rate, _interestAmount);
    }
    return new DepositZero(_currency, startTime, endTime, _notional, _notional, _paymentAccrualFactor, _rate, _interestAmount);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDepositZeroDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDepositZeroDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _endDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_interestAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _rate.hashCode();
    result = prime * result + _startDate.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DepositZeroDefinition other = (DepositZeroDefinition) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_endDate, other._endDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_startDate, other._startDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_interestAmount) != Double.doubleToLongBits(other._interestAmount)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_rate, other._rate)) {
      return false;
    }
    return true;
  }

}
