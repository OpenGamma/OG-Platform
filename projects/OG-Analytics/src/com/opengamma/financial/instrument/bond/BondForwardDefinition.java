/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

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
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondForwardDefinition implements FixedIncomeInstrumentDefinition<BondForward> {
  private final BondDefinition _underlyingBond;
  private final LocalDate _forwardDate;
  private final BondConvention _convention;
  private final double _accruedInterestAtDelivery;
  private static final CouponFixed[] EMPTY_ARRAY = new CouponFixed[0];

  public BondForwardDefinition(final BondDefinition underlyingBond, final LocalDate forwardDate, final BondConvention convention) {
    Validate.notNull(underlyingBond, "underlying bond");
    Validate.notNull(forwardDate, "forward date");
    Validate.notNull(convention, "convention");
    final LocalDate[] underlyingNominalDates = underlyingBond.getNominalDates();
    Validate.isTrue(forwardDate.isBefore(underlyingNominalDates[underlyingNominalDates.length - 1]), "forward date is after bond maturity");
    _underlyingBond = underlyingBond;
    _forwardDate = forwardDate;
    _convention = convention;
    final BondConvention underlyingConvention = _underlyingBond.getConvention();
    final double coupon = underlyingBond.getCoupons()[0]; //TODO not necessarily - coupons might not be equal (unlikely but should be tested for)
    _accruedInterestAtDelivery = AccruedInterestCalculator.getAccruedInterest(underlyingConvention.getDayCount(), _forwardDate, _underlyingBond.getNominalDates(), coupon,
        _underlyingBond.getCouponsPerYear(), underlyingConvention.isEOM(), underlyingConvention.getExDividendDays(), underlyingConvention.getWorkingDayCalendar());
  }

  /**
   * Gets the bond currency.
   * @return The currency
   */
  public Currency getCurrency() {
    return _underlyingBond.getCurrency();
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

  @Override
  public BondForward toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(_forwardDate), date + " is after forward date (" + _forwardDate + ")");
    Validate.notNull(yieldCurveNames, "yield curve names");
    final LocalDate settlementDate = getSettlementDate(date, _convention.getWorkingDayCalendar(), _convention.getBusinessDayConvention(), _convention.getSettlementDays());
    final double accruedInterest = _underlyingBond.toDerivative(date, yieldCurveNames).getAccruedInterest();
    final Bond bond = _underlyingBond.toDerivative(date, yieldCurveNames);
    final DayCount repoDaycount = _convention.getDayCount();
    final double timeToExpiry = repoDaycount.getDayCountFraction(settlementDate.atMidnight().atZone(TimeZone.UTC), _forwardDate.atMidnight().atZone(TimeZone.UTC));
    final LocalDate[] schedule = _underlyingBond.getSettlementDates();
    final double[] coupons = _underlyingBond.getCoupons();
    final List<CouponFixed> expiredCoupons = new ArrayList<CouponFixed>();
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
        expiredCoupons.add(new CouponFixed(_underlyingBond.getCurrency(), period, yieldCurveNames[0], timeBetweenPeriods, notional, coupons[i]));
      }
      i++;
    }
    return new BondForward(bond, timeToExpiry, accruedInterest, _accruedInterestAtDelivery, expiredCoupons.toArray(EMPTY_ARRAY));
  }

  private LocalDate getSettlementDate(final LocalDate today, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    LocalDate date = businessDayConvention.adjustDate(calendar, today.plusDays(1));
    for (int i = 0; i < settlementDays; i++) {
      date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
    }
    return date;
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondForwardDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondForwardDefinition(this);
  }
}
