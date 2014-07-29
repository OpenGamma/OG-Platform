/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.TotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of a total return swap with an underlying bill and a funding leg.
 * The TRS asset leg pays on the termination date ``all-in'' value of the bill.
 */
public class BillTotalReturnSwapDefinition extends TotalReturnSwapDefinition {

  /** The quantity of the bill reference in the TRS. Can be negative or positive. */
  private final double _quantity;

  /**
   * Constructor of the bill total return swap.
   * @param effectiveDate The effective date.
   * @param terminationDate The termination date.
   * @param annuity The funding leg, not null
   * @param bill The bill. Not null.
   * @param quantity The quantity of the bill referenced in the TRS. Can be negative or positive.
   */
  public BillTotalReturnSwapDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime terminationDate,
      final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final BillSecurityDefinition bill, final double quantity) {
    super(effectiveDate, terminationDate, annuity, bill);
    _quantity = quantity;
  }

  /**
   * Gets the bill underlying the TRS.
   * @return The bill.
   */
  @Override
  public BillSecurityDefinition getAsset() {
    return (BillSecurityDefinition) super.getAsset();
  }

  /**
   * Returns the bill quantity.
   * @return The quantity.
   */
  public double getQuantity() {
    return _quantity;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillTotalReturnSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillTotalReturnSwapDefinition(this);
  }

  @Override
  public BillTotalReturnSwap toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries data) {
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date, data);
    BillSecurity bill = getAsset().toDerivative(date, getEffectiveDate());
    return new BillTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bill, _quantity);
  }

  @Override
  public BillTotalReturnSwap toDerivative(final ZonedDateTime date) {
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date);
    BillSecurity bill = getAsset().toDerivative(date, getEffectiveDate());
    return new BillTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, bill, _quantity);
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries data, final String... yieldCurveNames) {
    throw new NotImplementedException("toDerivative with curve name not implemented for Bill TRS.");
  }

  @Override
  public BondTotalReturnSwap toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new NotImplementedException("toDerivative with curve name not implemented for Bill TRS.");
  }

}
