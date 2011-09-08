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

import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting FixedIncomeStripWithIdentifier instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithIdentifier.class)
public class FixedIncomeStripWithIdentifierBuilder implements FudgeBuilder<FixedIncomeStripWithIdentifier> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedIncomeStripWithIdentifier object) {
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "type", null, object.getInstrumentType());
    serializer.addToMessage(message, "tenor", null, object.getMaturity());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add("numFutures", object.getNumberOfFuturesAfterTenor());
    }
    serializer.addToMessage(message, "identifier", null, object.getSecurity());
    return message; 
  }

  @Override
  public FixedIncomeStripWithIdentifier buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    StripInstrumentType type = deserializer.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName("tenor"));
    ExternalId security = deserializer.fieldValueToObject(ExternalId.class, message.getByName("identifier"));
    if (type == StripInstrumentType.FUTURE) {
      int numFutures = message.getInt("numFutures");
      return new FixedIncomeStripWithIdentifier(type, tenor, numFutures, security);
    } else { 
      return new FixedIncomeStripWithIdentifier(type, tenor, security);
    }
  }

}
