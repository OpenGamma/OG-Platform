/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

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
import com.opengamma.engine.view.calcnode.CalculationJobItem;

/**
 * Fudge message builder for {@code CalculationJobItem}.
 */
@FudgeBuilderFor(CalculationJobItem.class)
public class CalculationJobItemFudgeBuilder implements FudgeBuilder<CalculationJobItem> {
  
  private static final String FUNCTION_UNIQUE_ID_FIELD_NAME = "function";
  private static final String FUNCTION_PARAMETERS_FIELD_NAME = "parameters";
  private static final String INPUT_IDENTIFIERS_FIELD_NAME = "input";
  private static final String OUTPUT_IDENTIFIERS_FIELD_NAME = "output";

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
    msg.add(INPUT_IDENTIFIERS_FIELD_NAME, inputs);
    long[] outputs = object.getOutputIdentifiers();
    msg.add(OUTPUT_IDENTIFIERS_FIELD_NAME, outputs);
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

    long[] inputIdentifiers = (long[]) message.getByName(INPUT_IDENTIFIERS_FIELD_NAME).getValue();
    long[] outputIdentifiers = (long[]) message.getByName(OUTPUT_IDENTIFIERS_FIELD_NAME).getValue();
    
    return new CalculationJobItem(functionUniqueId, functionParameters, computationTargetSpecification, inputIdentifiers, outputIdentifiers);
  }


  @Override
  public String toString() {
    return "CalculationJobItemBuilder []";
  }

}
