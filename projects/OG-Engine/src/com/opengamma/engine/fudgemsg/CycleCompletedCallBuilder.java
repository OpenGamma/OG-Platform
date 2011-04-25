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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.listener.CycleCompletedCall;

/**
 * Fudge message builder for {@link CycleCompletedCall}
 */
@FudgeBuilderFor(CycleCompletedCall.class)
public class CycleCompletedCallBuilder implements FudgeBuilder<CycleCompletedCall> {

  private static final String FULL_RESULT_FIELD = "fullResult";
  private static final String DELTA_RESULT_FIELD = "deltaResult";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, CycleCompletedCall object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, FULL_RESULT_FIELD, null, object.getFullResult());
    context.addToMessage(msg, DELTA_RESULT_FIELD, null, object.getDeltaResult());
    return msg;
  }

  @Override
  public CycleCompletedCall buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    FudgeField fullResultField = msg.getByName(FULL_RESULT_FIELD);
    ViewComputationResultModel fullResult = fullResultField != null ? context.fieldValueToObject(ViewComputationResultModel.class, fullResultField) : null;
    FudgeField deltaResultField = msg.getByName(DELTA_RESULT_FIELD);
    ViewDeltaResultModel deltaResult = deltaResultField != null ? context.fieldValueToObject(ViewDeltaResultModel.class, deltaResultField) : null;
    return new CycleCompletedCall(fullResult, deltaResult);
  }

}
