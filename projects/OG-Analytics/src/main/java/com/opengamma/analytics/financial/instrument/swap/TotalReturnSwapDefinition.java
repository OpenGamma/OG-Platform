/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of a total return swap with an underlying asset and a funding leg.
 */
public abstract class TotalReturnSwapDefinition implements InstrumentDefinitionWithData<TotalReturnSwap, ZonedDateTimeDoubleTimeSeries> {

  /** The funding leg */
  private final AnnuityDefinition<? extends PaymentDefinition> _fundingLeg;
  /** The asset */
  private final InstrumentDefinition<?> _asset;
  /** The effective date; the date at which the TRS becomes effective and the cash flows of the asset are paid. */
  private final ZonedDateTime _effectiveDate;
  /** The termination date; the date at which the TRS terminates. */
  private final ZonedDateTime _terminationDate;

  /**
   * Default constructor.
   * @param effectiveDate The effective date.
   * @param terminationDate The termination date.
   * @param fundingLeg The funding leg, not null
   * @param asset The asset, not null
   */
  public TotalReturnSwapDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime terminationDate,
      final AnnuityDefinition<? extends PaymentDefinition> fundingLeg, final InstrumentDefinition<?> asset) {
    ArgumentChecker.notNull(fundingLeg, "fundingLeg");
    ArgumentChecker.notNull(asset, "asset");
    ArgumentChecker.notNull(effectiveDate, "effective date");
    ArgumentChecker.notNull(terminationDate, "termination date");
    _fundingLeg = fundingLeg;
    _asset = asset;
    _effectiveDate = effectiveDate;
    _terminationDate = terminationDate;
  }

  /**
   * Default constructor. 
   * The effective date is the start accrual date of the first annuity payment; the first annuity payment must be a CouponDefinition.
   * The termination date is the date of the last annuity payment.
   * @param fundingLeg The funding leg, not null
   * @param asset The asset, not null
   * @deprecated Use the constructor with explicit effective date and termnation date.
   */
  @Deprecated
  public TotalReturnSwapDefinition(final AnnuityDefinition<? extends PaymentDefinition> fundingLeg, final InstrumentDefinition<?> asset) {
    ArgumentChecker.notNull(fundingLeg, "fundingLeg");
    ArgumentChecker.notNull(asset, "asset");
    _fundingLeg = fundingLeg;
    _asset = asset;
    PaymentDefinition payment0 = fundingLeg.getNthPayment(0);
    ArgumentChecker.isTrue(payment0 instanceof CouponDefinition, "first payment must be of type CouponDefinition");
    _effectiveDate = ((CouponDefinition) payment0).getAccrualStartDate();
    _terminationDate = fundingLeg.getNthPayment(fundingLeg.getNumberOfPayments() - 1).getPaymentDate();
  }

  /**
   * Gets the funding leg.
   * @return The funding leg
   */
  public AnnuityDefinition<? extends PaymentDefinition> getFundingLeg() {
    return _fundingLeg;
  }

  /**
   * Gets the asset.
   * @return The asset
   */
  public InstrumentDefinition<?> getAsset() {
    return _asset;
  }

  /**
   * Returns the TRS effective date.
   * @return The date.
   */
  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  /**
   * Returns the TRS termination date.
   * @return The date.
   */
  public ZonedDateTime getTerminationDate() {
    return _terminationDate;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitTotalReturnSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitTotalReturnSwapDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _asset.hashCode();
    result = prime * result + _fundingLeg.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TotalReturnSwapDefinition)) {
      return false;
    }
    final TotalReturnSwapDefinition other = (TotalReturnSwapDefinition) obj;
    if (!ObjectUtils.equals(_asset, other._asset)) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingLeg, other._fundingLeg)) {
      return false;
    }
    return true;
  }

}
