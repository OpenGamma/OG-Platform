/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BatchExecutor implements DependencyGraphExecutor {
  
  private DependencyGraphExecutor _delegate;
  
  public BatchExecutor(DependencyGraphExecutor delegate) {
    ArgumentChecker.notNull(delegate, "Delegate executor");
    _delegate = delegate;
  }

  @Override
  public Future<?> execute(DependencyGraph graph) {
    // Execute primitives
    final Set<DependencyNode> primitives = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    DependencyGraph primitiveGraph = graph.subGraph(new DependencyNodeFilter() {
      @Override
      public boolean accept(DependencyNode node) {
        for (DependencyNode input : node.getInputNodes()) {
          if (input.getComputationTarget().getType() != ComputationTargetType.PRIMITIVE) {
            throw new IllegalStateException("A primitive depends on a non-primitive. " +
              "This is not yet supported.");
          }
        }
        
        return primitives.contains(node);
      }
    });
    Future<?> future = _delegate.execute(primitiveGraph);
    try {
      future.get();
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException("Should not have been interrupted");
    } catch (ExecutionException e) {
      
    }
    
    // Execute positions
    
    // Execute portfolios
    
    
    return null;
    
    
  }
  
}
