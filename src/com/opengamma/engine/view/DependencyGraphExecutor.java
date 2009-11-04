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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.RevisedDependencyGraph;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionReference;
import com.opengamma.engine.security.Security;
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
  private final RevisedDependencyGraph _dependencyGraph;
  private final ViewProcessingContext _processingContext;
  // Running State:
  private final Set<DependencyNode> _nodesToExecute = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _executingNodes = new HashSet<DependencyNode>();
  private final Map<CalculationJobSpecification, DependencyNode> _executingSpecifications =
    new HashMap<CalculationJobSpecification, DependencyNode>();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _failedNodes = new HashSet<DependencyNode>();
  private final BlockingQueue<CalculationJobResult> _pendingResults = new ArrayBlockingQueue<CalculationJobResult>(100);
  
  public DependencyGraphExecutor(
      String viewName,
      Security security,
      RevisedDependencyGraph dependencyGraph,
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
      RevisedDependencyGraph dependencyGraph,
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
      RevisedDependencyGraph dependencyGraph,
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
      RevisedDependencyGraph dependencyGraph,
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
  public RevisedDependencyGraph getDependencyGraph() {
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
        DependencyNode completedNode = _executingSpecifications.remove(jobResult.getSpecification());
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
  protected void addAllNodesToExecute(Set<DependencyNode> nodes) {
    for(DependencyNode node : nodes) {
      addAllNodesToExecute(node);
    }
  }
  
  protected void addAllNodesToExecute(DependencyNode node) {
    // TODO kirk 2009-11-02 -- Handle optimization for where computation
    // targets don't match. We don't have to enqueue the node at all.
    for(DependencyNode inputNode : node.getInputNodes()) {
      addAllNodesToExecute(inputNode);
    }
    _nodesToExecute.add(node);
  }

  /**
   * @param completedNode
   */
  protected void failNodesAbove(DependencyNode failedNode) {
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
    Iterator<DependencyNode> depNodeIter = _nodesToExecute.iterator();
    while(depNodeIter.hasNext()) {
      DependencyNode depNode = depNodeIter.next();
      if(depNode.getFunction() instanceof LiveDataSourcingFunction) {
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
    Iterator<DependencyNode> depNodeIter = _nodesToExecute.iterator();
    while(depNodeIter.hasNext()) {
      DependencyNode depNode = depNodeIter.next();
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
  @SuppressWarnings("unchecked")
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
    
    CalculationJobSpecification jobSpec = new CalculationJobSpecification(getViewName(), iterationTimestamp, jobId);
    CalculationJob job;
    switch (depNode.getComputationTargetType()) {
    case PRIMITIVE:
      s_logger.debug("Enqueuing job {} to invoke {} on primative function",
          new Object[] {jobId, depNode.getFunction().getShortName()});
      job = new CalculationJob(
          getViewName(),
          iterationTimestamp,
          jobId,
          depNode.getFunction().getUniqueIdentifier(),
          resolvedInputs);
      break;
    case SECURITY:
      Security security = (Security)depNode.getComputationTarget();
      s_logger.debug("Enqueuing job {} to invoke {} on security {}",
          new Object[] {jobId, depNode.getFunction().getShortName(), security.getIdentityKey()});
      job = new CalculationJob(
          getViewName(),
          iterationTimestamp,
          jobId,
          depNode.getFunction().getUniqueIdentifier(),
          security.getIdentityKey(),
          resolvedInputs);
      break;
    case POSITION:
      Position position = (Position)depNode.getComputationTarget();
      s_logger.debug("Enqueuing job {} to invoke {} on position {}",
          new Object[] {jobId, depNode.getFunction().getShortName(), position});
      job = new CalculationJob(
          getViewName(),
          iterationTimestamp,
          jobId,
          depNode.getFunction().getUniqueIdentifier(),
          new PositionReference(position),
          resolvedInputs);
      break;
    case MULTIPLE_POSITIONS:
      Collection<Position> positions = (Collection<Position>)depNode.getComputationTarget();
      s_logger.debug("Enqueuing job {} to invoke {} on list of positions {}",
          new Object[] {jobId, depNode.getFunction().getShortName(), positions});
      job = new CalculationJob(
          getViewName(),
          iterationTimestamp,
          jobId,
          depNode.getFunction().getUniqueIdentifier(),
          convertToPositionReferences(positions),
          resolvedInputs);
      break;
    default:
      throw new OpenGammaRuntimeException("Unhandled case in switch");
    }

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
