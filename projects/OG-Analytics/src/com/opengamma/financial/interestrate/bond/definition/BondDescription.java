/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Describes a generic single currency bond issue.
 * @param <C> The coupon type.
 */
public abstract class BondDescription<C extends Payment> implements InterestRateDerivative {
  /**
   * The notional payments. For bullet bond, it is restricted to a single payment.
   */
  private final GenericAnnuity<PaymentFixed> _nominal;
  /**
   * The bond coupons. The coupons notional should be in line with the bond nominal.
   */
  private final GenericAnnuity<C> _coupon;
  /**
   * Time to standard settlement (spot).
   */
  private final double _spotTime;

  //TODO: Do we need _exCouponTime?

  /**
   * Bond constructor from the bond nominal and coupon.
   * @param nominal The notional payments.
   * @param coupon The bond coupons.
   * @param spotTime Time to standard settlement.
   */
  public BondDescription(GenericAnnuity<PaymentFixed> nominal, GenericAnnuity<C> coupon, double spotTime) {
    Validate.notNull(nominal, "Nominal");
    Validate.notNull(coupon, "Coupon");
    _nominal = nominal;
    _coupon = coupon;
    _spotTime = spotTime;
  }

  /**
   * Gets the _nominal field.
   * @return The nominal payments.
   */
  public GenericAnnuity<PaymentFixed> getNominal() {
    return _nominal;
  }

  /**
   * Gets the _coupon field.
   * @return The coupons.
   */
  public GenericAnnuity<C> getCoupon() {
    return _coupon;
  }

  /**
   * Gets the _settlementTime field.
   * @return the _settlementTime
   */
  public double getSpotTime() {
    return _spotTime;
  }

  @Override
  public String toString() {
    String result = "Bond Description: Spot time=" + _spotTime + "\n";
    result += "Nominal: " + _nominal.toString();
    result += "Coupon: " + _coupon.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _coupon.hashCode();
    result = prime * result + _nominal.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spotTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    BondDescription<?> other = (BondDescription<?>) obj;
    if (!ObjectUtils.equals(_coupon, other._coupon)) {
      return false;
    }
    if (!ObjectUtils.equals(_nominal, other._nominal)) {
      return false;
    }
    if (Double.doubleToLongBits(_spotTime) != Double.doubleToLongBits(other._spotTime)) {
      return false;
    }
    return true;
  }

}
