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

/**
 * Fudge builder for {@code LocalDate}.
 * For cases where you're passing as LocalDate in a polymorphic field (e.g. generics) and you
 * can't use the secondary type encoding directly.
 */
@FudgeBuilderFor(LocalDate.class)
public final class LocalDateFudgeBuilder implements FudgeBuilder<LocalDate> {

  /** Field name. */
  public static final String DATE_FIELD_NAME = "date";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, LocalDate object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, DATE_FIELD_NAME, null, object);
    return msg;
  }

  @Override
  public LocalDate buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final LocalDate ld = msg.getValue(LocalDate.class, DATE_FIELD_NAME);
    if (ld == null) {
      throw new IllegalArgumentException("Fudge message is not a LocalDate - field 'date' is not present");
    }
    return ld;
  }

}
