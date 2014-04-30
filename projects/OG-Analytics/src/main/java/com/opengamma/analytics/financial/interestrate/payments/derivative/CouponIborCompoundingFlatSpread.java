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
 * The one referred in this class is the "Flat Compounding" (not "Compounding" and not "Compounding treating spread as simple interest").
 * The Ibor fixing are compounded over several sub-periods.
 * The amount paid is described in the reference below.
 * The fixing have their own start dates, end dates and accrual factors. In general they are close to the accrual
 * dates used to compute the coupon accrual factors.
 * <p> Reference: Mengle, D. (2009). Alternative compounding methods for over-the-counter derivative transactions. ISDA.
 */
public class CouponIborCompoundingFlatSpread extends Coupon implements DepositIndexCompoundingCoupon<IborIndex> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * All the coupon sub-periods fix on the same index.
   */
  private final IborIndex _index;
  /**
   * The accrual factors (or year fraction) associated to the sub-periods not yet fixed.
   */
  private final double[] _subperiodsAccrualFactors;
  /**
   * The coupon fixing times.
   */
  private final double[] _fixingTimes;
  /**
   * The start times of the fixing periods.
   */
  private final double[] _fixingSubperiodsStartTimes;
  /**
   * The end times of the fixing periods.
   */
  private final double[] _fixingSubperiodsEndTimes;
  /**
   * The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   */
  private final double[] _fixingSubperiodsAccrualFactors;
  /**
   * The compounding periods amounts for the sub-periods already fixed.
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
  public CouponIborCompoundingFlatSpread(final Currency currency, final double paymentTime, final double paymentAccrualFactor, final double notional, final double compoundingPeriodAmountAccumulated,
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
    _subperiodsAccrualFactors = paymentAccrualFactors;
    _fixingTimes = fixingTimes;
    _fixingSubperiodsStartTimes = fixingPeriodStartTimes;
    _fixingSubperiodsEndTimes = fixingPeriodEndTimes;
    _fixingSubperiodsAccrualFactors = fixingPeriodAccrualFactors;
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
  public double[] getSubperiodsAccrualFactors() {
    return _subperiodsAccrualFactors;
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
   * 
   * @deprecated use {@link #getFixingPeriodStartTimes()}.
   */
  @Deprecated
  public double[] getFixingSubperiodsStartTimes() {
    return _fixingSubperiodsStartTimes;
  }

  /**
   * Gets the fixing period start times (in years).
   * @return The times.
   */
  @Override
  public double[] getFixingPeriodStartTimes() {
    return _fixingSubperiodsStartTimes;
  }

  /**
   * Gets the fixing period end times (in years).
   * @return The times.
   * 
   * @deprecated use {@link #getFixingPeriodEndTimes()}.
   */
  @Deprecated
  public double[] getFixingSubperiodsEndTimes() {
    return _fixingSubperiodsEndTimes;
  }

  /**
   * Gets the fixing period start times (in years).
   * @return The times.
   */
  @Override
  public double[] getFixingPeriodEndTimes() {
    return _fixingSubperiodsEndTimes;
  }

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   * @deprecated use {@link #getFixingPeriodAccrualFactors()}.
   */
  @Deprecated
  public double[] getFixingSubperiodsAccrualFactors() {
    return _fixingSubperiodsAccrualFactors;
  }

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   */
  @Override
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingSubperiodsAccrualFactors;
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
    return null; // TODO
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIborCompoundingFlatSpread(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIborCompoundingFlatSpread(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_compoundingPeriodAmountAccumulated);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_fixingSubperiodsAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingSubperiodsEndTimes);
    result = prime * result + Arrays.hashCode(_fixingSubperiodsStartTimes);
    result = prime * result + Arrays.hashCode(_fixingTimes);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_subperiodsAccrualFactors);
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
    CouponIborCompoundingFlatSpread other = (CouponIborCompoundingFlatSpread) obj;
    if (Double.doubleToLongBits(_compoundingPeriodAmountAccumulated) != Double.doubleToLongBits(other._compoundingPeriodAmountAccumulated)) {
      return false;
    }
    if (!Arrays.equals(_fixingSubperiodsAccrualFactors, other._fixingSubperiodsAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingSubperiodsEndTimes, other._fixingSubperiodsEndTimes)) {
      return false;
    }
    if (!Arrays.equals(_fixingSubperiodsStartTimes, other._fixingSubperiodsStartTimes)) {
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
    if (!Arrays.equals(_subperiodsAccrualFactors, other._subperiodsAccrualFactors)) {
      return false;
    }
    return true;
  }

}
