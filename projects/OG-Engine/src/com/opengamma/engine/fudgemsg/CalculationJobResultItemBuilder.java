/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.InvocationResult;

/**
 * Fudge message builder for {@code CalculationJobResultItem}.
 */
@FudgeBuilderFor(CalculationJobResultItem.class)
public class CalculationJobResultItemBuilder implements FudgeBuilder<CalculationJobResultItem> {
  private static final String ITEM_FIELD_NAME = "item";
  private static final String INVOCATION_RESULT_FIELD_NAME = "result";
  private static final String EXCEPTION_CLASS_FIELD_NAME = "exceptionClass";
  private static final String EXCEPTION_MSG_FIELD_NAME = "exceptionMsg";
  private static final String STACK_TRACE_FIELD_NAME = "stackTrace";
  private static final String MISSING_INPUTS_FIELD_NAME = "missingInputs";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, CalculationJobResultItem object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, ITEM_FIELD_NAME, null, object.getItem());
    msg.add(INVOCATION_RESULT_FIELD_NAME, object.getResult().name());
    msg.add(EXCEPTION_CLASS_FIELD_NAME, object.getExceptionClass());
    msg.add(EXCEPTION_MSG_FIELD_NAME, object.getExceptionMsg());
    msg.add(STACK_TRACE_FIELD_NAME, object.getStackTrace());
    context.addToMessage(msg, MISSING_INPUTS_FIELD_NAME, null, object.getMissingInputs());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CalculationJobResultItem buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    CalculationJobItem item = context.fudgeMsgToObject(CalculationJobItem.class, message.getMessage(ITEM_FIELD_NAME));
    String resultName = message.getString(INVOCATION_RESULT_FIELD_NAME);
    InvocationResult result = InvocationResult.valueOf(resultName);
    String exceptionClass = message.getString(EXCEPTION_CLASS_FIELD_NAME);
    String exceptionMsg = message.getString(EXCEPTION_MSG_FIELD_NAME);
    String stackTrace = message.getString(STACK_TRACE_FIELD_NAME);
    Set<ValueSpecification> missingInputs = context.fieldValueToObject(Set.class, message.getByName(MISSING_INPUTS_FIELD_NAME));
    return CalculationJobResultItem.create(item, result, exceptionClass, exceptionMsg, stackTrace, missingInputs);
  }

}
