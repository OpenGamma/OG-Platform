/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExpiryBuilder;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code FXOptionSecurity}.
 */
@FudgeBuilderFor(FXOptionSecurity.class)
public class FXOptionSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXOptionSecurity> {

  /** Field name. */
  public static final String PUT_CURRENCY_KEY = "putCurrency";
  /** Field name. */
  public static final String CALL_CURRENCY_KEY = "callCurrency";
  /** Field name. */
  public static final String PUT_AMOUNT_KEY = "putAmount";
  /** Field name. */
  public static final String CALL_AMOUNT_KEY = "callAmount";
  /** Field name. */
  public static final String EXPIRY_KEY = "expiry";
  /** Field name. */
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  /** Field name. */
  public static final String IS_LONG_KEY = "isLong";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FXOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXOptionSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FXOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, PUT_CURRENCY_KEY, object.getPutCurrency());
    addToMessage(msg, CALL_CURRENCY_KEY, object.getCallCurrency());
    addToMessage(msg, PUT_AMOUNT_KEY, object.getPutAmount());
    addToMessage(msg, CALL_AMOUNT_KEY, object.getCallAmount());
    addToMessage(msg, EXPIRY_KEY, ExpiryBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, SETTLEMENT_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, IS_LONG_KEY, object.getIsLong());
  }

  @Override
  public FXOptionSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FXOptionSecurity object = FinancialSecurityBuilder.backdoorCreateClass(FXOptionSecurity.class);
    FXOptionSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FXOptionSecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setPutCurrency(msg.getValue(Currency.class, PUT_CURRENCY_KEY));
    object.setCallCurrency(msg.getValue(Currency.class, CALL_CURRENCY_KEY));
    object.setPutAmount(msg.getDouble(PUT_AMOUNT_KEY));
    object.setCallAmount(msg.getDouble(CALL_AMOUNT_KEY));
    object.setExpiry(ExpiryBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_KEY)));
    object.setSettlementDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_KEY)));
    object.setIsLong(msg.getBoolean(IS_LONG_KEY));
  }

}
