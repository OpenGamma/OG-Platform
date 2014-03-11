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

/**
 * Fudge builder for SuccessResult.
 */
@FudgeBuilderFor(SuccessResult.class)
public class SuccessResultFudgeBuilder implements FudgeBuilder<SuccessResult<?>> {

  private static final String RESULT = "result";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SuccessResult<?> result) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(msg, RESULT, null, result.getValue());
    return msg;
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Override
  public SuccessResult<?> buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Object result = deserializer.fieldValueToObject(msg.getByName(RESULT));
    return new SuccessResult(result);
  }

}
