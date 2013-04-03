/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.surface.BloombergEquityFuturePriceCurveInstrumentProvider;

/**
 *
 */
@FudgeBuilderFor(BloombergEquityFuturePriceCurveInstrumentProvider.class)
public class BloombergEquityFuturePriceCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergEquityFuturePriceCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergEquityFuturePriceCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergEquityFuturePriceCurveInstrumentProvider.class);
    message.add("futurePrefix", object.getFuturePrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    message.add("tickerScheme", object.getTickerScheme());
    message.add("exchange", object.getExchange());
    return message;
  }

  @Override
  public BloombergEquityFuturePriceCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergEquityFuturePriceCurveInstrumentProvider(message.getString("futurePrefix"),
        message.getString("postfix"),
        message.getString("dataFieldName"),
        message.getString("tickerScheme"),
        message.getString("exchange"));
  }
}
