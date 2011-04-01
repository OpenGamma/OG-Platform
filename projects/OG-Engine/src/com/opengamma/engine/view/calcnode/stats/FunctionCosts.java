/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Central point for function statistics.
 * <p>
 * This acts as a local gatherer, central point for remote nodes and supplier of data.
 */
public final class FunctionCosts implements FunctionInvocationStatisticsGatherer {

  /** Logger. */
  static final Logger s_logger = LoggerFactory.getLogger(FunctionCosts.class);
  /**
   * Name used for the mean.
   */
  private static final String MEAN_STATISTICS = "MEAN";

  /**
   * The statistics per configuration.
   */
  private final ConcurrentMap<String, FunctionCostsPerConfiguration> _data = new ConcurrentHashMap<String, FunctionCostsPerConfiguration>();
  /**
   * The items that persistence is dealing with.
   */
  private final Queue<Pair<String, FunctionInvocationStatistics>> _persistedItems = new ConcurrentLinkedQueue<Pair<String, FunctionInvocationStatistics>>();
  /**
   * The persistent storage.
   */
  private final FunctionCostsMaster _costsMaster;
  /**
   * The mean statistics as persisted.
   */
  private final FunctionInvocationStatistics _meanStatistics;

  /**
   * Constructor using an in-memory master.
   */
  public FunctionCosts() {
    this(new InMemoryFunctionCostsMaster());
  }

  /**
   * Constructor.
   * 
   * @param costsMaster  the costs master, not null
   */
  public FunctionCosts(final FunctionCostsMaster costsMaster) {
    ArgumentChecker.notNull(costsMaster, "costsMaster");
    _costsMaster = costsMaster;
    _meanStatistics = loadMeanStatistics();
  }

  /**
   * Loads the mean statistics.
   */
  private FunctionInvocationStatistics loadMeanStatistics() {
    s_logger.debug("Loading initial mean statistics");
    FunctionCostsDocument doc = _costsMaster.load(MEAN_STATISTICS, MEAN_STATISTICS, null);
    if (doc == null) {
      s_logger.debug("No initial mean statistics");
      doc = new FunctionCostsDocument(MEAN_STATISTICS, MEAN_STATISTICS);
      new FunctionInvocationStatistics(MEAN_STATISTICS).populateDocument(doc);
    }
    return new FunctionInvocationStatistics(doc);
  }

  //-------------------------------------------------------------------------
  /**
   * Gathers statistics from the central node and records them.
   * 
   * @param configurationName  the configuration name, not null
   * @param functionId  the function id, not null
   * @param invocationCount  the number of invocations the data is for
   * @param executionNanos  the execution time, in nanoseconds, of the invocation(s)
   * @param dataInputBytes  the mean data input, bytes per input node, or {@code NaN} to mean statistics aren't available
   * @param dataOutputBytes  the mean data output, bytes per output node, or {@code NaN} to mean statistics aren't available
   */
  @Override
  public void functionInvoked(
      final String configurationName, final String functionId, final int invocationCount,
      final double executionNanos, final double dataInputBytes, final double dataOutputBytes) {
    getStatistics(configurationName, functionId).recordInvocation(invocationCount, executionNanos, dataInputBytes, dataOutputBytes);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets statistics for a configuration.
   * 
   * @param configurationName  the configuration name, not null
   * @return the statistics, not null
   */
  public FunctionCostsPerConfiguration getStatistics(final String configurationName) {
    FunctionCostsPerConfiguration data = _data.get(configurationName);
    if (data == null) {
      _data.putIfAbsent(configurationName, new FunctionCostsPerConfiguration(this, configurationName));
      data = _data.get(configurationName);
    }
    return data;
  }

  /**
   * Gets statistics for a function.
   * 
   * @param configurationName  the configuration name, not null
   * @param functionId  the function id, not null
   * @return the statistics, not null
   */
  public FunctionInvocationStatistics getStatistics(final String configurationName, final String functionId) {
    return getStatistics(configurationName).getStatistics(functionId);
  }

  /**
   * Loads the statistics.
   * 
   * @param configurationName  the configuration name, not null
   * @param functionId  the function id, not null
   * @return the statistics, not null
   */
  /* package */ FunctionInvocationStatistics loadStatistics(final FunctionCostsPerConfiguration configurationCosts, final String functionId) {
    String configurationName = configurationCosts.getConfigurationName();
    
    s_logger.debug("Loading statistics for {}/{}", configurationName, functionId);
    FunctionCostsDocument doc = _costsMaster.load(configurationName, functionId, null);
    if (doc != null) {
      s_logger.debug("Found previous statistics for {}/{}", configurationName, functionId);
      FunctionInvocationStatistics stats = new FunctionInvocationStatistics(doc);
      configurationCosts.getCosts().putIfAbsent(functionId, stats);
      return configurationCosts.getCosts().get(functionId);
      
    } else {
      s_logger.debug("No previous statistics for {}/{}", configurationName, functionId);
      FunctionInvocationStatistics stats = new FunctionInvocationStatistics(functionId);
      stats.setCosts(_meanStatistics.getInvocationCost(), _meanStatistics.getDataInputCost(), _meanStatistics.getDataOutputCost());
      FunctionInvocationStatistics newStats = configurationCosts.getCosts().putIfAbsent(functionId, stats);
      if (newStats != null) {
        return newStats;  // another thread created the statistics
      }
      _persistedItems.add(Pair.of(configurationName, stats));
      return stats;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a runnable to write the statistics.
   * 
   * @return the runnable, not null
   */
  public Runnable createPersistenceWriter() {
    // the statistics are added to a concurrent collection, which is processed here
    
    return new Runnable() {
      @Override
      public void run() {
        s_logger.info("Persisting function execution statistics");
        final FunctionInvocationStatistics meanStatistics = _meanStatistics;
        final long lastUpdate = meanStatistics.getLastUpdateNanos();
        
        // store updates and calculate mean
        int count = 1;
        double invocationCost = meanStatistics.getInvocationCost();
        double dataInputCost = meanStatistics.getDataInputCost();
        double dataOutputCost = meanStatistics.getDataOutputCost();
        for (Pair<String, FunctionInvocationStatistics> pair : _persistedItems) {
          final FunctionInvocationStatistics stats = pair.getSecond();
          if (stats.getLastUpdateNanos() > lastUpdate) {
            // store
            s_logger.debug("Storing {}/{}", pair.getFirst(), stats.getFunctionId());
            FunctionCostsDocument doc = new FunctionCostsDocument(pair.getFirst(), stats.getFunctionId());
            stats.populateDocument(doc);
            _costsMaster.store(doc);
            
            // calculate mean
            invocationCost += stats.getInvocationCost();
            dataInputCost += stats.getDataInputCost();
            dataOutputCost += stats.getDataOutputCost();
            count++;
          }
        }
        meanStatistics.setCosts(invocationCost / (double) count, dataInputCost / (double) count, dataOutputCost / (double) count);
        if (count > 1) {
          s_logger.debug("Storing new mean statistics {}", meanStatistics);
          FunctionCostsDocument doc = new FunctionCostsDocument(MEAN_STATISTICS, MEAN_STATISTICS);
          meanStatistics.populateDocument(doc);
          _costsMaster.store(doc);
        }
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "FunctionCosts";
  }

  //-------------------------------------------------------------------------
  // For debug purposes only
  public FudgeFieldContainer toFudgeMsg(final FudgeMsgFactory factory) {
    final MutableFudgeFieldContainer message = factory.newMessage();
    for (Map.Entry<String, FunctionCostsPerConfiguration> configuration : _data.entrySet()) {
      final MutableFudgeFieldContainer configurationMessage = factory.newMessage();
      for (Map.Entry<String, FunctionInvocationStatistics> function : configuration.getValue().getCosts().entrySet()) {
        final MutableFudgeFieldContainer functionMessage = factory.newMessage();
        functionMessage.add("invocationCost", function.getValue().getInvocationCost());
        functionMessage.add("dataInput", function.getValue().getDataInputCost());
        functionMessage.add("dataOutput", function.getValue().getDataOutputCost());
        configurationMessage.add(function.getKey(), functionMessage);
      }
      message.add(configuration.getKey(), configurationMessage);
    }
    return message;
  }

}
