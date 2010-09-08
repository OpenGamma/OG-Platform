/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.Map;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.calc.stats.GraphExecutionStatistics;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calc.stats.PerViewStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.TotallingStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.Capability;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.PlatformCapabilities;
import com.opengamma.engine.view.calcnode.stats.CalculationNodeStatistics;
import com.opengamma.engine.view.calcnode.stats.TotallingStatisticsGatherer;
import com.opengamma.util.ArgumentChecker;

/**
 * <p>Continuously tunes the parameters to a {@link MultipleNodeExecutorFactory} to maintain good performance
 * while aspects of the computing cluster change. Schedule this to run periodically to update its sampling
 * and make continuous adjustments.</p>
 * 
 * <h2>Tuning rules<h2>
 * 
 * <p>Set maximum concurrency to the average node count of the job invokers. Requires a {@link JobDispatcher}.</p>
 * 
 * <p>TODO: [ENG-200] Tuning of job size and cost parameters</p>
 */
public class MultipleNodeExecutorTuner implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutorTuner.class);

  private final MultipleNodeExecutorFactory _factory;

  private JobDispatcher _jobDispatcher;
  private PerViewStatisticsGathererProvider _graphExecutionStatistics;
  private TotallingStatisticsGatherer _jobDispatchStatistics;

  /**
   * @param factory The factory to tune
   */
  public MultipleNodeExecutorTuner(final MultipleNodeExecutorFactory factory) {
    ArgumentChecker.notNull(factory, "factory");
    _factory = factory;
  }

  protected MultipleNodeExecutorFactory getFactory() {
    return _factory;
  }

  public void setJobDispatcher(final JobDispatcher jobDispatcher) {
    _jobDispatcher = jobDispatcher;
  }

  protected JobDispatcher getJobDispatcher() {
    return _jobDispatcher;
  }

  public void setGraphExecutionStatistics(final PerViewStatisticsGathererProvider graphExecutionStatistics) {
    _graphExecutionStatistics = graphExecutionStatistics;
  }

  protected PerViewStatisticsGathererProvider getGraphExecutionStatistics() {
    return _graphExecutionStatistics;
  }

  public void setJobDispatchStatistics(final TotallingStatisticsGatherer jobDispatchStatistics) {
    _jobDispatchStatistics = jobDispatchStatistics;
  }

  protected TotallingStatisticsGatherer getJobDispatchStatistics() {
    return _jobDispatchStatistics;
  }

  /**
   * Makes one tuning adjustment.
   */
  @Override
  public void run() {
    if (getJobDispatcher() != null) {
      final Map<String, Collection<Capability>> allCapabilities = getJobDispatcher().getAllCapabilities();
      int nodesPerInvokerCount = 0;
      double nodesPerInvoker = 0;
      for (Map.Entry<String, Collection<Capability>> capabilities : allCapabilities.entrySet()) {
        for (Capability capability : capabilities.getValue()) {
          if (PlatformCapabilities.NODE_COUNT.equals(capability.getIdentifier())) {
            nodesPerInvokerCount++;
            nodesPerInvoker += capability.getUpperBoundParameter();
          }
        }
      }
      if (nodesPerInvokerCount > 0) {
        s_logger.debug("Found {} nodes at {} invokers", nodesPerInvoker, nodesPerInvokerCount);
        int maxConcurrency = getFactory().getMaximumConcurrency();
        int newMaxConcurrency = (int) Math.ceil(nodesPerInvoker / (double) nodesPerInvokerCount);
        if (newMaxConcurrency != maxConcurrency) {
          s_logger.info("Changing maximum concurrency to {}", newMaxConcurrency);
          getFactory().setMaximumConcurrency(newMaxConcurrency);
        }
      }
    }
    if (getGraphExecutionStatistics() != null) {
      for (GraphExecutorStatisticsGatherer gatherer : getGraphExecutionStatistics().getViewStatistics()) {
        for (GraphExecutionStatistics statistics : ((TotallingStatisticsGathererProvider.Statistics) gatherer).getExecutionStatistics()) {
          statistics.decay(0.1);
        }
      }
    }
    if (getJobDispatchStatistics() != null) {
      for (CalculationNodeStatistics statistics : getJobDispatchStatistics().getNodeStatistics()) {
        statistics.decay(0.1);
      }
    }
  }

  private FudgeFieldContainer dumpCapabilities(final FudgeSerializationContext context, final Collection<Capability> capabilities) {
    final MutableFudgeFieldContainer message = context.newMessage();
    for (Capability capability : capabilities) {
      context.objectToFudgeMsgWithClassHeaders(message, capability.getIdentifier(), null, capability, Capability.class);
    }
    return message;
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "factory", null, getFactory());
    if (getJobDispatcher() != null) {
      for (Map.Entry<String, Collection<Capability>> capabilities : getJobDispatcher().getAllCapabilities().entrySet()) {
        message.add(capabilities.getKey(), dumpCapabilities(context, capabilities.getValue()));
      }
    }
    return message;
  }

}
