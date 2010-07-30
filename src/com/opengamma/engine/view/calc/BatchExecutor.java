/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BatchExecutor implements DependencyGraphExecutor {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BatchExecutor.class);
  
  private DependencyGraphExecutor _delegate;
  
  public BatchExecutor(DependencyGraphExecutor delegate) {
    ArgumentChecker.notNull(delegate, "Delegate executor");
    _delegate = delegate;
  }

  @Override
  public Future<?> execute(DependencyGraph graph) {
    // Partition graph into primitives, securities, positions, portfolios
    final Collection<DependencyNode> primitiveNodes = new HashSet<DependencyNode>();
    final List<Map<UniqueIdentifier, Collection<DependencyNode>>> passNumber2Target2SecurityAndPositionNodes = 
      new ArrayList<Map<UniqueIdentifier, Collection<DependencyNode>>>();
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
              passNumber2Target2SecurityAndPositionNodes.add(new HashMap<UniqueIdentifier, Collection<DependencyNode>>());
            }
          }
          
          Map<UniqueIdentifier, Collection<DependencyNode>> target2SecurityAndPositionNodes = 
            passNumber2Target2SecurityAndPositionNodes.get(passNumber);
          
          Collection<DependencyNode> nodeCollection = target2SecurityAndPositionNodes.get(node.getComputationTarget().getUniqueIdentifier());
          if (nodeCollection == null) {
            nodeCollection = new HashSet<DependencyNode>();
            target2SecurityAndPositionNodes.put(node.getComputationTarget().getUniqueIdentifier(), nodeCollection);
          }
          nodeCollection.add(node);
          break;
          
        case PORTFOLIO_NODE:
          
          for (DependencyNode input : node.getInputNodes()) {
            if (input.getComputationTarget().getType() != ComputationTargetType.POSITION) {
              throw new IllegalStateException("A portfolio node can only depend on position nodes. "
                  + node + " depended on " + node.getInputNodes());
            }
          }
          
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
      Future<?> future = _delegate.execute(primitiveGraph);
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
    for (Map<UniqueIdentifier, Collection<DependencyNode>> target2SecurityAndPositionNodes : 
      passNumber2Target2SecurityAndPositionNodes) {
      
      s_logger.info("Executing pass number {}", passNumber);
      
      LinkedList<Future<?>> secAndPositionFutures = new LinkedList<Future<?>>();
      int nodeCount = 0;
      for (Collection<DependencyNode> nodesRelatedToSingleTarget : target2SecurityAndPositionNodes.values()) {
        DependencyGraph secAndPositionGraph = graph.subGraph(nodesRelatedToSingleTarget);
        nodeCount += nodesRelatedToSingleTarget.size();
        Future<?> future = _delegate.execute(secAndPositionGraph);
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
    
    // Execute portfolios
    s_logger.info("Executing {} PORTFOLIO_NODE nodes", portfolioNodes.size());
    
    DependencyGraph portfolioGraph = graph.subGraph(portfolioNodes);
    try {
      Future<?> future = _delegate.execute(portfolioGraph);
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
              if (input.getFunctionDefinition() instanceof LiveDataSourcingFunction) {
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
        if (node.getInputNodes().size() != 1) {
          throw new IllegalArgumentException("A POSITION node should only depend on its SECURITY");
        }
        DependencyNode input = node.getInputNodes().iterator().next();
        if (input.getComputationTarget().getType() != ComputationTargetType.SECURITY) {
          throw new IllegalArgumentException("A POSITION node should only depend on its SECURITY");
        }
        return determinePassNumber(input);
      default:
        throw new IllegalArgumentException("Unexpected node type " + node);
    }
  }
  
  private class BatchExecutorFuture extends FutureTask<DependencyGraph> {
    
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
      return "BatchExecutorFuture[calcConfName=" + _graph.getCalcConfName() + "]";
    }
  }
}
