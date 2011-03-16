/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed for ibor-like payments swap. Both legs are in the same currency.
 */
public class ZZZSwapFixedIborDefinition extends ZZZSwapDefinition<CouponFixedDefinition, CouponIborDefinition> {

  /**
   * Constructor of the fixed-ibor swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param iborLeg The ibor leg.
   */
  public ZZZSwapFixedIborDefinition(AnnuityCouponFixedDefinition fixedLeg, AnnuityCouponIborDefinition iborLeg) {
    super(fixedLeg, iborLeg);
    Validate.isTrue(fixedLeg.getCurrency() == iborLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * The fixed leg of the swap.
   * @return Fixed leg.
   */
  public AnnuityCouponFixedDefinition getFixedLeg() {
    return (AnnuityCouponFixedDefinition) getFirstLeg();
  }

  /**
   * The Ibor leg of the swap.
   * @return Ibor leg.
   */
  public AnnuityCouponIborDefinition getIborLeg() {
    return (AnnuityCouponIborDefinition) getSecondLeg();
  }

  /**
   * Return the currency of the swap. 
   * @return The currency.
   */
  public Currency getCurrency() {
    return getFixedLeg().getCurrency();
  }

  @SuppressWarnings("unchecked")
  @Override
  public FixedCouponSwap<Payment> toDerivative(LocalDate date, String... yieldCurveNames) {
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Payment> iborLeg = this.getIborLeg().toDerivative(date, yieldCurveNames);
    return new FixedCouponSwap<Payment>(fixedLeg, (GenericAnnuity<Payment>) iborLeg);
  }

}
