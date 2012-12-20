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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.id.ExternalId;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(StaticCurveInstrumentProvider.class)
public class StaticCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<StaticCurveInstrumentProvider> {
  /**
   * type used as a human readable subclass discriminator for mongo (which strips out type information).
   */
  public static final String TYPE = "Static";
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, StaticCurveInstrumentProvider object) {
    MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, StaticCurveInstrumentProvider.class);
    message.add("type", TYPE); // so we can tell what type it is when mongo throws away the class header.
    serializer.addToMessage(message, "instrument", null, object.getInstrument(null, null));
    return message; 
  }

  @Override
  public StaticCurveInstrumentProvider buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField instrumentIdentifier = message.getByName("instrument");
    ExternalId identifier = deserializer.fieldValueToObject(ExternalId.class, instrumentIdentifier);
    return new StaticCurveInstrumentProvider(identifier);
  }

}
