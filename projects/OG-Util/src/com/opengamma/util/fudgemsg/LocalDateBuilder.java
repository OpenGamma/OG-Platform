/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Fudge builder for {@code LocalDate}.
 * For cases where you're passing as LocalDate in a polymorphic field (e.g. generics) and you
 * can't use the secondary type encoding directly.
 */
@FudgeBuilderFor(LocalDate.class)
public final class LocalDateBuilder implements FudgeBuilder<LocalDate> {

  /** Field name. */
  public static final String DATE_FIELD_NAME = "date";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, LocalDate object) {
    final MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, DATE_FIELD_NAME, null, object);
    return msg;
  }

  @Override
  public LocalDate buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    final LocalDate ld = msg.getValue(LocalDate.class, DATE_FIELD_NAME);
    if (ld == null) {
      throw new IllegalArgumentException("Fudge message is not a LocalDate - field 'date' is not present");
    }
    return ld;
  }

}
