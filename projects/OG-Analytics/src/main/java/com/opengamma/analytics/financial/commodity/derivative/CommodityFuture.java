/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Abstract commodity future derivative.
 */
public abstract class CommodityFuture implements InstrumentDerivative {
  /** Time (in years as a double) until the date-time at which the future expires */
  private final double _expiry;
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

  // extra variables taken from SimpleFuture
  // TODO: Check they are needed

  /** settlement time (in y*/
  private final double _settlement;
  /** reference price */
  private final double _referencePrice;
  /** currency */
  private final Currency _currency;

  /**
   * @param expiry Time (in years as a double) until the date-time at which the future expires
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param firstDeliveryDate Date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate Date of last delivery - PHYSICAL settlement
   * @param amount Number of units
   * @param unitName Description of unit size
   * @param settlementType Settlement type - PHYSICAL or CASH
   * @param settlement  Time (in years as a double) until the date-time at which the future is settled
   * @param referencePrice reference price
   * @param currency the currency
   */
  public CommodityFuture(final double expiry, final ExternalId underlying, final double unitAmount, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final double amount, final String unitName, final SettlementType settlementType, final double settlement, final double referencePrice, final Currency currency) {
    ArgumentChecker.isTrue(expiry >= 0, "time to expiry must be positive");

    _expiry = expiry;
    _underlying = underlying;
    _unitAmount = unitAmount;
    _firstDeliveryDate = firstDeliveryDate;
    _lastDeliveryDate = lastDeliveryDate;
    _amount = amount;
    _unitName = unitName;
    _settlementType = settlementType;
    _settlement = settlement;
    _referencePrice = referencePrice;
    _currency = currency;
  }

  /**
   * Gets the expiry.
   * @return the expiry
   */
  public double getExpiry() {
    return _expiry;
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
    return _amount; // FIXME Resolve confusion about _amount and _unitAmount - What is _amount? Speak to Casey. Also need to look at Converters (Security > Definition > Derivative)
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

  /**
   * Gets the settlement.
   * @return the settlement
   */
  public double getSettlement() {
    return _settlement;
  }

  /**
   * Gets the referencePrice.
   * @return the referencePrice
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _underlying.hashCode();
    if (_firstDeliveryDate != null) {
      result = prime * result + _firstDeliveryDate.hashCode();
    }
    if (_lastDeliveryDate != null) {
      result = prime * result + _lastDeliveryDate.hashCode();
    }
    result = prime * result + _unitName.hashCode();
    result = prime * result + _settlementType.hashCode();
    result = prime * result + _currency.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_expiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CommodityFuture)) {
      return false;
    }
    final CommodityFuture other = (CommodityFuture) obj;
    if (Double.doubleToLongBits(_expiry) != Double.doubleToLongBits(other._expiry)) {
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
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (Double.compare(_amount, other._amount) != 0) {
      return false;
    }
    if (Double.compare(_settlement, other._settlement) != 0) {
      return false;
    }
    if (Double.compare(_referencePrice, other._referencePrice) != 0) {
      return false;
    }
    return true;
  }

}
