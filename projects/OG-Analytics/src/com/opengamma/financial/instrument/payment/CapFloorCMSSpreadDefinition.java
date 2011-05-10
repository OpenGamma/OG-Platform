/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;

/**
 * Class describing a caplet/floorlet on CMS spread. The notional is positive for long the option and negative for short the option.
 * The pay-off of the instrument is a cap/floor on the difference between the first CMS rate and the second CMS rate. 
 * Both swaps underlying the CMS need to have the same settlement date.
 */
public class CapFloorCMSSpreadDefinition extends CouponFloatingDefinition implements CapFloor {

  /**
   * The swap underlying the first CMS.
   */
  private final SwapFixedIborDefinition _underlyingSwap1;
  /**
   * The index associated to the first CMS.
   */
  private final CMSIndex _cmsIndex1;
  /**
   * The swap underlying the second CMS.
   */
  private final SwapFixedIborDefinition _underlyingSwap2;
  /**
   * The index associated to the second CMS.
   */
  private final CMSIndex _cmsIndex2;
  /**
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * Cap/floor CMS spread constructor from all the details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap1 The swap underlying the first CMS.
   * @param cmsIndex1 The index associated to the first CMS.
   * @param underlyingSwap2 The swap underlying the second CMS.
   * @param cmsIndex2 The index associated to the second CMS.
   * @param strike The strike
   * @param isCap The cap (true) /floor (false) flag.
   */
  public CapFloorCMSSpreadDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional,
      ZonedDateTime fixingDate, SwapFixedIborDefinition underlyingSwap1, CMSIndex cmsIndex1, SwapFixedIborDefinition underlyingSwap2, CMSIndex cmsIndex2, double strike, boolean isCap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    Validate.notNull(underlyingSwap1, "underlying swap");
    Validate.notNull(cmsIndex1, "CMS index");
    Validate.notNull(underlyingSwap2, "underlying swap");
    Validate.notNull(cmsIndex2, "CMS index");
    Validate.notNull(underlyingSwap1.getFixedLeg().getNthPayment(0).getAccrualStartDate() == underlyingSwap2.getFixedLeg().getNthPayment(0).getAccrualStartDate(), "identic settlement date");
    _underlyingSwap1 = underlyingSwap1;
    _cmsIndex1 = cmsIndex1;
    _underlyingSwap2 = underlyingSwap2;
    _cmsIndex2 = cmsIndex2;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Gets the underlying swap associated to the first CMS.
   * @return The underlying swap
   */
  public SwapFixedIborDefinition getUnderlyingSwap1() {
    return _underlyingSwap1;
  }

  /**
   * Gets the index associated to the first CMS.
   * @return The CMS index.
   */
  public CMSIndex getCmsIndex1() {
    return _cmsIndex1;
  }

  /**
   * Gets the underlying swap associated to the second CMS.
   * @return The underlying swap
   */
  public SwapFixedIborDefinition getUnderlyingSwap2() {
    return _underlyingSwap2;
  }

  /**
   * Gets the index associated to the second CMS.
   * @return The CMS index.
   */
  public CMSIndex getCmsIndex2() {
    return _cmsIndex2;
  }

  @Override
  public double geStrike() {
    return _strike;
  }

  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  /**
   * The "fixing" is the difference between the first and the second CMS rates fixings.
   */
  public double payOff(double fixing) {
    double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public Payment toDerivative(LocalDate date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    // First curve used for discounting. If two curves, the same forward is used for both swaps; 
    // if more than two curves, the second is used for the forward of the first swap and the third for the forward of the second swap.
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate());
    if (isFixed()) { // The CMS coupon has already fixed, it is now a fixed coupon.
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), payOff(getFixedRate()));
    } else { // CMS spread is not fixed yet, all the details are required.
      final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate());
      final double settlementTime = actAct.getDayCountFraction(zonedDate, _underlyingSwap1.getFixedLeg().getNthPayment(0).getAccrualStartDate());
      FixedCouponSwap<Payment> swap1 = _underlyingSwap1.toDerivative(date, yieldCurveNames);
      String[] yieldCurveNames2;
      if (yieldCurveNames.length == 2) {
        yieldCurveNames2 = yieldCurveNames;
      } else {
        yieldCurveNames2 = new String[] {yieldCurveNames[0], yieldCurveNames[2]};
      }
      FixedCouponSwap<Payment> swap2 = _underlyingSwap2.toDerivative(date, yieldCurveNames2);
      return new CapFloorCMSSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap1, _cmsIndex1, swap2, _cmsIndex2, settlementTime, _strike, _isCap,
          fundingCurveName);
    }

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _cmsIndex1.hashCode();
    result = prime * result + _cmsIndex2.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingSwap1.hashCode();
    result = prime * result + _underlyingSwap2.hashCode();
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
    CapFloorCMSSpreadDefinition other = (CapFloorCMSSpreadDefinition) obj;
    if (!ObjectUtils.equals(_cmsIndex1, other._cmsIndex1)) {
      return false;
    }
    if (!ObjectUtils.equals(_cmsIndex2, other._cmsIndex2)) {
      return false;
    }
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap1, other._underlyingSwap1)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap2, other._underlyingSwap2)) {
      return false;
    }
    return true;
  }

}
