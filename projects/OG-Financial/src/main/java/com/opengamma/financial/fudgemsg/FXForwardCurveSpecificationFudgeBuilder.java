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

import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification.QuoteType;
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
    message.add("quoteType", object.getQuoteType().name());
    message.add("marketQuoteConvention", object.isMarketQuoteConvention());
    serializer.addToMessageWithClassHeaders(message, "curveInstrumentProvider", null, object.getCurveInstrumentProvider());
    return message;
  }

  @Override
  public FXForwardCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UnorderedCurrencyPair target = deserializer.fieldValueToObject(UnorderedCurrencyPair.class, message.getByName("target"));
    final String name = message.getString("name");
    final FXForwardCurveInstrumentProvider provider = deserializer.fieldValueToObject(FXForwardCurveInstrumentProvider.class, message.getByName("curveInstrumentProvider"));
    if (message.hasField("quoteType")) {
      if (message.hasField("marketQuoteConvention")) {
        return new FXForwardCurveSpecification(name, target, provider, QuoteType.valueOf(message.getString("quoteType")), message.getBoolean("marketQuoteConvention"));
      }
      return new FXForwardCurveSpecification(name, target, provider, QuoteType.valueOf(message.getString("quoteType")));
    }
    return new FXForwardCurveSpecification(name, target, provider);
  }

}
