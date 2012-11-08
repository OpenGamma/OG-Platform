/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Energy future definition
 */
public class EnergyFutureDefinition extends CommodityFutureDefinition implements InstrumentDefinition<EnergyFuture> {

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
  public EnergyFutureDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate, double amount, String unitName,
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
  public EnergyFutureDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName) {
    this(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH);
  }

  /**
   * Static constructor method for cash settled futures
   * @param expiryDate “is the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract.”
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param amount Number of units
   * @param unitName Description of unit size
   * @return The future
   */
  public static EnergyFutureDefinition withCashSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName) {
    return new EnergyFutureDefinition(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH);
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
  * @return The future
  */
  public static EnergyFutureDefinition withPhysicalSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate,
      double amount, String unitName) {
    return new EnergyFutureDefinition(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, SettlementType.PHYSICAL);
  }

  /**
   * Get the derivative at a given fix time from the definition
   * @param date fixing time
   * @return the fixed derivative
   */
  @Override
  public EnergyFuture toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    return new EnergyFuture(timeToFixing, getUnderlying(), getUnitAmount(), getFirstDeliveryDate(), getLastDeliveryDate(), getAmount(), getUnitName(), getSettlementType());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitEnergyFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitEnergyFutureDefinition(this);
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
    if (!(obj instanceof EnergyFutureDefinition)) {
      return false;
    }
    return super.equals(obj);
  }

}
