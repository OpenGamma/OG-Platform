/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes an interest inflation indexed bond issue. Only the coupon are indexed on a price index.
 * @param <N> Type of fixed payment.
 * @param <C> Type of inflation coupon.
 */
public class BondInterestIndexedSecurity<N extends PaymentFixed, C extends Coupon> extends BondSecurity<N, C> {

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
   * The description of the bond settlement. It is used only for the dates.
   * The notional is 0 if the settlement is in the past and 1 if not.
   */
  private final PaymentFixed _settlement;

  /**
   * The price index associated to the bond.
   */
  private final IndexPrice _priceIndex;

  /**
   * Constructor of the Capital inflation indexed bond. The legal entity contains only the issuer name.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param settlementTime The time (in years) to settlement date.
   * @param accruedInterest The real accrued interest at the settlement date.
   * @param factorToNextCoupon The real accrual factor to the first coupon.
   * @param yieldConvention The bond yield convention.
   * @param couponPerYear Number of coupon per year.
   * @param settlement The description of the bond settlement.
   * @param priceIndex The price index
   * @param issuer The bond issuer name.
   */
  public BondInterestIndexedSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final double accruedInterest, final double factorToNextCoupon,
      final YieldConvention yieldConvention, final int couponPerYear, final PaymentFixed settlement, final String issuer, final IndexPrice priceIndex) {
    this(nominal, coupon, settlementTime, accruedInterest, factorToNextCoupon, yieldConvention, couponPerYear, settlement, new LegalEntity(null, issuer, null, null, null), priceIndex);
  }

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
   * @param priceIndex The price index
   * @param issuer The bond issuer name.
   */
  public BondInterestIndexedSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final double accruedInterest, final double factorToNextCoupon,
      final YieldConvention yieldConvention, final int couponPerYear, final PaymentFixed settlement, final LegalEntity issuer, final IndexPrice priceIndex) {
    super(nominal, coupon, settlementTime, issuer);
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    ArgumentChecker.notNull(settlement, "Settlement");
    ArgumentChecker.notNull(priceIndex, "Price Index");
    _yieldConvention = yieldConvention;
    _accruedInterest = accruedInterest;
    _couponPerYear = couponPerYear;
    _factorToNextCoupon = factorToNextCoupon;
    _settlement = settlement;
    _priceIndex = priceIndex;

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
  public IndexPrice getPriceIndex() {
    return _priceIndex;
  }

  /**
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public PaymentFixed getSettlement() {
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
    return visitor.visitBondInterestIndexedSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondInterestIndexedSecurity(this);
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
    result = prime * result + ((_settlement == null) ? 0 : _settlement.hashCode());
    result = prime * result + ((_yieldConvention == null) ? 0 : _yieldConvention.hashCode());
    return result;
  }

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
    final BondInterestIndexedSecurity<?, ?> other = (BondInterestIndexedSecurity<?, ?>) obj;
    if (Double.doubleToLongBits(_accruedInterest) != Double.doubleToLongBits(other._accruedInterest)) {
      return false;
    }
    if (_couponPerYear != other._couponPerYear) {
      return false;
    }
    if (Double.doubleToLongBits(_factorToNextCoupon) != Double.doubleToLongBits(other._factorToNextCoupon)) {
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
