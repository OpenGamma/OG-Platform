/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code ZonedDateTime}.
 */
@FudgeBuilderFor(ZonedDateTime.class)
public final class ZonedDateTimeFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ZonedDateTime> {

  /** Field name. */
  public static final String DATETIME_FIELD_NAME = "datetime";
  /** Field name. */
  public static final String ZONE_FIELD_NAME = "zone";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ZonedDateTime object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ZonedDateTime object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final ZonedDateTime object, final MutableFudgeMsg msg) {
    addToMessage(msg, DATETIME_FIELD_NAME, object.toOffsetDateTime());
    addToMessage(msg, ZONE_FIELD_NAME, object.getZone());
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTime buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static ZonedDateTime fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final OffsetDateTime odt = msg.getValue(OffsetDateTime.class, DATETIME_FIELD_NAME);
    final ZoneId zone = msg.getValue(ZoneId.class, ZONE_FIELD_NAME);
    return odt.atZoneSameInstant(zone);
  }

}
