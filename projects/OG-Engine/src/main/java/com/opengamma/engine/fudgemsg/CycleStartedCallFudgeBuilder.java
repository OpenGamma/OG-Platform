/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.listener.CycleStartedCall;

/**
 * Fudge message builder for {@link CycleStartedCall}.
 */
@FudgeBuilderFor(CycleStartedCall.class)
public class CycleStartedCallFudgeBuilder implements FudgeBuilder<CycleStartedCall> {

  private static final String METADATA_FIELD = "metadata";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CycleStartedCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, METADATA_FIELD, null, object.getCycleMetadata());
    return msg;
  }

  @Override
  public CycleStartedCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField viewCycleInfoField = msg.getByName(METADATA_FIELD);
    ViewCycleMetadata cycleInfo = viewCycleInfoField != null ? deserializer.fieldValueToObject(ViewCycleMetadata.class, viewCycleInfoField) : null;
    return new CycleStartedCall(cycleInfo);
  }

}
