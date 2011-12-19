/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.SwapGenerator;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a fixed for ibor-like payments swap. Both legs are in the same currency.
 */
public class SwapFixedIborDefinition extends SwapDefinition {

  /**
   * Constructor of the fixed-ibor swap from its two legs. This constructor is intended to be used when there is an initial floating
   * rate defined in the swap contract - the stream of payments on the floating leg then consists of a {@link CouponFixedDefinition} and
   * then a series of {@link CouponIborDefinition}.
   * @param fixedLeg The fixed leg.
   * @param iborLeg The ibor leg.
   */
  public SwapFixedIborDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityDefinition<? extends PaymentDefinition> iborLeg) {
    super(fixedLeg, iborLeg);
    Validate.isTrue(fixedLeg.getCurrency() == iborLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * Constructor of the fixed-ibor swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param iborLeg The ibor leg.
   */
  public SwapFixedIborDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponIborDefinition iborLeg) {
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
  public static SwapFixedIborDefinition from(final ZonedDateTime settlementDate, final IndexSwap cmsIndex, final double notional, final double fixedRate, final boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(cmsIndex, "CMS index");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(cmsIndex.getCurrency(), settlementDate, cmsIndex.getTenor(), cmsIndex.getFixedLegPeriod(), cmsIndex.getIborIndex()
        .getCalendar(), cmsIndex.getFixedLegDayCount(), cmsIndex.getIborIndex().getBusinessDayConvention(), cmsIndex.getIborIndex().isEndOfMonth(), notional, fixedRate, isPayer);
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(settlementDate, cmsIndex.getTenor(), notional, cmsIndex.getIborIndex(), !isPayer);
    return new SwapFixedIborDefinition(fixedLeg, iborLeg);
  }

  /**
   * Vanilla swap builder from the settlement date, a CMS index and other details of a swap.
   * @param settlementDate The settlement date.
   * @param tenor The swap total tenor.
   * @param generator The swap generator.
   * @param notional The swap notional
   * @param fixedRate The swap fixed rate.
   * @param isPayer The payer flag of the fixed leg.
   * @return The vanilla swap.
   */
  public static SwapFixedIborDefinition from(final ZonedDateTime settlementDate, final Period tenor, final SwapGenerator generator, final double notional, final double fixedRate, 
      final boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(tenor, "Tenor");
    Validate.notNull(generator, "Swap generator");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(generator.getCurrency(), settlementDate, tenor, generator.getFixedLegPeriod(), generator.getCalendar(),
        generator.getFixedLegDayCount(), generator.getIborIndex().getBusinessDayConvention(), generator.getIborIndex().isEndOfMonth(), notional, fixedRate, isPayer);
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(settlementDate, tenor, notional, generator.getIborIndex(), !isPayer);
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

  /**
   * Creates a new swap containing the coupons with start accrual date strictly before the given date.
   * @param trimDate The date.
   * @return The trimmed swap.
   */
  public SwapFixedIborDefinition trimStart(final ZonedDateTime trimDate) {
    AnnuityCouponFixedDefinition fixedLegTrimmed = getFixedLeg().trimStart(trimDate);
    AnnuityCouponIborDefinition iborLegTrimmed = getIborLeg().trimStart(trimDate);
    return new SwapFixedIborDefinition(fixedLegTrimmed, iborLegTrimmed);
  }

  @SuppressWarnings("unchecked")
  @Override
  public FixedCouponSwap<Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    this.getIborLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date, yieldCurveNames);
    return new FixedCouponSwap<Coupon>(fixedLeg, (GenericAnnuity<Coupon>) iborLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public FixedCouponSwap<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] indexDataTS, final String... yieldCurveNames) {
    Validate.notNull(indexDataTS, "index data time series array");
    Validate.isTrue(indexDataTS.length > 0, "index data time series must contain at least one element");
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date, indexDataTS[0], yieldCurveNames);
    return new FixedCouponSwap<Coupon>(fixedLeg, (GenericAnnuity<Coupon>) iborLeg);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitSwapFixedIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapFixedIborDefinition(this);
  }

}
