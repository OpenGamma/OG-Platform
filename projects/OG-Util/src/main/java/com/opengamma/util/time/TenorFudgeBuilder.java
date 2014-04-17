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

/**
 * Fudge builder for {@code Tenor}.
 * There is also a secondary type.
 */
@FudgeBuilderFor(Tenor.class)
public class TenorFudgeBuilder implements FudgeBuilder<Tenor> {

  /** Field name. */
  public static final String TENOR_FIELD_NAME = "tenor";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Tenor object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    
    msg.add(TENOR_FIELD_NAME, object.toFormattedString());
    return msg;
  }

  @Override
  public Tenor buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final String tenorStr = msg.getString(TENOR_FIELD_NAME);
    if (tenorStr == null) {
      throw new IllegalArgumentException("Fudge message is not a Tenor - field 'tenor' is not present");
    }
    return Tenor.parse(tenorStr);
  }

}
