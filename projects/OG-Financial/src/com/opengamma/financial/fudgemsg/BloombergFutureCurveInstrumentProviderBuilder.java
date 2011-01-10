/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergFutureCurveInstrumentProvider.class)
public class BloombergFutureCurveInstrumentProviderBuilder implements FudgeBuilder<BloombergFutureCurveInstrumentProvider> {
  /**
   * type used as a human readable subclass discriminator for mongo (which strips out type information).
   */
  public static final String TYPE = "Future";
  
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, BloombergFutureCurveInstrumentProvider object) {
    MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, BloombergFutureCurveInstrumentProvider.class);
    message.add("type", TYPE); // so we can tell what type it is when mongo throws away the class header.
    message.add("prefix", object.getFuturePrefix());
    message.add("marketSector", object.getMarketSector());
    return message; 
  }

  @Override
  public BloombergFutureCurveInstrumentProvider buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return new BloombergFutureCurveInstrumentProvider(message.getString("prefix"), message.getString("marketSector"));
  }

}
