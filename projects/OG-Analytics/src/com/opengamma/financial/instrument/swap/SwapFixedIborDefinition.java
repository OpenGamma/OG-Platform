/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.CMSIndex;
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
public class SwapFixedIborDefinition extends SwapDefinition<CouponFixedDefinition, CouponIborDefinition> {

  /**
   * Constructor of the fixed-ibor swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param iborLeg The ibor leg.
   */
  public SwapFixedIborDefinition(AnnuityCouponFixedDefinition fixedLeg, AnnuityCouponIborDefinition iborLeg) {
    super(fixedLeg, iborLeg);
    Validate.isTrue(fixedLeg.getCurrency() == iborLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * Vanilla swap builder from the settlement date, a CMS index and other details of a swap.
   * @param settlementDate The settlement date.
   * @param cmsIndex The CMS index from which the swap is constructed.
   * @param notional The swap notional
   * @param fixedRate The swap fixed rate.
   * @param isPayer The payer flag of the fixed leg.
   * @return The vanilla swap.
   */
  public static SwapFixedIborDefinition from(ZonedDateTime settlementDate, CMSIndex cmsIndex, double notional, double fixedRate, boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(cmsIndex, "CMS index");
    AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(cmsIndex.getCurrency(), settlementDate, cmsIndex.getTenor(), cmsIndex.getFixedLegPeriod(), cmsIndex.getIborIndex()
        .getCalendar(), cmsIndex.getFixedLegDayCount(), cmsIndex.getIborIndex().getBusinessDayConvention(), cmsIndex.getIborIndex().isEndOfMonth(), notional, fixedRate, isPayer);
    AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(settlementDate, cmsIndex.getTenor(), notional, cmsIndex.getIborIndex(), !isPayer);
    return new SwapFixedIborDefinition(fixedLeg, iborLeg);
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

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitSwapFixedIborDefinition(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapFixedIborDefinition(this);
  }

}
