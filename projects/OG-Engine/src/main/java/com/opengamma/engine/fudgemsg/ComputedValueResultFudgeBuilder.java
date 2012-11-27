/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.calcnode.InvocationResult;

/**
 * Fudge message builder for {@link ComputedValueResult}.
 */
@FudgeBuilderFor(ComputedValueResult.class)
public class ComputedValueResultFudgeBuilder implements FudgeBuilder<ComputedValueResult> {

  private static final String EXECUTION_LOG_FIELD = "executionLog";
  private static final String COMPUTE_NODE_ID_FIELD = "computeNodeId";
  private static final String MISSING_INPUTS_FIELD_NAME = "missingInputs";
  private static final String INVOCATION_RESULT_FIELD_NAME = "result";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComputedValueResult object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ComputedValueFudgeBuilder.appendToMsg(serializer, object, msg);
    serializer.addToMessage(msg, EXECUTION_LOG_FIELD, null, object.getExecutionLog());
    if (object.getComputeNodeId() != null) {
      msg.add(COMPUTE_NODE_ID_FIELD, object.getComputeNodeId());
    }
    if (object.getMissingInputs() != null) {
      serializer.addToMessage(msg, MISSING_INPUTS_FIELD_NAME, null, object.getMissingInputs());
    }
    if (object.getInvocationResult() != null) {
      msg.add(INVOCATION_RESULT_FIELD_NAME, object.getInvocationResult().name());
    }
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ComputedValueResult buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ValueSpecification valueSpec = ComputedValueFudgeBuilder.getValueSpecification(deserializer, msg);
    Object valueObject = ComputedValueFudgeBuilder.getValueObject(deserializer, msg);
    ExecutionLog executionLog = deserializer.fieldValueToObject(ExecutionLog.class, msg.getByName(EXECUTION_LOG_FIELD));
    String computeNodeId = msg.getString(COMPUTE_NODE_ID_FIELD);
    FudgeField missingInputsField = msg.getByName(MISSING_INPUTS_FIELD_NAME);
    Set<ValueSpecification> missingInputs = missingInputsField != null ? deserializer.fieldValueToObject(Set.class, missingInputsField) : null;
    String invocationResultName = msg.getString(INVOCATION_RESULT_FIELD_NAME);
    InvocationResult invocationResult = invocationResultName != null ? InvocationResult.valueOf(invocationResultName) : null;
    return new ComputedValueResult(valueSpec, valueObject, executionLog, computeNodeId, missingInputs, invocationResult);
  }

}
