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

import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code AgricultureFutureSecurity}.
 */
@FudgeBuilderFor(AgricultureFutureSecurity.class)
public class AgricultureFutureSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<AgricultureFutureSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, AgricultureFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    AgricultureFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, AgricultureFutureSecurity object, final MutableFudgeMsg msg) {
    CommodityFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
  }

  @Override
  public AgricultureFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    AgricultureFutureSecurity object = FinancialSecurityBuilder.backdoorCreateClass(AgricultureFutureSecurity.class);
    AgricultureFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, AgricultureFutureSecurity object) {
    CommodityFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
  }

}
