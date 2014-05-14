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
 * The one referred in this class is the "Compounding treating spread as simple interest" (not "Compounding" and not "Flat Compounding").
 * The Ibor fixing are compounded over several sub-periods.
 * The amount paid is described in the reference below.
 * The fixing have their own start dates, end dates and accrual factors. In general they are close to the accrual
 * dates used to compute the coupon accrual factors.
 * <p> Reference: Mengle, D. (2009). Alternative compounding methods for over-the-counter derivative transactions. ISDA.
 */
public class CouponIborCompoundingSimpleSpread extends Coupon implements DepositIndexCompoundingCoupon<IborIndex> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * All the coupon sub-periods fix on the same index.
   */
  private final IborIndex _index;
  /**
   * The accrual factors (or year fraction) associated to the sub-periods not yet fixed.
   */
  private final double[] _periodsAccrualFactors;
  /**
   * The coupon fixing times.
   */
  private final double[] _fixingTimes;
  /**
   * The start times of the fixing periods.
   */
  private final double[] _fixingPeriodsStartTimes;
  /**
   * The end times of the fixing periods.
   */
  private final double[] _fixingPeriodsEndTimes;
  /**
   * The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodsAccrualFactors;
  /**
   * The compounding periods amounts for the sub-periods already fixed, i.e. N * Prod(1 + R * d).
   */
  private final double _compoundingPeriodAmountAccumulated;
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
   * @param compoundingPeriodAmountAccumulated The compounding periods amounts accumulated for the sub-periods already fixed.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods not yet fixed.
   * @param fixingTimes The start times of the fixing periods.
   * @param fixingPeriodStartTimes The start times of the fixing periods.
   * @param fixingPeriodEndTimes The end times of the fixing periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   * @param spread The spread paid above the Ibor rate.
   */
  public CouponIborCompoundingSimpleSpread(final Currency currency, final double paymentTime, final double paymentAccrualFactor, final double notional,
      final double compoundingPeriodAmountAccumulated,
      final IborIndex index, final double[] paymentAccrualFactors, final double[] fixingTimes, final double[] fixingPeriodStartTimes, final double[] fixingPeriodEndTimes,
      final double[] fixingPeriodAccrualFactors, final double spread) {
    super(currency, paymentTime, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(fixingTimes, "Fixing times");
    ArgumentChecker.isTrue(fixingTimes.length == fixingPeriodStartTimes.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.isTrue(fixingTimes.length == fixingPeriodEndTimes.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.isTrue(fixingTimes.length == fixingPeriodAccrualFactors.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.isTrue(fixingTimes.length == paymentAccrualFactors.length, "Fixing times and fixing period should have same length");
    ArgumentChecker.notNull(index, "Ibor index");
    _index = index;
    _periodsAccrualFactors = paymentAccrualFactors;
    _fixingTimes = fixingTimes;
    _fixingPeriodsStartTimes = fixingPeriodStartTimes;
    _fixingPeriodsEndTimes = fixingPeriodEndTimes;
    _fixingPeriodsAccrualFactors = fixingPeriodAccrualFactors;
    _spread = spread;
    _compoundingPeriodAmountAccumulated = compoundingPeriodAmountAccumulated;
  }

  /**
   * Returns the compounding periods amounts for the sub-periods already fixed.
   * @return The amount.
   */
  public double getCompoundingPeriodAmountAccumulated() {
    return _compoundingPeriodAmountAccumulated;
  }

  /**
   * Returns the Ibor index underlying the coupon.
   * @return The index.
   */
  @Override
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Returns the payment accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getPaymentPeriodAccrualFactors() {
    return _periodsAccrualFactors;
  }

  /**
   * Returns the fixing times for the different remaining periods.
   * @return The times.
   */
  @Override
  public double[] getFixingTimes() {
    return _fixingTimes;
  }

  /**
   * Gets the fixing period start times (in years).
   * @return The times.
   */
  @Override
  public double[] getFixingPeriodStartTimes() {
    return _fixingPeriodsStartTimes;
  }

  /**
   * Gets the fixing period start times (in years).
   * @return The times.
   */
  @Override
  public double[] getFixingPeriodEndTimes() {
    return _fixingPeriodsEndTimes;
  }

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   */
  @Override
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodsAccrualFactors;
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
    throw new UnsupportedOperationException("withNotional method not supported for coupon Ibor with compounding.");
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIborCompoundingSimpleSpread(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIborCompoundingSimpleSpread(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_compoundingPeriodAmountAccumulated);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_fixingPeriodsAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodsEndTimes);
    result = prime * result + Arrays.hashCode(_fixingPeriodsStartTimes);
    result = prime * result + Arrays.hashCode(_fixingTimes);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_periodsAccrualFactors);
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
    CouponIborCompoundingSimpleSpread other = (CouponIborCompoundingSimpleSpread) obj;
    if (Double.doubleToLongBits(_compoundingPeriodAmountAccumulated) != Double.doubleToLongBits(other._compoundingPeriodAmountAccumulated)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodsAccrualFactors, other._fixingPeriodsAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodsEndTimes, other._fixingPeriodsEndTimes)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodsStartTimes, other._fixingPeriodsStartTimes)) {
      return false;
    }
    if (!Arrays.equals(_fixingTimes, other._fixingTimes)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (!Arrays.equals(_periodsAccrualFactors, other._periodsAccrualFactors)) {
      return false;
    }
    return true;
  }

}
