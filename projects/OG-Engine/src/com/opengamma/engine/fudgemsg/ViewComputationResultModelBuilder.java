/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
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
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewComputationResultModel resultModel) {
    final MutableFudgeFieldContainer message = ViewResultModelBuilder.createResultModelMessage(context, resultModel);
    for (ComputedValue value : resultModel.getAllLiveData()) {
      message.add(FIELD_LIVEDATA, value);
    }
    return message;
  }

  @Override
  public ViewComputationResultModel buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    InMemoryViewComputationResultModel resultModel = (InMemoryViewComputationResultModel) bootstrapCommonDataFromMessage(context, message);
    
    List<FudgeField> liveData = message.getAllByName(FIELD_LIVEDATA);
    for (FudgeField value : liveData) {
      resultModel.addLiveData((ComputedValue) value.getValue());
    }
    
    return resultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewComputationResultModel();
  }

}
