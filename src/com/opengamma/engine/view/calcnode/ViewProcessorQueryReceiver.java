/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.DependentValueSpecificationsRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.OpenGammaRuntimeException;
/**
 * 
 *
 * @author jim
 */
public class ViewProcessorQueryReceiver implements FudgeRequestReceiver {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorQueryReceiver.class);
  private Map<CalculationJobSpecification, DependencyNode> _jobToDepNodeMap;

  @Override
  public FudgeFieldContainer requestReceived(FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
    ArgumentChecker.checkNotNullInjected(_jobToDepNodeMap, "Job Specification to Dependency Node Map");
    Object message = context.fudgeMsgToObject(requestEnvelope.getMessage());
    if (message instanceof DependentValueSpecificationsRequest) {
      DependentValueSpecificationsRequest request = (DependentValueSpecificationsRequest) message;
      DependencyNode dependencyNode = _jobToDepNodeMap.get(request.getJobSpec());
      Collection<ValueSpecification> valueSpecs = collectAllValueSpecifications(dependencyNode, new HashSet<ValueSpecification>());
      FudgeSerializationContext fudgeSerializationContext = new FudgeSerializationContext(context.getFudgeContext());
      MutableFudgeFieldContainer msg = fudgeSerializationContext.objectToFudgeMsg(new DependentValueSpecificationsReply(request.getJobSpec(), valueSpecs));
      FudgeSerializationContext.addClassHeader(msg, DependentValueSpecificationsReply.class);
      return msg;
    } else {
      throw new OpenGammaRuntimeException("Unrecognized message object "+message);
    }
  }
  
  Collection<ValueSpecification> collectAllValueSpecifications(DependencyNode node, Collection<ValueSpecification> specsSoFar) {
    Set<ValueRequirement> inputRequirements = node.getInputRequirements();
    for (ValueRequirement valueRequirement : inputRequirements) {
      ValueSpecification mappedRequirement = node.getMappedRequirement(valueRequirement);
      specsSoFar.add(mappedRequirement);
    }
    for (DependencyNode subNode : node.getInputNodes()) {
      collectAllValueSpecifications(subNode, specsSoFar);
    }
    return specsSoFar;
  }

  /**
   * @param executingSpecifications
   */
  public void setJobToDepNodeMap(Map<CalculationJobSpecification, DependencyNode> executingSpecifications) {
    _jobToDepNodeMap = executingSpecifications;
  }

}
