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
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ExpiryFudgeBuilder;

/**
 * A Fudge builder for {@code FutureSecurity}.
 */
public class FutureSecurityFudgeBuilder extends AbstractFudgeBuilder {

  /** Field name. */
  public static final String EXPIRY_FIELD_NAME = "expiry";
  /** Field name. */
  public static final String TRADING_EXCHANGE_FIELD_NAME = "tradingExchange";
  /** Field name. */
  public static final String SETTLEMENT_EXCHANGE_FIELD_NAME = "settlementExchange";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String UNIT_AMOUNT_FIELD_NAME = "unitAmount";
  /** Field name. */
  public static final String CONTRACT_CATEGORY_FIELD_NAME = "contractCategory";

  public static void toFudgeMsg(FudgeSerializer serializer, FutureSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, EXPIRY_FIELD_NAME, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, TRADING_EXCHANGE_FIELD_NAME, object.getTradingExchange());
    addToMessage(msg, SETTLEMENT_EXCHANGE_FIELD_NAME, object.getSettlementExchange());
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, UNIT_AMOUNT_FIELD_NAME, object.getUnitAmount());
    addToMessage(msg, CONTRACT_CATEGORY_FIELD_NAME, object.getContractCategory());
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FutureSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setExpiry(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_FIELD_NAME)));
    object.setTradingExchange(msg.getString(TRADING_EXCHANGE_FIELD_NAME));
    object.setSettlementExchange(msg.getString(SETTLEMENT_EXCHANGE_FIELD_NAME));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setUnitAmount(msg.getDouble(UNIT_AMOUNT_FIELD_NAME));
    object.setContractCategory(msg.getString(CONTRACT_CATEGORY_FIELD_NAME));
  }

}
