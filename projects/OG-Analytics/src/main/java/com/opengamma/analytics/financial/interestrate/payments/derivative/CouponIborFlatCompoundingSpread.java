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
 * 
 */
public class CouponIborFlatCompoundingSpread extends CouponIborAverageCompounding {

  private final double _spread;

  /**
   * Constructor from all details
   * @param currency The payment currency
   * @param paymentTime Time (in years) up to the payment
   * @param paymentAccrualFactor Accrual factor for the coupon payment
   * @param notional Coupon notional
   * @param paymentAccrualFactors Accrual factors associated to the sub-periods
   * @param index Ibor-like index
   * @param fixingTime  Time (in years) up to fixing
   * @param weight The weights
   * @param fixingPeriodStartTime The fixing period start time (in years) of the index
   * @param fixingPeriodEndTime The fixing period end time (in years) of the index
   * @param fixingPeriodAccrualFactor The accrual factors of fixing periods
   * @param amountAccrued The interest amount accrued over the periods already fixed.
   * @param spread The spread
   */
  public CouponIborFlatCompoundingSpread(final Currency currency, final double paymentTime, final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors,
      final IborIndex index, final double[][] fixingTime, final double[][] weight, final double[][] fixingPeriodStartTime, final double[][] fixingPeriodEndTime,
      final double[][] fixingPeriodAccrualFactor, final double amountAccrued, final double spread) {
    super(currency, paymentTime, paymentAccrualFactor, notional, paymentAccrualFactors, index, fixingTime, weight, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor,
        amountAccrued);
    _spread = spread;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFlatCompoundingSpread(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFlatCompoundingSpread(this);
  }

  @Override
  public CouponIborFlatCompoundingSpread withNotional(final double notional) {
    return new CouponIborFlatCompoundingSpread(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPaymentAccrualFactors(), getIndex(), getFixingTime(), getWeight(),
        getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingPeriodAccrualFactor(), getAmountAccrued(), getSpread());
  }

  /**
   * Gets the spread.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
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
    if (!(obj instanceof CouponIborFlatCompoundingSpread)) {
      return false;
    }
    CouponIborFlatCompoundingSpread other = (CouponIborFlatCompoundingSpread) obj;
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

}
