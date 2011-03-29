/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithSecurity.class)
public class FixedIncomeStripWithSecurityBuilder implements FudgeBuilder<FixedIncomeStripWithSecurity> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, FixedIncomeStripWithSecurity object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "type", null, object.getInstrumentType());
    context.objectToFudgeMsg(message, "tenor", null, object.getTenor());
    context.objectToFudgeMsg(message, "resolvedTenor", null, object.getResolvedTenor());
    ZonedDateTimeBuilder zonedDateTimeBuilder = new ZonedDateTimeBuilder();
    MutableFudgeFieldContainer subMessage = zonedDateTimeBuilder.buildMessage(context, object.getMaturity());
    context.objectToFudgeMsg(message, "maturity", null, subMessage);
    context.objectToFudgeMsg(message, "identifier", null, object.getSecurityIdentifier());
    context.objectToFudgeMsgWithClassHeaders(message, "security", null, object.getSecurity());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add("numFutures", object.getNumberOfFuturesAfterTenor());
    }
    return message; 
  }

  @Override
  public FixedIncomeStripWithSecurity buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    StripInstrumentType type = context.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    Tenor tenor = context.fieldValueToObject(Tenor.class, message.getByName("tenor"));
    Tenor resolvedTenor = context.fieldValueToObject(Tenor.class, message.getByName("resolvedTenor"));
    ZonedDateTimeBuilder zonedDateTimeBuilder = new ZonedDateTimeBuilder();
    ZonedDateTime maturity = zonedDateTimeBuilder.buildObject(context, message.getMessage("maturity"));
    Identifier identifier = context.fieldValueToObject(Identifier.class, message.getByName("identifier"));
    Security security = (Security) context.fieldValueToObject(message.getByName("security"));
    if (type == StripInstrumentType.FUTURE) {
      int numFutures = message.getInt("numFutures");
      return new FixedIncomeStripWithSecurity(type, tenor, resolvedTenor, numFutures, maturity, identifier, security);
    } else { 
      return new FixedIncomeStripWithSecurity(type, tenor, resolvedTenor, maturity, identifier, security);
    }
  }

}
