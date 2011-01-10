/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
public class ResolvedFixedIncomeStripBuilder implements FudgeBuilder<FixedIncomeStripWithIdentifier> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, FixedIncomeStripWithIdentifier object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "type", null, object.getInstrumentType());
    context.objectToFudgeMsg(message, "maturity", null, object.getMaturity());
    context.objectToFudgeMsg(message, "security", null, object.getSecurity());
    return message; 
  }

  @Override
  public FixedIncomeStripWithIdentifier buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    StripInstrumentType type = context.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    Tenor maturity = context.fieldValueToObject(Tenor.class, message.getByName("maturity"));
    Identifier security = context.fieldValueToObject(Identifier.class, message.getByName("security"));
    return new FixedIncomeStripWithIdentifier(type, maturity, security);
  }

}
