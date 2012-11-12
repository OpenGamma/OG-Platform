/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract commodity forward definition.
 */
public abstract class CommodityForwardDefinition {
  /** Expiry date */
  private final ZonedDateTime _expiryDate;
  /** Identifier of the underlying commodity */
  private final ExternalId _underlying;
  /** Size of a unit */
  private final double _unitAmount;
  /** Date of first delivery - PHYSICAL settlement */
  private final ZonedDateTime _firstDeliveryDate;
  /** Date of last delivery - PHYSICAL settlement */
  private final ZonedDateTime _lastDeliveryDate;
  /** Number of units */
  private final double _amount;
  /** Description of unit size */
  private final String _unitName;
  /** Settlement type - PHYISCAL or CASH */
  private final SettlementType _settlementType;

  /**
   * Constructor for forwards with delivery dates (i.e. physical settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a forward contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param firstDeliveryDate  date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate  date of last delivery - PHYSICAL settlement
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param settlementType  settlement type - PHYSICAL or CASH
   */
  public CommodityForwardDefinition(final ZonedDateTime expiryDate, final ExternalId underlying, final double unitAmount, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final double amount, final String unitName, final SettlementType settlementType) {
    ArgumentChecker.notNull(expiryDate, "expiry time");
    ArgumentChecker.notNull(underlying, "underlying");

    ArgumentChecker.notEmpty(unitName, "unit name");
    ArgumentChecker.notNull(settlementType, "settlement type");
    if (settlementType.equals(SettlementType.PHYSICAL)) {
      ArgumentChecker.inOrderOrEqual(firstDeliveryDate, lastDeliveryDate, "first delivery date", "last delivery date");
    } else {
      ArgumentChecker.isTrue(firstDeliveryDate == null, "first delivery date must be null for non physical settlement");
      ArgumentChecker.isTrue(lastDeliveryDate == null, "last delivery date must be null for non physical settlement");
    }

    _expiryDate = expiryDate;
    _underlying = underlying;
    _unitAmount = unitAmount;
    _firstDeliveryDate = firstDeliveryDate;
    _lastDeliveryDate = lastDeliveryDate;
    _amount = amount;
    _unitName = unitName;
    _settlementType = settlementType;
  }

  /**
   * Constructor for forward without delivery dates (e.g. cash settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   */
  public CommodityForwardDefinition(final ZonedDateTime expiryDate, final ExternalId underlying, final double unitAmount,
      final double amount, final String unitName) {
    this(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH);
  }

  /**
   * Gets the expiryDate.
   * @return the expiryDate
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  public ExternalId getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the unitAmount.
   * @return the unitAmount
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  /**
   * Gets the firstDeliveryDate.
   * @return the firstDeliveryDate
   */
  public ZonedDateTime getFirstDeliveryDate() {
    return _firstDeliveryDate;
  }

  /**
   * Gets the lastDeliveryDate.
   * @return the lastDeliveryDate
   */
  public ZonedDateTime getLastDeliveryDate() {
    return _lastDeliveryDate;
  }

  /**
   * Gets the amount.
   * @return the amount
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Gets the unitName.
   * @return the unitName
   */
  public String getUnitName() {
    return _unitName;
  }

  /**
   * Gets the settlementType.
   * @return the settlementType
   */
  public SettlementType getSettlementType() {
    return _settlementType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _underlying.hashCode();
    result = prime * result + _expiryDate.hashCode();
    if (_firstDeliveryDate != null) {
      result = prime * result + _firstDeliveryDate.hashCode();
    }
    if (_lastDeliveryDate != null) {
      result = prime * result + _lastDeliveryDate.hashCode();
    }
    result = prime * result + _unitName.hashCode();
    result = prime * result + _settlementType.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommodityForwardDefinition)) {
      return false;
    }
    final CommodityForwardDefinition other = (CommodityForwardDefinition) obj;
    if (!ObjectUtils.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    if (!ObjectUtils.equals(_firstDeliveryDate, other._firstDeliveryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_lastDeliveryDate, other._lastDeliveryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_unitName, other._unitName)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementType, other._settlementType)) {
      return false;
    }
    if (Double.compare(_amount, other._amount) != 0) {
      return false;
    }
    if (Double.compare(_unitAmount, other._unitAmount) != 0) {
      return false;
    }
    return true;
  }

}
