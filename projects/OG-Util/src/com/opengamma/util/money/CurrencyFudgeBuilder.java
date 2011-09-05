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
 * Fudge builder for {@code Currency}.
 */
@FudgeBuilderFor(Currency.class)
public final class CurrencyFudgeBuilder implements FudgeBuilder<Currency> {

  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Currency object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FudgeSerializer.addClassHeader(msg, Currency.class);
    serializer.addToMessage(msg, CURRENCY_FIELD_NAME, null, object.getCode());
    return msg;
  }

  @Override
  public Currency buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final String currencyStr = msg.getString(CURRENCY_FIELD_NAME);
    if (currencyStr == null) {
      throw new IllegalArgumentException("Fudge message is not a Currency - field 'currency' is not present");
    }
    return Currency.of(currencyStr);
  }

}
