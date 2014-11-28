/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
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
 * The TRS asset leg pays all the cash-flows of the bond (coupons) paid by the bond issuer to an holder of the bond between the 
 * effective date and the termination date of the TRS. On the termination date, the total return payer also pays the 
 * ``all-in'' value of the bond, i.e. the dirty value.
 */
public class BondTotalReturnSwapDefinition extends TotalReturnSwapDefinition {

  /** The quantity of the bond reference in the TRS. Can be negative or positive. */
  private final double _quantity;

  /**
   * Constructor of the bond total return swap.
   * @param effectiveDate The effective date.
   * @param terminationDate The termination date.
   * @param annuity The funding leg, not null
   * @param bond The fixed coupon bond. Not null.
   * @param quantity The quantity of the bond reference in the TRS. Can be negative or positive.
   */
  public BondTotalReturnSwapDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime terminationDate,
      final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final BondFixedSecurityDefinition bond, final double quantity) {
    super(effectiveDate, terminationDate, annuity, bond);
    _quantity = quantity;
  }

  /**
   * Gets the fixed bond underlying the TRS.
   * @return The bond.
   */
  @Override
  public BondFixedSecurityDefinition getAsset() {
    return (BondFixedSecurityDefinition) super.getAsset();
  }

  /**
   * Returns the bond quantity.
   * @return The quantity.
   */
  public double getQuantity() {
    return _quantity;
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
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries data) {
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date, data);
    BondFixedSecurity bond = getAsset().toDerivative(date, getEffectiveDate());
    return new BondTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond, _quantity);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date) {
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date);
    BondFixedSecurity bond = getAsset().toDerivative(date, getEffectiveDate());
    return new BondTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bond, _quantity);
  }

}
