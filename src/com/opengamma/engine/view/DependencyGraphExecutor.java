/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.SecurityDependencyGraph;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.InvocationResult;

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
  private final Security _security;
  private final SecurityDependencyGraph _dependencyGraph;
  private final ViewProcessingContext _processingContext;
  // Running State:
  private final Set<DependencyNode> _executingNodes = new HashSet<DependencyNode>();
  private final Map<CalculationJobSpecification, DependencyNode> _executingSpecifications =
    new HashMap<CalculationJobSpecification, DependencyNode>();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _failedNodes = new HashSet<DependencyNode>();
  
  public DependencyGraphExecutor(
      String viewName,
      Security security,
      SecurityDependencyGraph dependencyGraph,
      ViewProcessingContext processingContext) {
    if(viewName == null) {
      throw new NullPointerException("Must provide the name of the view being executed.");
    }
    if(security == null) {
      throw new NullPointerException("Must provide a security over which to execute.");
    }
    if(dependencyGraph == null) {
      throw new NullPointerException("Must provide a dependency graph to execute.");
    }
    if(processingContext == null) {
      throw new NullPointerException("Must provide a processing context.");
    }
    _viewName = viewName;
    _security = security;
    _dependencyGraph = dependencyGraph;
    _processingContext = processingContext;
  }

  /**
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
  }

  /**
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  /**
   * @return the dependencyGraph
   */
  public SecurityDependencyGraph getDependencyGraph() {
    return _dependencyGraph;
  }

  /**
   * @return the processingContext
   */
  public ViewProcessingContext getProcessingContext() {
    return _processingContext;
  }

  public synchronized void executeGraph(long iterationTimestamp) {
    markLiveDataSourcingFunctionsCompleted();
    AtomicLong jobIdSource = new AtomicLong(0l);
    
    while(_executedNodes.size() < getDependencyGraph().getNodeCount()) {
      enqueueAllAvailableNodes(iterationTimestamp, jobIdSource);
      // REVIEW kirk 2009-10-20 -- I'm not happy with this check here.
      // The rationale is that if we get a failed node, the next time we attempt to enqueue we may
      // mark everything above it as done, and then we'll be done, but we'll wait for the timeout
      // before determining that. There's almost certainly a better way to do this, but I needed
      // to resolve this one quickly.
      /*if(_executedNodes.size() >= getDependencyGraph().getNodeCount()) {
        break;
      }*/
      assert !_executingSpecifications.isEmpty();
      // Suck everything available off the retrieval source.
      // First time we're willing to wait for some period, but after that we pull
      // off as fast as we can.
      CalculationJobResult jobResult = null;
      jobResult = getProcessingContext().getJobCompletionRetriever().getNextCompleted(1, TimeUnit.SECONDS);
      while(jobResult != null) {
        DependencyNode completedNode = _executingSpecifications.remove(jobResult.getSpecification());
        assert completedNode != null;
        _executingNodes.remove(completedNode);
        _executedNodes.add(completedNode);
        if(jobResult.getResult() != InvocationResult.SUCCESS) {
          _failedNodes.add(completedNode);
          failNodesAbove(completedNode);
        }
        jobResult = getProcessingContext().getJobCompletionRetriever().getNextCompletedNoWait();
      }
    }
  }

  /**
   * @param completedNode
   */
  protected void failNodesAbove(DependencyNode failedNode) {
    for(DependencyNode node : getDependencyGraph().getTopLevelNodes()) {
      failNodesAbove(failedNode, node);
    }
  }

  /**
   * @param failedNode
   * @param node
   */
  protected boolean failNodesAbove(DependencyNode failedNode, DependencyNode node) {
    if(node == failedNode) {
      return true;
    }
    boolean wasFailing = false;
    for(DependencyNode inputNode : node.getInputNodes()) {
      if(failNodesAbove(failedNode, inputNode)) {
        _executedNodes.add(node);
        _failedNodes.add(node);
        // NOTE kirk 2009-10-20 -- Because of the diamond nature of the DAG,
        // DO NOT just break or return early here. You have to evaluate all the siblings under
        // this node because multiple might reach the failed node independently.
        wasFailing = true;
      }
    }
    return wasFailing;
  }

  /**
   * 
   */
  protected void markLiveDataSourcingFunctionsCompleted() {
    for(DependencyNode node : getDependencyGraph().getTopLevelNodes()) {
      markLiveDataSourcingFunctionsCompleted(node);
    }
  }
  
  protected void markLiveDataSourcingFunctionsCompleted(DependencyNode node) {
    if(node.getFunction() instanceof LiveDataSourcingFunction) {
      _executedNodes.add(node);
    }
    for(DependencyNode inputNode : node.getInputNodes()) {
      markLiveDataSourcingFunctionsCompleted(inputNode);
    }
  }

  /**
   * @param completionService
   */
  protected synchronized void enqueueAllAvailableNodes(
      long iterationTimestamp,
      AtomicLong jobIdSource) {
    DependencyNode depNode = findExecutableNode();
    while(depNode != null) {
      _executingNodes.add(depNode);
      CalculationJobSpecification jobSpec = submitNodeInvocationJob(iterationTimestamp, jobIdSource, depNode);
      _executingSpecifications.put(jobSpec, depNode);
      depNode = findExecutableNode();
    }
  }
  
  /**
   * @param depNode
   */
  protected CalculationJobSpecification submitNodeInvocationJob(
      long iterationTimestamp,
      AtomicLong jobIdSource,
      DependencyNode depNode) {
    assert !(depNode.getFunction() instanceof LiveDataSourcingFunction);
    Collection<AnalyticValueDefinition<?>> resolvedInputs = new HashSet<AnalyticValueDefinition<?>>();
    for(AnalyticValueDefinition<?> input : depNode.getInputValues()) {
      resolvedInputs.add(depNode.getResolvedInput(input));
    }
    long jobId = jobIdSource.addAndGet(1l);
    s_logger.debug("Enqueuing job {} to invoke {} on security {}",
        new Object[] {jobId, depNode.getFunction().getShortName(), getSecurity().getIdentityKey()});
    CalculationJobSpecification jobSpec = new CalculationJobSpecification(getViewName(), iterationTimestamp, jobId);
    CalculationJob job = new CalculationJob(
        getViewName(),
        iterationTimestamp,
        jobId,
        depNode.getFunction().getUniqueIdentifier(),
        getSecurity().getIdentityKey(),
        resolvedInputs);
    getProcessingContext().getJobSink().invoke(job);
    return jobSpec;
  }

  /**
   * @return
   */
  protected synchronized DependencyNode findExecutableNode() {
    for(DependencyNode node : getDependencyGraph().getTopLevelNodes()) {
      DependencyNode result = findExecutableNode(node); 
      if(result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * @param node
   * @return
   */
  protected DependencyNode findExecutableNode(DependencyNode node) {
    // If it's already queued up, we can't do anything and don't descend.
    if(_executedNodes.contains(node) || _executingNodes.contains(node)) {
      return null;
    }
    // Are all inputs done?
    boolean allInputsExecuted = true;
    for(DependencyNode inputNode : node.getInputNodes()) {
      if(!_executedNodes.contains(inputNode)) {
        allInputsExecuted = false;
        break;
      }
    }
    if(allInputsExecuted) {
      return node;
    }
    // Inputs aren't executed. Have to descend to execute.
    for(DependencyNode inputNode : node.getInputNodes()) {
      DependencyNode nodeToExecute = findExecutableNode(inputNode);
      if(nodeToExecute != null) {
        return nodeToExecute;
      }
    }
    return null;
  }
}
