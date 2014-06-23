/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CouponIborAverageSinglePeriod extends Coupon {

  private final double[] _fixingTime;
  private final IborIndex _index;
  private final double[] _weight;
  private final double[] _fixingPeriodStartTime;
  private final double[] _fixingPeriodEndTime;

  /**
   * Constructor from all details
   * @param currency The payment currency
   * @param paymentTime Time (in years) up to the payment
   * @param paymentAccrualFactor Accrual factor for the coupon payment
   * @param notional Coupon notional
   * @param index Ibor-like index
   * @param fixingTime  Time (in years) up to fixing
   * @param weight The weights
   * @param fixingPeriodStartTime The fixing period start time (in years) of the index
   * @param fixingPeriodEndTime The fixing period end time (in years) of the index
   */
  public CouponIborAverageSinglePeriod(final Currency currency, final double paymentTime, final double paymentAccrualFactor, final double notional, final IborIndex index, final double[] fixingTime,
      final double[] weight, final double[] fixingPeriodStartTime, final double[] fixingPeriodEndTime) {
    super(currency, paymentTime, paymentAccrualFactor, notional);

    final int nDates = fixingTime.length;
    ArgumentChecker.isTrue(nDates == weight.length, "weight length different from fixingTime length");
    ArgumentChecker.isTrue(nDates == fixingPeriodStartTime.length, "fixingPeriodStartTime length different from fixingTime length");
    ArgumentChecker.isTrue(nDates == fixingPeriodEndTime.length, "fixingPeriodEndTime length different from fixingTime length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");

    _fixingTime = Arrays.copyOf(fixingTime, nDates);
    _index = index;
    _weight = Arrays.copyOf(weight, nDates);
    _fixingPeriodStartTime = Arrays.copyOf(fixingPeriodStartTime, nDates);
    _fixingPeriodEndTime = Arrays.copyOf(fixingPeriodEndTime, nDates);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageSinglePeriod(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageSinglePeriod(this);
  }

  @Override
  public CouponIborAverageSinglePeriod withNotional(double notional) {
    return new CouponIborAverageSinglePeriod(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getIndex(), getFixingTime(), getWeight(), getFixingPeriodStartTime(),
        getFixingPeriodEndTime());
  }

  /**
   * Gets the fixingTime.
   * @return the fixingTime
   */
  public double[] getFixingTime() {
    return _fixingTime;
  }

  /**
   * Gets the index.
   * @return the index
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the weight.
   * @return the weight
   */
  public double[] getWeight() {
    return _weight;
  }

  /**
   * Gets the fixingPeriodStartTime.
   * @return the fixingPeriodStartTime
   */
  public double[] getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixingPeriodEndTime.
   * @return the fixingPeriodEndTime
   */
  public double[] getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodEndTime);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartTime);
    result = prime * result + Arrays.hashCode(_fixingTime);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + Arrays.hashCode(_weight);
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
    if (!(obj instanceof CouponIborAverageSinglePeriod)) {
      return false;
    }
    CouponIborAverageSinglePeriod other = (CouponIborAverageSinglePeriod) obj;
    if (!Arrays.equals(_fixingPeriodEndTime, other._fixingPeriodEndTime)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartTime, other._fixingPeriodStartTime)) {
      return false;
    }
    if (!Arrays.equals(_fixingTime, other._fixingTime)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (!Arrays.equals(_weight, other._weight)) {
      return false;
    }
    return true;
  }
}
