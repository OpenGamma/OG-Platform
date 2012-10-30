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

import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.id.ExternalId;

/**
 * Builder for converting FixedIncomeStripWithIdentifier instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithIdentifier.class)
public class FixedIncomeStripWithIdentifierFudgeBuilder implements FudgeBuilder<FixedIncomeStripWithIdentifier> {
  private static final String STRIP_NAME = "strip";
  private static final String IDENTIFIER_NAME = "identifier";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedIncomeStripWithIdentifier object) {
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, STRIP_NAME, null, object.getStrip());
    serializer.addToMessage(message, IDENTIFIER_NAME, null, object.getSecurity());
    return message; 
  }

  @Override
  public FixedIncomeStripWithIdentifier buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FixedIncomeStrip type = deserializer.fieldValueToObject(FixedIncomeStrip.class, message.getByName(STRIP_NAME));
    ExternalId security = deserializer.fieldValueToObject(ExternalId.class, message.getByName(IDENTIFIER_NAME));
    return new FixedIncomeStripWithIdentifier(type, security);
  }

}
