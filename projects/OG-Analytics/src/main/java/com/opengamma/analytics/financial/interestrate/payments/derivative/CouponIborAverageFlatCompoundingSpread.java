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
public class CouponIborAverageFlatCompoundingSpread extends Coupon {

  private final double[][] _fixingTime;
  private final IborIndex _index;
  private final double[][] _weight;
  private final double[][] _fixingPeriodStartTime;
  private final double[][] _fixingPeriodEndTime;

  private final double[][] _fixingPeriodAccrualFactor;
  private final double[] _paymentAccrualFactors;
  private final double _amountAccrued;
  private final double _rateFixed;

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
   * @param rateFixed The interest rate fixed already
   * @param spread The spread
   */
  public CouponIborAverageFlatCompoundingSpread(final Currency currency, final double paymentTime, final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors,
      final IborIndex index, final double[][] fixingTime, final double[][] weight, final double[][] fixingPeriodStartTime, final double[][] fixingPeriodEndTime,
      final double[][] fixingPeriodAccrualFactor, final double amountAccrued, final double rateFixed, final double spread) {
    super(currency, paymentTime, paymentAccrualFactor, notional);

    final int nPeriods = fixingTime.length;
    final int[] nDates = new int[nPeriods]; //number of fixing dates per period
    ArgumentChecker.isTrue(nPeriods == paymentAccrualFactors.length, "paymentAccrualFactors length different from fixingTime length");
    ArgumentChecker.isTrue(nPeriods == weight.length, "weight length different from fixingTime length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodStartTime.length, "fixingPeriodStartDates length different from fixingTime length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodEndTime.length, "fixingPeriodEndDates length different from fixingTime length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodAccrualFactor.length, "fixingPeriodAccrualFactor length different from fixingTime length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    for (int i = 0; i < nPeriods; ++i) {
      nDates[i] = fixingTime[i].length;
      ArgumentChecker.isTrue(nDates[i] == weight[i].length, "{}-th array: weight length different from fixingTime length", i);
      ArgumentChecker.isTrue(nDates[i] == fixingPeriodStartTime[i].length, "{}-th array: fixingPeriodStartDates length different from fixingTime length", i);
      ArgumentChecker.isTrue(nDates[i] == fixingPeriodEndTime[i].length, "{}-th array: fixingPeriodEndDates length different from fixingTime length", i);
      ArgumentChecker.isTrue(nDates[i] == fixingPeriodAccrualFactor[i].length, "{}-th array: fixingPeriodAccrualFactor length different from fixingTime length", i);
    }

    _weight = new double[nPeriods][];
    _fixingTime = new double[nPeriods][];
    _fixingPeriodStartTime = new double[nPeriods][];
    _fixingPeriodEndTime = new double[nPeriods][];
    _fixingPeriodAccrualFactor = new double[nPeriods][];

    _index = index;
    _paymentAccrualFactors = Arrays.copyOf(paymentAccrualFactors, nPeriods);
    _amountAccrued = amountAccrued;
    _rateFixed = rateFixed;

    for (int i = 0; i < nPeriods; ++i) {
      _weight[i] = Arrays.copyOf(weight[i], nDates[i]);
      _fixingTime[i] = Arrays.copyOf(fixingTime[i], nDates[i]);
      _fixingPeriodStartTime[i] = Arrays.copyOf(fixingPeriodStartTime[i], nDates[i]);
      _fixingPeriodEndTime[i] = Arrays.copyOf(fixingPeriodEndTime[i], nDates[i]);
      _fixingPeriodAccrualFactor[i] = Arrays.copyOf(fixingPeriodAccrualFactor[i], nDates[i]);
    }
    _spread = spread;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageFlatCompoundingSpread(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageFlatCompoundingSpread(this);
  }

  @Override
  public CouponIborAverageFlatCompoundingSpread withNotional(final double notional) {
    return new CouponIborAverageFlatCompoundingSpread(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPaymentAccrualFactors(), getIndex(), getFixingTime(), getWeight(),
        getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingPeriodAccrualFactor(), getAmountAccrued(), getRateFixed(), getSpread());
  }

  /**
   * Gets the index.
   * @return the index
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the paymentAccrualFactors.
   * @return the paymentAccrualFactors
   */
  public double[] getPaymentAccrualFactors() {
    return _paymentAccrualFactors;
  }

  /**
   * Gets the weight.
   * @return the weight
   */
  public double[][] getWeight() {
    return _weight;
  }

  /**
   * Gets the fixingPeriodStartTime.
   * @return the fixingPeriodStartTime
   */
  public double[][] getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixingPeriodEndTime.
   * @return the fixingPeriodEndTime
   */
  public double[][] getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the fixingTime.
   * @return the fixingTime
   */
  public double[][] getFixingTime() {
    return _fixingTime;
  }

  /**
   * Gets the fixingPeriodAccrualFactor.
   * @return the fixingPeriodAccrualFactor
   */
  public double[][] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Returns the accrued amount for the period already fixed.
   * @return The amount accrued.
   */
  public double getAmountAccrued() {
    return _amountAccrued;
  }

  /**
   * Returns the rate for the period already fixed.
   * @return The rate fixed.
   */
  public double getRateFixed() {
    return _rateFixed;
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
    temp = Double.doubleToLongBits(_amountAccrued);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.deepHashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.deepHashCode(_fixingPeriodEndTime);
    result = prime * result + Arrays.deepHashCode(_fixingPeriodStartTime);
    result = prime * result + Arrays.deepHashCode(_fixingTime);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + Arrays.hashCode(_paymentAccrualFactors);
    temp = Double.doubleToLongBits(_rateFixed);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.deepHashCode(_weight);
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
    if (!(obj instanceof CouponIborAverageFlatCompoundingSpread)) {
      return false;
    }
    CouponIborAverageFlatCompoundingSpread other = (CouponIborAverageFlatCompoundingSpread) obj;
    if (Double.doubleToLongBits(_amountAccrued) != Double.doubleToLongBits(other._amountAccrued)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodEndTime, other._fixingPeriodEndTime)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodStartTime, other._fixingPeriodStartTime)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingTime, other._fixingTime)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (!Arrays.equals(_paymentAccrualFactors, other._paymentAccrualFactors)) {
      return false;
    }
    if (Double.doubleToLongBits(_rateFixed) != Double.doubleToLongBits(other._rateFixed)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (!Arrays.deepEquals(_weight, other._weight)) {
      return false;
    }
    return true;
  }

}
