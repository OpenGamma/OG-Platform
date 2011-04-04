/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import javax.time.calendar.Period;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.time.Tenor;

/**
 * Fudge builder for {@code Tenor}.
 * There is also a secondary type.
 */
@FudgeBuilderFor(Tenor.class)
public class TenorBuilder implements FudgeBuilder<Tenor> {

  /** Field name. */
  public static final String TENOR_FIELD_NAME = "tenor";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, Tenor object) {
    final MutableFudgeMsg msg = context.newMessage();
    msg.add(TENOR_FIELD_NAME, object.getPeriod().toString());
    return msg;
  }

  @Override
  public Tenor buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    final String tenorStr = msg.getString(TENOR_FIELD_NAME);
    if (tenorStr == null) {
      throw new IllegalArgumentException("Fudge message is not a Tenor - field 'tenor' is not present");
    }
    return new Tenor(Period.parse(tenorStr));
  }

}
