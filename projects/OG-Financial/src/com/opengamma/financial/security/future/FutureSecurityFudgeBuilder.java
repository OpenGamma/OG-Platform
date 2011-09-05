/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExpiryBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code FutureSecurity}.
 */
public class FutureSecurityFudgeBuilder extends AbstractFudgeBuilder {

  /** Field name. */
  public static final String EXPIRY_KEY = "expiry";
  /** Field name. */
  public static final String TRADING_EXCHANGE_KEY = "tradingExchange";
  /** Field name. */
  public static final String SETTLEMENT_EXCHANGE_KEY = "settlementExchange";
  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String UNIT_AMOUNT_KEY = "unitAmount";

  public static void toFudgeMsg(FudgeSerializer serializer, FutureSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, EXPIRY_KEY, ExpiryBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, TRADING_EXCHANGE_KEY, object.getTradingExchange());
    addToMessage(msg, SETTLEMENT_EXCHANGE_KEY, object.getSettlementExchange());
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, UNIT_AMOUNT_KEY, object.getUnitAmount());
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FutureSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setExpiry(ExpiryBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_KEY)));
    object.setTradingExchange(msg.getString(TRADING_EXCHANGE_KEY));
    object.setSettlementExchange(msg.getString(SETTLEMENT_EXCHANGE_KEY));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setUnitAmount(msg.getDouble(UNIT_AMOUNT_KEY));
  }

}
