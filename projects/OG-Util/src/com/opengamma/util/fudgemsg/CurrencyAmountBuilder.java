/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.util.money.CurrencyAmount;

/**
 * Fudge builder for {@code CurrencyAmount}.
 */
@FudgeBuilderFor(CurrencyAmount.class)
public final class CurrencyAmountBuilder implements FudgeBuilder<CurrencyAmount> {

  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String AMOUNT_KEY = "amount";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, CurrencyAmount object) {
    final MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, CURRENCY_KEY, null, object.getCurrency());
    context.addToMessage(msg, AMOUNT_KEY, null, object.getAmount());
    return msg;
  }

  @Override
  public CurrencyAmount buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    final Currency currency = msg.getValue(Currency.class, CURRENCY_KEY);
    if (currency == null) {
      throw new IllegalArgumentException("Fudge message is not a CurrencyAmount - field 'currency' is not present");
    }
    final Double amount = msg.getDouble(AMOUNT_KEY);
    if (amount == null) {
      throw new IllegalArgumentException("Fudge message is not a CurrencyAmount - field 'amount' is not present");
    }
    return CurrencyAmount.of(currency, amount);
  }

}
