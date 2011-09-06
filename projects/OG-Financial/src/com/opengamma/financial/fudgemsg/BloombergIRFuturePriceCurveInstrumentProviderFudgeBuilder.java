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

import com.opengamma.financial.analytics.volatility.surface.BloombergIRFuturePriceCurveInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergIRFuturePriceCurveInstrumentProvider.class)
public class BloombergIRFuturePriceCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergIRFuturePriceCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergIRFuturePriceCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergIRFuturePriceCurveInstrumentProvider.class);
    message.add("futurePrefix", object.getFuturePrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergIRFuturePriceCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergIRFuturePriceCurveInstrumentProvider(message.getString("futurePrefix"),
                                                             message.getString("postfix"),
                                                             message.getString("dataFieldName"));
  }
}
