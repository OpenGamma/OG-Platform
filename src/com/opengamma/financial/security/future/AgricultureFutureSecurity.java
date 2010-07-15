/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

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

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    // No additional fields
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    // No additional fields
  }

  public static AgricultureFutureSecurity fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final AgricultureFutureSecurity security = new AgricultureFutureSecurity(context.fieldValueToObject(Expiry.class,
        message.getByName(EXPIRY_KEY)), message.getString(TRADINGEXCHANGE_KEY), message
        .getString(SETTLEMENTEXCHANGE_KEY),
        context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_KEY)), message
            .getString(COMMODITYTYPE_KEY),
        message.getDouble(UNITNUMBER_KEY), message.getString(UNITNAME_KEY));
    security.fromFudgeMsgImpl(context, message);
    return security;
  }

}
