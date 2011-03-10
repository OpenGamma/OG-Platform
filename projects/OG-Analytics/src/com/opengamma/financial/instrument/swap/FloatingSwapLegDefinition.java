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

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * 
 */
public class FloatingSwapLegDefinition implements FixedIncomeInstrumentDefinition<GenericAnnuity<Payment>> {
  private static final Logger s_logger = LoggerFactory.getLogger(FloatingSwapLegDefinition.class);
  private final ZonedDateTime _effectiveDate;
  private final ZonedDateTime[] _nominalDates;
  private final ZonedDateTime[] _settlementDates;
  private final ZonedDateTime[] _resetDates;
  private final ZonedDateTime[] _maturityDates;
  private final double _notional;
  private final double _spread;
  private final double _initialRate;
  private final SwapConvention _convention;

  public FloatingSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime[] nominalDates, final ZonedDateTime[] settlementDates, final ZonedDateTime[] resetDates,
      final ZonedDateTime[] maturityDates, final double notional, final double initialRate, final SwapConvention convention) {
    this(effectiveDate, nominalDates, settlementDates, resetDates, maturityDates, notional, initialRate, 0, convention);
  }

  public FloatingSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime[] nominalDates, final ZonedDateTime[] settlementDates, final ZonedDateTime[] resetDates,
      final ZonedDateTime[] maturityDates, final double notional, final double initialRate, final double spread, final SwapConvention convention) {
    Validate.notNull(effectiveDate, "effective date");
    Validate.notNull(nominalDates, "nominal dates");
    Validate.notNull(settlementDates, "settlement dates");
    final int n = nominalDates.length;
    Validate.isTrue(n == settlementDates.length, "settlement dates array must be the same length as the nominal dates array");
    Validate.notNull(resetDates, "reset dates");
    Validate.isTrue(n == resetDates.length, "reset dates array length must be equal to nominal dates array length");
    Validate.notNull(maturityDates, "FRA maturity dates");
    Validate.isTrue(n == maturityDates.length, "maturity dates array length must be equal to nominal dates array length");
    Validate.notNull(convention, "convention");
    _effectiveDate = effectiveDate;
    _nominalDates = nominalDates;
    _settlementDates = settlementDates;
    _resetDates = resetDates;
    _maturityDates = maturityDates;
    _notional = notional;
    _spread = spread;
    _initialRate = initialRate;
    _convention = convention;
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

  public ZonedDateTime[] getResetDates() {
    return _resetDates;
  }

  public ZonedDateTime[] getMaturityDates() {
    return _maturityDates;
  }

  public double getNotional() {
    return _notional;
  }

  public double getSpread() {
    return _spread;
  }

  public double getInitialRate() {
    return _initialRate;
  }

  public SwapConvention getConvention() {
    return _convention;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    result = prime * result + _effectiveDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_initialRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_maturityDates);
    result = prime * result + Arrays.hashCode(_nominalDates);
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_resetDates);
    result = prime * result + Arrays.hashCode(_settlementDates);
    temp = Double.doubleToLongBits(_spread);
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
    final FloatingSwapLegDefinition other = (FloatingSwapLegDefinition) obj;
    if (!ObjectUtils.equals(_effectiveDate, other._effectiveDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_initialRate) != Double.doubleToLongBits(other._initialRate)) {
      return false;
    }
    if (!Arrays.equals(_maturityDates, other._maturityDates)) {
      return false;
    }
    if (!Arrays.equals(_nominalDates, other._nominalDates)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (!Arrays.equals(_resetDates, other._resetDates)) {
      return false;
    }
    if (!Arrays.equals(_settlementDates, other._settlementDates)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public GenericAnnuity<Payment> toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(_settlementDates[_settlementDates.length - 1].toLocalDate()), date + " is after final settlement date (" + _settlementDates[_settlementDates.length - 1] + ")");
    //TODO
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1);
    s_logger.info("Using the first yield curve name as the funding curve name and the second as the libor curve name");
    final String fundingCurveName = yieldCurveNames[0];
    final String liborCurveName = yieldCurveNames[1];
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    double[] paymentTimes = ScheduleCalculator.getTimes(_settlementDates, actAct, zonedDate);
    double[] resetTimes = ScheduleCalculator.getTimes(_resetDates, actAct, zonedDate);
    double[] maturityTimes = ScheduleCalculator.getTimes(_maturityDates, actAct, zonedDate);
    double[] yearFractions = ScheduleCalculator.getYearFractions(_settlementDates, _convention.getDayCount(), _effectiveDate);
    final int n = ScheduleCalculator.numberOfNegativeValues(paymentTimes);
    if (date.equals(_effectiveDate.toLocalDate())) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, 1);
      resetTimes = ScheduleCalculator.removeFirstNValues(resetTimes, 1);
      maturityTimes = ScheduleCalculator.removeFirstNValues(maturityTimes, 1);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, 1);
    } else if (n > 0) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, n);
      resetTimes = ScheduleCalculator.removeFirstNValues(resetTimes, n);
      maturityTimes = ScheduleCalculator.removeFirstNValues(maturityTimes, n);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, n);
    }
    final Payment[] payments = new Payment[paymentTimes.length];
    if (date.isBefore(_nominalDates[1].toLocalDate())) {
      payments[0] = new CouponFixed(paymentTimes[0], fundingCurveName, yearFractions[0], _notional, _initialRate + _spread);
    } else {
      //TODO need to handle paymentYearFraction differently from forwardYearFraction 
      //TODO copied from original implementation
      payments[0] = new CouponIbor(paymentTimes[0], fundingCurveName, yearFractions[0], _notional, resetTimes[0], resetTimes[0], maturityTimes[0], yearFractions[0], _spread, liborCurveName);
    }
    for (int i = 1; i < payments.length; i++) {
      //TODO need to handle paymentYearFraction differently from forwardYearFraction 
      //TODO copied from original implementation
      payments[i] = new CouponIbor(paymentTimes[i], fundingCurveName, yearFractions[i], _notional, resetTimes[i], resetTimes[i], maturityTimes[i], yearFractions[i], _spread, liborCurveName);
    }
    return new GenericAnnuity<Payment>(payments);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitFloatingSwapLegDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitFloatingSwapLegDefinition(this);
  }
}
