/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import javax.time.Instant;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;

/**
 * Fudge message builder for {@link ViewDefinitionCompilationFailedCall}
 */
@FudgeBuilderFor(ViewDefinitionCompilationFailedCall.class)
public class ViewDefinitionCompilationFailedCallBuilder implements FudgeBuilder<ViewDefinitionCompilationFailedCall> {

  private static final String VALUATION_TIME_FIELD = "valuationTime";
  private static final String EXCEPTION_FIELD = "exception";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ViewDefinitionCompilationFailedCall object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, VALUATION_TIME_FIELD, null, object.getValuationTime());
    context.addToMessage(msg, EXCEPTION_FIELD, null, object.getException());
    return msg;
  }

  @Override
  public ViewDefinitionCompilationFailedCall buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    Instant valuationTime = context.fieldValueToObject(Instant.class, msg.getByName(VALUATION_TIME_FIELD));
    Exception exception = context.fieldValueToObject(Exception.class, msg.getByName(EXCEPTION_FIELD));
    return new ViewDefinitionCompilationFailedCall(valuationTime, exception);
  }

}
