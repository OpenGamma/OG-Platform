/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a Ibor+Spread for Ibor+Spread payments swap. The two legs can be in different currencies.
 */
public class SwapXCcyIborIborDefinition extends SwapDefinition {

  /**
   * Constructor of the ibor-ibor swap from its two legs.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapXCcyIborIborDefinition(final AnnuityDefinition<CouponDefinition> firstLeg, final AnnuityDefinition<CouponDefinition> secondLeg) {
    super(firstLeg, secondLeg);
  }

  /**
   * Builder from the settlement date and a generator. The legs have different notionals.
   * The notionals are paid on the settlement date and final payment date of each leg.
   * @param settlementDate The settlement date.
   * @param tenor The swap tenor.
   * @param generator The Ibor/Ibor swap generator.
   * @param notional1 The first leg notional.
   * @param notional2 The second leg notional.
   * @param spread The spread to be applied to the first leg.
   * @param isPayer The payer flag for the first leg.
   * @param calendar1 The holiday calendar for the first leg.
   * @param calendar2 The holiday calendar for the second leg.
   * @return The swap.
   */
  public static SwapXCcyIborIborDefinition from(final ZonedDateTime settlementDate, final Period tenor, final GeneratorSwapXCcyIborIbor generator, final double notional1, final double notional2,
      final double spread, final boolean isPayer, final Calendar calendar1, final Calendar calendar2) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenor, "Tenor");
    ArgumentChecker.notNull(generator, "Swap generator");
    // TODO: create a mechanism for the simultaneous payments on both legs, i.e. joint calendar
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return from(settlementDate, maturityDate, generator, notional1, notional2, spread, 0.0, isPayer);
  }

  /**
   * Builder from the settlement date and a generator. The legs have different notionals.
   * The notionals are paid on the settlement date and final payment date of each leg.
   * @param settlementDate The settlement date.
   * @param maturityDate The swap maturity date.
   * @param generator The Ibor/Ibor swap generator.
   * @param notional1 The first leg notional.
   * @param notional2 The second leg notional.
   * @param spread1 The spread to be applied to the first leg.
   * @param spread2 The spread to be applied to the second leg.
   * @param isPayer The payer flag for the first leg.
   * @return The swap.
   */
  public static SwapXCcyIborIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final GeneratorSwapXCcyIborIbor generator, final double notional1,
      final double notional2, final double spread1, final double spread2, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(generator, "Swap generator");
    // TODO: create a mechanism for the simultaneous payments on both legs, i.e. joint calendar
    final AnnuityDefinition<CouponDefinition> firstLegNotional = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(settlementDate, maturityDate,
        notional1, spread1, generator.getIborIndex1(), isPayer, generator.getCalendar1(), StubType.SHORT_START, 0, true, true);
    final AnnuityDefinition<CouponDefinition> secondLegNotional = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(settlementDate, maturityDate,
        notional2, spread2, generator.getIborIndex2(), !isPayer, generator.getCalendar2(), StubType.SHORT_START, 0, true, true);
    return new SwapXCcyIborIborDefinition(firstLegNotional, secondLegNotional);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapXCcyIborIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapXCcyIborIborDefinition(this);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS, final String... yieldCurveNames) {
    ArgumentChecker.notNull(indexDataTS, "index data time series array");
    ArgumentChecker.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    ArgumentChecker.isTrue(yieldCurveNames.length >= 4, "Should have at least 4 curve names");
    final String[] firstLegCurveNames = new String[] {yieldCurveNames[0], yieldCurveNames[1] };
    final String[] secondLegCurveNames = new String[] {yieldCurveNames[2], yieldCurveNames[3] };
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date, indexDataTS[0], firstLegCurveNames);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date, indexDataTS[1], secondLegCurveNames);
    return new Swap<>(firstLeg, secondLeg);
  }

  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date) {
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date);
    return new Swap<>(firstLeg, secondLeg);
  }

  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS) {
    ArgumentChecker.notNull(indexDataTS, "index data time series array");
    ArgumentChecker.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date, indexDataTS[0]);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date, indexDataTS[1]);
    return new Swap<>(firstLeg, secondLeg);
  }
}
