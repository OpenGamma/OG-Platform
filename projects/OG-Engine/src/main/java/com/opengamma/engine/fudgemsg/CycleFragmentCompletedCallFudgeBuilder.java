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

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.listener.CycleFragmentCompletedCall;

/**
 * Fudge message builder for {@link CycleFragmentCompletedCall}
 */
@FudgeBuilderFor(CycleFragmentCompletedCall.class)
public class CycleFragmentCompletedCallFudgeBuilder implements FudgeBuilder<CycleFragmentCompletedCall> {

  private static final String FULL_FRAGMENT_FIELD = "fullFragment";
  private static final String DELTA_FRAGMENT_FIELD = "deltaFragment";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CycleFragmentCompletedCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, FULL_FRAGMENT_FIELD, null, object.getFullFragment());
    serializer.addToMessage(msg, DELTA_FRAGMENT_FIELD, null, object.getDeltaFragment());
    return msg;
  }

  @Override
  public CycleFragmentCompletedCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField fullResultField = msg.getByName(FULL_FRAGMENT_FIELD);
    ViewComputationResultModel fullResult = fullResultField != null ? deserializer.fieldValueToObject(ViewComputationResultModel.class, fullResultField) : null;
    FudgeField deltaResultField = msg.getByName(DELTA_FRAGMENT_FIELD);
    ViewDeltaResultModel deltaResult = deltaResultField != null ? deserializer.fieldValueToObject(ViewDeltaResultModel.class, deltaResultField) : null;
    return new CycleFragmentCompletedCall(fullResult, deltaResult);
  }

}
