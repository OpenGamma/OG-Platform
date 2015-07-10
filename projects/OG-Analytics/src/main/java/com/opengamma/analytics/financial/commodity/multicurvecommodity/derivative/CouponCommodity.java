/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  Class describing a generic commodity coupon.
 */
public abstract class CouponCommodity extends Payment {

  /**
   * The payment period year fraction (or accrual factor).
   */
  private final double _paymentYearFraction;

  /**
   * The underlying commodity
   */
  private final CommodityUnderlying _underlying;

  /** 
   * Description of unit size (for example :  barrel)
   */
  private final String _unitName;

  /** 
   * Notional which is a number of  unit 
   */
  private final double _notional;

  /** 
   * Settlement time. 
   */
  private final double _settlementTime;

  /**
   * The holiday calendar.
   */
  private final Calendar _calendar;

  /**
   * Constructor with all details.
   * @param paymentYearFraction The payment year fraction, positive
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param notional notional The number of unit
   * @param settlementTime The settlement time, , positive
   * @param calendar The holiday calendar, not null
   */
  public CouponCommodity(final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional,
      final double settlementTime, final Calendar calendar) {
    super(underlying.getCurrency(), settlementTime);
    ArgumentChecker.isTrue(paymentYearFraction >= 0, "payment year fraction must be positive");
    ArgumentChecker.isTrue(settlementTime >= 0, "settlement time must be positive");
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNegativeOrZero(notional, "unit amount");
    ArgumentChecker.notEmpty(unitName, "unit name");
    ArgumentChecker.notNull(calendar, "calendar");
    _paymentYearFraction = paymentYearFraction;
    _underlying = underlying;
    _notional = notional;
    _unitName = unitName;
    _settlementTime = settlementTime;
    _calendar = calendar;
  }

  /**
   * @return the _paymentYearFraction
   */
  public double getPaymentYearFractione() {
    return _paymentYearFraction;
  }

  /**
   * @return the _underlying
   */
  public CommodityUnderlying getUnderlying() {
    return _underlying;
  }

  /**
   * @return the _underlying
   */
  @Override
  public Currency getCurrency() {
    return _underlying.getCurrency();
  }

  /**
   * @return the _unitName
   */
  public String getUnitName() {
    return _unitName;
  }

  /**
   * @return the _notional
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * @return the _settlementTime
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * @return the _calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_calendar == null) ? 0 : _calendar.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_underlying == null) ? 0 : _underlying.hashCode());
    result = prime * result + ((_unitName == null) ? 0 : _unitName.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final CouponCommodity other = (CouponCommodity) obj;
    if (_calendar == null) {
      if (other._calendar != null) {
        return false;
      }
    } else if (!_calendar.equals(other._calendar)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentYearFraction) != Double.doubleToLongBits(other._paymentYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (_underlying == null) {
      if (other._underlying != null) {
        return false;
      }
    } else if (!_underlying.equals(other._underlying)) {
      return false;
    }
    if (_unitName == null) {
      if (other._unitName != null) {
        return false;
      }
    } else if (!_unitName.equals(other._unitName)) {
      return false;
    }
    return true;
  }

}
