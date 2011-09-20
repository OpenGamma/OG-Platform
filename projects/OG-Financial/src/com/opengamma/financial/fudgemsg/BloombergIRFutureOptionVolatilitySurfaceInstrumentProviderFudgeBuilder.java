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

import com.opengamma.financial.analytics.volatility.surface.BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class);
    message.add("futureOptionPrefix", object.getFutureOptionPrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    message.add("useCallAboveStrikeValue", object.useCallAboveStrike());
    return message;
  }

  @Override
  public BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(message.getString("futureOptionPrefix"),
                                                                          message.getString("postfix"),
                                                                          message.getString("dataFieldName"),
                                                                          Double.parseDouble(message.getString("useCallAboveStrikeValue")));
  }

}
