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

import com.google.common.collect.Sets;
import com.opengamma.engine.calcnode.InvocationResult;
import com.opengamma.engine.exec.DefaultAggregatedExecutionLog;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;

/**
 * Fudge message builder for {@link ComputedValueResult}.
 */
@FudgeBuilderFor(ComputedValueResult.class)
public class ComputedValueResultFudgeBuilder implements FudgeBuilder<ComputedValueResult> {

  private static final String AGGREGATED_EXECUTION_LOG_FIELD = "log";
  private static final String COMPUTE_NODE_ID_FIELD = "computeNodeId";
  private static final String MISSING_INPUTS_FIELD_NAME = "missingInputs";
  private static final String INVOCATION_RESULT_FIELD_NAME = "result";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ComputedValueResult object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ComputedValueFudgeBuilder.appendToMsg(serializer, object, msg);
    serializer.addToMessage(msg, AGGREGATED_EXECUTION_LOG_FIELD, null, object.getAggregatedExecutionLog());
    if (object.getComputeNodeId() != null) {
      msg.add(COMPUTE_NODE_ID_FIELD, object.getComputeNodeId());
    }
    if (object.getMissingInputs() != null) {
      final MutableFudgeMsg missingInputs = msg.addSubMessage(MISSING_INPUTS_FIELD_NAME, null);
      for (final ValueSpecification missingInput : object.getMissingInputs()) {
        serializer.addToMessage(missingInputs, null, null, missingInput);
      }
    }
    if (object.getInvocationResult() != null) {
      msg.add(INVOCATION_RESULT_FIELD_NAME, object.getInvocationResult().name());
    }
    return msg;
  }

  @Override
  public ComputedValueResult buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ValueSpecification valueSpec = ComputedValueFudgeBuilder.getValueSpecification(deserializer, msg);
    final Object valueObject = ComputedValueFudgeBuilder.getValueObject(deserializer, msg);
    final AggregatedExecutionLog aggregatedExecutionLog = deserializer.fieldValueToObject(DefaultAggregatedExecutionLog.class, msg.getByName(AGGREGATED_EXECUTION_LOG_FIELD));
    final String computeNodeId = msg.getString(COMPUTE_NODE_ID_FIELD);
    final FudgeMsg missingInputsMsg = msg.getMessage(MISSING_INPUTS_FIELD_NAME);
    final Set<ValueSpecification> missingInputs;
    if (missingInputsMsg != null) {
      missingInputs = Sets.newHashSetWithExpectedSize(missingInputsMsg.getNumFields());
      for (final FudgeField missingInput : missingInputsMsg) {
        missingInputs.add(deserializer.fieldValueToObject(ValueSpecification.class, missingInput));
      }
    } else {
      missingInputs = null;
    }
    final String invocationResultName = msg.getString(INVOCATION_RESULT_FIELD_NAME);
    final InvocationResult invocationResult = invocationResultName != null ? InvocationResult.valueOf(invocationResultName) : null;
    return new ComputedValueResult(valueSpec, valueObject, aggregatedExecutionLog, computeNodeId, missingInputs, invocationResult);
  }

}
