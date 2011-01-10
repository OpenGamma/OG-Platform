/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time.fudgemsg;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Builder to convert ZonedDateTime to and from Fudge.
 */
@FudgeBuilderFor(ZonedDateTime.class)
public final class ZonedDateTimeBuilder implements FudgeBuilder<ZonedDateTime> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ZonedDateTime object) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    final OffsetDateTime offsetDateTime = object.toOffsetDateTime();
    String zone = object.getZone().getID();
    context.objectToFudgeMsg(msg, "offsetDateTime", null, offsetDateTime);
    msg.add("zone", zone);
    return msg;
  }

  @Override
  public ZonedDateTime buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeField fudgeField = message.getByName("offsetDateTime");
    if (fudgeField == null) {
      throw new IllegalArgumentException("Fudge message is not a ZonedDateTime - field 'offsetDateTime' is not present");
    }
    OffsetDateTime offsetDateTime = context.fieldValueToObject(OffsetDateTime.class, fudgeField);
    String zone = message.getString("zone");
    if (zone == null) {
      throw new IllegalArgumentException("Fudge message is not a ZonedDateTime - field 'zone' is not present");
    }
    return ZonedDateTime.of(offsetDateTime, TimeZone.of(zone));
  }

}
