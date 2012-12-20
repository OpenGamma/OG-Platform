/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge builder for {@code CurrencyAmount}.
 */
@FudgeBuilderFor(CurrencyAmount.class)
public final class CurrencyAmountFudgeBuilder implements FudgeBuilder<CurrencyAmount> {

  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String AMOUNT_FIELD_NAME = "amount";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CurrencyAmount object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, CURRENCY_FIELD_NAME, null, object.getCurrency());
    serializer.addToMessage(msg, AMOUNT_FIELD_NAME, null, object.getAmount());
    return msg;
  }

  @Override
  public CurrencyAmount buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final Currency currency = msg.getValue(Currency.class, CURRENCY_FIELD_NAME);
    if (currency == null) {
      throw new IllegalArgumentException("Fudge message is not a CurrencyAmount - field 'currency' is not present");
    }
    final Double amount = msg.getDouble(AMOUNT_FIELD_NAME);
    if (amount == null) {
      throw new IllegalArgumentException("Fudge message is not a CurrencyAmount - field 'amount' is not present");
    }
    return CurrencyAmount.of(currency, amount);
  }

}
