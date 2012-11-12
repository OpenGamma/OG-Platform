/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Metal forward definition
 */
public class MetalForwardDefinition extends CommodityForwardDefinition implements InstrumentDefinition<MetalForward> {

  /**
   * Constructor for forwards with delivery dates (i.e. physical settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param firstDeliveryDate  date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate  date of last delivery - PHYSICAL settlement
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param settlementType  settlement type - PHYSICAL or CASH
   */
  public MetalForwardDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate, double amount, String unitName,
      SettlementType settlementType) {
    super(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, settlementType);
  }

  /**
   * Constructor for forwards without delivery dates (e.g. cash settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   */
  public MetalForwardDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName) {
    this(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH);
  }

  /**
   * Static constructor method for cash settled forwards
   * 
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   * @return the forward
   */
  public static MetalForwardDefinition withCashSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName) {
    return new MetalForwardDefinition(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH);
  }

  /**
  * Static constructor method for physical settlement futures
  * 
  * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
  * @param underlying  identifier of the underlying commodity
  * @param unitAmount  size of a unit
  * @param firstDeliveryDate  date of first delivery - PHYSICAL settlement
  * @param lastDeliveryDate  date of last delivery - PHYSICAL settlement
  * @param amount  number of units
  * @param unitName  description of unit size
  * @return the forward
  */
  public static MetalForwardDefinition withPhysicalSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate,
      double amount, String unitName) {
    return new MetalForwardDefinition(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, SettlementType.PHYSICAL);
  }

  /**
   * Get the derivative at a given fix time from the definition
   * @param date fixing time
   * @param yieldCurveNames  
   * @return the fixed derivative
   */
  @Override
  public MetalForward toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    return new MetalForward(timeToFixing, getUnderlying(), getUnitAmount(), getFirstDeliveryDate(), getLastDeliveryDate(), getAmount(), getUnitName(), getSettlementType());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitMetalForwardDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitMetalForwardDefinition(this);
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
    if (!(obj instanceof MetalForwardDefinition)) {
      return false;
    }
    return super.equals(obj);
  }

}
