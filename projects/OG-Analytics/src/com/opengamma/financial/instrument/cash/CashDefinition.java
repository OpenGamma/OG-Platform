/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.cash;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CashDefinition implements InstrumentDefinition<Cash> {
  private static final Logger s_logger = LoggerFactory.getLogger(CashDefinition.class);
  private final Currency _currency;
  private final Convention _convention;
  private final ZonedDateTime _maturityDate;
  private final double _notional;
  private final double _rate;

  public CashDefinition(final Currency currency, final ZonedDateTime maturityDate, final double notional, final double rate, final Convention convention) {
    Validate.notNull(currency, "currency");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(convention, "convention");
    _currency = currency;
    _convention = convention;
    _maturityDate = maturityDate;
    _notional = notional;
    _rate = rate;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public Convention getConvention() {
    return _convention;
  }

  public ZonedDateTime getMaturity() {
    return _maturityDate;
  }

  public double getNotional() {
    return _notional;
  }

  public double getRate() {
    return _rate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _convention.hashCode();
    result = prime * result + _maturityDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CashDefinition other = (CashDefinition) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (!ObjectUtils.equals(_maturityDate, other._maturityDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public Cash toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    Validate.isTrue(!date.isAfter(_maturityDate), "Date is after maturity");
    if (yieldCurveNames.length > 1) {
      s_logger.info("Have more than one yield curve name: cash is only sensitive to one curve so using the first");
    }
    final LocalDate settlementDate = getSettlementDate(date.toLocalDate(), _convention.getWorkingDayCalendar(), _convention.getBusinessDayConvention(), _convention.getSettlementDays());
    final ZonedDateTime zonedStartDate = ZonedDateTime.of(LocalDateTime.ofMidnight(settlementDate), TimeZone.UTC);
    final DayCount dayCount = _convention.getDayCount();
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final double tradeTime = actAct.getDayCountFraction(date, zonedStartDate);
    final double paymentTime = actAct.getDayCountFraction(date, _maturityDate);
    final double yearFraction = dayCount.getDayCountFraction(zonedStartDate, _maturityDate);
    return new Cash(_currency, tradeTime, paymentTime, _notional, _rate, yearFraction, yieldCurveNames[0]);
  }

  // TODO this only works for following
  // TODO this code needs to be extracted out - it will be used in many FI definitions
  private LocalDate getSettlementDate(final LocalDate today, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    LocalDate date = businessDayConvention.adjustDate(calendar, today.plusDays(0)); //TODO is this right? was causing problems for O/N
    for (int i = 0; i < settlementDays; i++) {
      date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
    }
    return date;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitCashDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCashDefinition(this);
  }

}
