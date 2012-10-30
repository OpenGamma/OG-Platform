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

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code MultipleCurrencyAmount}.
 */
@FudgeBuilderFor(MultipleCurrencyAmount.class)
public final class MultipleCurrencyAmountFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<MultipleCurrencyAmount> {

  // REVIEW 2012-03-14 Andrew -- Using the Fudge field name for the currency code and give that the value field would be a more efficient encoding and be simpler to decode 

  /** Field name. */
  public static final String CURRENCIES_FIELD_NAME = "currencies";
  /** Field name. */
  public static final String AMOUNTS_FIELD_NAME = "amounts";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, MultipleCurrencyAmount object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final MultipleCurrencyAmount object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final MultipleCurrencyAmount object, final MutableFudgeMsg msg) {
    CurrencyAmount[] currencyAmounts = object.getCurrencyAmounts();
    String[] currencies = new String[currencyAmounts.length];
    double[] amounts = new double[currencyAmounts.length];
    int i = 0;
    for (CurrencyAmount ca : currencyAmounts) {
      currencies[i] = ca.getCurrency().getCode();
      amounts[i++] = ca.getAmount();
    }
    serializer.addToMessage(msg, CURRENCIES_FIELD_NAME, null, currencies);
    serializer.addToMessage(msg, AMOUNTS_FIELD_NAME, null, amounts);
  }

  //-------------------------------------------------------------------------
  @Override
  public MultipleCurrencyAmount buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static MultipleCurrencyAmount fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    String[] currencyNames = deserializer.fieldValueToObject(String[].class, msg.getByName(CURRENCIES_FIELD_NAME));
    int length = currencyNames.length;
    Currency[] currencies = new Currency[length];
    for (int i = 0; i < length; i++) {
      currencies[i] = Currency.of(currencyNames[i]);
    }
    double[] amounts = deserializer.fieldValueToObject(double[].class, msg.getByName(AMOUNTS_FIELD_NAME));
    return MultipleCurrencyAmount.of(currencies, amounts);
  }

}
