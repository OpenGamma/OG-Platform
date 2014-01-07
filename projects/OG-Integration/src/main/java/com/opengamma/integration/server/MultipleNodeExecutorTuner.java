/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.calcnode.stats.CalculationNodeStatistics;
import com.opengamma.engine.calcnode.stats.TotallingNodeStatisticsGatherer;
import com.opengamma.engine.exec.MultipleNodeExecutorFactory;
import com.opengamma.engine.exec.stats.GraphExecutionStatistics;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Modifies the parameters available to MultipleNodeExecutorFactory to report the effect of each.
 */
public class MultipleNodeExecutorTuner extends TerminatableJob implements Lifecycle, InitializingBean {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutorTuner.class);

  private final Queue<Pair<Integer, Integer>> _minimumItems = new LinkedList<Pair<Integer, Integer>>();
  private final Queue<Pair<Long, Long>> _minimumCost = new LinkedList<Pair<Long, Long>>();

  private MultipleNodeExecutorFactory _executorFactory;
  private TotallingGraphStatisticsGathererProvider _graphStatistics;
  private TotallingNodeStatisticsGatherer _nodeStatistics;
  private int _warmupGraphExecutions = 5;
  private int _sampleTime = 10;
  @SuppressWarnings("unused")
  private int _minimumMaximumConcurrency = 1;
  @SuppressWarnings("unused")
  private int _maximumMaximumConcurrency = 16;
  private String _filename = System.getProperty("java.io.tmpdir") + File.separatorChar + getClass().getSimpleName() + ".csv";

  private int _sampleCount;
  private boolean _running;

  public MultipleNodeExecutorTuner() {
    _minimumItems.add(Pairs.of(1, Integer.MAX_VALUE - 10));
    _minimumCost.add(Pairs.of(1L, Long.MAX_VALUE - 10));
  }

  public void setExecutorFactory(final MultipleNodeExecutorFactory executorFactory) {
    _executorFactory = executorFactory;
  }

  public void setGraphStatistics(final TotallingGraphStatisticsGathererProvider graphStatistics) {
    _graphStatistics = graphStatistics;
  }

  public void setNodeStatistics(final TotallingNodeStatisticsGatherer nodeStatistics) {
    _nodeStatistics = nodeStatistics;
  }

  public void setMinimumJobItemsLowerLimit(final Integer minimumItems) {
    _minimumItems.add(Pairs.of(minimumItems, _minimumItems.poll().getSecond()));
  }

  public void setMinimumJobItemsUpperLimit(final Integer maximumItems) {
    _minimumItems.add(Pairs.of(_minimumItems.poll().getFirst(), maximumItems));
  }

  public void setMinimumJobCostLowerLimit(final Long minimumCost) {
    _minimumCost.add(Pairs.of(minimumCost, _minimumCost.poll().getSecond()));
  }

  public void setMinimumJobCostUpperLimit(final Long maximumCost) {
    _minimumCost.add(Pairs.of(_minimumCost.poll().getFirst(), maximumCost));
  }

  public void setMinimumMaximumConcurrency(final int minimumMaximumConcurrency) {
    _minimumMaximumConcurrency = minimumMaximumConcurrency;
  }

  public void setMaximumMaximumConcurrency(final int maximumMaximumConcurrency) {
    _maximumMaximumConcurrency = maximumMaximumConcurrency;
  }

  public void setWarmupGraphExecutions(final int warmupGraphExecutions) {
    _warmupGraphExecutions = warmupGraphExecutions;
  }

  public void setSampleTime(final int sampleTime) {
    _sampleCount = sampleTime;
  }

  public void setFilename(final String filename) {
    _filename = filename;
  }

  @Override
  public synchronized boolean isRunning() {
    return _running;
  }

  @Override
  public synchronized void start() {
    if (!isRunning() && !isTerminated()) {
      s_logger.info("Starting tuner");
      _running = true;
      new Thread(this).start();
    } else {
      s_logger.warn("Tuner already started (or already terminated)");
    }
  }

  @Override
  public synchronized void stop() {
    s_logger.info("Stopping tuner");
    terminate();
    _running = false;
  }

  // THIS IS A HACK; TAKE THIS OUT WHEN THE CONTEXT STARTS US PROPERLY
  @Override
  public void afterPropertiesSet() throws Exception {
    start();
  }

  private boolean warmedUp() {
    if (_sampleCount > 0) {
      return true;
    }
    for (GraphExecutorStatisticsGatherer gatherers : _graphStatistics.getViewStatistics()) {
      for (GraphExecutionStatistics stats : ((TotallingGraphStatisticsGathererProvider.Statistics) gatherers).getExecutionStatistics()) {
        if (stats.getExecutedGraphs() > _warmupGraphExecutions) {
          return true;
        }
      }
    }
    return false;
  }

  private void tickAndReset() {
    for (GraphExecutorStatisticsGatherer gatherers : _graphStatistics.getViewStatistics()) {
      for (GraphExecutionStatistics stats : ((TotallingGraphStatisticsGathererProvider.Statistics) gatherers).getExecutionStatistics()) {
        while (stats.getExecutedGraphs() < _warmupGraphExecutions) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
          }
        }
        stats.reset();
      }
    }
    for (CalculationNodeStatistics stats : _nodeStatistics.getNodeStatistics()) {
      stats.reset();
    }
  }

  private void report(final String data) {
    try {
      final FileWriter writer = new FileWriter(_filename, true);
      final PrintWriter pw = new PrintWriter(writer, true);
      pw.println(data);
      pw.close();
      writer.close();
    } catch (IOException e) {
      s_logger.warn("Error writing tuning data", e);
    }
  }

  private void writeRow(final boolean reset) {
    final StringBuilder sb = new StringBuilder();
    sb.append(_executorFactory.getMinimumJobItems()).append(',');
    sb.append(_executorFactory.getMaximumJobItems()).append(',');
    sb.append(_executorFactory.getMinimumJobCost()).append(',');
    sb.append(_executorFactory.getMaximumJobCost()).append(',');
    sb.append(_executorFactory.getMaximumConcurrency()).append(',');
    for (GraphExecutorStatisticsGatherer gatherers : _graphStatistics.getViewStatistics()) {
      for (GraphExecutionStatistics stats : ((TotallingGraphStatisticsGathererProvider.Statistics) gatherers).getExecutionStatistics()) {
        sb.append(stats.getViewProcessId()).append(',').append(stats.getCalcConfigName()).append(',');
        sb.append(stats.getActualTime()).append(',');
        sb.append(stats.getAverageActualTime()).append(',');
        sb.append(stats.getAverageExecutionTime()).append(',');
        sb.append(stats.getAverageGraphSize()).append(',');
        sb.append(stats.getAverageJobSize()).append(',');
        sb.append(stats.getAverageJobCycleCost()).append(',');
        sb.append(stats.getAverageJobDataCost()).append(',');
        sb.append(stats.getExecutedGraphs()).append(',');
        sb.append(stats.getExecutedNodes()).append(',');
        sb.append(stats.getExecutionTime()).append(',');
        sb.append(stats.getProcessedGraphs()).append(',');
        sb.append(stats.getProcessedJobs()).append(',');
        if (reset) {
          stats.reset();
        }
      }
    }
    int count = 0;
    double averageExecutionTime = 0;
    double averageJobItems = 0;
    double averageNonExecutionTime = 0;
    double executionTime = 0;
    double jobItems = 0;
    double nonExecutionTime = 0;
    double successfulJobs = 0;
    double unsuccessfulJobs = 0;
    for (CalculationNodeStatistics stats : _nodeStatistics.getNodeStatistics()) {
      averageExecutionTime += stats.getAverageExecutionTime();
      averageJobItems += stats.getAverageJobItems();
      averageNonExecutionTime += stats.getAverageNonExecutionTime();
      executionTime += stats.getExecutionTime();
      jobItems += stats.getJobItems();
      nonExecutionTime += stats.getNonExecutionTime();
      successfulJobs += stats.getSuccessfulJobs();
      unsuccessfulJobs += stats.getUnsuccessfulJobs();
      count++;
    }
    if (count > 0) {
      sb.append(averageExecutionTime / (double) count).append(',');
      sb.append(averageJobItems / (double) count).append(',');
      sb.append(averageNonExecutionTime / (double) count).append(',');
      sb.append(executionTime / (double) count).append(',');
      sb.append(jobItems / (double) count).append(',');
      sb.append(nonExecutionTime / (double) count).append(',');
      sb.append(successfulJobs / (double) count).append(',');
      sb.append(unsuccessfulJobs / (double) count);
    }
    report(sb.toString());
  }

  @Override
  protected void runOneCycle() {
    if (warmedUp()) {
      final boolean reset = (_sampleCount++ % _sampleTime) == 0;
      s_logger.debug("Sample {}", _sampleCount);
      writeRow(reset);
      if (reset) {
        s_logger.debug("Reseting statistics");
        /*
         * final int maxConcurrency = _executorFactory.getMaximumConcurrency();
         * if (maxConcurrency >= _maximumMaximumConcurrency) {
         * _executorFactory.setMaximumConcurrency(_minimumMaximumConcurrency);
         * final Pair<Integer, Integer> minimum = _minimumItems.poll();
         * if (minimum != null) {
         * final Integer midpoint = (minimum.getFirst() + minimum.getSecond()) >> 1;
         * _executorFactory.setMinimumJobItems(midpoint);
         * s_logger.info("Setting minimum job items to {}", midpoint);
         * if (midpoint > minimum.getFirst()) {
         * _minimumItems.add(Pair.of(minimum.getFirst(), midpoint));
         * }
         * if (midpoint < minimum.getSecond()) {
         * _minimumItems.add(Pair.of(midpoint, minimum.getSecond()));
         * }
         * }
         * } else {
         * if (maxConcurrency < 4) {
         * _executorFactory.setMaximumConcurrency(maxConcurrency + 1);
         * } else {
         * _executorFactory.setMaximumConcurrency(maxConcurrency + (maxConcurrency >> 2));
         * }
         * }
         */
        final Pair<Long, Long> minimum = _minimumCost.poll();
        if (minimum != null) {
          final Long midpoint = (minimum.getFirst() + minimum.getSecond()) >> 1;
          _executorFactory.setMinimumJobCost(midpoint);
          s_logger.info("Setting minimum job cost to {}", midpoint);
          if (midpoint > minimum.getFirst()) {
            _minimumCost.add(Pairs.of(minimum.getFirst(), midpoint));
          }
          if (midpoint < minimum.getSecond()) {
            _minimumCost.add(Pairs.of(midpoint, minimum.getSecond()));
          }
        }
        tickAndReset();
        s_logger.debug("Statistics reset");
      }
    } else {
      s_logger.info("Waiting for system to warm up");
    }
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
  }
}
