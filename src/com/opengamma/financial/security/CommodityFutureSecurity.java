/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * A commodity future.
 */
public abstract class CommodityFutureSecurity extends FutureSecurity {

  /** The commodity type. */
  private final String _commodityType;
  /** The unit number. */
  private final Double _unitNumber;
  /** The unit name. */
  private final String _unitName;

  /**
   * Creates a commodity future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the commodity type
   * @param unitNumber  the unit number
   * @param unitName  the unit name
   */
  public CommodityFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String type, final Double unitNumber, final String unitName) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _commodityType = type;
    _unitNumber = unitNumber;
    _unitName = unitName;
  }

  /**
   * Creates a commodity future with no amount.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the commodity type
   */
  public CommodityFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String type) {
    this(expiry, tradingExchange, settlementExchange, currency, type, null, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the commodity type.
   * @return the commodity type
   */
  public String getCommodityType() {
    return _commodityType;
  }

  /**
   * Gets the unit number.
   * @return the unit number
   */
  public Double getUnitNumber() {
    return _unitNumber;
  }

  /**
   * Gets the unit name.
   * @return the unit name
   */
  public String getUnitName() {
    return _unitName;
  }

}
