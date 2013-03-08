/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.view.ExecutionLogMode;

/**
 * Fudge message builder for {@code CalculationJobItem}.
 * 
 * <pre>
 * message CalculationJobItem extends ComputationTargetSpecification {
 *   optional int target;                    // index into target specification dictionary
 *   optional string function;               // function identifier
 *   optional int function;                  // index into job function dictionary
 *   optional FunctionParameters parameters; // function parameters
 *   optional int parameters;                // index into job parameter dictionary
 *   required long[] input;                  // input value specifications
 *   required long[] output;                 // output value specifications
 * }
 * </pre>
 */
@FudgeBuilderFor(CalculationJobItem.class)
public class CalculationJobItemFudgeBuilder implements FudgeBuilder<CalculationJobItem> {

  private static final String TARGET_FIELD_NAME = "target";
  private static final String FUNCTION_UNIQUE_ID_FIELD_NAME = "function";
  private static final String FUNCTION_PARAMETERS_FIELD_NAME = "parameters";
  private static final String INPUT_IDENTIFIERS_FIELD_NAME = "input";
  private static final String OUTPUT_IDENTIFIERS_FIELD_NAME = "output";
  private static final String LOG_MODE_FIELD_NAME = "logMode";

  public static MutableFudgeMsg buildMessageImpl(final FudgeSerializer serializer, final CalculationJobItem object, final Map<ComputationTargetSpecification, Integer> targets,
      final Map<String, Integer> functions, final Map<FunctionParameters, Integer> parameters) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (targets != null) {
      Integer i = targets.get(object.getComputationTargetSpecification());
      if (i != null) {
        msg.add(TARGET_FIELD_NAME, i);
      } else {
        i = targets.size();
        targets.put(object.getComputationTargetSpecification(), i);
        ComputationTargetReferenceFudgeBuilder.buildMessageImpl(serializer, msg, object.getComputationTargetSpecification());
      }
    } else {
      ComputationTargetReferenceFudgeBuilder.buildMessageImpl(serializer, msg, object.getComputationTargetSpecification());
    }
    if (functions != null) {
      Integer i = functions.get(object.getFunctionUniqueIdentifier());
      if (i != null) {
        msg.add(FUNCTION_UNIQUE_ID_FIELD_NAME, i);
      } else {
        i = functions.size();
        functions.put(object.getFunctionUniqueIdentifier(), i);
        msg.add(FUNCTION_UNIQUE_ID_FIELD_NAME, object.getFunctionUniqueIdentifier());
      }
    } else {
      msg.add(FUNCTION_UNIQUE_ID_FIELD_NAME, object.getFunctionUniqueIdentifier());
    }
    if (!(object.getFunctionParameters() instanceof EmptyFunctionParameters)) {
      if (parameters != null) {
        Integer i = parameters.get(object.getFunctionParameters());
        if (i != null) {
          msg.add(FUNCTION_PARAMETERS_FIELD_NAME, i);
        } else {
          i = parameters.size();
          parameters.put(object.getFunctionParameters(), i);
          serializer.addToMessageWithClassHeaders(msg, FUNCTION_PARAMETERS_FIELD_NAME, null, object.getFunctionParameters(), FunctionParameters.class);
        }
      } else {
        serializer.addToMessageWithClassHeaders(msg, FUNCTION_PARAMETERS_FIELD_NAME, null, object.getFunctionParameters(), FunctionParameters.class);
      }
    }
    msg.add(INPUT_IDENTIFIERS_FIELD_NAME, object.getInputIdentifiers());
    msg.add(OUTPUT_IDENTIFIERS_FIELD_NAME, object.getOutputIdentifiers());
    serializer.addToMessage(msg, LOG_MODE_FIELD_NAME, null, object.getLogMode());
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CalculationJobItem object) {
    return buildMessageImpl(serializer, object, null, null, null);
  }

  public static CalculationJobItem buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message, final Map<Integer, ComputationTargetSpecification> targets,
      final Map<Integer, String> functions, final Map<Integer, FunctionParameters> parameters) {
    FudgeField field = message.getByName(TARGET_FIELD_NAME);
    final ComputationTargetSpecification computationTargetSpecification;
    if (field != null) {
      computationTargetSpecification = targets.get(((Number) field.getValue()).intValue());
    } else {
      computationTargetSpecification = ComputationTargetReferenceFudgeBuilder.buildObjectImpl(deserializer, message).getSpecification();
      if (targets != null) {
        targets.put(targets.size(), computationTargetSpecification);
      }
    }
    field = message.getByName(FUNCTION_UNIQUE_ID_FIELD_NAME);
    final String functionUniqueId;
    if (field.getValue() instanceof Number) {
      functionUniqueId = functions.get(((Number) field.getValue()).intValue());
    } else {
      functionUniqueId = (String) field.getValue();
      if (functions != null) {
        functions.put(functions.size(), functionUniqueId);
      }
    }
    final FunctionParameters functionParameters;
    field = message.getByName(FUNCTION_PARAMETERS_FIELD_NAME);
    if (field != null) {
      if (field.getValue() instanceof Number) {
        functionParameters = parameters.get(((Number) field.getValue()).intValue());
      } else {
        functionParameters = deserializer.fieldValueToObject(FunctionParameters.class, field);
        if (parameters != null) {
          parameters.put(parameters.size(), functionParameters);
        }
      }
    } else {
      functionParameters = EmptyFunctionParameters.INSTANCE;
    }
    final long[] inputIdentifiers = message.getValue(long[].class, INPUT_IDENTIFIERS_FIELD_NAME);
    final long[] outputIdentifiers = message.getValue(long[].class, OUTPUT_IDENTIFIERS_FIELD_NAME);
    final ExecutionLogMode logMode = deserializer.fieldValueToObject(ExecutionLogMode.class, message.getByName(LOG_MODE_FIELD_NAME));
    return new CalculationJobItem(functionUniqueId, functionParameters, computationTargetSpecification, inputIdentifiers, outputIdentifiers, logMode);
  }

  @Override
  public CalculationJobItem buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectImpl(deserializer, message, null, null, null);
  }

  @Override
  public String toString() {
    return "CalculationJobItemBuilder []";
  }

}
