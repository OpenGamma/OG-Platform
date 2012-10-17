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

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.listener.CycleCompletedCall;

/**
 * Fudge message builder for {@link CycleCompletedCall}
 */
@FudgeBuilderFor(CycleCompletedCall.class)
public class CycleCompletedCallFudgeBuilder implements FudgeBuilder<CycleCompletedCall> {

  private static final String FULL_RESULT_FIELD = "fullResult";
  private static final String DELTA_RESULT_FIELD = "deltaResult";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CycleCompletedCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ViewComputationResultModel fullResult = object.getFullResult();
    ViewDeltaResultModel deltaResult = object.getDeltaResult();
    serializer.addToMessage(msg, FULL_RESULT_FIELD, null, fullResult);
    serializer.addToMessage(msg, DELTA_RESULT_FIELD, null, deltaResult);
    return msg;
  }

  @Override
  public CycleCompletedCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField fullResultField = msg.getByName(FULL_RESULT_FIELD);
    ViewComputationResultModel fullResult = fullResultField != null ? deserializer.fieldValueToObject(ViewComputationResultModel.class, fullResultField) : null;
    FudgeField deltaResultField = msg.getByName(DELTA_RESULT_FIELD);
    ViewDeltaResultModel deltaResult = deltaResultField != null ? deserializer.fieldValueToObject(ViewDeltaResultModel.class, deltaResultField) : null;
    return new CycleCompletedCall(fullResult, deltaResult);
  }

}
