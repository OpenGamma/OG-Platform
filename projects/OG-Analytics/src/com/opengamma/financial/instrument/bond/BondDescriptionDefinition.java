/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.util.money.Currency;

/**
 * Describes a generic single currency bond issue.
 * @param <C> The coupon type.
 */
public abstract class BondDescriptionDefinition<C extends CouponDefinition> {
  /**
   * The notional payments. For bullet bond, it is restricted to a single payment.
   */
  private final AnnuityPaymentFixedDefinition _nominal;
  /**
   * The bond coupons. The coupons notional should be in line with the bond nominal.
   */
  private final AnnuityDefinition<C> _coupon;
  /**
   * Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   */
  private final int _exCouponDays;
  /**
   * Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   */
  private final int _settlementDays;
  /**
   * The calendar used to compute the standard settlement date.
   */
  private final Calendar _calendar;
  /**
   * The coupon day count convention.
   */
  private final DayCount _dayCount;
  /**
   * The bond issuer name.
   */
  private final String _issuer;
  /**
   * The bond repo type.
   */
  private final String _repoType;

  /**
   * Bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   */
  public BondDescriptionDefinition(AnnuityPaymentFixedDefinition nominal, AnnuityDefinition<C> coupon, int exCouponDays, int settlementDays, Calendar calendar, DayCount dayCount) {
    Validate.notNull(nominal, "Nominal");
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(dayCount, "Day count");
    Validate.isTrue(nominal.getCurrency() == coupon.getCurrency(), "Currency of nominal and coupons should be the same");
    Validate.isTrue(!nominal.isPayer(), "Notional should be positive");
    Validate.isTrue(!coupon.isPayer(), "Coupon notional should be positive");
    this._nominal = nominal;
    this._coupon = coupon;
    this._exCouponDays = exCouponDays;
    this._settlementDays = settlementDays;
    this._dayCount = dayCount;
    _calendar = calendar;
    _issuer = "";
    _repoType = "";
  }

  /**
   * Gets the _nominal field.
   * @return the _nominal
   */
  public AnnuityPaymentFixedDefinition getNominal() {
    return _nominal;
  }

  /**
   * Gets the _coupon field.
   * @return the _coupon
   */
  public AnnuityDefinition<C> getCoupon() {
    return _coupon;
  }

  /**
   * Gets the _exCouponDays field.
   * @return the _exCouponDays
   */
  public int getExCouponDays() {
    return _exCouponDays;
  }

  /**
   * Gets the _settlementDays field.
   * @return the _settlementDays
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Gets the _dayCount field.
   * @return the _dayCount
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the _issuer field.
   * @return the _issuer
   */
  public String getIssuer() {
    return _issuer;
  }

  /**
   * Gets the _repoType field.
   * @return the _repoType
   */
  public String getRepoType() {
    return _repoType;
  }

  /**
   * Gets the currency.
   * @return The bond currency.
   */
  public Currency getCurrency() {
    return _nominal.getCurrency();
  }

  /**
   * Gets the _calendar field.
   * @return the _calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  @Override
  public String toString() {
    String result = "Bond : \n";
    result += "Notional: " + _nominal.toString();
    result += "\nCoupons: \n" + _coupon.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _coupon.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _exCouponDays;
    result = prime * result + _issuer.hashCode();
    result = prime * result + _nominal.hashCode();
    result = prime * result + _repoType.hashCode();
    result = prime * result + _settlementDays;
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
    BondDescriptionDefinition<?> other = (BondDescriptionDefinition<?>) obj;
    if (!ObjectUtils.equals(_coupon, other._coupon)) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (_exCouponDays != other._exCouponDays) {
      return false;
    }
    if (!ObjectUtils.equals(_issuer, other._issuer)) {
      return false;
    }
    if (!ObjectUtils.equals(_nominal, other._nominal)) {
      return false;
    }
    if (!ObjectUtils.equals(_repoType, other._repoType)) {
      return false;
    }
    if (_settlementDays != other._settlementDays) {
      return false;
    }
    return true;
  }

}
