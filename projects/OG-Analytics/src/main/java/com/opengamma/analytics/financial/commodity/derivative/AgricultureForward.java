/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Metal  commodity derivative
 */
public class AgricultureForward extends CommodityForward {

  /**
   * @param expiry time (in years as a double) until the date-time at which the forward expires
   * @param underlying identifier of the underlying commodity
   * @param unitAmount size of a unit
   * @param firstDeliveryDate date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate date of last delivery - PHYSICAL settlement
   * @param amount number of units
   * @param unitName description of unit size
   * @param settlementType settlement type - PHYISCAL or CASH
   * @param settlement time (in years as a double) until the date-time at which the future is settled
   * @param referencePrice reference price
   * @param currency currency
   */
  public AgricultureForward(final double expiry, final ExternalId underlying, final double unitAmount, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final double amount, final String unitName, final SettlementType settlementType, final double settlement, final double referencePrice, final Currency currency) {
    super(expiry, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, settlementType, settlement, referencePrice, currency);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureForward(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureForward(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AgricultureForward)) {
      return false;
    }
    return super.equals(obj);
  }
}
