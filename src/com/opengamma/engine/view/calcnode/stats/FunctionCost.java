/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.util.ArgumentChecker;

/**
 * Gather function invocation statistics and supplies the data to anything
 * that needs it. 
 */
public class FunctionCost implements FunctionInvocationStatisticsGatherer {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionCost.class);

  /**
   * 
   */
  public final class ForConfiguration {

    private final ConcurrentMap<String, FunctionInvocationStatistics> _data = new ConcurrentHashMap<String, FunctionInvocationStatistics>();
    private final String _configurationName;

    private ForConfiguration(final String configurationName) {
      _configurationName = configurationName;
    }

    private String getDocumentName(final String functionIdentifier) {
      return getConfigurationName().replaceAll("[\\\\:]", "\\\\$0") + "::" + functionIdentifier;
    }

    public String getConfigurationName() {
      return _configurationName;
    }

    public FunctionInvocationStatistics getStatistics(final String functionIdentifier) {
      FunctionInvocationStatistics stats = _data.get(functionIdentifier);
      if (stats == null) {
        if (_configMaster != null) {
          s_logger.debug("Loading statistics for {}/{}", getConfigurationName(), functionIdentifier);
          final ConfigSearchRequest request = new ConfigSearchRequest();
          request.setName(getDocumentName(functionIdentifier));
          final ConfigSearchResult<FunctionInvocationStatistics> result = _configMaster.search(request);
          final List<ConfigDocument<FunctionInvocationStatistics>> docs = result.getDocuments();
          if (docs.size() > 0) {
            stats = docs.get(0).getValue();
            if (docs.size() > 1) {
              s_logger.warn("Multiple documents found for {}/{}", getConfigurationName(), functionIdentifier);
            }
          } else {
            s_logger.debug("No previous statistics for {}/{}", getConfigurationName(), functionIdentifier);
          }
        }
        if (stats == null) {
          stats = new FunctionInvocationStatistics(functionIdentifier);
          // TODO [ENG-229] Initial values should be averages for this configuration
        }
        FunctionInvocationStatistics newStats = _data.putIfAbsent(functionIdentifier, stats);
        if (newStats != null) {
          stats = newStats;
        } else {
          // We created function statistics, so poke it into storage
          if (_configMaster != null) {
            final DefaultConfigDocument<FunctionInvocationStatistics> doc = new DefaultConfigDocument<FunctionInvocationStatistics>();
            doc.setValue(stats);
            doc.setName(getDocumentName(functionIdentifier));
            _configDocuments.add(doc);
          }
        }
      }
      return stats;
    }

  }

  private final ConcurrentMap<String, ForConfiguration> _data = new ConcurrentHashMap<String, ForConfiguration>();
  private final Queue<ConfigDocument<FunctionInvocationStatistics>> _configDocuments = new ConcurrentLinkedQueue<ConfigDocument<FunctionInvocationStatistics>>();
  private ConfigMaster<FunctionInvocationStatistics> _configMaster;

  public ForConfiguration getStatistics(final String calculationConfiguration) {
    ForConfiguration data = _data.get(calculationConfiguration);
    if (data == null) {
      data = new ForConfiguration(calculationConfiguration);
      ForConfiguration newData = _data.putIfAbsent(calculationConfiguration, data);
      if (newData != null) {
        data = newData;
      }
    }
    return data;
  }

  public FunctionInvocationStatistics getStatistics(final String calculationConfiguration, final String functionIdentifier) {
    return getStatistics(calculationConfiguration).getStatistics(functionIdentifier);
  }

  @Override
  public void functionInvoked(final String configurationName, final String functionIdentifier, final int count, final double invocationTime, final double dataInput, final double dataOutput) {
    getStatistics(configurationName, functionIdentifier).recordInvocation(count, invocationTime, dataInput, dataOutput);
  }

  public void setPersistence(final ConfigMaster<FunctionInvocationStatistics> configMaster) {
    _configMaster = configMaster;
  }

  public Runnable createPersistenceWriter() {
    ArgumentChecker.notNull(_configMaster, "persistence");
    return new Runnable() {

      private long _lastUpdate;

      @Override
      public void run() {
        s_logger.info("Persisting function execution statistics");
        for (int i = _configDocuments.size(); i > 0; i--) {
          final ConfigDocument<FunctionInvocationStatistics> configDocument = _configDocuments.poll();
          if (configDocument.getUniqueIdentifier() != null) {
            if (configDocument.getValue().getLastUpdateNanos() > _lastUpdate) {
              s_logger.debug("Updating document {} with {}", configDocument.getUniqueIdentifier(), configDocument.getName());
              _configDocuments.add(_configMaster.update(configDocument));
            } else {
              _configDocuments.add(configDocument);
            }
          } else {
            s_logger.debug("Adding {} to store", configDocument.getName());
            _configDocuments.add(_configMaster.add(configDocument));
          }
        }
        _lastUpdate = System.nanoTime();
      }
    };
  }

  // For debug purposes only
  public FudgeFieldContainer toFudgeMsg(final FudgeMessageFactory factory) {
    final MutableFudgeFieldContainer message = factory.newMessage();
    for (Map.Entry<String, ForConfiguration> configuration : _data.entrySet()) {
      final MutableFudgeFieldContainer configurationMessage = factory.newMessage();
      for (Map.Entry<String, FunctionInvocationStatistics> function : configuration.getValue()._data.entrySet()) {
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
