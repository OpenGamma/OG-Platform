/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.Collection;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.calcnode.Capability;
import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.calcnode.PlatformCapabilities;
import com.opengamma.engine.calcnode.stats.CalculationNodeStatistics;
import com.opengamma.engine.calcnode.stats.TotallingNodeStatisticsGatherer;
import com.opengamma.engine.exec.stats.GraphExecutionStatistics;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * <p>
 * Continuously tunes the parameters to a {@link MultipleNodeExecutorFactory} to maintain good performance while aspects of the computing cluster change. Schedule this to run periodically to update
 * its sampling and make continuous adjustments.
 * </p>
 * <h2>Tuning rules<h2>
 * <p>
 * Set maximum concurrency to the average node count of the job invokers. Requires a {@link JobDispatcher}.
 * </p>
 * <p>
 * TODO: [ENG-200] Tuning of job size and cost parameters
 * </p>
 */
public class MultipleNodeExecutorTuner implements Runnable {

  // REVIEW 2010-09-09 Andrew -- Instead of running periodically and relying on other statistics gatherers, this
  // should implement the gathering interfaces and make adjustments as statistical data arrives, possibly acting
  // as a pass-through so it can sit on top of other gathering implementations

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutorTuner.class);

  private final MultipleNodeExecutorFactory _factory;

  private JobDispatcher _jobDispatcher;
  private TotallingGraphStatisticsGathererProvider _graphExecutionStatistics;
  private TotallingNodeStatisticsGatherer _jobDispatchStatistics;
  private double _statisticDecayRate = 0.1; // 10% decay every schedule
  private int _statisticsKeepAlive = 300; // keep for 5 minutes

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

  public void setGraphExecutionStatistics(final TotallingGraphStatisticsGathererProvider graphExecutionStatistics) {
    _graphExecutionStatistics = graphExecutionStatistics;
  }

  protected TotallingGraphStatisticsGathererProvider getGraphExecutionStatistics() {
    return _graphExecutionStatistics;
  }

  public void setJobDispatchStatistics(final TotallingNodeStatisticsGatherer jobDispatchStatistics) {
    _jobDispatchStatistics = jobDispatchStatistics;
  }

  protected TotallingNodeStatisticsGatherer getJobDispatchStatistics() {
    return _jobDispatchStatistics;
  }

  public void setStatisticsKeepAlive(final int seconds) {
    _statisticsKeepAlive = seconds;
  }

  protected int getStatisticsKeepAlive() {
    return _statisticsKeepAlive;
  }

  public void setStatisticsDecayRate(final double decayRate) {
    _statisticDecayRate = decayRate;
  }

  protected double getStatisticsDecayRate() {
    return _statisticDecayRate;
  }

  /**
   * Makes one tuning adjustment.
   */
  @Override
  public void run() {
    if (getJobDispatcher() != null) {
      s_logger.debug("Processing capabilities");
      final Map<String, Collection<Capability>> allCapabilities = getJobDispatcher().getAllCapabilities();
      int nodesPerInvokerCount = 0;
      double nodesPerInvoker = 0;
      boolean changed = false;
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
          changed = true;
        }
      }
      if (changed) {
        getFactory().invalidateCache();
      }
    }
    if (getGraphExecutionStatistics() != null) {
      s_logger.debug("Processing graph execution statistics");
      for (TotallingGraphStatisticsGathererProvider.Statistics gatherer : getGraphExecutionStatistics().getViewStatistics()) {
        for (GraphExecutionStatistics statistics : gatherer.getExecutionStatistics()) {
          statistics.decay(getStatisticsDecayRate());
        }
      }
      getGraphExecutionStatistics().dropStatisticsBefore(Instant.now().minusSeconds(getStatisticsKeepAlive()));
    }
    if (getJobDispatchStatistics() != null) {
      s_logger.debug("Processing job dispatch statistics");
      for (CalculationNodeStatistics statistics : getJobDispatchStatistics().getNodeStatistics()) {
        statistics.decay(getStatisticsDecayRate());
      }
      getJobDispatchStatistics().dropStatisticsBefore(Instant.now().minusSeconds(getStatisticsKeepAlive()));
    }
  }

  private FudgeMsg dumpCapabilities(final FudgeSerializer serializer, final String invokerId, final Collection<Capability> capabilities) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("identifier", invokerId);
    for (Capability capability : capabilities) {
      serializer.addToMessageWithClassHeaders(message, capability.getIdentifier(), null, capability, Capability.class);
    }
    return message;
  }

  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "factory", null, getFactory());
    if (getJobDispatcher() != null) {
      for (Map.Entry<String, Collection<Capability>> capabilities : getJobDispatcher().getAllCapabilities().entrySet()) {
        message.add("Invoker", dumpCapabilities(serializer, capabilities.getKey(), capabilities.getValue()));
      }
    }
    return message;
  }

}
