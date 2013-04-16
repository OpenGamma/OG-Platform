/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.impl.InMemoryViewResultModel;

/**
 * 
 */
@GenericFudgeBuilderFor(ViewDeltaResultModel.class)
public class ViewDeltaResultModelFudgeBuilder extends ViewResultModelFudgeBuilder implements FudgeBuilder<ViewDeltaResultModel> {

  private static final String FIELD_PREVIOUSTS = "previousTS";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewDeltaResultModel deltaModel) {
    final MutableFudgeMsg message = ViewResultModelFudgeBuilder.createResultModelMessage(serializer, deltaModel);
    message.add(FIELD_PREVIOUSTS, deltaModel.getPreviousResultTimestamp());
    return message;
  }

  @Override
  public ViewDeltaResultModel buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    InMemoryViewDeltaResultModel viewDeltaResultModel = (InMemoryViewDeltaResultModel) bootstrapCommonDataFromMessage(deserializer, message);
    
    final Instant parentResultTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_PREVIOUSTS));
    viewDeltaResultModel.setPreviousCalculationTime(parentResultTimestamp);
    
    return viewDeltaResultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewDeltaResultModel();
  }

}
