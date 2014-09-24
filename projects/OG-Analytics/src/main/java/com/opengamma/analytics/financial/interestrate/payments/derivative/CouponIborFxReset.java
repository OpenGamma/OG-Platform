/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

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
  private final double _fixingTime;
  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index;
  /**
   * The fixing period start time (in years).
   */
  private final double _fixingPeriodStartTime;
  /**
   * The fixing period end time (in years).
   */
  private final double _fixingPeriodEndTime;
  /**
   * The fixing period year fraction (or accrual factor) in the fixing convention.
   */
  private final double _fixingAccrualFactor;
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
   * @param fixingTime Time (in years) up to fixing.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param spread The spread.
   * @param referenceCurrency The reference currency for the FX reset. Not null.
   * @param fxFixingTime The FX fixing date. The notional used for the payment is the FX rate between the reference 
   * currency (RC) and the payment currency (PC): 1 RC = X . PC.
   * @param fxDeliveryTime The spot or delivery date for the FX transaction underlying the FX fixing.
   */
  public CouponIborFxReset(final Currency currency, final double paymentTime, final double paymentYearFraction,
      final double notional, final double fixingTime, final IborIndex index, final double fixingPeriodStartTime,
      final double fixingPeriodEndTime, final double fixingYearFraction, final double spread,
      final Currency referenceCurrency, double fxFixingTime, double fxDeliveryTime) {
    super(currency, paymentTime, paymentYearFraction, notional);
    ArgumentChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    ArgumentChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    ArgumentChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(referenceCurrency, "reference currency");
    ArgumentChecker.isTrue(fixingTime >= 0.0, "fixing time < 0");
    _fixingTime = fixingTime;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingYearFraction;
    _index = index;
    _spread = spread;
    _referenceCurrency = referenceCurrency;
    _fxFixingTime = fxFixingTime;
    _fxDeliveryTime = fxDeliveryTime;
  }

  /**
   * Gets the floating coupon fixing time.
   * @return The fixing time.
   */
  public double getFixingTime() {
    return _fixingTime;
  }

  /**
   * Gets the fixing period start time (in years).
   * @return The fixing period start time.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period end time (in years).
   * @return The fixing period end time.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the accrual factor for the fixing period.
   * @return The accrual factor.
   */
  public double getFixingAccrualFactor() {
    return _fixingAccrualFactor;
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

}
