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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 */
@GenericFudgeBuilderFor(ViewComputationResultModel.class)
public class ViewComputationResultModelBuilder extends ViewResultModelBuilder implements FudgeBuilder<ViewComputationResultModel> {
  
  private static final String FIELD_LIVEDATA = "liveData";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ViewComputationResultModel resultModel) {
    final MutableFudgeMsg message = ViewResultModelBuilder.createResultModelMessage(context, resultModel);
    
    final MutableFudgeMsg liveDataMsg = context.newMessage();
    for (ComputedValue value : resultModel.getAllLiveData()) {
      context.addToMessage(liveDataMsg, null, 1, value);
    }
    message.add(FIELD_LIVEDATA, liveDataMsg);
    
    return message;
  }

  @Override
  public ViewComputationResultModel buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    InMemoryViewComputationResultModel resultModel = (InMemoryViewComputationResultModel) bootstrapCommonDataFromMessage(context, message);
    
    for (FudgeField field : message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_LIVEDATA))) {
      ComputedValue liveData = context.fieldValueToObject(ComputedValue.class, field);
      resultModel.addLiveData(liveData);      
    }
    
    return resultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewComputationResultModel();
  }

}
