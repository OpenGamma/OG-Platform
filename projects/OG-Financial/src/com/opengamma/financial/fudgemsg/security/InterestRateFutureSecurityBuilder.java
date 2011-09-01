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

import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;

/**
 * A Fudge builder for {@code InterestRateFutureSecurity}.
 */
@FudgeBuilderFor(InterestRateFutureSecurity.class)
public class InterestRateFutureSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<InterestRateFutureSecurity> {

  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, InterestRateFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    InterestRateFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, InterestRateFutureSecurity object, final MutableFudgeMsg msg) {
    FutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, UNDERLYING_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
  }

  @Override
  public InterestRateFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    InterestRateFutureSecurity object = new InterestRateFutureSecurity();
    InterestRateFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, InterestRateFutureSecurity object) {
    FutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setUnderlyingId(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_KEY)));
  }

}
