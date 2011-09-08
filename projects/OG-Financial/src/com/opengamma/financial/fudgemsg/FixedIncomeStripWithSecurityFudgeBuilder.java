/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithSecurity.class)
public class FixedIncomeStripWithSecurityFudgeBuilder implements FudgeBuilder<FixedIncomeStripWithSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedIncomeStripWithSecurity object) {
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "type", null, object.getInstrumentType());
    serializer.addToMessage(message, "tenor", null, object.getTenor());
    serializer.addToMessage(message, "resolvedTenor", null, object.getResolvedTenor());
    ZonedDateTimeFudgeBuilder zonedDateTimeBuilder = new ZonedDateTimeFudgeBuilder();
    MutableFudgeMsg subMessage = zonedDateTimeBuilder.buildMessage(serializer, object.getMaturity());
    serializer.addToMessage(message, "maturity", null, subMessage);
    serializer.addToMessage(message, "identifier", null, object.getSecurityIdentifier());
    serializer.addToMessageWithClassHeaders(message, "security", null, object.getSecurity());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add("numFutures", object.getNumberOfFuturesAfterTenor());
    }
    return message; 
  }

  @Override
  public FixedIncomeStripWithSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    StripInstrumentType type = deserializer.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName("tenor"));
    Tenor resolvedTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName("resolvedTenor"));
    ZonedDateTimeFudgeBuilder zonedDateTimeBuilder = new ZonedDateTimeFudgeBuilder();
    ZonedDateTime maturity = zonedDateTimeBuilder.buildObject(deserializer, message.getMessage("maturity"));
    ExternalId identifier = deserializer.fieldValueToObject(ExternalId.class, message.getByName("identifier"));
    Security security = (Security) deserializer.fieldValueToObject(message.getByName("security"));
    if (type == StripInstrumentType.FUTURE) {
      int numFutures = message.getInt("numFutures");
      return new FixedIncomeStripWithSecurity(type, tenor, resolvedTenor, numFutures, maturity, identifier, security);
    } else { 
      return new FixedIncomeStripWithSecurity(type, tenor, resolvedTenor, maturity, identifier, security);
    }
  }

}
