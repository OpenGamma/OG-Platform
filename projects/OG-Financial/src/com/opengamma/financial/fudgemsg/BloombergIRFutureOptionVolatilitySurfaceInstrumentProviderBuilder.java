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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.volatility.surface.BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProviderBuilder implements FudgeBuilder<BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class);
    message.add("futureOptionPrefix", object.getFutureOptionPrefix());
    message.add("postfix", object.getPostfix());
    return message;
  }

  @Override
  public BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    return new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(message.getString("futureOptionPrefix"),
                                                                          message.getString("postfix"));
  }

}
