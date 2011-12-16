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

import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

/**
 * Class describing a standard deposit. The notional amount is paid on the start date and received back on the end (maturity) date with interests.
 * The interest amount is computed based on a fixed rate and a accrual factor.
 */
public class DepositDefinition implements InstrumentDefinition<Cash> {

  /**
   * The deposit currency.
   */
  private final Currency _currency;
  /**
   * The deposit start date.
   */
  private final ZonedDateTime _startDate;
  /**
   * The deposit end (or maturity) date.
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
   * The interest amount to be paid at end date (=_notional * _rate * _ccrualFactor)
   */
  private final double _interestAmount;

  /**
   * Constructor from all details.
   * @param currency The deposit currency.
   * @param startDate The deposit start date.
   * @param endDate The deposit end date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param accrualFactor The deposit accrual factor.
   */
  public DepositDefinition(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime endDate, final double notional, final double rate, final double accrualFactor) {
    Validate.notNull(startDate, "Start date");
    Validate.notNull(endDate, "End date");
    Validate.notNull(currency, "Currency");
    Validate.isTrue(endDate.isAfter(startDate), "End date should be strictly after start date");
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
    return new DepositDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor);
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
    return new DepositDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor);
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
    return new DepositDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor);
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
    return new DepositDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor);
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
  public Cash toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.isTrue(!date.isAfter(_endDate), "date is after end date");
    double startTime = TimeCalculator.getTimeBetween(date, _startDate);
    if (startTime < 0) {
      return new Cash(_currency, 0, TimeCalculator.getTimeBetween(date, _endDate), _notional, 0, _rate, _accrualFactor, yieldCurveNames[0]);
    }
    return new Cash(_currency, startTime, TimeCalculator.getTimeBetween(date, _endDate), _notional, _rate, _accrualFactor, yieldCurveNames[0]);
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

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitDepositDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitDepositDefinition(this);
  }

}
