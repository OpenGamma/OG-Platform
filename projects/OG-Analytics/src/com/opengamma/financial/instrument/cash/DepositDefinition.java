/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.cash;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;

/**
 * Class describing a standard deposit.
 */
public class DepositDefinition {

  /**
   * The deposit start date.
   */
  private final ZonedDateTime _startDate;
  /**
   * The deposit end date.
   */
  private final ZonedDateTime _endDate;
  /**
   * The deposit notional.
   */
  private final double _notional;
  /**
   * The accrual factor (or year fraction).
   */
  private final double _accrualFactor;
  /**
   * The deposit rate.
   */
  private final double _rate;
  /**
   * The interest amount to be paid at end date (=_notional * _rate * _paymentAccrualFactor)
   */
  private final double _interestAmount;
  /**
   * The deposit currency.
   */
  private final Currency _currency;

  /**
   * Constructor from all details.
   * @param startDate The deposit start date.
   * @param endDate The deposit end date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param currency The deposit currency.
   * @param accrualFactor The deposit accrual factor.
   */
  public DepositDefinition(final ZonedDateTime startDate, final ZonedDateTime endDate, final double notional, final double rate, final Currency currency, final double accrualFactor) {
    Validate.notNull(startDate, "Start date");
    Validate.notNull(endDate, "End date");
    Validate.notNull(currency, "Currency");
    _startDate = startDate;
    _endDate = endDate;
    _notional = notional;
    _rate = rate;
    _currency = currency;
    _accrualFactor = accrualFactor;
    _interestAmount = _notional * _rate * _accrualFactor;
  }

  /**
   * Build a deposit from the financial description and the start (or settlement) date.
   * @param startDate The deposit start date.
   * @param tenor The deposit tenor.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @return The deposit.
   */
  public static DepositDefinition fromStart(final ZonedDateTime startDate, final Period tenor, final double notional, final double rate, final GeneratorDeposit generator) {
    Validate.notNull(startDate, "Start date");
    Validate.notNull(tenor, "Tenor");
    Validate.notNull(generator, "Generator");
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, generator);
    double accrualFactor = generator.getDayCount().getDayCountFraction(startDate, endDate);
    return new DepositDefinition(startDate, endDate, notional, rate, generator.getCurrency(), accrualFactor);
  }

  /**
   * Build a overnight deposit from the financial description and the start (or settlement) date.
   * @param startDate The deposit start date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @return The deposit.
   */
  public static DepositDefinition fromStart(final ZonedDateTime startDate, final double notional, final double rate, final GeneratorDeposit generator) {
    Validate.notNull(startDate, "Start date");
    Validate.notNull(generator, "Generator");
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, generator.getCalendar());
    double accrualFactor = generator.getDayCount().getDayCountFraction(startDate, endDate);
    return new DepositDefinition(startDate, endDate, notional, rate, generator.getCurrency(), accrualFactor);
  }

  /**
   * Build a deposit from the financial description and the trade date.
   * @param tradeDate The deposit trade date. The start date is the trade date plus the generator spot lag.
   * @param tenor The deposit tenor.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @return The deposit.
   */
  public static DepositDefinition fromTrade(final ZonedDateTime tradeDate, final Period tenor, final double notional, final double rate, final GeneratorDeposit generator) {
    Validate.notNull(tradeDate, "Trade date");
    Validate.notNull(tenor, "Tenor");
    Validate.notNull(generator, "Generator");
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(tradeDate, generator.getSpotLag(), generator.getCalendar());
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, generator);
    double accrualFactor = generator.getDayCount().getDayCountFraction(startDate, endDate);
    return new DepositDefinition(startDate, endDate, notional, rate, generator.getCurrency(), accrualFactor);
  }

  /**
   * Build an over-night deposit from the financial description and the trade date.
   * @param tradeDate The deposit trade date. The start date is the trade date plus the generator spot lag.
   * @param start The number of business days to start date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @return The deposit.
   */
  public static DepositDefinition fromTrade(final ZonedDateTime tradeDate, final int start, final double notional, final double rate, final GeneratorDeposit generator) {
    Validate.notNull(tradeDate, "Trade date");
    Validate.notNull(generator, "Generator");
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(tradeDate, start, generator.getCalendar());
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, generator.getCalendar());
    double accrualFactor = generator.getDayCount().getDayCountFraction(startDate, endDate);
    return new DepositDefinition(startDate, endDate, notional, rate, generator.getCurrency(), accrualFactor);
  }

  /**
   * Gets the deposit start date.
   * @return The date.
   */
  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  /**
   * Gets the deposit end date.
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
   * Gets the deposit accrual factor.
   * @return The accrual factor.
   */
  public double getAccrualFactor() {
    return _accrualFactor;
  }

  /**
   * Gets the deposit rate.
   * @return The rate.
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Gets the interest amount.
   * @return The amount.
   */
  public double getInterestAmount() {
    return _interestAmount;
  }

  /**
   * Gets the deposit currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public String toString() {
    return "Deposit " + _currency + " [" + _startDate + " - " + _endDate + "] - notional: " + _notional + " - rate: " + _rate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _currency.hashCode();
    result = prime * result + _endDate.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _startDate.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DepositDefinition other = (DepositDefinition) obj;
    if (Double.doubleToLongBits(_accrualFactor) != Double.doubleToLongBits(other._accrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_endDate, other._endDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_startDate, other._startDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
