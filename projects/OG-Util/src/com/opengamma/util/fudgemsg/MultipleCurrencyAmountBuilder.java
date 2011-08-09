/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Fudge builder for {@code MultipleCurrencyAmount}.
 */
@FudgeBuilderFor(MultipleCurrencyAmount.class)
public final class MultipleCurrencyAmountBuilder implements FudgeBuilder<MultipleCurrencyAmount> {

  /** Field name. */
  public static final String CURRENCIES_KEY = "currencies";
  /** Field name. */
  public static final String AMOUNTS_KEY = "amounts";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, MultipleCurrencyAmount object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CurrencyAmount[] currencyAmounts = object.getCurrencyAmounts();
    Currency[] currencies = new Currency[currencyAmounts.length];
    double[] amounts = new double[currencyAmounts.length];
    int i = 0;
    for (CurrencyAmount ca : currencyAmounts) {
      currencies[i] = ca.getCurrency();
      amounts[i++] = ca.getAmount();
    }
    serializer.addToMessage(msg, CURRENCIES_KEY, null, currencies);
    serializer.addToMessage(msg, AMOUNTS_KEY, null, amounts);
    return msg;
  }

  @Override
  public MultipleCurrencyAmount buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField currenciesField = msg.getByName(CURRENCIES_KEY);
    if (currenciesField == null) {
      throw new IllegalArgumentException("Fudge message is not a MultipleCurrencyAmount - field 'currencies' is not present");
    }
    FudgeField amountsField = msg.getByName(AMOUNTS_KEY);
    if (amountsField == null) {
      throw new IllegalArgumentException("Fudge message is not a MultipleCurrencyAmount - field 'amounts' is not present");
    }
    String[] currencyNames = deserializer.fieldValueToObject(String[].class, currenciesField);
    int length = currencyNames.length;
    Currency[] currencies = new Currency[length];
    for (int i = 0; i < length; i++) {
      currencies[i] = Currency.of(currencyNames[i]);
    }
    double[] amounts = deserializer.fieldValueToObject(double[].class, amountsField);
    return MultipleCurrencyAmount.of(currencies, amounts);
  }

}
