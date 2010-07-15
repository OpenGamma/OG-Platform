/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * An agricultural commodity future.
 */
public class AgricultureFutureSecurity extends CommodityFutureSecurity {

  /**
   * Creates an agricultural commodity future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the type of future (e.g. Winter Wheat, Cotton, etc.)
   * @param unitNumber  number of units to deliver
   * @param unitName  the unit to deliver (e.g. Bushels of wheat)
   */
  public AgricultureFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency,
      final String type, final Double unitNumber, final String unitName) {
    super(expiry, tradingExchange, settlementExchange, currency, type, unitNumber, unitName);
  }

  /**
   * Creates an agricultural commodity future with no amount.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the type of future (e.g. Winter Wheat, Cotton, etc.)
   */
  public AgricultureFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String type) {
    super(expiry, tradingExchange, settlementExchange, currency, type);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitAgricultureFutureSecurity(this);
  }

}
