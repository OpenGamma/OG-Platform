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
import com.opengamma.financial.instrument.InterestRateDerivativeProvider;
import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * 
 */
public class FixedSwapLegDefinition implements InterestRateDerivativeProvider<GenericAnnuity<FixedCouponPayment>> {
  private static final Logger s_logger = LoggerFactory.getLogger(FixedSwapLegDefinition.class);
  private final ZonedDateTime _effectiveDate;
  private final ZonedDateTime[] _nominalDates;
  private final ZonedDateTime[] _settlementDates;
  private final double _notional;
  private final double _rate;
  private final SwapConvention _convention;

  public FixedSwapLegDefinition(ZonedDateTime effectiveDate, ZonedDateTime[] nominalDates, ZonedDateTime[] settlementDates, double notional, double rate, SwapConvention convention) {
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
    FixedSwapLegDefinition other = (FixedSwapLegDefinition) obj;
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
  public GenericAnnuity<FixedCouponPayment> toDerivative(LocalDate date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    s_logger.info("Using the first yield curve name as the funding curve name");
    ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    double[] paymentTimes = ScheduleCalculator.getTimes(_settlementDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"), zonedDate);
    double[] yearFractions = ScheduleCalculator.getYearFractions(_settlementDates, _convention.getDayCount(), _effectiveDate);
    final int n = ScheduleCalculator.numberOfNegativeValues(paymentTimes);
    if (n >= paymentTimes.length) {
      return new FixedCouponAnnuity(new double[] {0.0}, 0.0, 0.0, yieldCurveNames[0]);
    }
    if (n > 0) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, n);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, n);
    }
    return new FixedCouponAnnuity(paymentTimes, _notional, _rate, yearFractions, yieldCurveNames[0]);
  }

}
