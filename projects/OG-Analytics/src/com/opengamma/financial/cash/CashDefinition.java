/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.cash;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.bond.Convention;
import com.opengamma.financial.bond.InterestRateDerivativeProvider;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.cash.definition.Cash;

/**
 * 
 */
public class CashDefinition implements InterestRateDerivativeProvider<Cash> {
  private static final Logger s_logger = LoggerFactory.getLogger(CashDefinition.class);
  private final Convention _convention;
  private final ZonedDateTime _maturityDate;
  private final double _rate;

  public CashDefinition(final ZonedDateTime maturityDate, final double rate, final Convention convention) {
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(convention, "convention");
    _convention = convention;
    _maturityDate = maturityDate;
    _rate = rate;
  }

  public Convention getConvention() {
    return _convention;
  }

  public ZonedDateTime getMaturity() {
    return _maturityDate;
  }

  public double getRate() {
    return _rate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    result = prime * result + _maturityDate.hashCode();
    long temp;
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
    if (!ObjectUtils.equals(_maturityDate, other._maturityDate)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public Cash toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    if (yieldCurveNames.length > 1) {
      s_logger.warn("Have more than one yield curve name: cash is only sensitive to one curve so using the first");
    }
    Validate.isTrue(_maturityDate.toLocalDate().isAfter(date) || _maturityDate.equals(date), "Date for security is after maturity");
    final LocalDate startDate = getSettlementDate(date, _convention.getWorkingDayCalendar(), _convention.getBusinessDayConvention(), _convention.getSettlementDays());
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final ZonedDateTime zonedStartDate = ZonedDateTime.of(LocalDateTime.ofMidnight(startDate), TimeZone.UTC);
    final DayCount dayCount = _convention.getDayCount();
    final double tradeTime = dayCount.getDayCountFraction(zonedDate, ZonedDateTime.of(LocalDateTime.ofMidnight(startDate), TimeZone.UTC));
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final double paymentTime = actAct.getDayCountFraction(zonedDate, _maturityDate);
    final double yearFraction = dayCount.getDayCountFraction(zonedStartDate, _maturityDate);
    return new Cash(paymentTime, _rate, tradeTime, yearFraction, yieldCurveNames[0]);
  }

  //TODO this only works for following 
  //TODO this code needs to be extracted out - it will be used in many FI definitions
  private LocalDate getSettlementDate(final LocalDate today, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    LocalDate date = businessDayConvention.adjustDate(calendar, today.plusDays(1));
    for (int i = 0; i < settlementDays; i++) {
      date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
    }
    return date;
  }

}
