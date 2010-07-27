/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * This DependencyGraphExecutor executes the given dependency graph
 * on a single calculation node in a single thread. Whether that node 
 * is the local machine or a remote machine on the grid depends on the
 * the {@link com.opengamma.engine.view.calcnode.JobRequestSender} configured in 
 * {@link com.opengamma.engine.view.ViewProcessingContext}.
 * <p>
 * The graph is executed atomically: the entire graph either succeeds or fails,
 * and there is no partial failure.  
 */
public class AtomicExecutor implements DependencyGraphExecutor, JobResultReceiver {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AtomicExecutor.class);
  
  private final SingleComputationCycle _cycle;
  
  private final Map<CalculationJobSpecification, AtomicExecutorFuture> _executingSpecifications =
    new ConcurrentHashMap<CalculationJobSpecification, AtomicExecutorFuture>();
  
  public AtomicExecutor(SingleComputationCycle cycle) {
    ArgumentChecker.notNull(cycle, "Computation cycle");
    _cycle = cycle;    
  }

  @Override
  public Future<?> execute(DependencyGraph graph) {
    long jobId = JobIdSource.getId();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification(
        _cycle.getViewName(),
        graph.getCalcConfName(),
        _cycle.getValuationTime().toEpochMillisLong(),
        jobId);
    
    List<DependencyNode> order = graph.getExecutionOrder();
    
    List<CalculationJobItem> items = new ArrayList<CalculationJobItem>();
    for (DependencyNode node : order) {
      // LiveData functions do not need to be computed. A little hacky. 
      if (node.getFunctionDefinition() instanceof LiveDataSourcingFunction) {
        continue;        
      }
      
      CalculationJobItem jobItem = new CalculationJobItem(
          node.getFunctionDefinition().getUniqueIdentifier(),
          node.getComputationTarget().toSpecification(),
          node.getInputValues(), 
          node.getOutputRequirements());
      items.add(jobItem);
    }
    
    CalculationJob job = new CalculationJob(jobSpec, items);
    
    s_logger.info("Enqueuing job {} to invoke {} functions",
        new Object[]{jobId, graph.getSize()});
    s_logger.debug("Enqueuing job {}", job);
    
    AtomicExecutorFuture future = new AtomicExecutorFuture(graph);
    _executingSpecifications.put(jobSpec, future);
    _cycle.getProcessingContext().getViewProcessorQueryReceiver().addJob(jobSpec, graph);
    _cycle.getProcessingContext().getComputationJobRequestSender().sendRequest(job, this);
    
    return future;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public void resultReceived(CalculationJobResult result) {
    AtomicExecutorFuture future = _executingSpecifications.remove(result.getSpecification());
    if (future == null) {
      s_logger.error("Got unexpected result {}", result);
      return;
    }
    
    for (DependencyNode node : future._graph.getDependencyNodes()) {
      _cycle.markExecuted(node);
      
      // atomicity: entire graph succeeds/fails
      if (result.getResult() != InvocationResult.SUCCESS) {
        _cycle.markFailed(node);
      }
    }
    
    // mark Future complete
    future.run();
  }
  
  private class AtomicExecutorFuture extends FutureTask<DependencyGraph> {
    
    private DependencyGraph _graph;
    
    public AtomicExecutorFuture(DependencyGraph graph) {
      super(new Runnable() {
        @Override
        public void run() {
        }
      }, null);
      _graph = graph;
    }

    @Override
    public String toString() {
      return "AtomicExecutorFuture[calcConfName=" + _graph.getCalcConfName() + "]";
    }
    
  }
  
}
