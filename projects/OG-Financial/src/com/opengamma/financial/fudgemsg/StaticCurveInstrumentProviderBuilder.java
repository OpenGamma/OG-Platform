/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.id.ExternalId;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(StaticCurveInstrumentProvider.class)
public class StaticCurveInstrumentProviderBuilder implements FudgeBuilder<StaticCurveInstrumentProvider> {
  /**
   * type used as a human readable subclass discriminator for mongo (which strips out type information).
   */
  public static final String TYPE = "Static";
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, StaticCurveInstrumentProvider object) {
    MutableFudgeMsg message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, StaticCurveInstrumentProvider.class);
    message.add("type", TYPE); // so we can tell what type it is when mongo throws away the class header.
    context.addToMessage(message, "instrument", null, object.getInstrument(null, null));
    return message; 
  }

  @Override
  public StaticCurveInstrumentProvider buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    FudgeField instrumentIdentifier = message.getByName("instrument");
    ExternalId identifier = context.fieldValueToObject(ExternalId.class, instrumentIdentifier);
    return new StaticCurveInstrumentProvider(identifier);
  }

}
