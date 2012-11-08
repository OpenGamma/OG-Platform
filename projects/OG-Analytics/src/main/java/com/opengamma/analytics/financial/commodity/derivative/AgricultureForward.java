/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.id.ExternalId;

/**
 * Metal  commodity derivative
 */
public class AgricultureForward extends CommodityForward {

  /**
   * @param expiry Time (in years as a double) until the date-time at which the forward expires
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param firstDeliveryDate Date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate Date of last delivery - PHYSICAL settlement
   * @param amount Number of units
   * @param unitName Description of unit size
   * @param settlementType Settlement type - PHYISCAL or CASH
   */
  public AgricultureForward(final double expiry, final ExternalId underlying, final double unitAmount, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final double amount, final String unitName,
      final SettlementType settlementType) {
    super(expiry, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, settlementType);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitAgricultureForward(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitAgricultureForward(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
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
