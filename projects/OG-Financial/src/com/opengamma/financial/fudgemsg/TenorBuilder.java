/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import javax.time.calendar.Period;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.time.Tenor;

/**
 * Builder for converting Tenor instances to/from Fudge messages.  Sometimes we need this in preference to a secondary type.
 */
@FudgeBuilderFor(Tenor.class)
public class TenorBuilder implements FudgeBuilder<Tenor> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, Tenor object) {
    MutableFudgeFieldContainer message = context.newMessage();
    message.add("tenor", object.getPeriod().toString());
    return message; 
  }

  @Override
  public Tenor buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    String tenorStr = message.getString("tenor");
    return new Tenor(Period.parse(tenorStr));
  }

}
