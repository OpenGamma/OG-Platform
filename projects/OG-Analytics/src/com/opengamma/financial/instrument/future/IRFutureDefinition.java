/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.instrument.FixedIncomeFutureInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeFutureInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;

/**
 * 
 */
public class IRFutureDefinition implements FixedIncomeFutureInstrumentDefinition<InterestRateFuture> {
  private static final Logger s_logger = LoggerFactory.getLogger(IRFutureDefinition.class);
  private final ZonedDateTime _lastTradeDate;
  private final ZonedDateTime _maturityDate;
  private final IRFutureConvention _convention;

  public IRFutureDefinition(final ZonedDateTime lastTradeDate, final ZonedDateTime maturityDate, final IRFutureConvention convention) {
    Validate.notNull(lastTradeDate, "last trade date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(convention, "convention");
    Validate.isTrue(maturityDate.isAfter(lastTradeDate), "maturity must be after last trade date");
    _lastTradeDate = lastTradeDate;
    _maturityDate = maturityDate;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    result = prime * result + _maturityDate.hashCode();
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
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public InterestRateFuture toDerivative(final ZonedDateTime date, final double price, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    Validate.isTrue(!date.isAfter(_maturityDate), "Date is after maturity");
    s_logger.info("Assuming first yield curve name is the index curve");
    final Calendar calendar = _convention.getWorkingDayCalendar();
    final String indexCurveName = yieldCurveNames[0];
    final BusinessDayConvention businessDayConvention = _convention.getBusinessDayConvention();
    //final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final ZonedDateTime lastTradeDate = businessDayConvention.adjustDate(calendar, _lastTradeDate);
    final ZonedDateTime startDate = lastTradeDate.plusDays(_convention.getSettlementDays());
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, _maturityDate);
    final DayCount dayCount = _convention.getDayCount();
    final double yearFraction = dayCount.getDayCountFraction(lastTradeDate, maturityDate);
    final double valueYearFraction = _convention.getYearFraction();
    final double settlementDateFraction = dayCount.getDayCountFraction(date, startDate);
    final double lastTradeDateFraction = dayCount.getDayCountFraction(date, _lastTradeDate);
    final double maturityDateFraction = dayCount.getDayCountFraction(date, _maturityDate);
    return new InterestRateFuture(settlementDateFraction, lastTradeDateFraction, maturityDateFraction, yearFraction, valueYearFraction, price, indexCurveName);
  }

  @Override
  public <U, V> V accept(final FixedIncomeFutureInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitIRFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeFutureInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitIRFutureDefinition(this);
  }

}
