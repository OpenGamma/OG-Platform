/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

/**
 * A Fudge builder for {@link ZeroCouponInflationSwapSecurity}
 */
@FudgeBuilderFor(ZeroCouponInflationSwapSecurity.class)
public class ZeroCouponInflationSwapSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ZeroCouponInflationSwapSecurity> {
  /** The exchange notional field */
  private static final String EXCHANGE_NOTIONAL_FIELD = "exchangeNotional";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ZeroCouponInflationSwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    SwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    msg.add(EXCHANGE_NOTIONAL_FIELD, object.isExchangeNotional());
    return msg;
  }

  @Override
  public ZeroCouponInflationSwapSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ZeroCouponInflationSwapSecurity object = new ZeroCouponInflationSwapSecurity();
    SwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setExchangeNotional(msg.getBoolean(EXCHANGE_NOTIONAL_FIELD));
    return object;
  }
}
