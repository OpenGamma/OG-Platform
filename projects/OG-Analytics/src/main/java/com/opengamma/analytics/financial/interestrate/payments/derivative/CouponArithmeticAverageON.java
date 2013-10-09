/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates).
 */
public final class CouponArithmeticAverageON extends Coupon {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency. Not null.
   */
  private final IndexON _index;
  /**
   * The times of the fixing periods. The length is one greater than the number of periods, as it includes accrual start and end.
   */
  private final double[] _fixingPeriodTimes;
  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;
  /**
   * The interest accrued over the periods already fixed multiplied by the accrual factors, i.e. the sum (\delta_i r_i).
   */
  private final double _rateAccrued;
  /**
   * The accrual factor (or year fraction) associated to the remaining fixing period in the Index day count convention.
   */
  private final double _fixingPeriodRemainingAccrualFactor;

  // TODO: Implement the rate cut-off mechanism (the two last periods use the same fixing)

  /**
   * Constructor.
   * @param currency The coupon currency.
   * @param paymentTime The coupon payment time.
   * @param paymentAccrualFactor The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodTimes The times of the remaining fixing. The length is one greater than the number of periods, as it includes accrual start and end.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param rateAccrued The interest accrued over the periods already fixed.
   * @param fixingPeriodRemainingAccrualFactor ??
   */
  private CouponArithmeticAverageON(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexON index, final double[] fixingPeriodTimes,
      final double[] fixingPeriodAccrualFactors,
      final double rateAccrued, final double fixingPeriodRemainingAccrualFactor) {
    super(currency, paymentTime, paymentYearFraction, notional);
    _index = index;
    _fixingPeriodTimes = fixingPeriodTimes;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _rateAccrued = rateAccrued;
    _fixingPeriodRemainingAccrualFactor = fixingPeriodRemainingAccrualFactor;
  }

  /**
   * Builder from financial details.
   * @param paymentTime The coupon payment time.
   * @param paymentAccrualFactor The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodTimes The times of the remaining fixing. The length is one greater than the number of periods, as it includes accrual start and end.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param rateAccrued The interest accrued over the periods already fixed.
   * @return The coupon.
   */
  public static CouponArithmeticAverageON from(final double paymentTime, final double paymentAccrualFactor, final double notional, final IndexON index, final double[] fixingPeriodTimes,
      final double[] fixingPeriodAccrualFactors,
      final double rateAccrued) {
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(fixingPeriodTimes, "Fixing Times");
    ArgumentChecker.notNull(fixingPeriodAccrualFactors, "Accrual Factors");
    double fixingPeriodRemainingAccrualFactor = 0.0;
    for (final double fixingPeriodAccrualFactor : fixingPeriodAccrualFactors) {
      fixingPeriodRemainingAccrualFactor += fixingPeriodAccrualFactor;
    }
    return new CouponArithmeticAverageON(index.getCurrency(), paymentTime, paymentAccrualFactor, notional, index, fixingPeriodTimes, fixingPeriodAccrualFactors, rateAccrued,
        fixingPeriodRemainingAccrualFactor);
  }

  /**
   * Gets the index.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the times of the fixing periods.
   * @return The times.
   */
  public double[] getFixingPeriodTimes() {
    return _fixingPeriodTimes;
  }

  /**
   * Gets the fixingPeriodAccrualFactors field.
   * @return the fixingPeriodAccrualFactors
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * Gets the notionalAccrued field.
   * @return the notionalAccrued
   */
  public double getRateAccrued() {
    return _rateAccrued;
  }

  /**
   * Gets the fixingPeriodTotalAccrualFactor field.
   * @return the fixingPeriodTotalAccrualFactor
   */
  public double getFixingPeriodRemainingAccrualFactor() {
    return _fixingPeriodRemainingAccrualFactor;
  }

  @Override
  public Coupon withNotional(final double notional) {
    return null; // TODO
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageON(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageON(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodRemainingAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_fixingPeriodTimes);
    result = prime * result + _index.hashCode();
    temp = Double.doubleToLongBits(_rateAccrued);
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
    final CouponArithmeticAverageON other = (CouponArithmeticAverageON) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodRemainingAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodRemainingAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodTimes, other._fixingPeriodTimes)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_rateAccrued) != Double.doubleToLongBits(other._rateAccrued)) {
      return false;
    }
    return true;
  }

}
