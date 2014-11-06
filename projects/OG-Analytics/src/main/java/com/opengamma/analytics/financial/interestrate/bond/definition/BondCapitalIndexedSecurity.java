/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;

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
   * The real accrual factor to the first coupon. Used for yield computation.
   */
  private final double _ratioPeriodToNextCoupon;
  /**
   * The description of the bond settlement. It is used only for the dates and inflation calculation.
   * The notional is 0 if the settlement is in the past and 1 if not.
   */
  private final Coupon _settlement;
  /**
   * The index value at the start of the bond.
   */
  private final double _indexStartValue;
  /**
   * The last known fixing of the price index.
   */
  private final double _lastIndexKnownFixing;

  /**
   * The fixing time last known fixing of the price index.
   */
  private final double _lastKnownFixingTime;

  /**
   * The index ratio.
   */
  private final double _indexRatio;

  /**
   * Constructor of the Capital inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param settlementTime The time (in years) to settlement date.
   * @param accruedInterest The real accrued interest at the settlement date.
   * @param factorToNextCoupon The real accrual factor to the first coupon.
   * @param ratioPeriodToNextCoupon TODO
   * @param yieldConvention The bond yield convention.
   * @param couponPerYear Number of coupon per year.
   * @param settlement The description of the bond settlement.
   * @param indexStartValue The index value at the start of the bond.
   * @param lastIndexKnownFixing The last index known fixing.
   * @param lastKnownFixingTime The last known fixing.
   * @param indexRatio The last known fixing.
   * @param issuer The bond issuer name.
   */
  public BondCapitalIndexedSecurity(final Annuity<C> nominal, final Annuity<C> coupon, final double settlementTime, final double accruedInterest, final double factorToNextCoupon,
      final double ratioPeriodToNextCoupon, final YieldConvention yieldConvention, final int couponPerYear, final Coupon settlement, final double indexStartValue,
      final double lastIndexKnownFixing, final double lastKnownFixingTime, final double indexRatio, final String issuer) {
    this(nominal, coupon, settlementTime, accruedInterest, factorToNextCoupon, ratioPeriodToNextCoupon, yieldConvention, couponPerYear, settlement, indexStartValue, lastIndexKnownFixing,
        lastKnownFixingTime, indexRatio, new LegalEntity(
            null, issuer, null, null, null));
  }

  /**
   * Constructor of the Capital inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param settlementTime The time (in years) to settlement date.
   * @param accruedInterest The real accrued interest at the settlement date.
   * @param factorToNextCoupon The real accrual factor to the first coupon.
   * @param ratioPeriodToNextCoupon TODO
   * @param yieldConvention The bond yield convention.
   * @param couponPerYear Number of coupon per year.
   * @param settlement The description of the bond settlement.
   * @param indexStartValue The index value at the start of the bond.
   * @param lastIndexKnownFixing The last index known fixing.
   * @param lastKnownFixingTime The last known fixing.
   * @param indexRatio The index Ratio.
   * @param issuer The bond issuer name.
   */
  public BondCapitalIndexedSecurity(final Annuity<C> nominal, final Annuity<C> coupon, final double settlementTime, final double accruedInterest, final double factorToNextCoupon,
      final double ratioPeriodToNextCoupon, final YieldConvention yieldConvention, final int couponPerYear, final Coupon settlement, final double indexStartValue,
      final double lastIndexKnownFixing, final double lastKnownFixingTime, final double indexRatio, final LegalEntity issuer) {
    super(nominal, coupon, settlementTime, issuer);
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    ArgumentChecker.notNull(settlement, "Settlement");
    _yieldConvention = yieldConvention;
    _accruedInterest = accruedInterest;
    _couponPerYear = couponPerYear;
    _factorToNextCoupon = factorToNextCoupon;
    _ratioPeriodToNextCoupon = ratioPeriodToNextCoupon;
    _settlement = settlement;
    _indexStartValue = indexStartValue;
    _lastIndexKnownFixing = lastIndexKnownFixing;
    _lastKnownFixingTime = lastKnownFixingTime;
    _indexRatio = indexRatio;
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
   * Gets the real accrual factor to the first coupon.
   * @return The accrual factor to the first coupon.
   */
  public double getRatioPeriodToNextCoupon() {
    return _ratioPeriodToNextCoupon;
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
  public double getLastIndexKnownFixing() {
    return _lastIndexKnownFixing;
  }

  /**
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public double getLastButOneIndexKnownFixing() {
    return _lastIndexKnownFixing;
  }

  /**
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public double getLastKnownFixingTime() {
    return _lastKnownFixingTime;
  }

  /**
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public double getIndexRatio() {
    return _indexRatio;
  }

  /**
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public Coupon getSettlement() {
    return _settlement;
  }

  //  /**
  //   * Returns the issuer/currency pair for the bond.
  //   * @return The pair.
  //   */
  //  public Pair<String, Currency> getIssuerCurrency() {
  //    return ObjectsPair.of(getIssuer(), getCurrency());
  //  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondCapitalIndexedSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondCapitalIndexedSecurity(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
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
    temp = Double.doubleToLongBits(_lastIndexKnownFixing);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lastKnownFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_ratioPeriodToNextCoupon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_settlement == null) ? 0 : _settlement.hashCode());
    result = prime * result + ((_yieldConvention == null) ? 0 : _yieldConvention.hashCode());
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
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondCapitalIndexedSecurity other = (BondCapitalIndexedSecurity) obj;
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
    if (Double.doubleToLongBits(_lastIndexKnownFixing) != Double.doubleToLongBits(other._lastIndexKnownFixing)) {
      return false;
    }
    if (Double.doubleToLongBits(_lastKnownFixingTime) != Double.doubleToLongBits(other._lastKnownFixingTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_ratioPeriodToNextCoupon) != Double.doubleToLongBits(other._ratioPeriodToNextCoupon)) {
      return false;
    }
    if (_settlement == null) {
      if (other._settlement != null) {
        return false;
      }
    } else if (!_settlement.equals(other._settlement)) {
      return false;
    }
    if (_yieldConvention == null) {
      if (other._yieldConvention != null) {
        return false;
      }
    } else if (!_yieldConvention.equals(other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
