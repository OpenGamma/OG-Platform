/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed for Ibor+spread payments swap. Both legs are in the same currency.
 */
public class SwapFixedIborSpreadDefinition extends SwapDefinition<CouponFixedDefinition, CouponIborSpreadDefinition> {

  /**
   * Constructor of the fixed-ibor swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param iborLeg The ibor leg.
   */
  public SwapFixedIborSpreadDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponIborSpreadDefinition iborLeg) {
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
  public AnnuityCouponIborSpreadDefinition getIborLeg() {
    return (AnnuityCouponIborSpreadDefinition) getSecondLeg();
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
  public FixedCouponSwap<Payment> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Payment> iborLeg = this.getIborLeg().toDerivative(date, yieldCurveNames);
    return new FixedCouponSwap<Payment>(fixedLeg, (GenericAnnuity<Payment>) iborLeg);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitSwapFixedIborSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapFixedIborSpreadDefinition(this);
  }
}
