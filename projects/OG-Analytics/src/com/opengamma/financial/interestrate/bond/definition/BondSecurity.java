/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;

/**
 * Describes a generic single currency bond issue.
 * @param <N> The notional type (usually FixedPayment or CouponInflationZeroCoupon).
 * @param <C> The coupon type.
 */
public abstract class BondSecurity<N extends Payment, C extends Coupon> implements InstrumentDerivative {
  /**
   * The nominal payments. For bullet bond, it is restricted to a single payment.
   */
  private final GenericAnnuity<N> _nominal;
  /**
   * The bond coupons. The coupons notional should be in line with the bond nominal. The discounting curve should be the same for the nominal and the coupons.
   */
  private final GenericAnnuity<C> _coupon;
  /**
   * The time (in years) to settlement date. Used for dirty/clean price computation.
   */
  private final double _settlementTime;
  /**
   * The bond issuer name.
   */
  private final String _issuer;
  /**
   * The name of the curve used for settlement amount discounting.
   */
  private final String _discountingCurveName;

  /**
   * Bond constructor from the bond nominal and coupon.
   * @param nominal The notional payments.
   * @param coupon The bond coupons.
   * @param settlementTime The time (in years) to settlement date. 
   * @param discountingCurveName The name of the curve used for settlement amount discounting.
   * @param issuer The bond issuer name.
   */
  public BondSecurity(GenericAnnuity<N> nominal, GenericAnnuity<C> coupon, double settlementTime, String discountingCurveName, String issuer) {
    Validate.notNull(nominal, "Nominal");
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(discountingCurveName, "Repo curve name");
    Validate.notNull(issuer, "Issuer");
    _nominal = nominal;
    _coupon = coupon;
    _settlementTime = settlementTime;
    _discountingCurveName = discountingCurveName;
    _issuer = issuer;
  }

  /**
   * Gets the nominal payments.
   * @return The nominal payments.
   */
  public GenericAnnuity<N> getNominal() {
    return _nominal;
  }

  /**
   * Gets the coupons.
   * @return The coupons.
   */
  public GenericAnnuity<C> getCoupon() {
    return _coupon;
  }

  /**
   * Gets the settlement time.
   * @return The settlement time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the bond currency.
   * @return The bond currency.
   */
  public Currency getCurrency() {
    return _nominal.getCurrency();
  }

  /**
   * Gets the name of the curve used for settlement amount discounting.
   * @return The curve name.
   */
  public String getRepoCurveName() {
    return _discountingCurveName;
  }

  /**
   * Gets the issuer name.
   * @return The issuer name.
   */
  public String getIssuer() {
    return _issuer;
  }

  /**
   * Gets the name of the curve used for discounting.
   * @return The curve name.
   */
  public String getDiscountingCurveName() {
    return getNominal().getDiscountCurve();
  }

  @Override
  public String toString() {
    String result = "Bond Security:";
    result += "\nNominal: " + _nominal.toString();
    result += "\nCoupon: " + _coupon.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _coupon.hashCode();
    result = prime * result + _nominal.hashCode();
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
    BondSecurity<?, ?> other = (BondSecurity<?, ?>) obj;
    if (!ObjectUtils.equals(_coupon, other._coupon)) {
      return false;
    }
    if (!ObjectUtils.equals(_nominal, other._nominal)) {
      return false;
    }
    return true;
  }

}
