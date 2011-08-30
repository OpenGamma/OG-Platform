/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;

/**
 * A Fudge builder for {@code ForwardSwapSecurity}.
 */
@FudgeBuilderFor(ForwardSwapSecurity.class)
public class ForwardSwapSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ForwardSwapSecurity> {

  /** Field name. */
  public static final String FORWARD_START_DATE_KEY = "forwardStartDate";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ForwardSwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ForwardSwapSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, ForwardSwapSecurity object, final MutableFudgeMsg msg) {
    SwapSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, FORWARD_START_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getForwardStartDate()));
  }

  @Override
  public ForwardSwapSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ForwardSwapSecurity object = FinancialSecurityBuilder.backdoorCreateClass(ForwardSwapSecurity.class);
    ForwardSwapSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, ForwardSwapSecurity object) {
    SwapSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setForwardStartDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FORWARD_START_DATE_KEY)));
  }

}
