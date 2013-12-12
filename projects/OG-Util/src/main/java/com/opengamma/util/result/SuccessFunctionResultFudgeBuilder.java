/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

//@FudgeBuilderFor(SuccessFunctionResult.class)

public class SuccessFunctionResultFudgeBuilder implements FudgeBuilder<SuccessFunctionResult> {

  private static final String RESULT = "result";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SuccessFunctionResult result) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, RESULT, null, result.getResult());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public SuccessFunctionResult buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Object result = deserializer.fieldValueToObject(msg.getByName(RESULT));
    return new SuccessFunctionResult(result);
  }
}
