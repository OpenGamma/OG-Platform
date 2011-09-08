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

import com.opengamma.financial.analytics.volatility.surface.BloombergEquityOptionVolatilitySurfaceInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergEquityOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergEquityOptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergEquityOptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergEquityOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergEquityOptionVolatilitySurfaceInstrumentProvider.class);
    message.add("underlyingPrefix", object.getUnderlyingPrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergEquityOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergEquityOptionVolatilitySurfaceInstrumentProvider(
        message.getString("underlyingPrefix"), message.getString("postfix"), message.getString("dataFieldName"));
  }

}
