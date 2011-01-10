/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsReply;
import com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsRequest;
import com.opengamma.engine.view.calcnode.msg.ViewProcessorQueryMessage;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.FudgeSynchronousClient;

/**
 * 
 */
public class ViewProcessorQuerySender extends FudgeSynchronousClient {

  public ViewProcessorQuerySender(FudgeRequestSender sender) {
    super(sender);
  }

  public Collection<ValueSpecification> getDependentValueSpecifications(CalculationJobSpecification jobSpec) {
    final long correlationId = getNextCorrelationId();
    final FudgeSerializationContext sctx = new FudgeSerializationContext(getMessageSender().getFudgeContext());
    final MutableFudgeFieldContainer request = sctx.objectToFudgeMsg(new DependentValueSpecificationsRequest(correlationId, jobSpec));
    FudgeSerializationContext.addClassHeader(request, DependentValueSpecificationsRequest.class, ViewProcessorQueryMessage.class);
    final FudgeFieldContainer reply = sendRequestAndWaitForResponse(request, correlationId);
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(getMessageSender().getFudgeContext());
    final DependentValueSpecificationsReply replyObject = dctx.fudgeMsgToObject(DependentValueSpecificationsReply.class, reply);
    return replyObject.getValueSpecification();
  }

  @Override
  protected Long getCorrelationIdFromReply(final FudgeFieldContainer reply) {
    return reply.getLong(ViewProcessorQueryMessage.CORRELATION_ID_KEY);
  }
}
