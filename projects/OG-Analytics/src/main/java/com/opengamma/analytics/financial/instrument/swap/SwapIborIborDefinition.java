/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a Ibor+Spread for Ibor+Spread payments swap. The two legs should be in the same currency.
 */
public class SwapIborIborDefinition extends SwapDefinition {

  /**
   * Constructor of the ibor-ibor swap from its two legs.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapIborIborDefinition(final AnnuityCouponIborSpreadDefinition firstLeg, final AnnuityCouponIborSpreadDefinition secondLeg) {
    super(firstLeg, secondLeg);
    ArgumentChecker.isTrue(firstLeg.getCurrency() == secondLeg.getCurrency(), "legs should have the same currency");
  }

  public static SwapIborIborDefinition from(final AnnuityCouponIborSpreadDefinition firstLeg, final AnnuityCouponIborSpreadDefinition secondLeg) {
    return new SwapIborIborDefinition(firstLeg, secondLeg);
  }

  public static SwapIborIborDefinition from(final AnnuityCouponIborDefinition firstLeg, final AnnuityCouponIborSpreadDefinition secondLeg) {
    return new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(firstLeg), secondLeg);
  }

  public static SwapIborIborDefinition from(final AnnuityCouponIborSpreadDefinition firstLeg, final AnnuityCouponIborDefinition secondLeg) {
    return new SwapIborIborDefinition(firstLeg, AnnuityCouponIborSpreadDefinition.from(secondLeg));
  }

  public static SwapIborIborDefinition from(final AnnuityCouponIborDefinition firstLeg, final AnnuityCouponIborDefinition secondLeg) {
    return new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(firstLeg), AnnuityCouponIborSpreadDefinition.from(secondLeg));
  }

  /**
   * Builder from the settlement date and a generator. Both legs have the same notional.
   * @param settlementDate The settlement date.
   * @param tenor The swap tenor.
   * @param generator The Ibor/Ibor swap generator.
   * @param notional The swap notional.
   * @param spread The spread to be applied to the first leg.
   * @param isPayer The payer flag for the first leg.
   * @return The swap.
   */
  public static SwapIborIborDefinition from(final ZonedDateTime settlementDate, final Period tenor, final GeneratorSwapIborIbor generator, final double notional, final double spread,
      final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenor, "Tenor");
    ArgumentChecker.notNull(generator, "Swap generator");
    final AnnuityCouponIborSpreadDefinition firstLeg = AnnuityCouponIborSpreadDefinition.from(settlementDate, tenor, notional, generator.getIborIndex1(), spread, isPayer, generator.getCalendar1());
    final AnnuityCouponIborSpreadDefinition secondLeg = AnnuityCouponIborSpreadDefinition.from(settlementDate, tenor, notional, generator.getIborIndex2(), 0.0, !isPayer, generator.getCalendar2());
    return new SwapIborIborDefinition(firstLeg, secondLeg);
  }

  /**
   * The Ibor-leg with no spread.
   * @return The annuity.
   */
  @Override
  public AnnuityCouponIborSpreadDefinition getFirstLeg() {
    return (AnnuityCouponIborSpreadDefinition) super.getFirstLeg();
  }

  /**
   * The Ibor-leg with the spread.
   * @return The annuity.
   */
  @Override
  public AnnuityCouponIborSpreadDefinition getSecondLeg() {
    return (AnnuityCouponIborSpreadDefinition) super.getSecondLeg();
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapIborIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapIborIborDefinition(this);
  }

  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date) {
    final Annuity<Coupon> firstLeg = getFirstLeg().toDerivative(date);
    final Annuity<Coupon> secondLeg = getSecondLeg().toDerivative(date);
    return new Swap<>(firstLeg, secondLeg);
  }

  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS) {
    ArgumentChecker.notNull(indexDataTS, "index data time series array");
    ArgumentChecker.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<Coupon> firstLeg = getFirstLeg().toDerivative(date, indexDataTS[0]);
    final Annuity<Coupon> secondLeg = getSecondLeg().toDerivative(date, indexDataTS[1]);
    return new Swap<>(firstLeg, secondLeg);
  }
}
