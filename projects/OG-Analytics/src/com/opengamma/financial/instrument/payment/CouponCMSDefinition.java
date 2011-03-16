/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.swap.ZZZSwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Constant Maturity Swap coupon.
 */
public class CouponCMSDefinition extends CouponFloatingDefinition {

  //TODO: add a CMS index (history, ...)
  //TODO: change to a swap skeleton?
  private final ZZZSwapFixedIborDefinition _underlyingSwap;

  /**
   * Constructor of a CMS coupon from all the details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   */
  public CouponCMSDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional,
      ZonedDateTime fixingDate, ZZZSwapFixedIborDefinition underlyingSwap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    Validate.notNull(underlyingSwap, "underlying swap");
    _underlyingSwap = underlyingSwap;
  }

  /**
   * Constructor of a CMS coupon from all the details.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   * @return The CMS coupon.
   */
  public static CouponCMSDefinition from(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional, ZonedDateTime fixingDate,
      ZZZSwapFixedIborDefinition underlyingSwap) {
    Validate.notNull(underlyingSwap, "underlying swap");
    return new CouponCMSDefinition(underlyingSwap.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap);
  }

  /**
   * Builder from a floating coupon and an underlying swap.
   * @param coupon A floating coupon with the details of the coupon to construct.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   * @return The constructed CMS coupon.
   */
  public static CouponCMSDefinition from(CouponFloatingDefinition coupon, ZZZSwapFixedIborDefinition underlyingSwap) {
    Validate.notNull(coupon, "floating coupon");
    Validate.notNull(underlyingSwap, "underlying swap");
    return new CouponCMSDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), underlyingSwap);
  }

  /**
   * Gets the underlyingSwap field.
   * @return the underlyingSwap
   */
  public ZZZSwapFixedIborDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  @Override
  public String toString() {
    return super.toString() + ", Swap = " + _underlyingSwap.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _underlyingSwap.hashCode();
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
    CouponCMSDefinition other = (CouponCMSDefinition) obj;
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

  @Override
  public Payment toDerivative(LocalDate date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate());
    if (isFixed()) { // The CMS coupon has already fixed, it is now a fixed coupon.
      return new CouponFixed(paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), getFixedRate());
    } else { // CMS is not fixed yet, all the details are required.
      final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate());
      FixedCouponSwap<Payment> swap = _underlyingSwap.toDerivative(date, yieldCurveNames);
      //Implementation remark: SwapFixedIbor can not be used as the first coupon may have fixed already and one CouponIbor is now fixed.
      //TODO: Definition has no spread and time version has one: to be standardized.
      return new CouponCMS(paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap);
    }
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitCouponCMS(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponCMS(this);
  }

}
