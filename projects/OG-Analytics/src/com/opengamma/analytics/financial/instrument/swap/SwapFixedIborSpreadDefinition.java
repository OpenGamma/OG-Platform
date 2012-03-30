/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwap;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a fixed for Ibor+spread payments swap. Both legs are in the same currency.
 */
public class SwapFixedIborSpreadDefinition extends SwapDefinition {

  /**
   * Constructor of the fixed-ibor swap from its two legs. This constructor is intended to be used when there is an initial floating
   * rate defined in the swap contract - the stream of payments on the floating leg then consists of a {@link CouponFixedDefinition} and
   * then a series of {@link CouponIborSpreadDefinition}.
   * @param fixedLeg The fixed leg.
   * @param iborLeg The ibor leg.
   */
  public SwapFixedIborSpreadDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityDefinition<? extends PaymentDefinition> iborLeg) {
    super(fixedLeg, iborLeg);
    Validate.isTrue(fixedLeg.getCurrency() == iborLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * Constructor of the fixed-ibor swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param iborLeg The ibor leg.
   */
  public SwapFixedIborSpreadDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponIborDefinition iborLeg) {
    super(fixedLeg, iborLeg);
    Validate.isTrue(fixedLeg.getCurrency() == iborLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * Vanilla swap builder from the settlement date, a swap generator and other details of a swap.
   * @param settlementDate The settlement date.
   * @param maturityDate The swap maturity date.
   * @param generator The swap generator.
   * @param notionalFixed The fixed leg notional.
   * @param notionalIbor The ibor leg notional.
   * @param fixedRate The swap fixed rate.
   * @param spread The Ibor leg spread.
   * @param isPayer The payer flag of the fixed leg.
   * @return The vanilla swap.
   */
  public static SwapFixedIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final GeneratorSwap generator, final double notionalFixed,
      final double notionalIbor, final double fixedRate, final double spread, final boolean isPayer) {
    Validate.notNull(settlementDate, "Settlement date");
    Validate.notNull(maturityDate, "Maturity date");
    Validate.notNull(generator, "Swap generator");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(generator.getCurrency(), settlementDate, maturityDate, generator.getFixedLegPeriod(), generator.getCalendar(),
        generator.getFixedLegDayCount(), generator.getIborIndex().getBusinessDayConvention(), generator.getIborIndex().isEndOfMonth(), notionalFixed, fixedRate, isPayer);
    final AnnuityCouponIborSpreadDefinition iborLeg = AnnuityCouponIborSpreadDefinition.from(settlementDate, maturityDate, notionalIbor, generator.getIborIndex(), spread, !isPayer);
    return new SwapFixedIborDefinition(fixedLeg, iborLeg);
  }

  /**
   * Swap with spread builder from the all the details on the fixed and ibor leg. The currency is the currency of the Ibor index.
   * @param settlementDate The settlement date.
   * @param maturityDate The swap maturity date.
   * @param fixedLegPeriod The payment period for the fixed leg.
   * @param fixedLegDayCount The fixed leg day count.
   * @param fixedLegBusinessDayConvention The fixed leg business day convention.
   * @param fixedLegEOM The fixed leg end-of-month rule application.
   * @param fixedLegNotional The fixed leg notional.
   * @param fixedLegRate The fixed leg rate.
   * @param iborLegPeriod The Ibor leg payment period.
   * @param iborLegDayCount The Ibor leg day count convention.
   * @param iborLegBusinessDayConvention The Ibor leg business day convention.
   * @param iborLegEOM The Ibor leg end-of-month.
   * @param iborLegNotional The Ibor leg notional.
   * @param iborIndex The Ibor index.
   * @param iborLegSpread The Ibor leg spread.
   * @param isPayer The payer flag for the fixed leg.
   * @return The swap.
   */
  public static SwapFixedIborSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period fixedLegPeriod, final DayCount fixedLegDayCount,
      final BusinessDayConvention fixedLegBusinessDayConvention, final boolean fixedLegEOM, final double fixedLegNotional, final double fixedLegRate, final Period iborLegPeriod,
      final DayCount iborLegDayCount, final BusinessDayConvention iborLegBusinessDayConvention, final boolean iborLegEOM, final double iborLegNotional, final IborIndex iborIndex,
      final double iborLegSpread, final boolean isPayer) {
    Validate.notNull(iborIndex, "Ibor index");
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(iborIndex.getCurrency(), settlementDate, maturityDate, fixedLegPeriod, iborIndex.getCalendar(), fixedLegDayCount,
        fixedLegBusinessDayConvention, fixedLegEOM, fixedLegNotional, fixedLegRate, isPayer);
    final AnnuityCouponIborSpreadDefinition iborLeg = AnnuityCouponIborSpreadDefinition.from(settlementDate, maturityDate, iborLegPeriod, iborLegNotional, iborIndex, !isPayer,
        iborLegBusinessDayConvention, iborLegEOM, iborLegDayCount, iborLegSpread);
    return new SwapFixedIborSpreadDefinition(fixedLeg, iborLeg);
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
  public AnnuityDefinition<? extends PaymentDefinition> getIborLeg() {
    return getSecondLeg();
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
  public FixedCouponSwap<Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Payment> iborLeg = this.getIborLeg().toDerivative(date, yieldCurveNames);
    return new FixedCouponSwap<Coupon>(fixedLeg, (GenericAnnuity<Coupon>) iborLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public FixedCouponSwap<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] indexDataTS, final String... yieldCurveNames) {
    Validate.notNull(indexDataTS, "index data time series array");
    Validate.isTrue(indexDataTS.length > 0, "index data time series must contain at least one element");
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Payment> iborLeg = this.getIborLeg().toDerivative(date, indexDataTS[0], yieldCurveNames);
    return new FixedCouponSwap<Coupon>(fixedLeg, (GenericAnnuity<Coupon>) iborLeg);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitSwapFixedIborSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapFixedIborSpreadDefinition(this);
  }
}
