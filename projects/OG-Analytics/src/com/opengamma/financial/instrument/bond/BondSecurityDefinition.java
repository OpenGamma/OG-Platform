/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;

/**
 * Describes a generic single currency bond issue.
 * @param <N> The notional type (usually FixedPayment or CouponInflationZeroCoupon).
 * @param <C> The coupon type.
 */
public abstract class BondSecurityDefinition<N extends PaymentDefinition, C extends CouponDefinition> implements InstrumentDefinition<BondSecurity<? extends Payment, ? extends Coupon>> {
  /**
   * The notional payments. For bullet bond, it is restricted to a single payment.
   */
  private final AnnuityDefinition<N> _nominal;
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
   */
  public BondSecurityDefinition(AnnuityDefinition<N> nominal, AnnuityDefinition<C> coupon, int exCouponDays, int settlementDays, Calendar calendar) {
    Validate.notNull(nominal, "Nominal");
    Validate.notNull(coupon, "Coupon");
    Validate.isTrue(nominal.getCurrency() == coupon.getCurrency(), "Currency of nominal and coupons should be the same");
    Validate.isTrue(!nominal.isPayer(), "Notional should be positive");
    Validate.isTrue(!coupon.isPayer(), "Coupon notional should be positive");
    this._nominal = nominal;
    this._coupon = coupon;
    // TODO: check that the coupon and nominal correspond in term of remaining notional.
    this._exCouponDays = exCouponDays;
    this._settlementDays = settlementDays;
    _calendar = calendar;
    _issuer = "";
    _repoType = "";
  }

  /**
   * Bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param issuer The issuer name.
   * @param repoType The repo type name.
   */
  public BondSecurityDefinition(AnnuityDefinition<N> nominal, AnnuityDefinition<C> coupon, int exCouponDays, int settlementDays, Calendar calendar, final String issuer, final String repoType) {
    Validate.notNull(nominal, "Nominal");
    Validate.notNull(coupon, "Coupon");
    Validate.isTrue(nominal.getCurrency() == coupon.getCurrency(), "Currency of nominal and coupons should be the same");
    Validate.isTrue(!nominal.isPayer(), "Notional should be positive");
    Validate.isTrue(!coupon.isPayer(), "Coupon notional should be positive");
    this._nominal = nominal;
    this._coupon = coupon;
    // TODO: check that the coupon and nominal correspond in term of remaining notional.
    this._exCouponDays = exCouponDays;
    this._settlementDays = settlementDays;
    _calendar = calendar;
    _issuer = issuer;
    _repoType = repoType;
  }

  /**
   * Gets the nominal.
   * @return The nominal.
   */
  public AnnuityDefinition<N> getNominal() {
    return _nominal;
  }

  /**
   * Gets the coupons.
   * @return The coupons.
   */
  public AnnuityDefinition<C> getCoupon() {
    return _coupon;
  }

  /**
   * Gets the number of ex-coupon days.
   * @return The ex-coupon days.
   */
  public int getExCouponDays() {
    return _exCouponDays;
  }

  /**
   * Gets the number of days to standard settlement.
   * @return The days to settlement.
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Gets the issuer name.
   * @return The issuer name.
   */
  public String getIssuer() {
    return _issuer;
  }

  /**
   * Gets the _repoType name.
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
   * Gets the calendar used to compute the standard settlement date.
   * @return The calendar.
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
    BondSecurityDefinition<?, ?> other = (BondSecurityDefinition<?, ?>) obj;
    if (!ObjectUtils.equals(_coupon, other._coupon)) {
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
