/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.SecurityDependencyGraph;
import com.opengamma.engine.security.Security;

/**
 * Will walk through a particular {@link SecurityDependencyGraph} and execute
 * the required nodes.
 *
 * @author kirk
 */
public class DependencyGraphExecutor {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphExecutor.class);
  // Injected Inputs:
  private final Security _security;
  private final SecurityDependencyGraph _dependencyGraph;
  private final ViewProcessingContext _processingContext;
  private final ViewComputationCache _computationCache;
  private final ExecutorService _executor;
  private final AnalyticFunctionRepository _functionRepository;
  // Running State:
  private final Set<DependencyNode> _executingNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  
  public DependencyGraphExecutor(
      Security security,
      SecurityDependencyGraph dependencyGraph,
      ViewProcessingContext processingContext,
      ViewComputationCache computationCache,
      ExecutorService executor,
      AnalyticFunctionRepository functionRepository) {
    if(security == null) {
      throw new NullPointerException("Must provide a security over which to execute.");
    }
    if(dependencyGraph == null) {
      throw new NullPointerException("Must provide a dependency graph to execute.");
    }
    if(processingContext == null) {
      throw new NullPointerException("Must provide a processing context.");
    }
    if(computationCache == null) {
      throw new NullPointerException("Must provide a View Computation Cache.");
    }
    if(executor == null) {
      throw new NullPointerException("Must provide an executor.");
    }
    if(functionRepository == null) {
      throw new NullPointerException("Must provide an Analytic Function Repository");
    }
    _security = security;
    _dependencyGraph = dependencyGraph;
    _processingContext = processingContext;
    _computationCache = computationCache;
    _executor = executor;
    _functionRepository = functionRepository;
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

  /**
   * @return the computationCache
   */
  public ViewComputationCache getComputationCache() {
    return _computationCache;
  }

  /**
   * @return the executor
   */
  public ExecutorService getExecutor() {
    return _executor;
  }
  
  /**
   * @return the functionRepository
   */
  public AnalyticFunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public synchronized void executeGraph() {
    CompletionService<DependencyNode> completionService = new ExecutorCompletionService<DependencyNode>(getExecutor());
    
    markLiveDataSourcingFunctionsCompleted();
    
    while(_executedNodes.size() < getDependencyGraph().getNodeCount()) {
      enqueueAllAvailableNodes(completionService);
      DependencyNode completedNode = null;
      try {
        Future<DependencyNode> completedNodeFuture = completionService.poll(10, TimeUnit.SECONDS);
        completedNode = completedNodeFuture.get();
      } catch (InterruptedException e) {
        String warnMessage = MessageFormat.format("{1} {2} Was Interrupted", getSecurity(), completedNode);
        s_logger.warn(warnMessage, e);
      } catch (ExecutionException e) {
        String warnMessage = MessageFormat.format("{1} {2} Execution Failed", getSecurity(), completedNode);
        s_logger.warn(warnMessage, e);
      }
      _executingNodes.remove(completedNode);
      _executedNodes.add(completedNode);
    }
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
      CompletionService<DependencyNode> completionService) {
    DependencyNode depNode = findExecutableNode();
    while(depNode != null) {
      _executingNodes.add(depNode);
      submitNodeInvocationJob(completionService, depNode);
      depNode = findExecutableNode();
    }
  }
  
  /**
   * @param depNode
   */
  protected void submitNodeInvocationJob(CompletionService<DependencyNode> completionService, DependencyNode depNode) {
    assert !(depNode.getFunction() instanceof LiveDataSourcingFunction);
    Collection<AnalyticValueDefinition<?>> resolvedInputs = new HashSet<AnalyticValueDefinition<?>>();
    for(AnalyticValueDefinition<?> input : depNode.getFunction().getInputs(getSecurity())) {
      resolvedInputs.add(depNode.getResolvedInput(input));
    }
    AnalyticFunctionInvocationJob invocationJob = new AnalyticFunctionInvocationJob(
        depNode.getFunction().getUniqueIdentifier(),
        resolvedInputs,
        getSecurity(),
        getComputationCache(),
        getFunctionRepository()
      );
    completionService.submit(invocationJob, depNode);
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
