/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import java.util.Arrays;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FixedSwapLegDefinition implements FixedIncomeInstrumentDefinition<GenericAnnuity<CouponFixed>> {
  private static final Logger s_logger = LoggerFactory.getLogger(FixedSwapLegDefinition.class);
  private final ZonedDateTime _effectiveDate;
  private final ZonedDateTime[] _nominalDates;
  private final ZonedDateTime[] _settlementDates;
  private final double _notional;
  private final double _rate;
  private final SwapConvention _convention;
  /**
   * The leg currency.
   */
  private final Currency _currency;

  public FixedSwapLegDefinition(Currency currency, final ZonedDateTime effectiveDate, final ZonedDateTime[] nominalDates, final ZonedDateTime[] settlementDates, final double notional,
      final double rate, final SwapConvention convention) {
    Validate.notNull(effectiveDate, "effective date");
    Validate.notNull(nominalDates, "nominal dates");
    Validate.notNull(settlementDates, "settlement dates");
    Validate.notNull(convention, "convention");
    Validate.isTrue(rate > 0, "fixed rate must be greater than 0");
    Validate.isTrue(nominalDates.length == settlementDates.length);
    _effectiveDate = effectiveDate;
    _nominalDates = nominalDates;
    _settlementDates = settlementDates;
    _notional = notional;
    _rate = rate;
    _convention = convention;
    _currency = currency;
  }

  /**
   * Gets the _currency field.
   * @return the _currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public ZonedDateTime[] getNominalDates() {
    return _nominalDates;
  }

  public ZonedDateTime[] getSettlementDates() {
    return _settlementDates;
  }

  public double getNotional() {
    return _notional;
  }

  public double getRate() {
    return _rate;
  }

  public SwapConvention getConvention() {
    return _convention;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _effectiveDate.hashCode();
    result = prime * result + _convention.hashCode();
    result = prime * result + Arrays.hashCode(_nominalDates);
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_settlementDates);
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
    final FixedSwapLegDefinition other = (FixedSwapLegDefinition) obj;
    if (!ObjectUtils.equals(_effectiveDate, other._effectiveDate)) {
      return false;
    }
    if (!Arrays.equals(_nominalDates, other._nominalDates)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    if (!Arrays.equals(_settlementDates, other._settlementDates)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public GenericAnnuity<CouponFixed> toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(_settlementDates[_settlementDates.length - 1].toLocalDate()), date + " is after final settlement date (" + _settlementDates[_settlementDates.length - 1] + ")");
    //TODO
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    s_logger.info("Using the first yield curve name as the funding curve name");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    double[] paymentTimes = ScheduleCalculator.getTimes(_settlementDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"), zonedDate);
    double[] yearFractions = ScheduleCalculator.getYearFractions(_settlementDates, _convention.getDayCount(), _effectiveDate);
    final int n = ScheduleCalculator.numberOfNegativeValues(paymentTimes);
    if (n > 0) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, n);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, n);
    }
    //TODO: the payer/receiver flag should be stored at the leg level!
    return new AnnuityCouponFixed(_currency, paymentTimes, Math.abs(_notional), _rate, yearFractions, yieldCurveNames[0], _notional < 0);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitFixedSwapLegDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitFixedSwapLegDefinition(this);
  }

}
