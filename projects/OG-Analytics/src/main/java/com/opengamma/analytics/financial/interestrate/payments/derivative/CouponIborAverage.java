/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an average Ibor-like floating coupon.
 */

public class CouponIborAverage extends CouponFloating {

  /**
   * The first Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index1;
  /**
   * The second Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index2;
  /**
   * The weight for the first index.
   */
  private final double _weight1;
  /**
   * The weight of the second index.
   */
  private final double _weight2;
  /**
   * The fixing period start time (in years) of the first index.
   */
  private final double _fixingPeriodStartTime1;
  /**
   * The fixing period end time (in years) of the first index.
   */
  private final double _fixingPeriodEndTime1;
  /**
   * The fixing period year fraction (or accrual factor) of the first index in the fixing convention.
   */
  private final double _fixingAccrualFactor1;

  /**
   * The fixing period start time (in years) of the second index.
   */
  private final double _fixingPeriodStartTime2;
  /**
   * The fixing period end time (in years) of the second index.
   */
  private final double _fixingPeriodEndTime2;
  /**
   * The fixing period year fraction (or accrual factor) of the second index in the fixing convention.
   */
  private final double _fixingAccrualFactor2;

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index1 The first Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime1 The fixing period start time (in years) of the first index.
   * @param fixingPeriodEndTime1 The fixing period end time (in years) of the first index.
   * @param fixingYearFraction1 The year fraction (or accrual factor) for the fixing period of the first index.
   * @param index2 The second Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime2 The fixing period start time (in years) of the second index.
   * @param fixingPeriodEndTime2 The fixing period end time (in years) of the second index.
   * @param fixingYearFraction2 The year fraction (or accrual factor) for the fixing period of the second index.
   * @param weight1 The weight of the first index.
   * @param weight2 The weight of the second index.
   */
  public CouponIborAverage(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime,
      final IborIndex index1, final double fixingPeriodStartTime1, final double fixingPeriodEndTime1, final double fixingYearFraction1,
      final IborIndex index2, final double fixingPeriodStartTime2, final double fixingPeriodEndTime2, final double fixingYearFraction2,
      final double weight1, final double weight2) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.isTrue(fixingPeriodStartTime1 >= fixingTime, "fixing period start < fixing time");
    _fixingPeriodStartTime1 = fixingPeriodStartTime1;
    ArgumentChecker.isTrue(fixingPeriodEndTime1 >= fixingPeriodStartTime1, "fixing period end < fixing period start");
    _fixingPeriodEndTime1 = fixingPeriodEndTime1;
    ArgumentChecker.isTrue(fixingYearFraction1 >= 0, "forward year fraction < 0");
    _fixingAccrualFactor1 = fixingYearFraction1;
    ArgumentChecker.notNull(index1, "Index");
    ArgumentChecker.isTrue(currency.equals(index1.getCurrency()), "Index currency incompatible with coupon currency");
    _index1 = index1;
    _weight1 = weight1;
    ArgumentChecker.isTrue(fixingPeriodStartTime2 >= fixingTime, "fixing period start < fixing time");
    _fixingPeriodStartTime2 = fixingPeriodStartTime2;
    ArgumentChecker.isTrue(fixingPeriodEndTime2 >= fixingPeriodStartTime2, "fixing period end < fixing period start");
    _fixingPeriodEndTime2 = fixingPeriodEndTime2;
    ArgumentChecker.isTrue(fixingYearFraction2 >= 0, "forward year fraction < 0");
    _fixingAccrualFactor2 = fixingYearFraction2;
    ArgumentChecker.notNull(index2, "Index");
    ArgumentChecker.isTrue(currency.equals(index2.getCurrency()), "Index currency incompatible with coupon currency");
    _index2 = index2;
    _weight2 = weight2;
  }

  /**
   * Gets the first Ibor index of the instrument.
   * @return The first index.
   */
  public IborIndex getIndex1() {
    return _index1;
  }

  /**
   * Gets the second Ibor index of the instrument.
   * @return The second index.
   */
  public IborIndex getIndex2() {
    return _index2;
  }

  /**
   * Gets the weight of the first index.
   * @return The first weight.
   */
  public double getWeight1() {
    return _weight1;
  }

  /**
   * Gets the weight of the second index.
   * @return The second weight.
   */
  public double getWeight2() {
    return _weight2;
  }

  /**
   * Gets the fixing period start time (in years) of the first index.
   * @return The fixing period start time of the first index.
   */
  public double getFixingPeriodStartTime1() {
    return _fixingPeriodStartTime1;
  }

  /**
   * Gets the fixing period end time (in years) of the first index.
   * @return The fixing period end time of the first index.
   */
  public double getFixingPeriodEndTime1() {
    return _fixingPeriodEndTime1;
  }

  /**
   * Gets the accrual factor for the fixing period of the first index.
   * @return The accrual factor of the first index.
   */
  public double getFixingAccrualFactor1() {
    return _fixingAccrualFactor1;
  }

  /**
   * Gets the fixing period start time (in years) of the second index.
   * @return The fixing period start time of the second index.
   */
  public double getFixingPeriodStartTime2() {
    return _fixingPeriodStartTime2;
  }

  /**
   * Gets the fixing period end time (in years )of the second index.
   * @return The fixing period end time of the second index.
   */
  public double getFixingPeriodEndTime2() {
    return _fixingPeriodEndTime2;
  }

  /**
   * Gets the accrual factor for the fixing period of the second index.
   * @return The accrual factor of the second index.
   */
  public double getFixingAccrualFactor2() {
    return _fixingAccrualFactor2;
  }

  @Override
  public CouponIborAverage withNotional(final double notional) {
    return new CouponIborAverage(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _index1, getFixingPeriodStartTime1(),
        getFixingPeriodEndTime1(),
        getFixingAccrualFactor1(), _index2, getFixingPeriodStartTime2(),
        getFixingPeriodEndTime2(),
        getFixingAccrualFactor2(), getWeight1(), getWeight2());
  }

  @Override
  public String toString() {
    return "CouponIborAverage [_fixingPeriodStartTime1=" + _fixingPeriodStartTime1 + ", _fixingPeriodEndTime1=" + _fixingPeriodEndTime1 + ", _fixingAccrualFactor1=" + _fixingAccrualFactor1 +
        ", _fixingPeriodStartTime2=" + _fixingPeriodStartTime2 + ", _fixingPeriodEndTime2=" + _fixingPeriodEndTime2 + ", _fixingAccrualFactor2=" + _fixingAccrualFactor2 + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingAccrualFactor1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingAccrualFactor2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodEndTime1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodEndTime2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_index1 == null) ? 0 : _index1.hashCode());
    result = prime * result + ((_index2 == null) ? 0 : _index2.hashCode());
    temp = Double.doubleToLongBits(_weight1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_weight2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CouponIborAverage other = (CouponIborAverage) obj;
    if (Double.doubleToLongBits(_fixingAccrualFactor1) != Double.doubleToLongBits(other._fixingAccrualFactor1)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingAccrualFactor2) != Double.doubleToLongBits(other._fixingAccrualFactor2)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodEndTime1) != Double.doubleToLongBits(other._fixingPeriodEndTime1)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodEndTime2) != Double.doubleToLongBits(other._fixingPeriodEndTime2)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime1) != Double.doubleToLongBits(other._fixingPeriodStartTime1)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime2) != Double.doubleToLongBits(other._fixingPeriodStartTime2)) {
      return false;
    }
    if (_index1 == null) {
      if (other._index1 != null) {
        return false;
      }
    } else if (!_index1.equals(other._index1)) {
      return false;
    }
    if (_index2 == null) {
      if (other._index2 != null) {
        return false;
      }
    } else if (!_index2.equals(other._index2)) {
      return false;
    }
    if (Double.doubleToLongBits(_weight1) != Double.doubleToLongBits(other._weight1)) {
      return false;
    }
    if (Double.doubleToLongBits(_weight2) != Double.doubleToLongBits(other._weight2)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIborAverage(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIborAverage(this);
  }

}
