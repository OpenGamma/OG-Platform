/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflation;
import com.opengamma.financial.interestrate.payments.Coupon;

/**
 * Describes a capital inflation indexed bond issue. Both the coupon and the nominal are indexed on a price index.
 * @param <C> Type of inflation coupon.
 */
public class BondCapitalIndexedSecurity<C extends Coupon> extends BondSecurity<C, C> {

  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * The real accrued interest at the settlement date. The accrued interest is an amount (in line with the nominal).
   */
  private final double _accruedInterest;
  /**
   * Number of coupon per year.
   */
  private final int _couponPerYear;
  /**
   * The real accrual factor to the first coupon. Used for yield computation.
   */
  private final double _factorToNextCoupon;
  /**
   * The description of the bond settlement. It is used only for the dates and inflation calculation. 
   * The notional is 0 if the settlement is in the past and 1 if not.
   */
  private final CouponInflation _settlement;
  /**
   * The index value at the start of the bond.
   */
  private final double _indexStartValue;

  /**
   * Constructor of the Capital inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param settlementTime The time (in years) to settlement date.
   * @param accruedInterest The real accrued interest at the settlement date.
   * @param factorToNextCoupon The real accrual factor to the first coupon.
   * @param yieldConvention The bond yield convention.
   * @param couponPerYear Number of coupon per year.
   * @param settlement The description of the bond settlement.
   * @param indexStartValue The index value at the start of the bond.
   * @param issuer The bond issuer name.
   */
  public BondCapitalIndexedSecurity(GenericAnnuity<C> nominal, GenericAnnuity<C> coupon, double settlementTime, double accruedInterest, double factorToNextCoupon, YieldConvention yieldConvention,
      int couponPerYear, CouponInflation settlement, double indexStartValue, String issuer) {
    super(nominal, coupon, settlementTime, "Not used", issuer);
    Validate.notNull(yieldConvention, "Yield convention");
    Validate.notNull(settlement, "Settlement");
    _yieldConvention = yieldConvention;
    _accruedInterest = accruedInterest;
    _couponPerYear = couponPerYear;
    _factorToNextCoupon = factorToNextCoupon;
    _settlement = settlement;
    _indexStartValue = indexStartValue;
  }

  /**
   * Gets the bond yield convention.
   * @return The yield convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Gets the real accrued interest at the settlement date.
   * @return The accrued interest.
   */
  public double getAccruedInterest() {
    return _accruedInterest;
  }

  /**
   * Gets the number of coupon per year.
   * @return The number of coupon per year.
   */
  public int getCouponPerYear() {
    return _couponPerYear;
  }

  /**
   * Gets the real accrual factor to the first coupon.
   * @return The accrual factor to the first coupon.
   */
  public double getAccrualFactorToNextCoupon() {
    return _factorToNextCoupon;
  }

  /**
   * Gets the price index associated to the bond.
   * @return The price index.
   */
  public PriceIndex getPriceIndex() {
    return _settlement.getPriceIndex();
  }

  /**
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public CouponInflation getSettlement() {
    return _settlement;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondCapitalIndexedSecurity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondCapitalIndexedSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_accruedInterest);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _couponPerYear;
    temp = Double.doubleToLongBits(_factorToNextCoupon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _settlement.hashCode();
    result = prime * result + _yieldConvention.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondCapitalIndexedSecurity<?> other = (BondCapitalIndexedSecurity<?>) obj;
    if (Double.doubleToLongBits(_accruedInterest) != Double.doubleToLongBits(other._accruedInterest)) {
      return false;
    }
    if (_couponPerYear != other._couponPerYear) {
      return false;
    }
    if (Double.doubleToLongBits(_factorToNextCoupon) != Double.doubleToLongBits(other._factorToNextCoupon)) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlement, other._settlement)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
