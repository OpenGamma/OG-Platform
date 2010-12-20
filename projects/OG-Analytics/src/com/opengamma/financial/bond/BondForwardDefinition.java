/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.bond;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * 
 */
public class BondForwardDefinition implements InterestRateDerivativeProvider<BondForward> {
  private final BondDefinition _underlyingBond;
  private final LocalDate _forwardDate;
  private final BondConvention _convention;
  private static final FixedCouponPayment[] EMPTY_ARRAY = new FixedCouponPayment[0];

  public BondForwardDefinition(final BondDefinition underlyingBond, final LocalDate forwardDate, final BondConvention convention) {
    Validate.notNull(underlyingBond, "underlying bond");
    Validate.notNull(forwardDate, "forward date");
    Validate.notNull(convention, "convention");
    final LocalDate[] underlyingNominalDates = underlyingBond.getNominalDates();
    Validate.isTrue(forwardDate.isBefore(underlyingNominalDates[underlyingNominalDates.length - 1]), "forward date is after bond maturity");
    _underlyingBond = underlyingBond;
    _forwardDate = forwardDate;
    _convention = convention;
  }

  public BondDefinition getUnderlyingBond() {
    return _underlyingBond;
  }

  public LocalDate getForwardDate() {
    return _forwardDate;
  }

  public BondConvention getConvention() {
    return _convention;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _forwardDate.hashCode();
    result = prime * result + _underlyingBond.hashCode();
    result = prime * result + _convention.hashCode();
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
    final BondForwardDefinition other = (BondForwardDefinition) obj;
    if (!ObjectUtils.equals(_forwardDate, other._forwardDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingBond, other._underlyingBond)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  //TODO pass in the convention here?
  @Override
  public BondForward toDerivative(final LocalDate date, final String... yieldCurveNames) {
    final BondConvention underlyingConvention = _underlyingBond.getConvention(); //TODO need a bond forward convention?
    final LocalDate settlementDate = getSettlementDate(date, _convention.getWorkingDayCalendar(), _convention.getBusinessDayConvention(), _convention.getSettlementDays());
    final LocalDate bondSettlementDate = getSettlementDate(date, underlyingConvention.getWorkingDayCalendar(), underlyingConvention.getBusinessDayConvention(),
        underlyingConvention.getSettlementDays());
    final double accruedInterest = _underlyingBond.toDerivative(settlementDate, yieldCurveNames).getAccruedInterest();
    final double accruedInterestAtDelivery = AccruedInterestCalculator.getAccruedInterest(underlyingConvention.getDayCount(), _forwardDate, _underlyingBond.getNominalDates(),
        _underlyingBond.getCoupons()[0], _underlyingBond.getCouponsPerYear(), underlyingConvention.isEOM(), underlyingConvention.getExDividendDays()); //TODO move this into the forward definition
    System.out.println("accruedInterestAtDelivery\t" + accruedInterestAtDelivery + "\t forwardDate\t" + _forwardDate);
    final Bond bond = _underlyingBond.toDerivative(bondSettlementDate, yieldCurveNames);
    final DayCount repoDaycount = _convention.getDayCount();
    final double timeToExpiry = repoDaycount.getDayCountFraction(settlementDate.atMidnight().atZone(TimeZone.UTC), _forwardDate.atMidnight().atZone(TimeZone.UTC));
    final LocalDate[] schedule = _underlyingBond.getSettlementDates();//.getNominalDates(); // settlement dates should equal nominal dates for bond forwards
    final double[] coupons = _underlyingBond.getCoupons();
    final List<FixedCouponPayment> expiredCoupons = new ArrayList<FixedCouponPayment>();
    int i = 0;
    final double timeBetweenPeriods = 1. / _underlyingBond.getCouponsPerYear();
    final double notional = _underlyingBond.getNotional();
    ZonedDateTime couponDateWithZone;
    final ZonedDateTime dateWithZone = _forwardDate.atMidnight().atZone(TimeZone.UTC);
    for (final LocalDate couponDate : schedule) {
      if (couponDate.isAfter(settlementDate)) {
        if (couponDate.isAfter(_forwardDate)) {
          break;
        }
        couponDateWithZone = couponDate.atMidnight().atZone(TimeZone.UTC);
        final double period = repoDaycount.getDayCountFraction(couponDateWithZone, dateWithZone);
        expiredCoupons.add(new FixedCouponPayment(period, notional, timeBetweenPeriods, coupons[i], yieldCurveNames[0]));
      }
      i++;
    }
    return new BondForward(bond, timeToExpiry, accruedInterest, accruedInterestAtDelivery, expiredCoupons.toArray(EMPTY_ARRAY));
  }

  private LocalDate getSettlementDate(final LocalDate today, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    LocalDate date = businessDayConvention.adjustDate(calendar, today.plusDays(1));
    for (int i = 0; i < settlementDays; i++) {
      date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
    }
    return date;
  }
}
