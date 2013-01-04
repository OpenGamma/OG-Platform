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
 * Builder for converting BloombergFutureCurveInstrumentProvider instances to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergFutureCurveInstrumentProvider.class)
public class BloombergFutureCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergFutureCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergFutureCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("prefix", object.getFuturePrefix());
    message.add("marketSector", object.getMarketSector());
    return message;
  }

  @Override
  public BloombergFutureCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new BloombergFutureCurveInstrumentProvider(message.getString("prefix"), message.getString("marketSector"));
  }

}
