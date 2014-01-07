/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a generic Cross currency swap. The two legs should be in different currencies.
 * @deprecated Remove the class when the curve names are removed from instruments (provider in production).
 */
// TODO: This class was created only to deal with curve name of XCcy swaps. It should be deleted as soon as the curve names are removed from instrument description.
@Deprecated
public class SwapXCcyDefinition extends SwapDefinition {

  /**
   * Constructor of the ibor-ibor swap from its two legs. The currency of hte two curves should be different.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapXCcyDefinition(final AnnuityDefinition<? extends PaymentDefinition> firstLeg, final AnnuityDefinition<? extends PaymentDefinition> secondLeg) {
    super(firstLeg, secondLeg);
    ArgumentChecker.isTrue(firstLeg.getCurrency() != secondLeg.getCurrency(), "Currencies should be different");
  }

  /**
   * {@inheritDoc}
   * Convert to derivative version.
   * @param date The system date.
   * @param yieldCurveNames The yield curve names. The first two curves are used for the first leg. The next two are used for the second leg.
   * @return The derivative.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.isTrue(yieldCurveNames.length >= 4, "Should have at least 4 curve names");
    final String[] firstLegCurveNames = new String[] {yieldCurveNames[0], yieldCurveNames[1] };
    final String[] secondLegCurveNames = new String[] {yieldCurveNames[2], yieldCurveNames[3] };
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date, firstLegCurveNames);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date, secondLegCurveNames);
    return new Swap<>(firstLeg, secondLeg);
  }

  /**
   * {@inheritDoc}
   * Convert to derivative version.
   * @param date The system date.
   * @param yieldCurveNames The yield curve names. The first two curves are used for the first leg. The next two are used for the second leg.
   * @return The derivative.
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

  /**
   * {@inheritDoc}
   * Convert to derivative version.
   * @param date The system date.
   * @return The derivative.
   */
  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date) {
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date);
    return new Swap<>(firstLeg, secondLeg);
  }

  /**
   * {@inheritDoc}
   * Convert to derivative version.
   * @param date The system date.
   * @return The derivative.
   */
  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS) {
    ArgumentChecker.notNull(indexDataTS, "index data time series array");
    ArgumentChecker.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date, indexDataTS[0]);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date, indexDataTS[1]);
    return new Swap<>(firstLeg, secondLeg);
  }
}
