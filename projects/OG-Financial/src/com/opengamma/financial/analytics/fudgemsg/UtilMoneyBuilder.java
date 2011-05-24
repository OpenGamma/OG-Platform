/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
final class UtilMoneyBuilder {

  private UtilMoneyBuilder() {
  }
  
  @FudgeBuilderFor(CurrencyAmount.class)
  public static final class CurrencyAmountBuilder extends AbstractFudgeBuilder<CurrencyAmount> {
    private static final String CURRENCY_FIELD_NAME = "Currency";
    private static final String AMOUNT_FIELD_NAME = "Amount";
    
    @Override
    public CurrencyAmount buildObject(FudgeDeserializationContext context, FudgeMsg message) {
      final Currency ccy = context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_FIELD_NAME));
      double amount = context.fieldValueToObject(double.class, message.getByName(AMOUNT_FIELD_NAME));
      return CurrencyAmount.of(ccy, amount);
    }
    
    @Override
    protected void buildMessage(FudgeSerializationContext context, MutableFudgeMsg message, CurrencyAmount object) {
      context.addToMessage(message, CURRENCY_FIELD_NAME, null, object.getCurrency());
      context.addToMessage(message, AMOUNT_FIELD_NAME, null, object.getAmount());
    }
  }
  
  @FudgeBuilderFor(MultipleCurrencyAmount.class)
  public static final class MultipleCurrencyAmountBuilder extends AbstractFudgeBuilder<MultipleCurrencyAmount> {
    private static final String CURRENCIES_FIELD_NAME = "Currencies";
    private static final String AMOUNTS_FIELD_NAME = "Amounts";
    
    @Override
    public MultipleCurrencyAmount buildObject(FudgeDeserializationContext context, FudgeMsg message) {
      String[] currencyNames = context.fieldValueToObject(String[].class, message.getByName(CURRENCIES_FIELD_NAME));
      int length = currencyNames.length;
      Currency[] currencies = new Currency[length];
      for (int i = 0; i < length; i++) {
        currencies[i] = Currency.of(currencyNames[i]);
      }
      double[] amounts = context.fieldValueToObject(double[].class, message.getByName(AMOUNTS_FIELD_NAME));
      return MultipleCurrencyAmount.of(currencies, amounts);
    }

    @Override
    protected void buildMessage(FudgeSerializationContext context, MutableFudgeMsg message,
        MultipleCurrencyAmount object) {
      int size = object.size();
      Currency[] currencies = new Currency[size];
      double[] amounts = new double[size];
      int i = 0;
      for (Map.Entry<Currency, Double> ca : object) {
        currencies[i] = ca.getKey();
        amounts[i++] = ca.getValue();
      }
      context.addToMessage(message, CURRENCIES_FIELD_NAME, null, currencies);
      context.addToMessage(message, AMOUNTS_FIELD_NAME, null, amounts);
    }
    
  }
}
