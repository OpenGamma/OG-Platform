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

import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code EquityIndexDividendFutureSecurity}.
 */
@FudgeBuilderFor(EquityIndexDividendFutureSecurity.class)
public class EquityIndexDividendFutureSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquityIndexDividendFutureSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EquityIndexDividendFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquityIndexDividendFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EquityIndexDividendFutureSecurity object, final MutableFudgeMsg msg) {
    EquityFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
  }

  @Override
  public EquityIndexDividendFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EquityIndexDividendFutureSecurity object = new EquityIndexDividendFutureSecurity();
    EquityIndexDividendFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EquityIndexDividendFutureSecurity object) {
    EquityFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
  }

}
