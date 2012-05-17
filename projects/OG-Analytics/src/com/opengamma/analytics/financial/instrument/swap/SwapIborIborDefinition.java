/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapCouponCoupon;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Ibor+Spread for Ibor+Spread payments swap. Both legs are in the same currency.
 */
public class SwapIborIborDefinition extends SwapDefinition {

  /**
   * Constructor of the ibor-ibor swap from its two legs.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapIborIborDefinition(final AnnuityCouponIborSpreadDefinition firstLeg, final AnnuityCouponIborSpreadDefinition secondLeg) {
    super(firstLeg, secondLeg);
    Validate.isTrue(firstLeg.getCurrency() == secondLeg.getCurrency(), "legs should have the same currency");
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
    return visitor.visitSwapIborIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapIborIborDefinition(this);
  }

  @Override
  public SwapCouponCoupon<Coupon, Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final Annuity<Coupon> firstLeg = getFirstLeg().toDerivative(date, yieldCurveNames);
    final Annuity<Coupon> secondLeg = getSecondLeg().toDerivative(date, yieldCurveNames);
    return new SwapCouponCoupon<Coupon, Coupon>(firstLeg, secondLeg);
  }

  @Override
  public SwapCouponCoupon<Coupon, Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] indexDataTS, final String... yieldCurveNames) {
    Validate.notNull(indexDataTS, "index data time series array");
    Validate.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<Coupon> firstLeg = getFirstLeg().toDerivative(date, indexDataTS[0], yieldCurveNames);
    final Annuity<Coupon> secondLeg = getSecondLeg().toDerivative(date, indexDataTS[1], yieldCurveNames);
    return new SwapCouponCoupon<Coupon, Coupon>(firstLeg, secondLeg);
  }
}
