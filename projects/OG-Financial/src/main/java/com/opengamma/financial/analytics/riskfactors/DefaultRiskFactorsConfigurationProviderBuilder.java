/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.money.Currency;

/**
 * Fudge message builder for {@link DefaultRiskFactorsConfigurationProvider}.
 */
@FudgeBuilderFor(DefaultRiskFactorsConfigurationProvider.class)
public class DefaultRiskFactorsConfigurationProviderBuilder implements FudgeBuilder<DefaultRiskFactorsConfigurationProvider> {

  private static final String CURRENCY_FIELD = "currency";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, DefaultRiskFactorsConfigurationProvider object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, CURRENCY_FIELD, null, object.getCurrencyOverride());
    return msg;
  }

  @Override
  public DefaultRiskFactorsConfigurationProvider buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return new DefaultRiskFactorsConfigurationProvider(getCurrencyOverride(deserializer, msg));
  }

  protected Currency getCurrencyOverride(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField currencyField = msg.getByName(CURRENCY_FIELD);
    Currency currencyOverride = currencyField != null ? deserializer.fieldValueToObject(Currency.class, currencyField) : null;
    return currencyOverride;
  }

}
