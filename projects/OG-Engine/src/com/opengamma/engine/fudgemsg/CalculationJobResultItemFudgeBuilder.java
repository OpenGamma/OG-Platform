/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.calcnode.CalculationJobResultItem;

/**
 * Fudge message builder for {@code CalculationJobResultItem}.
 * 
 * <pre>
 * message CalculationJobResultItem {
 *   optional string exceptionClass;         // error/failure class
 *   optional string exceptionMsg;           // error/failure description
 *   optional string stackTrace;             // extended error/failure debugging information (e.g. a stack trace)
 *   optional long[] missingInputs;          // missing inputs that may have prevented execution
 *   optional long[] missingOutputs;         // missing outputs that were not produced
 * }
 * </pre>
 */
@FudgeBuilderFor(CalculationJobResultItem.class)
public class CalculationJobResultItemFudgeBuilder implements FudgeBuilder<CalculationJobResultItem> {

  private static final String EXCEPTION_CLASS_FIELD_NAME = "exceptionClass";
  private static final String EXCEPTION_MSG_FIELD_NAME = "exceptionMsg";
  private static final String STACK_TRACE_FIELD_NAME = "stackTrace";
  private static final String MISSING_INPUTS_FIELD_NAME = "missingInputs";
  private static final String MISSING_OUTPUTS_FIELD_NAME = "missingOutputs";

  public static MutableFudgeMsg buildMessageImpl(final FudgeSerializer serializer, final CalculationJobResultItem object) {
    MutableFudgeMsg msg = serializer.newMessage();
    if (object.getExceptionClass() != null) {
      msg.add(EXCEPTION_CLASS_FIELD_NAME, object.getExceptionClass());
      if (object.getExceptionMsg() != null) {
        msg.add(EXCEPTION_MSG_FIELD_NAME, object.getExceptionMsg());
      }
      if (object.getStackTrace() != null) {
        msg.add(STACK_TRACE_FIELD_NAME, object.getStackTrace());
      }
    }
    if (object.getMissingInputIdentifiers() != null) {
      msg.add(MISSING_INPUTS_FIELD_NAME, object.getMissingInputIdentifiers());
    }
    if (object.getMissingOutputIdentifiers() != null) {
      msg.add(MISSING_OUTPUTS_FIELD_NAME, object.getMissingOutputIdentifiers());
    }
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CalculationJobResultItem object) {
    return buildMessageImpl(serializer, object);
  }

  public static CalculationJobResultItem buildObjectImpl(final FudgeMsg message) {
    String exceptionClass = message.getString(EXCEPTION_CLASS_FIELD_NAME);
    String exceptionMsg = message.getString(EXCEPTION_MSG_FIELD_NAME);
    String stackTrace = message.getString(STACK_TRACE_FIELD_NAME);
    long[] missingInputs = message.getValue(long[].class, MISSING_INPUTS_FIELD_NAME);
    long[] missingOutputs = message.getValue(long[].class, MISSING_OUTPUTS_FIELD_NAME);
    return new CalculationJobResultItem(exceptionClass, exceptionMsg, stackTrace, missingInputs, missingOutputs);
  }

  @Override
  public CalculationJobResultItem buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return buildObjectImpl(message);
  }

}
