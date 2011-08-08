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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStripWithSecurity.class)
public class FixedIncomeStripWithSecurityBuilder implements FudgeBuilder<FixedIncomeStripWithSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, FixedIncomeStripWithSecurity object) {
    MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, "type", null, object.getInstrumentType());
    context.addToMessage(message, "tenor", null, object.getTenor());
    context.addToMessage(message, "resolvedTenor", null, object.getResolvedTenor());
    ZonedDateTimeBuilder zonedDateTimeBuilder = new ZonedDateTimeBuilder();
    MutableFudgeMsg subMessage = zonedDateTimeBuilder.buildMessage(context, object.getMaturity());
    context.addToMessage(message, "maturity", null, subMessage);
    context.addToMessage(message, "identifier", null, object.getSecurityIdentifier());
    context.addToMessageWithClassHeaders(message, "security", null, object.getSecurity());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add("numFutures", object.getNumberOfFuturesAfterTenor());
    }
    return message; 
  }

  @Override
  public FixedIncomeStripWithSecurity buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    StripInstrumentType type = context.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    Tenor tenor = context.fieldValueToObject(Tenor.class, message.getByName("tenor"));
    Tenor resolvedTenor = context.fieldValueToObject(Tenor.class, message.getByName("resolvedTenor"));
    ZonedDateTimeBuilder zonedDateTimeBuilder = new ZonedDateTimeBuilder();
    ZonedDateTime maturity = zonedDateTimeBuilder.buildObject(context, message.getMessage("maturity"));
    ExternalId identifier = context.fieldValueToObject(ExternalId.class, message.getByName("identifier"));
    Security security = (Security) context.fieldValueToObject(message.getByName("security"));
    if (type == StripInstrumentType.FUTURE) {
      int numFutures = message.getInt("numFutures");
      return new FixedIncomeStripWithSecurity(type, tenor, resolvedTenor, numFutures, maturity, identifier, security);
    } else { 
      return new FixedIncomeStripWithSecurity(type, tenor, resolvedTenor, maturity, identifier, security);
    }
  }

}
