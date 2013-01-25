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
import org.threeten.bp.LocalDate;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code LocalDateRange}.
 */
@FudgeBuilderFor(LocalDateRange.class)
public final class LocalDateRangeFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<LocalDateRange> {

  /** Field name. */
  public static final String START_FIELD_NAME = "start";
  /** Field name. */
  public static final String END_FIELD_NAME = "end";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, LocalDateRange object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final LocalDateRange object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final LocalDateRange object, final MutableFudgeMsg msg) {
    addToMessage(msg, START_FIELD_NAME, object.getStartDateInclusive());
    addToMessage(msg, END_FIELD_NAME, object.getEndDateInclusive());
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateRange buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static LocalDateRange fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final LocalDate start = msg.getValue(LocalDate.class, START_FIELD_NAME);
    final LocalDate end = msg.getValue(LocalDate.class, END_FIELD_NAME);
    return LocalDateRange.of(start, end, true);
  }

}
