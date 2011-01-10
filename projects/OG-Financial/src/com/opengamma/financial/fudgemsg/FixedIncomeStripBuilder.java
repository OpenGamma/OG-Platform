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

import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStrip.class)
public class FixedIncomeStripBuilder implements FudgeBuilder<FixedIncomeStrip> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, FixedIncomeStrip object) {
    MutableFudgeFieldContainer message = context.newMessage();
    //FudgeSerializationContext.addClassHeader(message, FixedIncomeStrip.class);
    //message.add("type", object.getInstrumentType().name());
    context.objectToFudgeMsg(message, "type", null, object.getInstrumentType());
    message.add("conventionName", object.getConventionName());
    context.objectToFudgeMsg(message, "tenor", null, object.getCurveNodePointTime());
    //message.add("tenorAsPeriod", object.getCurveNodePointTime().getPeriod().toString());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add("numFutures", object.getNumberOfFuturesAfterTenor());
    }
    return message; 
  }

  @Override
  public FixedIncomeStrip buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    //StripInstrumentType type = StripInstrumentType.valueOf(message.getString("type"));
    StripInstrumentType type = context.fieldValueToObject(StripInstrumentType.class, message.getByName("type"));
    String conventionName = message.getString("conventionName");
    Tenor tenor = context.fieldValueToObject(Tenor.class, message.getByName("tenor"));
    //Tenor tenor = new Tenor(Period.parse(message.getString("tenorAsPeriod")));
    if (type == StripInstrumentType.FUTURE) {
      int numFutures = message.getInt("numFutures");
      return new FixedIncomeStrip(type, tenor, numFutures, conventionName);
    } else { 
      return new FixedIncomeStrip(type, tenor, conventionName);
    }
  }

}
