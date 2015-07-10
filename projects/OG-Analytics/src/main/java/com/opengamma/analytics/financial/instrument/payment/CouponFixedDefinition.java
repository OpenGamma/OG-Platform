/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed payment coupon.
 */
public class CouponFixedDefinition extends CouponDefinition {

  /**
   * The fixed rate of the fixed coupon.
   */
  private final double _rate;
  /**
   * The amount to be paid by the fixed coupon (=getNotional() * _rate * getPaymentYearFraction())
   */
  private final double _amount;

  /**
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param rate Fixed rate.
   */
  public CouponFixedDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentYearFraction, final double notional, final double rate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    _rate = rate;
    _amount = notional * rate * paymentYearFraction;
  }

  /**
   * Fixed coupon constructor from a coupon and the fixed rate.
   * @param coupon Underlying coupon.
   * @param rate Fixed rate.
   */
  public CouponFixedDefinition(final CouponDefinition coupon, final double rate) {
    super(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional());
    _rate = rate;
    _amount = coupon.getNotional() * rate * coupon.getPaymentYearFraction();
  }

  /**
   * Static constructor for a fixed coupon definition.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param rate Fixed rate.
   * @return The fixed coupon definition
   */
  public static CouponFixedDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final double rate) {
    return new CouponFixedDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, rate);
  }

  public static CouponFixedDefinition from(final CouponFloatingDefinition floatingCoupon, final double fixedRate) {
    return new CouponFixedDefinition(floatingCoupon.getCurrency(), floatingCoupon.getPaymentDate(), floatingCoupon.getAccrualStartDate(),
        floatingCoupon.getAccrualEndDate(), floatingCoupon.getPaymentYearFraction(), floatingCoupon.getNotional(), fixedRate);
  }

  public static CouponFixedDefinition from(final CouponIborSpreadDefinition floatingCoupon, final double fixedRate) {
    return new CouponFixedDefinition(floatingCoupon.getCurrency(), floatingCoupon.getPaymentDate(), floatingCoupon.getAccrualStartDate(),
        floatingCoupon.getAccrualEndDate(), floatingCoupon.getPaymentYearFraction(), floatingCoupon.getNotional(), fixedRate + floatingCoupon.getSpread());
  }

  /**
   * Build a fixed coupon from a start date, tenor and deposit generator.
   * @param startDate The coupon start date.
   * @param tenor The coupon tenor. The end date is used both for the end accrual and the payment date.
   * @param generator The deposit generator with relevant conventions.
   * @param notional Coupon notional.
   * @param fixedRate The coupon fixed rate.
   * @return The coupon.
   */
  public static CouponFixedDefinition from(final ZonedDateTime startDate, final Period tenor, final GeneratorDeposit generator, final double notional,
      final double fixedRate) {
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, generator.getBusinessDayConvention(), generator.getCalendar(),
        generator.isEndOfMonth());
    final double paymentYearFraction = generator.getDayCount().getDayCountFraction(startDate, endDate, generator.getCalendar());
    return new CouponFixedDefinition(generator.getCurrency(), endDate, startDate, endDate, paymentYearFraction, notional, fixedRate);
  }

  /**
   * Gets the fixed rate.
   * @return The rate.
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Gets the payment amount.
   * @return The amount.
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Creates a new coupon with all the details the same except the notional which is the one provided.
   * @param notional The new notional.
   * @return The coupon.
   */
  public CouponFixedDefinition withNotional(final double notional) {
    return new CouponFixedDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, _rate);
  }

  /**
   * Creates a new coupon with all the details the same except the rate which is the one provided.
   * @param rate The new rate.
   * @return The coupon.
   */
  public CouponFixedDefinition withRate(final double rate) {
    return new CouponFixedDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), getNotional(), rate);
  }

  @Override
  public String toString() {
    return super.toString() + " *Fixed coupon* Rate = " + _rate + ", Amount = " + _amount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
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
    final CouponFixedDefinition other = (CouponFixedDefinition) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

  @Override
  public CouponFixed toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date {} is after payment date {}", date, getPaymentDate()); // Required: reference date <= payment date
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getRate(), getAccrualStartDate(), getAccrualEndDate());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedDefinition(this);
  }

}
