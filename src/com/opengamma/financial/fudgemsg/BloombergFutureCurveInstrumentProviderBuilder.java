/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
public class BloombergFutureCurveInstrumentProviderBuilder implements FudgeBuilder<BloombergFutureCurveInstrumentProvider> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, BloombergFutureCurveInstrumentProvider object) {
    MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, BloombergFutureCurveInstrumentProvider.class);
    message.add("prefix", object.getFuturePrefix());
    message.add("marketSector", object.getMarketSector());
    return message; 
  }

  @Override
  public BloombergFutureCurveInstrumentProvider buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return new BloombergFutureCurveInstrumentProvider(message.getString("prefix"), message.getString("marketSector"));
  }

}
