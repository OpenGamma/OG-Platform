/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.listener.ProcessTerminatedCall;

/**
 * Fudge message builder for {@link ProcessTerminatedCall}
 */
@FudgeBuilderFor(ProcessTerminatedCall.class)
public class ProcessTerminatedCallBuilder implements FudgeBuilder<ProcessTerminatedCall> {

  private static final String EXECUTION_INTERRUPTED_FIELD = "executionInterrupted";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ProcessTerminatedCall object) {
    MutableFudgeMsg msg = context.newMessage();
    msg.add(EXECUTION_INTERRUPTED_FIELD, object.isExecutionInterrupted());
    return msg;
  }

  @Override
  public ProcessTerminatedCall buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    boolean executionInterrupted = msg.getBoolean(EXECUTION_INTERRUPTED_FIELD);
    return new ProcessTerminatedCall(executionInterrupted);
  }

}
