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

import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.id.Identifier;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
public class StaticCurveInstrumentProviderBuilder implements FudgeBuilder<StaticCurveInstrumentProvider> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, StaticCurveInstrumentProvider object) {
    MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, StaticCurveInstrumentProvider.class);
    context.objectToFudgeMsg(message, "instrument", null, object.getInstrument(null, null));
    return message; 
  }

  @Override
  public StaticCurveInstrumentProvider buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeFieldContainer instrumentIdentifier = message.getMessage("instrument");
    Identifier identifier = context.fudgeMsgToObject(Identifier.class, instrumentIdentifier);
    return new StaticCurveInstrumentProvider(identifier);
  }

}
