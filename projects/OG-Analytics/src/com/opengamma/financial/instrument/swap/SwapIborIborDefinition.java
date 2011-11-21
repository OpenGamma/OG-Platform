/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Ibor for Ibor+spread payments swap. Both legs are in the same currency.
 */
public class SwapIborIborDefinition extends SwapDefinition {

  /**
   * Constructor of the ibor-ibor swap from its two legs. The first leg has no spread, the second leg has a spread.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapIborIborDefinition(final AnnuityCouponIborSpreadDefinition firstLeg, final AnnuityCouponIborSpreadDefinition secondLeg) {
    super(firstLeg, secondLeg);
    Validate.isTrue(firstLeg.getNthPayment(0).getSpread() == 0.0, "spread of first leg should be 0");
    Validate.isTrue(firstLeg.getCurrency() == secondLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * The Ibor-leg with no spread.
   * @return The annuity.
   */
  public AnnuityCouponIborSpreadDefinition getLegWithoutSpread() {
    return (AnnuityCouponIborSpreadDefinition) getFirstLeg();
  }

  /**
   * The Ibor-leg with the spread.
   * @return The annuity.
   */
  public AnnuityCouponIborSpreadDefinition getLegWithSpread() {
    return (AnnuityCouponIborSpreadDefinition) getSecondLeg();
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitSwapIborIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapIborIborDefinition(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public TenorSwap<Payment> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final GenericAnnuity<Payment> fixedLeg = (GenericAnnuity<Payment>) getLegWithoutSpread().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<Payment> iborLeg = (GenericAnnuity<Payment>) getLegWithSpread().toDerivative(date, yieldCurveNames);
    return new TenorSwap<Payment>(fixedLeg, iborLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public TenorSwap<Payment> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] indexDataTS, final String... yieldCurveNames) {
    Validate.notNull(indexDataTS, "index data time series array");
    Validate.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final GenericAnnuity<Payment> fixedLeg = (GenericAnnuity<Payment>) getLegWithoutSpread().toDerivative(date, indexDataTS[0], yieldCurveNames);
    final GenericAnnuity<Payment> iborLeg = (GenericAnnuity<Payment>) getLegWithSpread().toDerivative(date, indexDataTS[1], yieldCurveNames);
    return new TenorSwap<Payment>(fixedLeg, iborLeg);
  }
}
