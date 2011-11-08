/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.opengamma.engine.view.calcnode.CalculationJobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * This executor executes a large dependency graph, making certain assumptions about 
 * its structure to speed up its evaluation. The assumptions are:
 * <ul>
 * <li>A PRIMITIVE node can only depend on another PRIMITIVE node
 * <li>A SECURITY node can only depend on PRIMITIVE and SECURITY nodes
 * <li>A POSITION node can only depend on its SECURITY node
 * </ul>
 * The executor works by stages. 
 * <p>
 * 1. It first executes all PRIMITIVES in a single batch, on a single machine.
 * <p>
 * 2. It then executes all SECURITY and POSITION nodes. It divides
 * the nodes into groups by computation target. If a SECURITY node
 * (call it 'A') depends on another SECURITY node (call it 'B'), then
 * B is executed in a first pass before A. If there is no such
 * dependency, then A and B can execute in parallel.
 * <p>
 * POSITION nodes are evaluated at the same time as SECURITY nodes,
 * on the same machine, as they always depend on a single SECURITY 
 * node only. 
 * <p>
 * 3. PORTFOLIO nodes are evaluated in a single batch, on a single machine. 
 * 
 */
public class BatchExecutor implements DependencyGraphExecutor<Object> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BatchExecutor.class);
  
  private DependencyGraphExecutor<?> _delegate;
  
  public BatchExecutor(DependencyGraphExecutor<?> delegate) {
    ArgumentChecker.notNull(delegate, "Delegate executor");
    _delegate = delegate;
  }
  
  @Override
  public Future<Object> execute(final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue, final GraphExecutorStatisticsGatherer statistics) {
    // Partition graph into primitives, securities, positions, portfolios
    final Collection<DependencyNode> primitiveNodes = new HashSet<DependencyNode>();
    final List<Map<UniqueId, Collection<DependencyNode>>> passNumber2Target2SecurityAndPositionNodes = 
      new ArrayList<Map<UniqueId, Collection<DependencyNode>>>();
    final Collection<DependencyNode> portfolioNodes = new HashSet<DependencyNode>();
    
    for (DependencyNode node : graph.getDependencyNodes()) {
      switch (node.getComputationTarget().getType()) {
        
        case PRIMITIVE:
          
          for (DependencyNode input : node.getInputNodes()) {
            if (input.getComputationTarget().getType() != ComputationTargetType.PRIMITIVE) {
              throw new IllegalStateException("A primitive node can only depend on another primitive node. " + 
                  node + " depended on " + node.getInputNodes());
            }
          }

          primitiveNodes.add(node);
          break;

        case SECURITY:
        case POSITION:
          
          int passNumber = determinePassNumber(node);
          
          if (passNumber > passNumber2Target2SecurityAndPositionNodes.size() - 1) {
            for (int i = passNumber2Target2SecurityAndPositionNodes.size(); i <= passNumber; i++) {
              passNumber2Target2SecurityAndPositionNodes.add(new HashMap<UniqueId, Collection<DependencyNode>>());
            }
          }
          
          Map<UniqueId, Collection<DependencyNode>> target2SecurityAndPositionNodes = 
            passNumber2Target2SecurityAndPositionNodes.get(passNumber);
          
          UniqueId uniqueId;
          if (node.getComputationTarget().getType() == ComputationTargetType.SECURITY) {
            uniqueId = node.getComputationTarget().getUniqueId(); 
          } else if (node.getComputationTarget().getType() == ComputationTargetType.POSITION) {
            // execute positions with underlying securities
            DependencyNode securityNode = getSecurityNode(node); 
            uniqueId = securityNode.getComputationTarget().getUniqueId();
          } else {
            throw new RuntimeException("Should not get here");
          }
          
          Collection<DependencyNode> nodeCollection = target2SecurityAndPositionNodes.get(uniqueId);
          if (nodeCollection == null) {
            nodeCollection = new HashSet<DependencyNode>();
            target2SecurityAndPositionNodes.put(uniqueId, nodeCollection);
          }
          nodeCollection.add(node);
          break;
          
        case PORTFOLIO_NODE:
          
          portfolioNodes.add(node);
          break;
          
        default:
          throw new RuntimeException("Unexpected node type" + node.getComputationTarget().getType());
      }
    }
    
    // Execute primitives and wait for completion
    s_logger.info("Executing {} PRIMITIVE nodes", primitiveNodes.size());
    
    DependencyGraph primitiveGraph = graph.subGraph(primitiveNodes);
    try {
      Future<?> future = _delegate.execute(primitiveGraph, calcJobResultQueue, statistics);
      future.get();
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException("Should not have been interrupted");
    } catch (ExecutionException e) {
      throw new RuntimeException("Execution of primitives failed", e);
    }
    
    // Execute securities and positions, pass by pass, one by one and wait for completion
    s_logger.info("Executing {} passes of SECURITY and POSITION nodes", passNumber2Target2SecurityAndPositionNodes.size());
    
    int passNumber = 0;
    for (Map<UniqueId, Collection<DependencyNode>> target2SecurityAndPositionNodes : 
      passNumber2Target2SecurityAndPositionNodes) {
      
      s_logger.info("Executing pass number {}", passNumber, target2SecurityAndPositionNodes.size());
      
      LinkedList<Future<?>> secAndPositionFutures = new LinkedList<Future<?>>();
      int nodeCount = 0;
      for (Collection<DependencyNode> nodesRelatedToSingleTarget : target2SecurityAndPositionNodes.values()) {
        DependencyGraph secAndPositionGraph = graph.subGraph(nodesRelatedToSingleTarget);
        nodeCount += nodesRelatedToSingleTarget.size();
        Future<?> future = _delegate.execute(secAndPositionGraph, calcJobResultQueue, statistics);
        secAndPositionFutures.add(future);
      }
      
      s_logger.info("Pass number {} has {} different computation targets, and a total of {} nodes", 
          new Object[] {passNumber, target2SecurityAndPositionNodes.size(), nodeCount});
      
      for (Future<?> secAndPositionFuture : secAndPositionFutures) {
        try {
          secAndPositionFuture.get(); 
        } catch (InterruptedException e) {
          Thread.interrupted();
          throw new RuntimeException("Should not have been interrupted");
        } catch (ExecutionException e) {
          throw new RuntimeException("Execution of securities failed", e);
        }
      }
      
      passNumber++;
    }
    
    // Execute portfolios and wait for completion
    s_logger.info("Executing {} PORTFOLIO_NODE nodes", portfolioNodes.size());
    
    DependencyGraph portfolioGraph = graph.subGraph(portfolioNodes);
    try {
      Future<?> future = _delegate.execute(portfolioGraph, calcJobResultQueue, statistics);
      future.get();
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException("Should not have been interrupted");
    } catch (ExecutionException e) {
      throw new RuntimeException("Execution of positions failed", e);
    }
    
    BatchExecutorFuture future = new BatchExecutorFuture(graph);
    future.run();
    return future;
  }
  
  /**
   * @param node SECURITY or POSITION node
   * @return First pass = 0, second pass = 1, etc.
   */
  private int determinePassNumber(DependencyNode node) {
    switch (node.getComputationTarget().getType()) {
      case SECURITY:
        int maxPass = 0;
        for (DependencyNode input : node.getInputNodes()) {
          int pass;
          switch (input.getComputationTarget().getType()) {
            case PRIMITIVE:
              pass = 0;
              break;
            case SECURITY:
              if (input.getFunction().getFunction() instanceof MarketDataSourcingFunction) {
                // already evaluated
                pass = 0;
              } else if (input.getComputationTarget().toSpecification().equals(node.getComputationTarget().toSpecification())) {
                // same target? execute in the same pass on the same grid node
                pass = determinePassNumber(input);
              } else {
                // different target? execute in the next pass on (possibly) a different grid node
                pass = determinePassNumber(input) + 1;
              }
              break;
            default:
              throw new IllegalArgumentException("A SECURITY node should only depend on " +
                "PRIMITIVE and SECURITY nodes");
          }
          maxPass = Math.max(maxPass, pass);          
        }
        return maxPass;
      case POSITION:
        DependencyNode securityNode = getSecurityNode(node);
        return determinePassNumber(securityNode);
      default:
        throw new IllegalArgumentException("Unexpected node type " + node);
    }
  }

  private DependencyNode getSecurityNode(DependencyNode positionNode) {
    if (positionNode.getComputationTarget().getType() != ComputationTargetType.POSITION) {
      throw new IllegalArgumentException("Please pass in a POSITION node");
    }
    if (positionNode.getInputNodes().size() != 1) {
      throw new IllegalArgumentException("A POSITION node should only depend on its SECURITY");
    }
    DependencyNode securityNode = positionNode.getInputNodes().iterator().next();
    if (securityNode.getComputationTarget().getType() != ComputationTargetType.SECURITY) {
      throw new IllegalArgumentException("A POSITION node should only depend on its SECURITY");
    }
    return securityNode;
  }
  
  private class BatchExecutorFuture extends FutureTask<Object> {
    
    private DependencyGraph _graph;
    
    public BatchExecutorFuture(DependencyGraph graph) {
      super(new Runnable() {
        @Override
        public void run() {
        }
      }, null);
      _graph = graph;
    }

    @Override
    public String toString() {
      return "BatchExecutorFuture[calcConfName=" + _graph.getCalculationConfigurationName() + "]";
    }
  }
}
