/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Will walk through a particular {@link SecurityDependencyGraph} and execute
 * the required nodes.
 *
 * @author kirk
 */
public class DependencyGraphExecutor {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphExecutor.class);
  // Injected Inputs:
  private final String _viewName; 
  private final DependencyGraph _dependencyGraph;
  private final ViewProcessingContext _processingContext;
  private final SingleComputationCycle _cycleState;
  // Running State:
  private final Set<DependencyNode> _nodesToExecute = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _executingNodes = new HashSet<DependencyNode>();
  private final Map<CalculationJobSpecification, DependencyNode> _executingSpecifications =
    new HashMap<CalculationJobSpecification, DependencyNode>();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final BlockingQueue<CalculationJobResult> _pendingResults = new LinkedBlockingQueue<CalculationJobResult>();
  
  public DependencyGraphExecutor(
      String viewName,
      DependencyGraph dependencyGraph,
      ViewProcessingContext processingContext,
      SingleComputationCycle cycle) {
    ArgumentChecker.checkNotNull(viewName, "View Name");
    ArgumentChecker.checkNotNull(dependencyGraph, "Dependency Graph");
    ArgumentChecker.checkNotNull(processingContext, "View Processing Context");
    ArgumentChecker.checkNotNull(cycle, "Computation cycle");
    _viewName = viewName;
    _dependencyGraph = dependencyGraph;
    _processingContext = processingContext;
    _cycleState = cycle;
  }
  
  /**
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
  }

  /**
   * @return the dependencyGraph
   */
  public DependencyGraph getDependencyGraph() {
    return _dependencyGraph;
  }

  /**
   * @return the processingContext
   */
  public ViewProcessingContext getProcessingContext() {
    return _processingContext;
  }

  /**
   * @return the cycleState
   */
  public SingleComputationCycle getCycleState() {
    return _cycleState;
  }

  public synchronized void executeGraph(long iterationTimestamp, AtomicLong jobIdSource) {
    addAllNodesToExecute(getDependencyGraph().getNodes());
    markLiveDataSourcingFunctionsCompleted();
    
    while(!_nodesToExecute.isEmpty()) {
      boolean enqueued = enqueueAllAvailableNodes(iterationTimestamp, jobIdSource);
      if(!enqueued) {
        continue;
      }
      // Suck everything available off the retrieval source.
      // First time we're willing to wait for some period, but after that we pull
      // off as fast as we can.
      CalculationJobResult jobResult = null;
      try {
        jobResult = _pendingResults.poll(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.interrupted();
        // REVIEW kirk 2009-11-04 -- Anything else here to do?
      }
      while(jobResult != null) {
        DependencyNode completedNode = _executingSpecifications.remove(jobResult.getSpecification());
        assert completedNode != null : "Got result " + jobResult.getSpecification() + " for job we didn't enqueue. No node to remove.";
        _executingNodes.remove(completedNode);
        _executedNodes.add(completedNode);
        getCycleState().markExecuted(completedNode);
        if(jobResult.getResult() != InvocationResult.SUCCESS) {
          getCycleState().markFailed(completedNode);
        }
        jobResult = _pendingResults.poll();
      }
    }
  }

  /**
   * @param nodes
   */
  protected void addAllNodesToExecute(Collection<DependencyNode> nodes) {
    for(DependencyNode node : nodes) {
      addAllNodesToExecute(node);
    }
  }
  
  protected void addAllNodesToExecute(DependencyNode node) {
    // REVIEW kirk 2010-01-03 -- Should we have an optimization here that checks
    // before enqueuing? It makes the logic a little more complex but MIGHT help
    // some cycles. MIGHT being the operative word.
    for(DependencyNode inputNode : node.getInputNodes()) {
      addAllNodesToExecute(inputNode);
    }
    _nodesToExecute.add(node);
  }

  /**
   * 
   */
  protected void markLiveDataSourcingFunctionsCompleted() {
    Iterator<DependencyNode> depNodeIter = _nodesToExecute.iterator();
    while(depNodeIter.hasNext()) {
      DependencyNode depNode = depNodeIter.next();
      if(depNode.getFunctionDefinition() instanceof LiveDataSourcingFunction) {
        depNodeIter.remove();
        _executedNodes.add(depNode);
      }
    }
  }
  
  /**
   * @param completionService
   */
  protected synchronized boolean enqueueAllAvailableNodes(
      long iterationTimestamp,
      AtomicLong jobIdSource) {
    boolean enqueued = false;
    Iterator<DependencyNode> depNodeIter = _nodesToExecute.iterator();
    while(depNodeIter.hasNext()) {
      DependencyNode depNode = depNodeIter.next();
      if(getCycleState().isExecuted(depNode)) {
        s_logger.debug("Skipping node as already executed, probably by another executor.");
        depNodeIter.remove();
        _executedNodes.add(depNode);
        continue;
      }
      if(getCycleState().isFailed(depNode)) {
        s_logger.debug("Skipping node as it failed.");
        depNodeIter.remove();
        _executedNodes.add(depNode);
        continue;
      }
      if(anyInputsFailed(depNode)) {
        s_logger.debug("Failing without executing as all inputs failed.");
        depNodeIter.remove();
        _executedNodes.add(depNode);
        getCycleState().markFailed(depNode);
        continue;
      }
      if(canExecute(depNode)) {
        depNodeIter.remove();
        enqueued = true;
        _executingNodes.add(depNode);
        CalculationJobSpecification jobSpec = submitNodeInvocationJob(iterationTimestamp, jobIdSource, depNode);
        _executingSpecifications.put(jobSpec, depNode);
      }
    }
    return enqueued;
  }
  
  /**
   * @param depNode
   * @return
   */
  private boolean anyInputsFailed(DependencyNode depNode) {
    for(DependencyNode subNode : depNode.getInputNodes()) {
      if(getCycleState().isFailed(subNode)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param depNode
   * @return
   */
  private boolean canExecute(DependencyNode node) {
    assert !_executingNodes.contains(node);
    assert !_executedNodes.contains(node);
    
    // Are all inputs done?
    boolean allInputsExecuted = true;
    for(DependencyNode inputNode : node.getInputNodes()) {
      if(!_executedNodes.contains(inputNode)) {
        allInputsExecuted = false;
        break;
      }
    }
    return allInputsExecuted;
  }

  protected static Collection<PositionReference> convertToPositionReferences(Collection<Position> positions) {
    Collection<PositionReference> resultReferences = new ArrayList<PositionReference>(positions.size());
    for (Position position : positions) {
      resultReferences.add(new PositionReference(position));
    }
    return resultReferences;
  }
  
  /**
   * @param depNode
   */
  protected CalculationJobSpecification submitNodeInvocationJob(
      long iterationTimestamp,
      AtomicLong jobIdSource,
      DependencyNode depNode) {
    assert !(depNode.getFunctionDefinition() instanceof LiveDataSourcingFunction);
    
    long jobId = jobIdSource.addAndGet(1l);
    CalculationJobSpecification jobSpec = new CalculationJobSpecification(getViewName(), iterationTimestamp, jobId);
    s_logger.info("Enqueuing job {} to invoke {} on {}",
        new Object[]{jobId, depNode.getFunctionDefinition().getShortName(), depNode.getComputationTarget()});
    
    // Have to package up the required data
    Set<ValueSpecification> resolvedInputs = new HashSet<ValueSpecification>();
    for(ValueRequirement requirement : depNode.getInputRequirements()) {
      resolvedInputs.add(depNode.getMappedRequirement(requirement));
    }

    CalculationJob job = new CalculationJob(
        getViewName(),
        iterationTimestamp,
        jobId,
        depNode.getFunctionDefinition().getUniqueIdentifier(),
        depNode.getComputationTarget().getSpecification(),
        resolvedInputs);
    
    s_logger.debug("Enqueuing job with specification {}", jobSpec);
    invokeJob(job);
    return jobSpec;
  }

  /**
   * @param job
   */
  protected void invokeJob(CalculationJob job) {
    FudgeMsg jobMsg = job.toFudgeMsg(getProcessingContext().getComputationJobRequestSender().getFudgeContext());
    getProcessingContext().getComputationJobRequestSender().sendRequest(jobMsg, new FudgeMessageReceiver() {
      @Override
      public void messageReceived(
          FudgeContext fudgeContext,
          FudgeMsgEnvelope msgEnvelope) {
        CalculationJobResult jobResult = CalculationJobResult.fromFudgeMsg(msgEnvelope);
        jobResultReceived(jobResult);
      }
      
    });
  }
  
  protected void jobResultReceived(CalculationJobResult jobResult) {
    _pendingResults.add(jobResult);
  }
}
