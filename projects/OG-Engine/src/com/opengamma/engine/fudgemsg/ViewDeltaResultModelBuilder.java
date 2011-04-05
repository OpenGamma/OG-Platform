/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import javax.time.Instant;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.view.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;

/**
 * 
 */
@GenericFudgeBuilderFor(ViewDeltaResultModel.class)
public class ViewDeltaResultModelBuilder extends ViewResultModelBuilder implements FudgeBuilder<ViewDeltaResultModel> {

  private static final String FIELD_PREVIOUSTS = "previousTS";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ViewDeltaResultModel deltaModel) {
    final MutableFudgeMsg message = ViewResultModelBuilder.createResultModelMessage(context, deltaModel);
    message.add(FIELD_PREVIOUSTS, deltaModel.getPreviousResultTimestamp());
    return message;
  }

  @Override
  public ViewDeltaResultModel buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    InMemoryViewDeltaResultModel viewDeltaResultModel = (InMemoryViewDeltaResultModel) bootstrapCommonDataFromMessage(context, message);
    
    final Instant parentResultTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_PREVIOUSTS));
    viewDeltaResultModel.setPreviousResultTimestamp(parentResultTimestamp);
    
    return viewDeltaResultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewDeltaResultModel();
  }

}
