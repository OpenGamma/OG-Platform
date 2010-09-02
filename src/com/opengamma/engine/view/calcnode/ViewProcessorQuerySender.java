/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsReply;
import com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsRequest;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;

/**
 * 
 */
public class ViewProcessorQuerySender implements FudgeMessageReceiver {
  private FudgeRequestSender _sender;
  private BlockingQueue<DependentValueSpecificationsReply> _specsQueue = new LinkedBlockingQueue<DependentValueSpecificationsReply>();
  public ViewProcessorQuerySender(FudgeRequestSender sender) {
    // REVIEW kirk 2010-05-24 -- Any reason why this isn't null-checked?
    // Again, I'm not displeased about it for unit testing, but it strikes me that it SHOULD.
    _sender = sender;
  }
  
  public Collection<ValueSpecification> getDependentValueSpecifications(CalculationJobSpecification jobSpec) {
    synchronized (this) {
      // REVIEW: jim 29-March-2010 -- Is it okay to create the serialization context in line like this?
      FudgeSerializationContext ctx = new FudgeSerializationContext(_sender.getFudgeContext());
      MutableFudgeFieldContainer msg = ctx.objectToFudgeMsg(new DependentValueSpecificationsRequest(jobSpec));
      FudgeSerializationContext.addClassHeader(msg, DependentValueSpecificationsRequest.class);
      _sender.sendRequest(msg, this);
      DependentValueSpecificationsReply reply = _specsQueue.remove();
      assert reply.getJobSpec() == jobSpec;
      return reply.getValueSpecification();
    }
  }

  @Override
  public void messageReceived(FudgeContext context,
      FudgeMsgEnvelope msgEnvelope) {
    Object reply = context.fromFudgeMsg(msgEnvelope.getMessage());
    if (reply instanceof DependentValueSpecificationsReply) {
      try {
        _specsQueue.put((DependentValueSpecificationsReply) reply);
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted while queuing reply", e);
      }
    } else {
      throw new OpenGammaRuntimeException("Unknown type of reply");
    }
  }
}
