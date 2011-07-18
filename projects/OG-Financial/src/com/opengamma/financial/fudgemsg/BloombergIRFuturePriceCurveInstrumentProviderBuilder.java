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

import com.opengamma.financial.analytics.volatility.surface.BloombergIRFuturePriceCurveInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(BloombergIRFuturePriceCurveInstrumentProvider.class)
public class BloombergIRFuturePriceCurveInstrumentProviderBuilder implements FudgeBuilder<BloombergIRFuturePriceCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final BloombergIRFuturePriceCurveInstrumentProvider object) {
    final MutableFudgeMsg message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, BloombergIRFuturePriceCurveInstrumentProvider.class);
    message.add("futurePrefix", object.getFuturePrefix());
    message.add("postfix", object.getPostfix());
    message.add("dataFieldName", object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergIRFuturePriceCurveInstrumentProvider buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    return new BloombergIRFuturePriceCurveInstrumentProvider(message.getString("futurePrefix"),
                                                             message.getString("postfix"),
                                                             message.getString("dataFieldName"));
  }
}
