/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.irfuture;

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
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.instrument.InterestRateDerivativeProvider;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;

/**
 * 
 */
public class IRFutureDefinition implements InterestRateDerivativeProvider<InterestRateFuture> {
  private static final Logger s_logger = LoggerFactory.getLogger(IRFutureDefinition.class);
  private final ZonedDateTime _lastTradeDate;
  private final ZonedDateTime _maturityDate;
  private final IRFutureConvention _convention;
  private final double _rate;

  public IRFutureDefinition(ZonedDateTime lastTradeDate, ZonedDateTime maturityDate, double rate, IRFutureConvention convention) {
    Validate.notNull(lastTradeDate, "last trade date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(convention, "convention");
    Validate.isTrue(maturityDate.isAfter(lastTradeDate), "maturity must be after last trade date");
    Validate.isTrue(rate <= 100, "rate must be less than or equal to 100");
    _lastTradeDate = lastTradeDate;
    _maturityDate = maturityDate;
    _rate = rate;
    _convention = convention;
  }

  public ZonedDateTime getLastTradeDate() {
    return _lastTradeDate;
  }

  public ZonedDateTime getMaturity() {
    return _maturityDate;
  }

  public Convention getConvention() {
    return _convention;
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
    result = prime * result + _lastTradeDate.hashCode();
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
    final IRFutureDefinition other = (IRFutureDefinition) obj;
    if (!ObjectUtils.equals(_lastTradeDate, other._lastTradeDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_maturityDate, other._maturityDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public InterestRateFuture toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    Validate.isTrue(_maturityDate.toLocalDate().isAfter(date) || _maturityDate.equals(date), "Date for security is after maturity");
    s_logger.info("Assuming first yield curve name is the index curve");
    Calendar calendar = _convention.getWorkingDayCalendar();
    String indexCurveName = yieldCurveNames[0];
    BusinessDayConvention businessDayConvention = _convention.getBusinessDayConvention();
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final ZonedDateTime lastTradeDate = businessDayConvention.adjustDate(calendar, _lastTradeDate);
    final ZonedDateTime startDate = lastTradeDate.plusDays(_convention.getSettlementDays());
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, _maturityDate);
    DayCount dayCount = _convention.getDayCount();
    final double yearFraction = dayCount.getDayCountFraction(lastTradeDate, maturityDate);
    final double valueYearFraction = _convention.getYearFraction();
    final double settlementDateFraction = dayCount.getDayCountFraction(zonedDate, startDate);
    final double lastTradeDateFraction = dayCount.getDayCountFraction(zonedDate, _lastTradeDate);
    final double maturityDateFraction = dayCount.getDayCountFraction(zonedDate, _maturityDate);
    return new InterestRateFuture(settlementDateFraction, lastTradeDateFraction, maturityDateFraction, yearFraction, valueYearFraction, _rate, indexCurveName);
  }

}
