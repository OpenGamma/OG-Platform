/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.listener.JobResultReceivedCall;

/**
 * Fudge message builder for {@link JobResultReceivedCall}
 */
@FudgeBuilderFor(JobResultReceivedCall.class)
public class JobResultReceivedCallFudgeBuilder implements FudgeBuilder<JobResultReceivedCall> {

  private static final String FULL_RESULT_FIELD = "fullResult";
  private static final String DELTA_RESULT_FIELD = "deltaResult";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, JobResultReceivedCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, FULL_RESULT_FIELD, null, object.getFullResult());
    serializer.addToMessage(msg, DELTA_RESULT_FIELD, null, object.getDeltaResult());
    return msg;
  }

  @Override
  public JobResultReceivedCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField fullResultField = msg.getByName(FULL_RESULT_FIELD);
    ViewResultModel fullResult = fullResultField != null ? deserializer.fieldValueToObject(ViewResultModel.class, fullResultField) : null;
    FudgeField deltaResultField = msg.getByName(DELTA_RESULT_FIELD);
    ViewDeltaResultModel deltaResult = deltaResultField != null ? deserializer.fieldValueToObject(ViewDeltaResultModel.class, deltaResultField) : null;
    return new JobResultReceivedCall(fullResult, deltaResult);
  }

}
