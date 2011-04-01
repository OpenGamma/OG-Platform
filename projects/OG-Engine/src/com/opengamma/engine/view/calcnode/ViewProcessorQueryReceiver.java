/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsReply;
import com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsRequest;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public class ViewProcessorQueryReceiver implements FudgeRequestReceiver {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorQueryReceiver.class);
  private Map<CalculationJobSpecification, DependencyGraph> _jobToDepGraphMap = new WeakHashMap<CalculationJobSpecification, DependencyGraph>();

  @Override
  public synchronized FudgeMsg requestReceived(FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
    ArgumentChecker.notNullInjected(_jobToDepGraphMap, "Job Specification to Dependency Graph Map");
    Object message = context.fudgeMsgToObject(requestEnvelope.getMessage());
    if (message instanceof DependentValueSpecificationsRequest) {
      DependentValueSpecificationsRequest request = (DependentValueSpecificationsRequest) message;
      DependencyGraph dependencyGraph = _jobToDepGraphMap.get(request.getJob());
      Collection<ValueSpecification> valueSpecs = new HashSet<ValueSpecification>();
      for (DependencyNode root : dependencyGraph.getRootNodes()) {
        collectAllValueSpecifications(root, valueSpecs);
      }
      FudgeSerializationContext fudgeSerializationContext = new FudgeSerializationContext(context.getFudgeContext());
      MutableFudgeMsg msg = fudgeSerializationContext.objectToFudgeMsg(new DependentValueSpecificationsReply(request.getCorrelationId(), valueSpecs));
      FudgeSerializationContext.addClassHeader(msg, DependentValueSpecificationsReply.class);
      return msg;
    } else {
      throw new OpenGammaRuntimeException("Unrecognized message object " + message);
    }
  }

  private void collectAllValueSpecifications(DependencyNode node, Collection<ValueSpecification> specsSoFar) {
    specsSoFar.addAll(node.getInputValues());
    for (DependencyNode subNode : node.getInputNodes()) {
      collectAllValueSpecifications(subNode, specsSoFar);
    }
  }

  public synchronized void addJob(CalculationJobSpecification spec, DependencyGraph graph) {
    _jobToDepGraphMap.put(spec, graph);
  }

}
