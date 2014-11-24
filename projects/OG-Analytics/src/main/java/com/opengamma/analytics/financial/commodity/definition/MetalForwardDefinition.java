/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Metal forward definition
 */
public class MetalForwardDefinition extends CommodityForwardDefinition<MetalForward> {

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
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   */
  public MetalForwardDefinition(final ZonedDateTime expiryDate, final ExternalId underlying, final double unitAmount, final ZonedDateTime firstDeliveryDate,
      final ZonedDateTime lastDeliveryDate, final double amount, final String unitName,
      final SettlementType settlementType, final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    super(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, settlementType, referencePrice, currency, settlementDate);
  }

  /**
   * Constructor for forwards without delivery dates (e.g. cash settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   */
  public MetalForwardDefinition(final ZonedDateTime expiryDate, final ExternalId underlying, final double unitAmount, final double amount, final String unitName, final double referencePrice,
      final Currency currency, final ZonedDateTime settlementDate) {
    this(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH, referencePrice, currency, settlementDate);
  }

  /**
   * Static constructor method for cash settled forwards
   * 
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   * @return the forward
   */
  public static MetalForwardDefinition withCashSettlement(final ZonedDateTime expiryDate, final ExternalId underlying, final double unitAmount, final double amount, final String unitName,
      final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    return new MetalForwardDefinition(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH, referencePrice, currency, settlementDate);
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
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   * @return the forward
   */
  public static MetalForwardDefinition withPhysicalSettlement(final ZonedDateTime expiryDate, final ExternalId underlying, final double unitAmount,
      final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final double amount, final String unitName, final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    return new MetalForwardDefinition(expiryDate, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, SettlementType.PHYSICAL, referencePrice, currency, settlementDate);
  }

  @Override
  public MetalForward toDerivative(final ZonedDateTime date) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    final double timeToSettlement = TimeCalculator.getTimeBetween(date, this.getSettlementDate());
    return new MetalForward(timeToFixing, getUnderlying(), getUnitAmount(), getFirstDeliveryDate(), getLastDeliveryDate(), getAmount(), getUnitName(), getSettlementType(),
        timeToSettlement, getReferencePrice(), getCurrency());
  }

  @Override
  public MetalForward toDerivative(final ZonedDateTime date, final Double referencePrice) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    final double timeToSettlement = TimeCalculator.getTimeBetween(date, this.getSettlementDate());
    return new MetalForward(timeToFixing, getUnderlying(), getUnitAmount(), getFirstDeliveryDate(), getLastDeliveryDate(), getAmount(), getUnitName(), getSettlementType(), timeToSettlement,
        referencePrice.doubleValue(), getCurrency());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitMetalForwardDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitMetalForwardDefinition(this);
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
    if (!(obj instanceof MetalForwardDefinition)) {
      return false;
    }
    return super.equals(obj);
  }

}
