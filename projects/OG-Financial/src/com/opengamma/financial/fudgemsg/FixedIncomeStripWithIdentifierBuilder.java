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

import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithIdentifier.class)
public class FixedIncomeStripWithIdentifierBuilder implements FudgeBuilder<FixedIncomeStripWithIdentifier> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, FixedIncomeStripWithIdentifier object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "type", null, object.getInstrumentType());
    context.objectToFudgeMsg(message, "tenor", null, object.getMaturity());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add("numFutures", object.getNumberOfFuturesAfterTenor());
    }
    context.objectToFudgeMsg(message, "identifier", null, object.getSecurity());
    return message; 
  }

  @Override
  public FixedIncomeStripWithIdentifier buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    StripInstrumentType type = context.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    Tenor tenor = context.fieldValueToObject(Tenor.class, message.getByName("tenor"));
    Identifier security = context.fieldValueToObject(Identifier.class, message.getByName("identifier"));
    if (type == StripInstrumentType.FUTURE) {
      int numFutures = message.getInt("numFutures");
      return new FixedIncomeStripWithIdentifier(type, tenor, numFutures, security);
    } else { 
      return new FixedIncomeStripWithIdentifier(type, tenor, security);
    }
  }

}
