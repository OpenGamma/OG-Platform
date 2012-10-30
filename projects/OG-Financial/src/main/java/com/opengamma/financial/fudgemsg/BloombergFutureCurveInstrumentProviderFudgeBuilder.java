/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergFutureCurveInstrumentProvider.class)
public class BloombergFutureCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergFutureCurveInstrumentProvider> {
  /**
   * type used as a human readable subclass discriminator for mongo (which strips out type information).
   */
  public static final String TYPE = "Future";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BloombergFutureCurveInstrumentProvider object) {
    MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergFutureCurveInstrumentProvider.class);
    message.add("type", TYPE); // so we can tell what type it is when mongo throws away the class header.
    message.add("prefix", object.getFuturePrefix());
    message.add("marketSector", object.getMarketSector());
    return message; 
  }

  @Override
  public BloombergFutureCurveInstrumentProvider buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return new BloombergFutureCurveInstrumentProvider(message.getString("prefix"), message.getString("marketSector"));
  }

}
