/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import java.util.Arrays;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.InterestRateDerivativeProvider;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.util.CompareUtils;

/**
 * 
 * A class that defines a coupon bond. 
 */
public class BondDefinition implements InterestRateDerivativeProvider<Bond> {
  private static final Logger s_logger = LoggerFactory.getLogger(BondDefinition.class);
  private final LocalDate[] _nominalDates;
  private final LocalDate[] _settlementDates; // TODO settlement dates to be calculated in this class?
  private final double[] _coupons;
  private final double _notional;
  private final double _couponsPerYear;
  private final BondConvention _convention;

  public BondDefinition(final LocalDate[] nominalDates, final LocalDate[] settlementDates, final double couponRate, final double couponsPerYear, final BondConvention convention) {
    this(nominalDates, settlementDates, couponRate, 1, couponsPerYear, convention);
  }

  public BondDefinition(final LocalDate[] nominalDates, final LocalDate[] settlementDates, final double couponRate, final double notional, final double couponsPerYear, final BondConvention convention) {
    Validate.noNullElements(nominalDates, "nominal dates");
    Validate.noNullElements(settlementDates, "settlement dates");
    Validate.notEmpty(nominalDates, "nominal dates");
    final int n = nominalDates.length;
    Validate.isTrue(nominalDates.length == settlementDates.length);
    Validate.isTrue(couponsPerYear > 0, "must have positive number of coupons per year");
    Validate.notNull(convention);
    _nominalDates = nominalDates;
    _settlementDates = settlementDates;
    _coupons = new double[n - 1];
    Arrays.fill(_coupons, couponRate);
    _notional = notional;
    _couponsPerYear = couponsPerYear;
    _convention = convention;
  }

  public BondDefinition(final LocalDate[] nominalDates, final LocalDate[] settlementDates, final double[] coupons, final double couponsPerYear, final BondConvention convention) {
    this(nominalDates, settlementDates, coupons, 1, couponsPerYear, convention);
  }

  public BondDefinition(final LocalDate[] nominalDates, final LocalDate[] settlementDates, final double[] coupons, final double notional, final double couponsPerYear, final BondConvention convention) {
    Validate.noNullElements(nominalDates, "nominal dates");
    Validate.noNullElements(settlementDates, "settlement dates");
    Validate.notEmpty(nominalDates, "nominal dates");
    Validate.notNull(coupons, "coupons");
    final int n = nominalDates.length;
    Validate.isTrue(n == settlementDates.length);
    Validate.isTrue(n - 1 == coupons.length);
    Validate.isTrue(couponsPerYear > 0, "must have positive number of coupons per year");
    Validate.notNull(convention);
    _nominalDates = nominalDates;
    _settlementDates = settlementDates;
    _coupons = coupons;
    _notional = notional;
    _couponsPerYear = couponsPerYear;
    _convention = convention;
  }

  public LocalDate[] getNominalDates() {
    return _nominalDates;
  }

  public LocalDate[] getSettlementDates() {
    return _settlementDates;
  }

  public double[] getCoupons() {
    return _coupons;
  }

  public double getNotional() {
    return _notional;
  }

  public double getCouponsPerYear() {
    return _couponsPerYear;
  }

  public BondConvention getConvention() {
    return _convention;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    result = prime * result + Arrays.hashCode(_coupons);
    long temp;
    temp = Double.doubleToLongBits(_couponsPerYear);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_nominalDates);
    temp = Double.doubleToLongBits(_notional);
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
    final BondDefinition other = (BondDefinition) obj;
    if (!Arrays.equals(_coupons, other._coupons)) {
      return false;
    }
    if (!ObjectUtils.equals(_convention, other._convention)) {
      return false;
    }
    if (Double.doubleToLongBits(_couponsPerYear) != Double.doubleToLongBits(other._couponsPerYear)) {
      return false;
    }

    if (!Arrays.equals(_nominalDates, other._nominalDates)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    return Arrays.equals(_settlementDates, other._settlementDates);
  }

  @Override
  public Bond toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    s_logger.info("Using the first yield curve name as the funding curve name");
    final int index = Arrays.binarySearch(_nominalDates, date);
    int position = index;
    final int n = _settlementDates.length;
    double accruedInterest = 0;
    double accrualTime = 0;
    double coupon;
    if (index < 0) {
      position = -index - 2;
    } else if (index != 0) {
      position -= 1;
    }
    coupon = _coupons[position];
    final DayCount dayCount = _convention.getDayCount();
    final Calendar calendar = _convention.getWorkingDayCalendar();
    final LocalDate settlementDate = getSettlementDate(date, calendar, _convention.getBusinessDayConvention(), _convention.getSettlementDays());
    accruedInterest = AccruedInterestCalculator.getAccruedInterest(dayCount, settlementDate, _nominalDates, coupon, _couponsPerYear, _convention.isEOM(), _convention.getExDividendDays(), position);
    accrualTime = accruedInterest / coupon;
    final double timeBetweenCoupons = 1. / _couponsPerYear;
    final double[] paymentTimes = new double[n - position - 1];
    paymentTimes[0] = index == 0 ? 0 : timeBetweenCoupons - accrualTime;
    if (CompareUtils.closeEquals(paymentTimes[0], 0, 1e-16)) {
      paymentTimes[0] = 0;
    }
    for (int i = 1; i < paymentTimes.length; i++) {
      paymentTimes[i] = paymentTimes[i - 1] + timeBetweenCoupons;
    }
    return new Bond(paymentTimes, coupon, timeBetweenCoupons, accruedInterest, yieldCurveNames[0]);
  }

  // TODO this only works for following
  private LocalDate getSettlementDate(final LocalDate today, final Calendar calendar, final BusinessDayConvention businessDayConvention, final int settlementDays) {
    LocalDate date = businessDayConvention.adjustDate(calendar, today.plusDays(1));
    for (int i = 0; i < settlementDays; i++) {
      date = businessDayConvention.adjustDate(calendar, date.plusDays(1));
    }
    return date;
  }

}
