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
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithIdentifier.class)
public class ResolvedFixedIncomeStripBuilder implements FudgeBuilder<FixedIncomeStripWithIdentifier> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedIncomeStripWithIdentifier object) {
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "type", null, object.getInstrumentType());
    serializer.addToMessage(message, "maturity", null, object.getMaturity());
    serializer.addToMessage(message, "security", null, object.getSecurity());
    return message; 
  }

  @Override
  public FixedIncomeStripWithIdentifier buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    StripInstrumentType type = deserializer.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    Tenor maturity = deserializer.fieldValueToObject(Tenor.class, message.getByName("maturity"));
    ExternalId security = deserializer.fieldValueToObject(ExternalId.class, message.getByName("security"));
    return new FixedIncomeStripWithIdentifier(type, maturity, security);
  }

}
