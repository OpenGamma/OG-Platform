/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Builder for converting UnorderedCurrencyPair instances to/from Fudge messages.
 */
@FudgeBuilderFor(UnorderedCurrencyPair.class)
public class UnorderedCurrencyPairBuilder implements FudgeBuilder<UnorderedCurrencyPair> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final UnorderedCurrencyPair object) {
    final MutableFudgeMsg message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, UnorderedCurrencyPair.class);
    context.addToMessage(message, "currency1", null, object.getFirstCurrency());
    context.addToMessage(message, "currency2", null, object.getSecondCurrency());
    return message;
  }

  @Override
  public UnorderedCurrencyPair buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final Currency currency1 = context.fieldValueToObject(Currency.class, message.getByName("currency1"));
    final Currency currency2 = context.fieldValueToObject(Currency.class, message.getByName("currency2"));
    return UnorderedCurrencyPair.of(currency1, currency2);
  }

}
