/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code ForwardSwapSecurity}.
 */
@FudgeBuilderFor(ForwardSwapSecurity.class)
public class ForwardSwapSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ForwardSwapSecurity> {

  /** Field name. */
  public static final String FORWARD_START_DATE_FIELD_NAME = "forwardStartDate";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ForwardSwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ForwardSwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, ForwardSwapSecurity object, final MutableFudgeMsg msg) {
    SwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, FORWARD_START_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getForwardStartDate()));
  }

  @Override
  public ForwardSwapSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ForwardSwapSecurity object = new ForwardSwapSecurity();
    ForwardSwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, ForwardSwapSecurity object) {
    SwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setForwardStartDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FORWARD_START_DATE_FIELD_NAME)));
  }

}
