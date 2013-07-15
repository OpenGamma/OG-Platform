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
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithSecurity.class)
public class FixedIncomeStripWithSecurityFudgeBuilder implements FudgeBuilder<FixedIncomeStripWithSecurity> {
  private static final String STRIP_NAME = "strip";
  private static final String RESOLVED_TENOR_NAME = "resolvedTenor";
  private static final String MATURITY_NAME = "maturity";
  private static final String IDENTIFIER_NAME = "identifier";
  private static final String SECURITY_NAME = "security";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedIncomeStripWithSecurity object) {
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, STRIP_NAME, null, object.getStrip());
    serializer.addToMessage(message, RESOLVED_TENOR_NAME, null, object.getResolvedTenor());
    ZonedDateTimeFudgeBuilder zonedDateTimeBuilder = new ZonedDateTimeFudgeBuilder();
    MutableFudgeMsg subMessage = zonedDateTimeBuilder.buildMessage(serializer, object.getMaturity());
    serializer.addToMessage(message, MATURITY_NAME, null, subMessage);
    serializer.addToMessage(message, IDENTIFIER_NAME, null, object.getSecurityIdentifier());
    serializer.addToMessageWithClassHeaders(message, SECURITY_NAME, null, object.getSecurity());
    return message; 
  }

  @Override
  public FixedIncomeStripWithSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FixedIncomeStrip strip = deserializer.fieldValueToObject(FixedIncomeStrip.class, message.getByName(STRIP_NAME));
    Tenor resolvedTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(RESOLVED_TENOR_NAME));
    ZonedDateTimeFudgeBuilder zonedDateTimeBuilder = new ZonedDateTimeFudgeBuilder();
    ZonedDateTime maturity = zonedDateTimeBuilder.buildObject(deserializer, message.getMessage(MATURITY_NAME));
    ExternalId identifier = deserializer.fieldValueToObject(ExternalId.class, message.getByName(IDENTIFIER_NAME));
    Security security = (Security) deserializer.fieldValueToObject(message.getByName(SECURITY_NAME));
    return new FixedIncomeStripWithSecurity(strip, resolvedTenor, maturity, identifier, security);
  }

}
