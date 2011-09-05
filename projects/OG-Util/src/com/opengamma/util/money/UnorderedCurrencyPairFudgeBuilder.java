/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Builder for converting UnorderedCurrencyPair instances to/from Fudge messages.
 */
@FudgeBuilderFor(UnorderedCurrencyPair.class)
public class UnorderedCurrencyPairFudgeBuilder implements FudgeBuilder<UnorderedCurrencyPair> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final UnorderedCurrencyPair object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, UnorderedCurrencyPair.class);
    serializer.addToMessage(message, "currency1", null, object.getFirstCurrency());
    serializer.addToMessage(message, "currency2", null, object.getSecondCurrency());
    return message;
  }

  @Override
  public UnorderedCurrencyPair buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Currency currency1 = deserializer.fieldValueToObject(Currency.class, message.getByName("currency1"));
    final Currency currency2 = deserializer.fieldValueToObject(Currency.class, message.getByName("currency2"));
    return UnorderedCurrencyPair.of(currency1, currency2);
  }

}
