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
 * Builder for converting StaticCurveInstrumentProvider instances to/from Fudge messages.
 */
@FudgeBuilderFor(StaticCurveInstrumentProvider.class)
public class StaticCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<StaticCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final StaticCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "instrument", null, object.getInstrument(null, null));
    return message;
  }

  @Override
  public StaticCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FudgeField instrumentIdentifier = message.getByName("instrument");
    final ExternalId identifier = deserializer.fieldValueToObject(ExternalId.class, instrumentIdentifier);
    return new StaticCurveInstrumentProvider(identifier);
  }

}
