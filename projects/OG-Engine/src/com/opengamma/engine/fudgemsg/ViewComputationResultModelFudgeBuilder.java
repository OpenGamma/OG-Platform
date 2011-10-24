/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;

import java.util.Map;

/**
 */
@GenericFudgeBuilderFor(ViewComputationResultModel.class)
public class ViewComputationResultModelFudgeBuilder extends ViewResultModelFudgeBuilder implements FudgeBuilder<ViewComputationResultModel> {
  
  private static final String FIELD_LIVEDATA = "liveData";

  private static final String FIELD_SPECIFICATIONS = "specifications";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewComputationResultModel resultModel) {
    final MutableFudgeMsg message = ViewResultModelFudgeBuilder.createResultModelMessage(serializer, resultModel);
    
    // Prevent subclass headers from being added to the message later, ensuring that this builder will be used for deserialization
    FudgeSerializer.addClassHeader(message, ViewComputationResultModel.class);
    
    final MutableFudgeMsg liveDataMsg = serializer.newMessage();
    for (ComputedValue value : resultModel.getAllMarketData()) {
      serializer.addToMessage(liveDataMsg, null, 1, value);
    }
    message.add(FIELD_LIVEDATA, liveDataMsg);
    message.add(FIELD_SPECIFICATIONS, resultModel.getRequirementToSpecificationMapping());
    
    return message;
  }

  @Override
  public ViewComputationResultModel buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    InMemoryViewComputationResultModel resultModel = (InMemoryViewComputationResultModel) bootstrapCommonDataFromMessage(deserializer, message);
    
    for (FudgeField field : message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_LIVEDATA))) {
      ComputedValue liveData = deserializer.fieldValueToObject(ComputedValue.class, field);
      resultModel.addMarketData(liveData);      
    }
    resultModel.addRequirements((Map<ValueRequirement, ValueSpecification>) deserializer.fieldValueToObject(message.getByName(FIELD_SPECIFICATIONS)));
    
    return resultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewComputationResultModel();
  }

}
