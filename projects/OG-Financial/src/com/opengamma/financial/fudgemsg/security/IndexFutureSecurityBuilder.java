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

import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;

/**
 * A Fudge builder for {@code IndexFutureSecurity}.
 */
@FudgeBuilderFor(IndexFutureSecurity.class)
public class IndexFutureSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<IndexFutureSecurity> {

  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, IndexFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    IndexFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, IndexFutureSecurity object, final MutableFudgeMsg msg) {
    FutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, UNDERLYING_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
  }

  @Override
  public IndexFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    IndexFutureSecurity object = new IndexFutureSecurity();
    IndexFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, IndexFutureSecurity object) {
    FutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setUnderlyingId(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_KEY)));
  }

}
