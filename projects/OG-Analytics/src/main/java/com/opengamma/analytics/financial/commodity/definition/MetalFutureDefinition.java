/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Metal future definition
 */
public class MetalFutureDefinition extends CommodityFutureDefinition implements InstrumentDefinition<MetalFuture> {

  /**
   * Constructor for futures with delivery dates (i.e. physical settlement)
   *
   * @param expiryDate “is the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract.”
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param firstDeliveryDate Date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate Date of last delivery - PHYSICAL settlement
   * @param amount Number of units
   * @param unitName Description of unit size
   * @param settlementType Settlement type - PHYSICAL or CASH
   */
  public MetalFutureDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate, double amount, String unitName,
      SettlementType settlementType) {
    super(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, settlementType);
  }

  /**
   * Constructor for futures without delivery dates (e.g. cash settlement)
   *
   * @param expiryDate “is the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract.”
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param amount Number of units
   * @param unitName Description of unit size
   */
  public MetalFutureDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName) {
    this(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH);
  }

  /**
   * Static constructor method for cash settled future
   * @param expiryDate “is the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract.”
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param amount Number of units
   * @param unitName Description of unit size
   * @return The forward
   */
  public static MetalFutureDefinition withCashSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName) {
    return new MetalFutureDefinition(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH);
  }

  /**
   * Static constructor method for physical settlement futures
   * @param expiryDate “is the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract.”
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param firstDeliveryDate Date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate Date of last delivery - PHYSICAL settlement
   * @param amount Number of units
   * @param unitName Description of unit size
   * @return The forward
   */
  public static MetalFutureDefinition withPhysicalSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate,
      double amount, String unitName) {
    return new MetalFutureDefinition(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, SettlementType.PHYSICAL);
  }

  /**
   * Get the derivative at a given fix time from the definition
   * @param date fixing time
   * @return the fixed derivative
   */
  @Override
  public MetalFuture toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    return new MetalFuture(timeToFixing, getUnderlying(), getUnitAmount(), getFirstDeliveryDate(), getLastDeliveryDate(), getAmount(), getUnitName(), getSettlementType());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitMetalFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitMetalFutureDefinition(this);
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
    if (!(obj instanceof MetalFutureDefinition)) {
      return false;
    }
    return super.equals(obj);
  }


}
