/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.DependencyGraphExecutorListener;
import com.opengamma.util.ArgumentChecker;

/**
 * This DependencyGraphExecutor executes the given dependency graph
 * on a single calculation node in a single thread. Whether that node 
 * is the local machine or a remote machine on the grid depends on the
 * the {@link com.opengamma.engine.view.calcnode.JobRequestSender} configured in 
 * {@link com.opengamma.engine.view.ViewProcessingContext}.
 * 
 */
public class SingleNodeExecutor implements DependencyGraphExecutor, JobResultReceiver {
  
  private static final Logger s_logger = LoggerFactory.getLogger(SingleNodeExecutor.class);
  
  private final SingleComputationCycle _cycle;
  private final DependencyGraphExecutorListener _listener;
  
  private final Map<CalculationJobSpecification, AtomicExecutorFuture> _executingSpecifications =
    new ConcurrentHashMap<CalculationJobSpecification, AtomicExecutorFuture>();
  
  public SingleNodeExecutor(
      SingleComputationCycle cycle,
      DependencyGraphExecutorListener listener) {
    ArgumentChecker.notNull(cycle, "Computation cycle");
    ArgumentChecker.notNull(listener, "Single node executor listener");
    _cycle = cycle; 
    _listener = listener;
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
    Map<CalculationJobItem, DependencyNode> item2Node = new HashMap<CalculationJobItem, DependencyNode>();
    
    for (DependencyNode node : order) {
      boolean shouldWriteResults = _cycle.getViewDefinition().shouldWriteResults(node.getComputationTarget());
      
      CalculationJobItem jobItem = new CalculationJobItem(
          node.getFunctionDefinition().getUniqueIdentifier(),
          node.getComputationTarget().toSpecification(),
          node.getInputValues(), 
          node.getOutputRequirements(),
          shouldWriteResults);
      items.add(jobItem);
      item2Node.put(jobItem, node);
    }
    
    CalculationJob job = new CalculationJob(jobSpec, items);

    s_logger.info("Executing {} items", job.getJobItems().size());
    
    _listener.preExecute(this, job);
    
    s_logger.info("Enqueuing {} to invoke {} functions",
        new Object[]{jobSpec, job.getJobItems().size()});
    
    AtomicExecutorRunnable runnable = new AtomicExecutorRunnable();
    AtomicExecutorFuture future = new AtomicExecutorFuture(runnable, graph, item2Node);
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
  public ViewComputationCache getCache(CalculationJob job) {
    return _cycle.getComputationCache(job.getSpecification().getCalcConfigName());        
  }
  
  @Override
  public ViewComputationCache getCache(CalculationJobResult result) {
    return _cycle.getComputationCache(result.getSpecification().getCalcConfigName());        
  }

  @Override
  public void resultReceived(CalculationJobResult result) {
    AtomicExecutorFuture future = _executingSpecifications.remove(result.getSpecification());
    if (future == null) {
      s_logger.error("Got unexpected result {}", result);
      return;
    }
    
    for (CalculationJobResultItem item : result.getResultItems()) {
      DependencyNode node = future._item2Node.get(item.getItem());
      if (node == null) {
        s_logger.error("Got unexpected item {}", item);
        continue;
      }
      
      _cycle.markExecuted(node);
      
      if (item.getResult() != InvocationResult.SUCCESS) {
        _cycle.markFailed(node);
      }
    }
    
    try {
      _listener.postExecute(this, result);
    } catch (RuntimeException e) {
      future._runnable._exception = e;
      s_logger.error("Listener failed to execute", e);      
    }
    
    // mark Future complete
    future.run();
  }
  
  private class AtomicExecutorFuture extends FutureTask<DependencyGraph> {
    
    private AtomicExecutorRunnable _runnable;
    private DependencyGraph _graph;
    private Map<CalculationJobItem, DependencyNode> _item2Node;
    
    public AtomicExecutorFuture(
        AtomicExecutorRunnable runnable,
        DependencyGraph graph, 
        Map<CalculationJobItem, DependencyNode> item2Node) {
      super(runnable, null);
      _runnable = runnable;
      _graph = graph;
      _item2Node = item2Node;
    }

    @Override
    public String toString() {
      return "AtomicExecutorFuture[calcConfName=" + _graph.getCalcConfName() + "]";
    }
    
  }
  
  private class AtomicExecutorRunnable implements Runnable {
    private RuntimeException _exception;

    @Override
    public void run() {
      if (_exception != null) {
        throw _exception;
      }
    }
  }

}
