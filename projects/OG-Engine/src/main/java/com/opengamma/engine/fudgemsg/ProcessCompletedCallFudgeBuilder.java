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

import com.opengamma.engine.view.listener.ProcessCompletedCall;

/**
 * Fudge message builder for {@link ProcessCompletedCall}. 
 */
@FudgeBuilderFor(ProcessCompletedCall.class)
public class ProcessCompletedCallFudgeBuilder implements FudgeBuilder<ProcessCompletedCall> {
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ProcessCompletedCall object) {
    return serializer.newMessage();
  }

  @Override
  public ProcessCompletedCall buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return new ProcessCompletedCall();
  }

}
