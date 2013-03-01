/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates).
 * FIXME: Class under construction, don't use yet.
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
  private final Double[] _fixingPeriodAccrualFactors;
  /**
   * The notional augmented by the interest accrued over the periods already fixed.
   */
  private final double _notionalAccrued; // Interest accrued?
  /**
   * The accrual factor (or year fraction) associated to the total fixing period in the Index day count convention.
   */
  private final double _fixingPeriodTotalAccrualFactor; // required?

  /**
   * Constructor.
   * @param currency The coupon currency.
   * @param paymentTime The coupon payment time.
   * @param paymentYearFraction The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodTimes The times of the remaining fixing. The length is one greater than the number of periods, as it includes accrual start and end.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param notionalAccrued ??
   * @param fixingPeriodTotalAccrualFactor ??
   */
  private CouponArithmeticAverageON(Currency currency, double paymentTime, double paymentYearFraction, double notional, IndexON index, double[] fixingPeriodTimes, Double[] fixingPeriodAccrualFactors,
      double notionalAccrued, double fixingPeriodTotalAccrualFactor) {
    super(currency, paymentTime, "NOT USED", paymentYearFraction, notional);
    _index = index;
    _fixingPeriodTimes = fixingPeriodTimes;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _notionalAccrued = notionalAccrued;
    _fixingPeriodTotalAccrualFactor = fixingPeriodTotalAccrualFactor;
  }

  /**
   * Builder from financial details.
   * @param paymentTime The coupon payment time.
   * @param paymentYearFraction The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodTimes The times of the remaining fixing. The length is one greater than the number of periods, as it includes accrual start and end.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param notionalAccrued ??
   * @return The coupon.
   */
  public static CouponArithmeticAverageON from(double paymentTime, double paymentYearFraction, double notional, IndexON index, double[] fixingPeriodTimes, Double[] fixingPeriodAccrualFactors,
      double notionalAccrued) {
    ArgumentChecker.notNull(index, "Index");
    return new CouponArithmeticAverageON(index.getCurrency(), paymentTime, paymentYearFraction, notional, index, fixingPeriodTimes, fixingPeriodAccrualFactors, notionalAccrued, paymentYearFraction);
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
  public Double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * Gets the notionalAccrued field.
   * @return the notionalAccrued
   */
  public double getNotionalAccrued() {
    return _notionalAccrued;
  }

  /**
   * Gets the fixingPeriodTotalAccrualFactor field.
   * @return the fixingPeriodTotalAccrualFactor
   */
  public double getFixingPeriodTotalAccrualFactor() {
    return _fixingPeriodTotalAccrualFactor;
  }

  @Override
  public Coupon withNotional(double notional) {
    return null;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return null;
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return null;
  }

}
