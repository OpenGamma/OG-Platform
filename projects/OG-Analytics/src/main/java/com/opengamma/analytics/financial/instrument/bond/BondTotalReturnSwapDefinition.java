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
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of a total return swap with an underlying fixed coupon bond and a funding leg.
 */
public class BondTotalReturnSwapDefinition extends TotalReturnSwapDefinition {

  //  /** The bond quantity; the number of bonds refered in the TRS */
  //  private final long _quantity;

  /**
   * Constructor of the bond total return swap.
   * @param effectiveDate The effective date.
   * @param terminationDate The termination date.
   * @param annuity The funding leg, not null
   * @param bond The bond, not null
   */
  public BondTotalReturnSwapDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime terminationDate,
      final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final BondFixedSecurityDefinition bond) {
    super(effectiveDate, terminationDate, annuity, bond);
  }

  /**
   * @param annuity The funding leg, not null
   * @param bond The bond, not null
   * @deprecated Use the constructor with effective date and termination date.
   */
  @Deprecated
  public BondTotalReturnSwapDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final BondSecurityDefinition<? extends PaymentDefinition, ? extends CouponDefinition> bond) {
    super(annuity, bond);
  }

  /**
   * Gets the fixed bond underlying the TRS.
   * @return The bond.
   */
  @Override
  public BondFixedSecurityDefinition getAsset() {
    return (BondFixedSecurityDefinition) super.getAsset();
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
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date, data);
    BondFixedSecurity bond = getAsset().toDerivative(date, getEffectiveDate());
    return new BondTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date) {
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date);
    BondFixedSecurity bond = getAsset().toDerivative(date, getEffectiveDate());
    return new BondTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond);
  }

}
