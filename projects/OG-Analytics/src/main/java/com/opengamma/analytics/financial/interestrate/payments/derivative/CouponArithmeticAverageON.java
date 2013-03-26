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
  private final double[] _fixingPeriodAccrualFactors;
  /**
   * The interest accrued over the periods already fixed.
   */
  private final double _rateAccrued;
  /**
   * The accrual factor (or year fraction) associated to the remaining fixing period in the Index day count convention.
   */
  private final double _fixingPeriodRemainingAccrualFactor;

  /**
   * Constructor.
   * @param currency The coupon currency.
   * @param paymentTime The coupon payment time.
   * @param paymentYearFraction The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodTimes The times of the remaining fixing. The length is one greater than the number of periods, as it includes accrual start and end.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param rateAccrued The interest accrued over the periods already fixed.
   * @param fixingPeriodRemainingAccrualFactor ??
   */
  private CouponArithmeticAverageON(Currency currency, double paymentTime, double paymentYearFraction, double notional, IndexON index, double[] fixingPeriodTimes, double[] fixingPeriodAccrualFactors,
      double rateAccrued, double fixingPeriodRemainingAccrualFactor) {
    super(currency, paymentTime, "NOT USED", paymentYearFraction, notional);
    _index = index;
    _fixingPeriodTimes = fixingPeriodTimes;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _rateAccrued = rateAccrued;
    _fixingPeriodRemainingAccrualFactor = fixingPeriodRemainingAccrualFactor;
  }

  /**
   * Builder from financial details.
   * @param paymentTime The coupon payment time.
   * @param paymentYearFraction The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodTimes The times of the remaining fixing. The length is one greater than the number of periods, as it includes accrual start and end.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param rateAccrued The interest accrued over the periods already fixed.
   * @return The coupon.
   */
  public static CouponArithmeticAverageON from(double paymentTime, double paymentYearFraction, double notional, IndexON index, double[] fixingPeriodTimes, double[] fixingPeriodAccrualFactors,
      double rateAccrued) {
    ArgumentChecker.notNull(index, "Index");
    double fixingPeriodRemainingAccrualFactor = 0.0;
    for (int loopp = 0; loopp < fixingPeriodAccrualFactors.length; loopp++) {
      fixingPeriodRemainingAccrualFactor += fixingPeriodAccrualFactors[loopp];
    }
    return new CouponArithmeticAverageON(index.getCurrency(), paymentTime, paymentYearFraction, notional, index, fixingPeriodTimes, fixingPeriodAccrualFactors, rateAccrued,
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
  public Coupon withNotional(double notional) {
    return null;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageON(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageON(this);
  }

}
