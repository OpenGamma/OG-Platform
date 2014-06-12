/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Ibor-like coupon with compounding and spread. There are three ISDA versions of compounding with spread.
 * The one referred in this class is the "compounding" (not "Flat Compounding" and not "Compounding treating spread as simple interest").
 * The Ibor fixing are compounded over several sub-periods.
 * The amount paid is equal to
 * $$
 * \begin{equation*}
 * \left(\prod_{i=1}^n (1+\delta_i r_i + s) \right)-1
 * \end{equation*}
 * $$
 * where the $\delta_i$ are the accrual factors of the sub periods, the $r_i$ the fixing for the same periods and $s$ the common spread.
 * The fixing have their own start dates, end dates and accrual factors. In general they are close to the accrual
 * dates used to compute the coupon accrual factors.
 * <p> Reference: Mengle, D. (2009). Alternative compounding methods for over-the-counter derivative transactions. ISDA.
 */
public class CouponIborCompoundingSpread extends Coupon implements DepositIndexCompoundingCoupon<IborIndex> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * All the coupon sub-periods fix on the same index.
   */
  private final IborIndex _index;
  /**
   * The accrual factors (or year fraction) associated to the sub-periods not yet fixed.
   */
  private final double[] _paymentAccrualFactors;
  /**
   * The coupon fixing times.
   */
  private final double[] _fixingTimes;
  /**
   * The start times of the fixing periods.
   */
  private final double[] _fixingPeriodStartTimes;
  /**
   * The end times of the fixing periods.
   */
  private final double[] _fixingPeriodEndTimes;
  /**
   * The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;
  /**
   * The notional with the interest already fixed accrued, i.e. \prod_{i=1}^j (1+\delta_i r_i) where j is the number of fixed sub-periods.
   */
  private final double _notionalAccrued;
  /**
   * The spread paid above the Ibor rate.
   */
  private final double _spread;

  /**
   * Constructor.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentAccrualFactor The year fraction (or accrual factor) for the coupon payment.
   * @param notional The coupon notional.
   * @param notionalAccrued The notional with the interest already fixed accrued.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods not yet fixed.
   * @param fixingTimes The start times of the fixing periods.
   * @param fixingPeriodStartTimes The start times of the fixing periods.
   * @param fixingPeriodEndTimes The end times of the fixing periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   * @param spread The spread paid above the Ibor rate.
   */
  public CouponIborCompoundingSpread(final Currency currency, final double paymentTime, final double paymentAccrualFactor, final double notional, final double notionalAccrued,
      final IborIndex index, final double[] paymentAccrualFactors, final double[] fixingTimes, final double[] fixingPeriodStartTimes, final double[] fixingPeriodEndTimes,
      final double[] fixingPeriodAccrualFactors, final double spread) {
    super(currency, paymentTime, paymentAccrualFactor, notional);
    ArgumentChecker.isTrue(fixingTimes.length == fixingPeriodStartTimes.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.isTrue(fixingTimes.length == fixingPeriodEndTimes.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.isTrue(fixingTimes.length == fixingPeriodAccrualFactors.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.isTrue(fixingTimes.length == paymentAccrualFactors.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.notNull(index, "Ibor index");
    _notionalAccrued = notionalAccrued;
    _index = index;
    _paymentAccrualFactors = paymentAccrualFactors;
    _fixingTimes = fixingTimes;
    _fixingPeriodStartTimes = fixingPeriodStartTimes;
    _fixingPeriodEndTimes = fixingPeriodEndTimes;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _spread = spread;
  }

  /**
   * Returns the The notional with the interest already fixed accrued.
   * @return The notional accrued.
   */
  public double getNotionalAccrued() {
    return _notionalAccrued;
  }

  /**
   * Returns the Ibor index underlying the coupon.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Returns the payment accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getPaymentAccrualFactors() {
    return _paymentAccrualFactors;
  }

  /**
   * Returns the fixing times for the different remaining periods.
   * @return The times.
   */
  public double[] getFixingTimes() {
    return _fixingTimes;
  }

  /**
   * Gets the fixing period start times (in years).
   * @return The times.
   */
  public double[] getFixingPeriodStartTimes() {
    return _fixingPeriodStartTimes;
  }

  /**
   * Gets the fixing period end times (in years).
   * @return The times.
   */
  public double[] getFixingPeriodEndTimes() {
    return _fixingPeriodEndTimes;
  }

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * Returns the spread.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }

  @Override
  public Coupon withNotional(final double notional) {
    return new CouponIborCompoundingSpread(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, _notionalAccrued, _index, _paymentAccrualFactors, _fixingTimes,
        _fixingPeriodStartTimes, _fixingPeriodEndTimes, _fixingPeriodAccrualFactors, _spread);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIborCompoundingSpread(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIborCompoundingSpread(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndTimes);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartTimes);
    result = prime * result + Arrays.hashCode(_fixingTimes);
    result = prime * result + _index.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notionalAccrued);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_paymentAccrualFactors);
    temp = Double.doubleToLongBits(_spread);
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
    final CouponIborCompoundingSpread other = (CouponIborCompoundingSpread) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodEndTimes, other._fixingPeriodEndTimes)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartTimes, other._fixingPeriodStartTimes)) {
      return false;
    }
    if (!Arrays.equals(_fixingTimes, other._fixingTimes)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_notionalAccrued) != Double.doubleToLongBits(other._notionalAccrued)) {
      return false;
    }
    if (!Arrays.equals(_paymentAccrualFactors, other._paymentAccrualFactors)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

}
