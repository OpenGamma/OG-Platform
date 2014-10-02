/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing Ibor-like floating coupon with a spread and FX reset.
 * The currency is the currency of the payment. 
 * The notional is expressed in the reference currency, from which the FX reset will be computed.
 * For exact description of the instrument, see reference.
 * <P>
 * Reference: Coupon with FX Reset Notional, OpenGamma Documentation 26, September 2014.
 */
public class CouponIborFxReset extends Coupon implements DepositIndexCoupon<IborIndex> {

  /**
   * The floating coupon fixing time.
   */
  private final double _iborIndexFixingTime;
  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index;
  /**
   * The ibor index fixing period start time (in years).
   */
  private final double _iborIndexFixingPeriodStartTime;
  /**
   * The ibor index fixing period end time (in years).
   */
  private final double _iborIndexFixingPeriodEndTime;
  /**
   * The ibor index fixing period year fraction (or accrual factor) in the fixing convention.
   */
  private final double _iborIndexFixingAccrualFactor;
  /**
   * The spread paid above Ibor.
   */
  private final double _spread;
  /** 
   * The reference currency. 
   */
  private final Currency _referenceCurrency;
  /** 
   * The FX fixing time. The notional used for the payment is the FX rate between the reference currency (RC) and the 
   *  payment currency (PC): 1 RC = X . PC. 
   */
  private final double _fxFixingTime;
  /** 
   * The spot (delivery) time for the FX transaction underlying the FX fixing. 
   */
  private final double _fxDeliveryTime;

  /**
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional in the reference currency.
   * @param iborIndexFixingTime Time (in years) up to ibor index fixing.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param iborIndexFixingPeriodStartTime The ibor index fixing period start time (in years).
   * @param iborIndexFixingPeriodEndTime The ibor index fixing period end time (in years).
   * @param iborIndexFixingYearFraction The year fraction (or accrual factor) for the ibor index fixing period.
   * @param spread The spread.
   * @param referenceCurrency The reference currency for the FX reset. Not null.
   * @param fxFixingTime The FX fixing date. The notional used for the payment is the FX rate between the reference 
   * currency (RC) and the payment currency (PC): 1 RC = X . PC.
   * @param fxDeliveryTime The spot or delivery date for the FX transaction underlying the FX fixing.
   */
  public CouponIborFxReset(final Currency currency, final double paymentTime, final double paymentYearFraction,
      final double notional, final double iborIndexFixingTime, final IborIndex index,
      final double iborIndexFixingPeriodStartTime, final double iborIndexFixingPeriodEndTime,
      final double iborIndexFixingYearFraction, final double spread, final Currency referenceCurrency,
      double fxFixingTime, double fxDeliveryTime) {
    super(currency, paymentTime, paymentYearFraction, notional);
    ArgumentChecker.isTrue(iborIndexFixingPeriodStartTime >= iborIndexFixingTime,
        "ibor index fixing period start < ibor index fixing time");
    ArgumentChecker.isTrue(iborIndexFixingPeriodEndTime >= iborIndexFixingPeriodStartTime,
        "ibor index fixing period end < ibor index fixing period start");
    ArgumentChecker.isTrue(iborIndexFixingYearFraction >= 0, "ibor index fixing year fraction < 0");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(referenceCurrency, "reference currency");
    ArgumentChecker.isTrue(iborIndexFixingTime >= 0.0, "ibor index fixing time < 0");
    _iborIndexFixingTime = iborIndexFixingTime;
    _iborIndexFixingPeriodStartTime = iborIndexFixingPeriodStartTime;
    _iborIndexFixingPeriodEndTime = iborIndexFixingPeriodEndTime;
    _iborIndexFixingAccrualFactor = iborIndexFixingYearFraction;
    _index = index;
    _spread = spread;
    _referenceCurrency = referenceCurrency;
    _fxFixingTime = fxFixingTime;
    _fxDeliveryTime = fxDeliveryTime;
  }

  /**
   * Gets the floating coupon fixing time.
   * @return The ibor index fixing time.
   */
  public double getIborIndexFixingTime() {
    return _iborIndexFixingTime;
  }

  /**
   * Gets the ibor index fixing period start time (in years).
   * @return The ibor index fixing period start time.
   */
  public double getIborIndexFixingPeriodStartTime() {
    return _iborIndexFixingPeriodStartTime;
  }

  /**
   * Gets the ibor index fixing period end time (in years).
   * @return The ibor index fixing period end time.
   */
  public double getIborIndexFixingPeriodEndTime() {
    return _iborIndexFixingPeriodEndTime;
  }

  /**
   * Gets the accrual factor for the ibor index fixing period.
   * @return The accrual factor.
   */
  public double getIborIndexFixingAccrualFactor() {
    return _iborIndexFixingAccrualFactor;
  }

  /**
   * Gets the _spread field.
   * @return the _spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Returns the reference currency.
   * @return The currency.
   */
  public Currency getReferenceCurrency() {
    return _referenceCurrency;
  }

  /**
   * Returns the FX fixing time.
   * @return The time.
   */
  public double getFxFixingTime() {
    return _fxFixingTime;
  }

  /**
   * Returns the FX delivery time.
   * @return The time.
   */
  public double getFxDeliveryTime() {
    return _fxDeliveryTime;
  }

  /**
   * Gets the Ibor-like index.
   * @return The index.
   */
  @Override
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFxReset(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFxReset(this);
  }

  @Override
  public Coupon withNotional(double notional) {
    throw new UnsupportedOperationException("CouponIborFxReset does not support withNotional method.");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_iborIndexFixingAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_iborIndexFixingPeriodEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_iborIndexFixingPeriodStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_iborIndexFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fxDeliveryTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fxFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + ((_referenceCurrency == null) ? 0 : _referenceCurrency.hashCode());
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (!(obj instanceof CouponIborFxReset)) {
      return false;
    }
    CouponIborFxReset other = (CouponIborFxReset) obj;
    if (Double.doubleToLongBits(_iborIndexFixingAccrualFactor) != Double
        .doubleToLongBits(other._iborIndexFixingAccrualFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_iborIndexFixingPeriodEndTime) != Double
        .doubleToLongBits(other._iborIndexFixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_iborIndexFixingPeriodStartTime) != Double
        .doubleToLongBits(other._iborIndexFixingPeriodStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_iborIndexFixingTime) != Double.doubleToLongBits(other._iborIndexFixingTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fxDeliveryTime) != Double.doubleToLongBits(other._fxDeliveryTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fxFixingTime) != Double.doubleToLongBits(other._fxFixingTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceCurrency, other._referenceCurrency)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

}
