/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;

/**
 * Fudge message builder for {@link CycleExecutionFailedCall}
 */
@FudgeBuilderFor(CycleExecutionFailedCall.class)
public class CycleExecutionFailedCallFudgeBuilder implements FudgeBuilder<CycleExecutionFailedCall> {

  private static final String EXECUTION_OPTIONS_FIELD = "executionOptions";
  private static final String EXCEPTION_FIELD = "exception";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CycleExecutionFailedCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, EXECUTION_OPTIONS_FIELD, null, object.getExecutionOptions());
    serializer.addToMessage(msg, EXCEPTION_FIELD, null, object.getException());
    return msg;
  }

  @Override
  public CycleExecutionFailedCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ViewCycleExecutionOptions cycleExecutionOptions = deserializer.fieldValueToObject(ViewCycleExecutionOptions.class, msg.getByName(EXECUTION_OPTIONS_FIELD));
    FudgeField exceptionField = msg.getByName(EXCEPTION_FIELD);
    Exception exception = exceptionField != null ? deserializer.fieldValueToObject(Exception.class, exceptionField) : null;
    return new CycleExecutionFailedCall(cycleExecutionOptions, exception);
  }

}
