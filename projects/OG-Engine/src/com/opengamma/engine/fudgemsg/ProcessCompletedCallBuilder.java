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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.listener.ProcessCompletedCall;

/**
 * Fudge message builder for {@link ProcessCompletedCall}. 
 */
@FudgeBuilderFor(ProcessCompletedCall.class)
public class ProcessCompletedCallBuilder implements FudgeBuilder<ProcessCompletedCall> {
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ProcessCompletedCall object) {
    return context.newMessage();
  }

  @Override
  public ProcessCompletedCall buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    return new ProcessCompletedCall();
  }

}
