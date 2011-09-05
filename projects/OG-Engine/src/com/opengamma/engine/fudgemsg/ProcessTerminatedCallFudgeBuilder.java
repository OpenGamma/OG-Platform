/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.listener.ProcessTerminatedCall;

/**
 * Fudge message builder for {@link ProcessTerminatedCall}
 */
@FudgeBuilderFor(ProcessTerminatedCall.class)
public class ProcessTerminatedCallFudgeBuilder implements FudgeBuilder<ProcessTerminatedCall> {

  private static final String EXECUTION_INTERRUPTED_FIELD = "executionInterrupted";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ProcessTerminatedCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(EXECUTION_INTERRUPTED_FIELD, object.isExecutionInterrupted());
    return msg;
  }

  @Override
  public ProcessTerminatedCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    boolean executionInterrupted = msg.getBoolean(EXECUTION_INTERRUPTED_FIELD);
    return new ProcessTerminatedCall(executionInterrupted);
  }

}
