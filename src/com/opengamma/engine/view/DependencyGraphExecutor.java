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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.NewDependencyGraph;
import com.opengamma.engine.depgraph.NewDependencyNode;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionReference;
import com.opengamma.engine.security.Security;
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
  private Security _security; // compiler too stupid to let us make these final.
  private Position _position;
  private Collection<Position> _positions;
  private ComputationTargetType _computationTargetType;
  private final NewDependencyGraph _dependencyGraph;
  private final ViewProcessingContext _processingContext;
  // Running State:
  private final Set<NewDependencyNode> _nodesToExecute = new HashSet<NewDependencyNode>();
  private final Set<NewDependencyNode> _executingNodes = new HashSet<NewDependencyNode>();
  private final Map<CalculationJobSpecification, NewDependencyNode> _executingSpecifications =
    new HashMap<CalculationJobSpecification, NewDependencyNode>();
  private final Set<NewDependencyNode> _executedNodes = new HashSet<NewDependencyNode>();
  private final Set<NewDependencyNode> _failedNodes = new HashSet<NewDependencyNode>();
  private final BlockingQueue<CalculationJobResult> _pendingResults = new ArrayBlockingQueue<CalculationJobResult>(100);
  
  public DependencyGraphExecutor(
      String viewName,
      Security security,
      NewDependencyGraph dependencyGraph,
      ViewProcessingContext processingContext) {
    this(viewName, dependencyGraph, processingContext);
    ArgumentChecker.checkNotNull(security, "Security");
    _computationTargetType = ComputationTargetType.SECURITY;
    _security = security;
    _position = null;
    _positions = null;
  }
  
  public DependencyGraphExecutor(
      String viewName,
      Position position,
      NewDependencyGraph dependencyGraph,
      ViewProcessingContext processingContext) {
    this(viewName, dependencyGraph, processingContext);
    ArgumentChecker.checkNotNull(position, "Position");
    _computationTargetType = ComputationTargetType.POSITION;
    _security = null;
    _position = position;
    _positions = null;
  }
  
  public DependencyGraphExecutor(
      String viewName,
      Collection<Position> positions,
      NewDependencyGraph dependencyGraph,
      ViewProcessingContext processingContext) {
    this(viewName, dependencyGraph, processingContext);
    ArgumentChecker.checkNotNull(positions, "Positions");
    _computationTargetType = ComputationTargetType.MULTIPLE_POSITIONS;
    _security = null;
    _position = null;
    _positions = positions;
  }
  
  public DependencyGraphExecutor(
      String viewName,
      NewDependencyGraph dependencyGraph,
      ViewProcessingContext processingContext) {
    ArgumentChecker.checkNotNull(viewName, "View Name");
    ArgumentChecker.checkNotNull(dependencyGraph, "Dependency Graph");
    ArgumentChecker.checkNotNull(processingContext, "View Processing Context");
    _computationTargetType = ComputationTargetType.PRIMITIVE;
    _viewName = viewName;
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
   * @return the position
   */
  public Position getPosition() {
    return _position;
  }

  /**
   * @return the positions
   */
  public Collection<Position> getPositions() {
    return _positions;
  }

  /**
   * @return the dependencyGraph
   */
  public NewDependencyGraph getDependencyGraph() {
    return _dependencyGraph;
  }

  /**
   * @return the processingContext
   */
  public ViewProcessingContext getProcessingContext() {
    return _processingContext;
  }

  /**
   * @return the computationTargetType
   */
  public ComputationTargetType getComputationTargetType() {
    return _computationTargetType;
  }

  public synchronized void executeGraph(long iterationTimestamp, AtomicLong jobIdSource) {
    addAllNodesToExecute(getDependencyGraph().getNodes());
    markLiveDataSourcingFunctionsCompleted();
    
    while(!_nodesToExecute.isEmpty()) {
      enqueueAllAvailableNodes(iterationTimestamp, jobIdSource);
      // REVIEW kirk 2009-10-20 -- I'm not happy with this check here.
      // The rationale is that if we get a failed node, the next time we attempt to enqueue we may
      // mark everything above it as done, and then we'll be done, but we'll wait for the timeout
      // before determining that. There's almost certainly a better way to do this, but I needed
      // to resolve this one quickly.
      /*if(_executedNodes.size() >= getDependencyGraph().getNodeCount()) {
        break;
      }*/
      assert !_executingSpecifications.isEmpty() : "Graph problem found. Nodes available, but none enqueued. Breaks execution contract";
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
        NewDependencyNode completedNode = _executingSpecifications.remove(jobResult.getSpecification());
        assert completedNode != null : "Got result " + jobResult.getSpecification() + " for job we didn't enqueue. No node to remove.";
        _executingNodes.remove(completedNode);
        _executedNodes.add(completedNode);
        if(jobResult.getResult() != InvocationResult.SUCCESS) {
          _failedNodes.add(completedNode);
          failNodesAbove(completedNode);
        }
        jobResult = _pendingResults.poll();
      }
    }
  }

  /**
   * @param nodes
   */
  protected void addAllNodesToExecute(Collection<NewDependencyNode> nodes) {
    for(NewDependencyNode node : nodes) {
      addAllNodesToExecute(node);
    }
  }
  
  protected void addAllNodesToExecute(NewDependencyNode node) {
    // TODO kirk 2009-11-02 -- Handle optimization for where computation
    // targets don't match. We don't have to enqueue the node at all.
    for(NewDependencyNode inputNode : node.getInputNodes()) {
      addAllNodesToExecute(inputNode);
    }
    _nodesToExecute.add(node);
  }

  /**
   * @param completedNode
   */
  protected void failNodesAbove(NewDependencyNode failedNode) {
    // TODO kirk 2009-11-02 -- Have to figure out how to do this now.
    /*
    for(DependencyNode node : getDependencyGraph().getTopLevelNodes()) {
      failNodesAbove(failedNode, node);
    }
    */
  }

  /**
   * @param failedNode
   * @param node
   */
  protected boolean failNodesAbove(NewDependencyNode failedNode, NewDependencyNode node) {
    if(node == failedNode) {
      return true;
    }
    boolean wasFailing = false;
    for(NewDependencyNode inputNode : node.getInputNodes()) {
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
    Iterator<NewDependencyNode> depNodeIter = _nodesToExecute.iterator();
    while(depNodeIter.hasNext()) {
      NewDependencyNode depNode = depNodeIter.next();
      if(depNode.getFunctionDefinition() instanceof LiveDataSourcingFunction) {
        depNodeIter.remove();
        _executedNodes.add(depNode);
      }
    }
  }
  
  /**
   * @param completionService
   */
  protected synchronized void enqueueAllAvailableNodes(
      long iterationTimestamp,
      AtomicLong jobIdSource) {
    Iterator<NewDependencyNode> depNodeIter = _nodesToExecute.iterator();
    while(depNodeIter.hasNext()) {
      NewDependencyNode depNode = depNodeIter.next();
      if(canExecute(depNode)) {
        depNodeIter.remove();
        _executingNodes.add(depNode);
        CalculationJobSpecification jobSpec = submitNodeInvocationJob(iterationTimestamp, jobIdSource, depNode);
        _executingSpecifications.put(jobSpec, depNode);
      }
    }
  }
  
  /**
   * @param depNode
   * @return
   */
  private boolean canExecute(NewDependencyNode node) {
    assert !_executingNodes.contains(node);
    assert !_executedNodes.contains(node);
    
    // Are all inputs done?
    boolean allInputsExecuted = true;
    for(NewDependencyNode inputNode : node.getInputNodes()) {
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
      NewDependencyNode depNode) {
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
