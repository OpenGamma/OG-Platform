/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.TotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BondTotalReturnSwapDefinition extends TotalReturnSwapDefinition {

  /**
   * @param annuity The funding leg, not null
   * @param bond The bond, not null
   */
  public BondTotalReturnSwapDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final BondSecurityDefinition<? extends PaymentDefinition, ? extends CouponDefinition> bond) {
    super(annuity, bond);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondTotalReturnSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondTotalReturnSwapDefinition(this);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries data, final String... yieldCurveNames) {
    return toDerivative(date, data);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries data) {
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date, data);
    final BondSecurity<? extends Payment, ? extends Coupon> bond = (BondSecurity<? extends Payment, ? extends Coupon>) getAsset().toDerivative(date);
    return new BondTotalReturnSwap(fundingLeg, bond);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date) {
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date);
    final BondSecurity<? extends Payment, ? extends Coupon> bond = (BondSecurity<? extends Payment, ? extends Coupon>) getAsset().toDerivative(date);
    return new BondTotalReturnSwap(fundingLeg, bond);
  }

}
