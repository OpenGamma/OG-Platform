/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Energy future definition
 */
public class EnergyFutureDefinition extends CommodityFutureDefinition<EnergyFuture> {

  /**
   * Constructor for futures with delivery dates (i.e. physical settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param firstDeliveryDate  date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate  date of last delivery - PHYSICAL settlement
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param settlementType  settlement type - PHYSICAL or CASH
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   */
  public EnergyFutureDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate, double amount, String unitName,
      SettlementType settlementType, final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    super(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, settlementType, referencePrice, currency, settlementDate);
  }

  /**
   * Constructor for futures without delivery dates (e.g. cash settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   */
  public EnergyFutureDefinition(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName, final double referencePrice,
      final Currency currency, final ZonedDateTime settlementDate) {
    this(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH, referencePrice, currency, settlementDate);
  }

  /**
   * Static constructor method for cash settled futures
   * 
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param referencePrice number of units
   * @param currency currency
   * @param settlementDate settlement date
   * @return the future
   */
  public static EnergyFutureDefinition withCashSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, double amount, String unitName,
      final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    return new EnergyFutureDefinition(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH, referencePrice, currency, settlementDate);
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
  * @param referencePrice Number of units
  * @param currency Currency
  * @param settlementDate Settlement date
  * @return the future
  */
  public static EnergyFutureDefinition withPhysicalSettlement(ZonedDateTime expiryDate, ExternalId underlying, double unitAmount, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate,
      double amount, String unitName, final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    return new EnergyFutureDefinition(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, SettlementType.PHYSICAL, referencePrice, currency, settlementDate);
  }

  /**
   * Get the derivative at a given fix time from the definition
   * @param date fixing time
   * @param yieldCurveNames  
   * @return the fixed derivative
   */
  @Override
  public EnergyFuture toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    double timeToSettlement = TimeCalculator.getTimeBetween(date, this.getSettlementDate());
    return new EnergyFuture(timeToFixing, getUnderlying(), getUnitAmount(), getFirstDeliveryDate(), getLastDeliveryDate(), getAmount(), getUnitName(), getSettlementType(), timeToSettlement,
        getReferencePrice(), getCurrency());
  }

  /**
   * Get the derivative at a given fix time from the definition
   *
   * @param date  fixing time
   * @param referencePrice reference price
   * @return the fixed derivative
   */
  public EnergyFuture toDerivative(final ZonedDateTime date, final double referencePrice) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    double timeToSettlement = TimeCalculator.getTimeBetween(date, this.getSettlementDate());
    return new EnergyFuture(timeToFixing, getUnderlying(), getUnitAmount(), getFirstDeliveryDate(), getLastDeliveryDate(), getAmount(), getUnitName(), getSettlementType(), timeToSettlement,
        referencePrice, getCurrency());
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
