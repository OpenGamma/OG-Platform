/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudge;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Fudge builder for {@code ZonedDateTime}.
 */
@FudgeBuilderFor(ZonedDateTime.class)
public final class ZonedDateTimeBuilder implements FudgeBuilder<ZonedDateTime> {

  /** Field name. */
  public static final String ODT_FIELD_NAME = "odt";
  /** Field name. */
  public static final String ZONE_FIELD_NAME = "zone";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ZonedDateTime object) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(ODT_FIELD_NAME, object.toOffsetDateTime());
    msg.add(ZONE_FIELD_NAME, object.getZone().getID());
    return msg;
  }

  @Override
  public ZonedDateTime buildObject(FudgeDeserializationContext context, FudgeFieldContainer msg) {
    final OffsetDateTime odt = msg.getValue(OffsetDateTime.class, ODT_FIELD_NAME);
    if (odt == null) {
      throw new IllegalArgumentException("Fudge message is not a ZonedDateTime - field 'odt' is not present");
    }
    final String zone = msg.getString(ZONE_FIELD_NAME);
    if (zone == null) {
      throw new IllegalArgumentException("Fudge message is not a ZonedDateTime - field 'zone' is not present");
    }
    return ZonedDateTime.of(odt, TimeZone.of(zone));
  }

}
