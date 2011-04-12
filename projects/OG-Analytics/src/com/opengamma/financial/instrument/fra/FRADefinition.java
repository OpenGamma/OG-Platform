/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.fra;

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
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;

/**
 * 
 */
public class FRADefinition implements FixedIncomeInstrumentDefinition<ForwardRateAgreement> {
  private static final Logger s_logger = LoggerFactory.getLogger(FRADefinition.class);
  private final ZonedDateTime _startDate;
  private final ZonedDateTime _maturityDate;
  private final Convention _convention;
  private final double _rate;

  public FRADefinition(final ZonedDateTime startDate, final ZonedDateTime maturityDate, final double rate, final Convention convention) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(convention, "convention");
    Validate.isTrue(maturityDate.isAfter(startDate), "maturity must be after start date");
    _startDate = startDate;
    _maturityDate = maturityDate;
    _rate = rate;
    _convention = convention;
  }

  public ZonedDateTime getStartDate() {
    return _startDate;
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
    final FRADefinition other = (FRADefinition) obj;
    if (!ObjectUtils.equals(_startDate, other._startDate)) {
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
  public ForwardRateAgreement toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1);
    Validate.isTrue(!date.isAfter(_maturityDate.toLocalDate()), "Date is after maturity");
    s_logger.info("Assuming first yield curve name is the funding curve and the second is the index curve");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final Calendar calendar = _convention.getWorkingDayCalendar();
    final String fundingCurveName = yieldCurveNames[0];
    final String indexCurveName = yieldCurveNames[1];
    final BusinessDayConvention businessDayConvention = _convention.getBusinessDayConvention();
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final ZonedDateTime settlementDate = ZonedDateTime.of(LocalDateTime.ofMidnight(getSettlementDate(_startDate.toLocalDate(), calendar, businessDayConvention, _convention.getSettlementDays())),
        TimeZone.UTC);
    final ZonedDateTime fixingDate = businessDayConvention.adjustDate(calendar, _startDate);
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, _maturityDate);
    final double settlementTime = actAct.getDayCountFraction(zonedDate, settlementDate);
    final double maturityTime = actAct.getDayCountFraction(zonedDate, maturityDate);
    final double fixingTime = actAct.getDayCountFraction(zonedDate, fixingDate);
    final DayCount dayCount = _convention.getDayCount();
    final double forwardYearFraction = dayCount.getDayCountFraction(fixingDate, _maturityDate);
    final double discountingYearFraction = dayCount.getDayCountFraction(settlementDate, _maturityDate);
    return new ForwardRateAgreement(settlementTime, maturityTime, fixingTime, forwardYearFraction, discountingYearFraction, _rate, fundingCurveName, indexCurveName);
  }

  // TODO this only works for following
  // TODO this code needs to be extracted out - it will be used in many FI definitions
  private LocalDate getSettlementDate(final LocalDate today, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    LocalDate date = businessDayConvention.adjustDate(calendar, today.plusDays(0));
    for (int i = 0; i < settlementDays; i++) {
      date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
    }
    return date;
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitFRADefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitFRADefinition(this);
  }
}
