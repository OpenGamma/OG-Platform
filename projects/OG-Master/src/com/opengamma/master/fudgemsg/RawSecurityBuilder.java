/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.master.security.RawSecurity;

/**
 * A Fudge builder for {@code RawSecurity}.
 */
@FudgeBuilderFor(RawSecurity.class)
public class RawSecurityBuilder implements FudgeBuilder<RawSecurity> {

  /** Field name. */
  public static final String RAW_DATA_KEY = "rawData";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, RawSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ManageableSecurityBuilder.buildMessage(serializer, object, msg);
    RawSecurityBuilder.buildMessage(serializer, object, msg);
    return msg;
  }

  public static void buildMessage(FudgeSerializer serializer, RawSecurity object, final MutableFudgeMsg msg) {
    serializer.addToMessage(msg, RAW_DATA_KEY, null, object.getRawData());
  }

  @Override
  public RawSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    RawSecurity object = new RawSecurity("", new byte[0]);
    ManageableSecurityBuilder.buildObject(deserializer, msg, object);
    RawSecurityBuilder.buildObject(deserializer, msg, object);
    return object;
  }

  public static void buildObject(FudgeDeserializer deserializer, FudgeMsg msg, RawSecurity object) {
    object.setRawData(msg.getValue(byte[].class, RAW_DATA_KEY));
  }

}
