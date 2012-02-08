/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 */
@GenericFudgeBuilderFor(ViewComputationResultModel.class)
public class ViewComputationResultModelFudgeBuilder extends ViewResultModelFudgeBuilder implements FudgeBuilder<ViewComputationResultModel> {
  
  private static final String FIELD_LIVEDATA = "liveData";
  private static final String FIELD_SPECIFICATION_MAPPING = "specMapping";
  private static final String FIELD_SPECIFICATION = "specification";
  private static final String FIELD_REQUIREMENT = "requirement";

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

    for (Map.Entry<ValueSpecification, Set<ValueRequirement>> specMappingEntry : resultModel.getRequirementToSpecificationMapping().entrySet()) {
      final MutableFudgeMsg mappingMsg = serializer.newMessage();
      serializer.addToMessage(mappingMsg, FIELD_SPECIFICATION, null, specMappingEntry.getKey());
      for (ValueRequirement requirement : specMappingEntry.getValue()) {
        serializer.addToMessage(mappingMsg, FIELD_REQUIREMENT, null, requirement);
      }
      serializer.addToMessage(message, FIELD_SPECIFICATION_MAPPING, null, mappingMsg);
    }
    
    return message;
  }

  @Override
  public ViewComputationResultModel buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    InMemoryViewComputationResultModel resultModel = (InMemoryViewComputationResultModel) bootstrapCommonDataFromMessage(deserializer, message);
    
    for (FudgeField field : message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_LIVEDATA))) {
      ComputedValue liveData = deserializer.fieldValueToObject(ComputedValue.class, field);
      resultModel.addMarketData(liveData);      
    }
    for (FudgeField specMappingField : message.getAllByName(FIELD_SPECIFICATION_MAPPING)) {
      FudgeMsg mappingMsg = (FudgeMsg) specMappingField.getValue();
      ValueSpecification specification = deserializer.fieldValueToObject(ValueSpecification.class, mappingMsg.getByName(FIELD_SPECIFICATION));
      Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (FudgeField requirementField : mappingMsg.getAllByName(FIELD_REQUIREMENT)) {
        requirements.add(deserializer.fieldValueToObject(ValueRequirement.class, requirementField));
      }
      resultModel.addRequirements(requirements, specification);
    }
    return resultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewComputationResultModel();
  }

}
