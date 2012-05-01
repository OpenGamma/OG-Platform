/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.money.Currency;

/**
 * Fudge message builder for {@link CurrencyPair}.
 */
@FudgeBuilderFor(CurrencyPair.class)
public class CurrencyPairFudgeBuilder implements FudgeBuilder<CurrencyPair> {

  private static final String BASE_FIELD = "base";
  private static final String COUNTER_FIELD = "counter";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CurrencyPair object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, BASE_FIELD, null, object.getBase());
    serializer.addToMessage(msg, COUNTER_FIELD, null, object.getCounter());
    return msg;
  }

  @Override
  public CurrencyPair buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Currency base = deserializer.fieldValueToObject(Currency.class, msg.getByName(BASE_FIELD));
    Currency counter = deserializer.fieldValueToObject(Currency.class, msg.getByName(COUNTER_FIELD));
    return CurrencyPair.of(base, counter);
  }

}
