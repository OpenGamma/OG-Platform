/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cash;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a deposit underlying a Ibor index. Used in particular for Ibor fixing and curve construction.
 */
public class DepositIborDefinition extends CashDefinition {

  /**
   * The Ibor-like index associated to the deposit.
   */
  private final IborIndex _index;

  /**
   * Constructor from all details.
   * @param currency The deposit currency.
   * @param startDate The deposit start date.
   * @param endDate The deposit end date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param accrualFactor The deposit accrual factor.
   * @param index The associated index.
   */
  public DepositIborDefinition(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime endDate, final double notional, final double rate,
      final double accrualFactor, final IborIndex index) {
    super(currency, startDate, endDate, notional, rate, accrualFactor);
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Currency should be equal to index currency");
    _index = index;
  }

  /**
   * Build a deposit from the start date and an Ibor index. The index tenor is used as tenor of the deposit.
   * @param startDate The deposit start date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param index The associated index.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The deposit.
   */
  public static DepositIborDefinition fromStart(final ZonedDateTime startDate, final double notional, final double rate, final IborIndex index, final Calendar calendar) {
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, index, calendar);
    final double accrualFactor = index.getDayCount().getDayCountFraction(startDate, endDate, calendar);
    return new DepositIborDefinition(index.getCurrency(), startDate, endDate, notional, rate, accrualFactor, index);
  }

  /**
   * Build a deposit from the trade date and an Ibor index. The index tenor is used as tenor of the deposit. The deposit start date is the trade date plus the index spot lag.
   * @param tradeDate The deposit trade date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param index The associated index.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The deposit.
   */
  public static DepositIborDefinition fromTrade(final ZonedDateTime tradeDate, final double notional, final double rate, final IborIndex index, final Calendar calendar) {
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(tradeDate, index.getSpotLag(), calendar);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, index, calendar);
    final double accrualFactor = index.getDayCount().getDayCountFraction(startDate, endDate, calendar);
    return new DepositIborDefinition(index.getCurrency(), startDate, endDate, notional, rate, accrualFactor, index);
  }

  /**
   * Gets the Ibor-like index associated to the deposit.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public DepositIbor toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!date.isAfter(getEndDate()), "date is after end date");
    final double startTime = TimeCalculator.getTimeBetween(date, getStartDate());
    if (startTime < 0) {
      return new DepositIbor(getCurrency(), 0, TimeCalculator.getTimeBetween(date, getEndDate()), getNotional(), 0, getRate(), getAccrualFactor(), _index);
    }
    return new DepositIbor(getCurrency(), startTime, TimeCalculator.getTimeBetween(date, getEndDate()), getNotional(), getNotional(), getRate(), getAccrualFactor(),
        _index);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    final DepositIborDefinition other = (DepositIborDefinition) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDepositIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitDepositIborDefinition(this);
  }

}
