/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.analytics.util.amount.ReferenceAmount;

/**
 * Fudge builder for {@code ReferenceAmount}.
 */
@GenericFudgeBuilderFor(ReferenceAmount.class)
public final class ReferenceAmountFudgeBuilder implements FudgeBuilder<ReferenceAmount<?>> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ReferenceAmount<?> object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, null, null, object.getMap(), HashMap.class);
    return msg;
  }

  @Override
  public ReferenceAmount<?> buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    @SuppressWarnings("unchecked")
    HashMap<Object, Double> data = deserializer.fieldValueToObject(HashMap.class, msg.getByIndex(0));
    ReferenceAmount<Object> amount = new ReferenceAmount<>();
    for (Map.Entry<Object, Double> entry : data.entrySet()) {
      amount.add(entry.getKey(), entry.getValue());
    }
    return amount;
  }

}
