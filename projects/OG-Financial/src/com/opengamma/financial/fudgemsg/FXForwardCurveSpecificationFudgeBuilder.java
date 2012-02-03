/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.fxforwardcurve.BloombergFXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
@FudgeBuilderFor(FXForwardCurveSpecification.class)
public class FXForwardCurveSpecificationFudgeBuilder implements FudgeBuilder<FXForwardCurveSpecification> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FXForwardCurveSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add("name", object.getName());
    final BloombergFXForwardCurveInstrumentProvider temp = (BloombergFXForwardCurveInstrumentProvider) object.getCurveInstrumentProvider();
    final String dataFieldName = temp.getDataFieldName();
    final String prefix = temp.getPrefix();
    final String postfix = temp.getPostfix();
    message.add("dataFieldName", dataFieldName);
    message.add("prefix", prefix);
    message.add("postfix", postfix);
    //    message.add("curveInstrumentProvider", object.getCurveInstrumentProvider());
    return message;
  }

  @Override
  public FXForwardCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    //    final UnorderedCurrencyPair target = deserializer.fieldValueToObject(UnorderedCurrencyPair.class, message.getByName("target"));
    //    final String name = message.getString("name");
    //    final String dataFieldName = message.getString("dataFieldName");
    //    final String prefix = message.getString("prefix");
    //    final String postfix = message.getString("postfix");
    //final FXForwardCurveInstrumentProvider provider = deserializer.fieldValueToObject(FXForwardCurveInstrumentProvider.class, message.getByName("curveInstrumentProvider"));
    //return new FXForwardCurveSpecification(name, target, new BloombergFXForwardCurveInstrumentProvider(prefix, postfix, dataFieldName));
    return new FXForwardCurveSpecification("DEFAULT_FX_FORWARD", UnorderedCurrencyPair.of(Currency.USD, Currency.EUR), new BloombergFXForwardCurveInstrumentProvider("EUR", "Curncy", MarketDataRequirementNames.MARKET_VALUE));
  }

}
