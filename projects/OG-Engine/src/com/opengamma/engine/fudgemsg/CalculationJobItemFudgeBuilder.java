/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.calcnode.CalculationJobItem;

/**
 * Fudge message builder for {@code CalculationJobItem}.
 */
@FudgeBuilderFor(CalculationJobItem.class)
public class CalculationJobItemFudgeBuilder implements FudgeBuilder<CalculationJobItem> {
  
  private static final String FUNCTION_UNIQUE_ID_FIELD_NAME = "functionUniqueIdentifier";
  private static final String FUNCTION_PARAMETERS_FIELD_NAME = "functionParameters";
  private static final String INPUT_FIELD_NAME = "valueInput";
  private static final String DESIRED_VALUE_FIELD_NAME = "desiredValue";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CalculationJobItem object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ComputationTargetSpecification computationTargetSpecification = object.getComputationTargetSpecification();
    if (computationTargetSpecification != null) {
      MutableFudgeMsg specMsg = serializer.objectToFudgeMsg(computationTargetSpecification);
      for (FudgeField fudgeField : specMsg.getAllFields()) {
        msg.add(fudgeField);
      }
    }
    String functionUniqueIdentifier = object.getFunctionUniqueIdentifier();
    if (functionUniqueIdentifier != null) {
      msg.add(FUNCTION_UNIQUE_ID_FIELD_NAME, functionUniqueIdentifier);
    }
    FunctionParameters functionParameters = object.getFunctionParameters();
    if (functionParameters != null) {
      serializer.addToMessageWithClassHeaders(msg, FUNCTION_PARAMETERS_FIELD_NAME, null, functionParameters);
    }
    long[] inputs = object.getInputIdentifiers();
    msg.add(INPUT_FIELD_NAME, inputs);
    for (ValueRequirement desiredValue : object.getDesiredValues()) {
      serializer.addToMessage(msg, DESIRED_VALUE_FIELD_NAME, null, desiredValue);
    }
    return msg;
  }


  @Override
  public CalculationJobItem buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    
    ComputationTargetSpecification computationTargetSpecification = deserializer.fudgeMsgToObject(ComputationTargetSpecification.class, message);
    Validate.notNull(computationTargetSpecification, "Fudge message is not a CalculationJobItem - field 'computationTargetSpecification' is not present");
    
    String functionUniqueId = message.getString(FUNCTION_UNIQUE_ID_FIELD_NAME);
    Validate.notNull(functionUniqueId, "Fudge message is not a CalculationJobItem - field 'functionUniqueIdentifier' is not present");
    
    FudgeField fudgeField = message.getByName(FUNCTION_PARAMETERS_FIELD_NAME);
    Validate.notNull(fudgeField, "Fudge message is not a CalculationJobItem - field 'functionParameters' is not present");
    FunctionParameters functionParameters = deserializer.fieldValueToObject(FunctionParameters.class, fudgeField);

    final long[] inputIdentifiers = (long[]) message.getByName(INPUT_FIELD_NAME).getValue();

    List<ValueRequirement> desiredValues = new ArrayList<ValueRequirement>();
    for (FudgeField field : message.getAllByName(DESIRED_VALUE_FIELD_NAME)) {
      FudgeMsg valueMsg = (FudgeMsg) field.getValue();
      ValueRequirement desiredValue = deserializer.fudgeMsgToObject(ValueRequirement.class, valueMsg);
      desiredValues.add(desiredValue);
    }
    
    return CalculationJobItem.create(functionUniqueId, functionParameters, computationTargetSpecification, inputIdentifiers, desiredValues);
  }


  @Override
  public String toString() {
    return "CalculationJobItemBuilder []";
  }

}
