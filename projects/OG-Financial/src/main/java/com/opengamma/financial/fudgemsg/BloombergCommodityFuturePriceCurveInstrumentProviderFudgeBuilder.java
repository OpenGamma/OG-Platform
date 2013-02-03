/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.surface.BloombergCommodityFuturePriceCurveInstrumentProvider;

/**
 *
 */
@FudgeBuilderFor(BloombergCommodityFuturePriceCurveInstrumentProvider.class)
public class BloombergCommodityFuturePriceCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergCommodityFuturePriceCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergCommodityFuturePriceCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergCommodityFuturePriceCurveInstrumentProvider.class);
    message.add("futurePrefix", object.getFuturePrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    message.add("tickerScheme", object.getTickerScheme());
    return message;
  }

  @Override
  public BloombergCommodityFuturePriceCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergCommodityFuturePriceCurveInstrumentProvider(message.getString("futurePrefix"),
        message.getString("postfix"),
        message.getString("dataFieldName"),
        message.getString("tickerScheme"));
  }
}
