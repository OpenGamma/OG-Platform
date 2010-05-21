/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Will walk through a particular {@link SecurityDependencyGraph} and execute
 * the required nodes.
 *
 * @author kirk
 */
public class DependencyGraphExecutor implements JobResultReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphExecutor.class);
  // Injected Inputs:
  private final String _viewName;
  private final String _calcConfigName;
  private final DependencyGraph _dependencyGraph;
  private final ViewProcessingContext _processingContext;
  private final SingleComputationCycle _cycleState;
  // Running State:
  // Use a LinkedHashSet here as we add in the order that we probably want to
  // execute (Depth-First with post-order), so this is optimal.
  private final Set<DependencyNode> _nodesToExecute = new LinkedHashSet<DependencyNode>();
  private final Set<DependencyNode> _executingNodes = new HashSet<DependencyNode>();
  private final Map<CalculationJobSpecification, DependencyNode> _executingSpecifications =
    new ConcurrentHashMap<CalculationJobSpecification, DependencyNode>();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final BlockingQueue<CalculationJobResult> _pendingResults = new LinkedBlockingQueue<CalculationJobResult>();
  
  public DependencyGraphExecutor(
      String viewName,
      String calcConfigName,
      DependencyGraph dependencyGraph,
      ViewProcessingContext processingContext,
      SingleComputationCycle cycle) {
    ArgumentChecker.notNull(viewName, "View Name");
    ArgumentChecker.notNull(calcConfigName, "Calculation configuration name");
    ArgumentChecker.notNull(dependencyGraph, "Dependency Graph");
    ArgumentChecker.notNull(processingContext, "View Processing Context");
    ArgumentChecker.notNull(cycle, "Computation cycle");
    _viewName = viewName;
    _calcConfigName = calcConfigName;
    _dependencyGraph = dependencyGraph;
    _processingContext = processingContext;
    _cycleState = cycle;
    getProcessingContext().getViewProcessorQueryReceiver().setJobToDepNodeMap(_executingSpecifications);
  }
  
  /**
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
  }

  /**
   * @return the calcConfigName
   */
  public String getCalcConfigName() {
    return _calcConfigName;
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
    if(allNodesExecuted(getDependencyGraph().getDependencyNodes())) {
      return;
    }
    
    addAllNodesToExecute(getDependencyGraph().getDependencyNodes());
    markLiveDataSourcingFunctionsCompleted();
    
    while(!_nodesToExecute.isEmpty()) {
      /*boolean enqueued = */enqueueAllAvailableNodes(iterationTimestamp, jobIdSource);
      // REVIEW kirk 2010-04-01 -- This check is necessary to avoid a 1 second wait at the end
      // of computation where things are running. MASSIVE speed win.
      if(_executingNodes.isEmpty()) {
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
          markSubgraphAsFailed(completedNode);
        }
        jobResult = _pendingResults.poll();
      }
    }
  }
  
  /**
   * Returns {@code true} if all nodes have actually already been executed,
   * saving us from creating and moving all nodes from this collection through
   * the node workflow.
   * Doesn't have to walk down the graph from each node because if a node
   * has executed, we don't have to check the inputs.
   * 
   * @param nodes
   * @return
   */
  protected boolean allNodesExecuted(Collection<DependencyNode> nodes) {
    for(DependencyNode node: nodes) {
      if(!getCycleState().isExecuted(node)) {
        return false;
      }
    }
    return true;
  }

  protected void markSubgraphAsFailed(DependencyNode node) {
    _nodesToExecute.remove(node); // we assume dispatched jobs are a lost cause.
    getCycleState().markFailed(node);
    for (DependencyNode subGraphNode : node.getDependentNodes()) {
      markSubgraphAsFailed(subGraphNode);
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
    for(DependencyNode inputNode : node.getInputNodes()) {
      addAllNodesToExecute(inputNode);
    }
    if(getCycleState().isExecuted(node)) {
      _executedNodes.add(node);
    } else {
      _nodesToExecute.add(node);
    }
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
        /*CalculationJobSpecification jobSpec = */submitNodeInvocationJob(iterationTimestamp, jobIdSource, depNode);
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

  /**
   * @param depNode
   */
  protected CalculationJobSpecification submitNodeInvocationJob(
      long iterationTimestamp,
      AtomicLong jobIdSource,
      DependencyNode depNode) {
    assert !(depNode.getFunctionDefinition() instanceof LiveDataSourcingFunction);
    
    long jobId = jobIdSource.addAndGet(1l);
    CalculationJobSpecification jobSpec = new CalculationJobSpecification(getViewName(), getCalcConfigName(), iterationTimestamp, jobId);
    s_logger.info("Enqueuing job {} to invoke {} on {}",
        new Object[]{jobId, depNode.getFunctionDefinition().getShortName(), depNode.getComputationTarget()});
    
    // Have to package up the required data
    Set<ValueSpecification> resolvedInputs = new HashSet<ValueSpecification>();
    for(ValueRequirement requirement : depNode.getInputRequirements()) {
      resolvedInputs.add(depNode.getMappedRequirement(requirement));
    }
    Set<ValueRequirement> desiredValues = new HashSet<ValueRequirement>();
    for(ValueSpecification outputValue : depNode.getOutputValues()) {
      desiredValues.add(outputValue.getRequirementSpecification());
    }

    CalculationJob job = new CalculationJob(
        jobSpec,
        depNode.getFunctionDefinition().getUniqueIdentifier(),
        depNode.getComputationTarget().toSpecification(),
        resolvedInputs,
        desiredValues);
    
    s_logger.debug("Enqueuing job with specification {}", jobSpec);
    _executingSpecifications.put(jobSpec, depNode);
    getProcessingContext().getComputationJobRequestSender().sendRequest(job, this);
    return jobSpec;
  }

  protected void jobResultReceived(CalculationJobResult jobResult) {
    _pendingResults.add(jobResult);
  }

  @Override
  public void resultReceived(CalculationJobResult result) {
    _pendingResults.add(result);
  }
}
