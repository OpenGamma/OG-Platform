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
import com.opengamma.config.ConfigTypeMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Central point for function statistics.
 * <p>
 * This acts as a local gatherer, central point for remote nodes and supplier of data.
 */
public class FunctionCost implements FunctionInvocationStatisticsGatherer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(FunctionCost.class);
  /**
   * Name used in the config database.
   */
  private static final String MEAN_INVOCATION_DOCUMENT_NAME = "MEAN";

  /**
   * All statistics for a single configuration.
   */
  public final class ForConfiguration {

    /**
     * The configuration name.
     */
    private final String _configurationName;
    /**
     * The map of per function statistics.
     */
    private final ConcurrentMap<String, FunctionInvocationStatistics> _data = new ConcurrentHashMap<String, FunctionInvocationStatistics>();

    /**
     * Creates an instance for a configuration name.
     * 
     * @param configurationName  the configuration name, not null
     */
    private ForConfiguration(final String configurationName) {
      _configurationName = configurationName;
    }

    /**
     * Gets the configuration name.
     * 
     * @return the configuration name, not null
     */
    public String getConfigurationName() {
      return _configurationName;
    }

    /**
     * Converts a function id to a document name.
     * 
     * @param functionId  the function id, not null
     * @return the document name, not null
     */
    private String getDocumentName(final String functionId) {
      return getConfigurationName().replaceAll("[\\\\:]", "\\\\$0") + "::" + functionId;
    }

    /**
     * Gets the statistics for a function.
     * 
     * @param functionId  the function id, not null
     * @return the statistics, not null
     */
    public FunctionInvocationStatistics getStatistics(final String functionId) {
      FunctionInvocationStatistics stats = _data.get(functionId);
      if (stats == null) {
        ConfigDocument<FunctionInvocationStatistics> statsDoc = null;
        if (getPersistence() != null) {
          s_logger.debug("Loading statistics for {}/{}", getConfigurationName(), functionId);
          final ConfigSearchRequest request = new ConfigSearchRequest();
          request.setName(getDocumentName(functionId));
          final ConfigSearchResult<FunctionInvocationStatistics> result = getTypedPersistence().search(request);
          final List<ConfigDocument<FunctionInvocationStatistics>> docs = result.getDocuments();
          if (docs.size() > 0) {
            statsDoc = docs.get(0);
            stats = statsDoc.getValue();
            if (docs.size() > 1) {
              s_logger.warn("Multiple documents found for {}/{}", getConfigurationName(), functionId);
              for (int i = docs.size() - 1; i > 0; i--) {
                s_logger.info("Deleting {}", docs.get(i).getConfigId());
                getTypedPersistence().remove(docs.get(i).getConfigId());
              }
            }
          } else {
            s_logger.debug("No previous statistics for {}/{}", getConfigurationName(), functionId);
          }
        }
        if (stats == null) {
          stats = new FunctionInvocationStatistics(functionId);
          if (getMeanStatistics() != null) {
            stats.setCosts(getMeanStatistics().getInvocationCost(), getMeanStatistics().getDataInputCost(), getMeanStatistics().getDataOutputCost());
          }
        }
        FunctionInvocationStatistics newStats = _data.putIfAbsent(functionId, stats);
        if (newStats != null) {
          stats = newStats;
        } else {
          // we created function statistics, so poke it into storage
          if (getPersistence() != null) {
            if (statsDoc == null) {
              statsDoc = new ConfigDocument<FunctionInvocationStatistics>();
              statsDoc.setValue(stats);
              statsDoc.setName(getDocumentName(functionId));
            }
            getConfigDocuments().add(statsDoc);
          }
        }
      }
      return stats;
    }
  }

  /**
   * The statistics per configuration.
   */
  private final ConcurrentMap<String, ForConfiguration> _data = new ConcurrentHashMap<String, ForConfiguration>();
  /**
   * The persistent storage.
   */
  private ConfigMaster _configMaster;
  /**
   * The configuration documents.
   */
  private final Queue<ConfigDocument<FunctionInvocationStatistics>> _configDocuments = new ConcurrentLinkedQueue<ConfigDocument<FunctionInvocationStatistics>>();
  /**
   * The mean statistics as persisted.
   */
  private FunctionInvocationStatistics _meanStatistics;
  /**
   * The mean statistics document.
   */
  private ConfigDocument<FunctionInvocationStatistics> _meanStatisticsDocument;

  /**
   * Gets statistics for a configuration.
   * 
   * @param calculationConfiguration  the configuration id, not null
   * @return the statistics, not null
   */
  public ForConfiguration getStatistics(final String calculationConfiguration) {
    ForConfiguration data = _data.get(calculationConfiguration);
    if (data == null) {
      _data.putIfAbsent(calculationConfiguration, new ForConfiguration(calculationConfiguration));
      data = _data.get(calculationConfiguration);
    }
    return data;
  }

  /**
   * Gets statistics for a function.
   * 
   * @param calculationConfiguration  the configuration id, not null
   * @param functionId  the function id, not null
   * @return the statistics, not null
   */
  public FunctionInvocationStatistics getStatistics(final String calculationConfiguration, final String functionId) {
    return getStatistics(calculationConfiguration).getStatistics(functionId);
  }

  @Override
  public void functionInvoked(final String configurationName, final String functionIdentifier, final int count, final double invocationTime, final double dataInput, final double dataOutput) {
    getStatistics(configurationName, functionIdentifier).recordInvocation(count, invocationTime, dataInput, dataOutput);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the persistent store.
   * 
   * @return the persistent store.
   */
  public ConfigMaster getPersistence() {
    return _configMaster;
  }

  /**
   * Sets the persist store.
   * 
   * @param configMaster  the persist store, not null
   */
  public void setPersistence(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _configMaster = configMaster;
    s_logger.debug("Searching for initial mean statistics");
    final ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName(MEAN_INVOCATION_DOCUMENT_NAME);
    final ConfigSearchResult<FunctionInvocationStatistics> result = getTypedPersistence().search(request);
    final List<ConfigDocument<FunctionInvocationStatistics>> resultDocs = result.getDocuments();
    if (resultDocs.size() > 0) {
      _meanStatisticsDocument = resultDocs.get(0);
      if (resultDocs.size() > 1) {
        s_logger.warn("Multiple documents found for {}", MEAN_INVOCATION_DOCUMENT_NAME);
      }
    } else {
      s_logger.debug("No initial statistics");
      final ConfigDocument<FunctionInvocationStatistics> doc = new ConfigDocument<FunctionInvocationStatistics>();
      doc.setName(MEAN_INVOCATION_DOCUMENT_NAME);
      doc.setValue(new FunctionInvocationStatistics(MEAN_INVOCATION_DOCUMENT_NAME));
      _meanStatisticsDocument = doc;
    }
    _meanStatistics = _meanStatisticsDocument.getValue();
  }

  private ConfigTypeMaster<FunctionInvocationStatistics> getTypedPersistence() {
    return _configMaster.typed(FunctionInvocationStatistics.class);
  }

  private Queue<ConfigDocument<FunctionInvocationStatistics>> getConfigDocuments() {
    return _configDocuments;
  }

  private FunctionInvocationStatistics getMeanStatistics() {
    return _meanStatistics;
  }

  /**
   * Creates a runnable to write the statistics.
   * 
   * @return the runnable, not null
   */
  public Runnable createPersistenceWriter() {
    ArgumentChecker.notNull(getTypedPersistence(), "persistence");
    return new Runnable() {
      @Override
      public void run() {
        s_logger.info("Persisting function execution statistics");
        long lastUpdate = _meanStatistics.getLastUpdateNanos();
        int count = 1;
        double invocationCost = getMeanStatistics().getInvocationCost();
        double dataInputCost = getMeanStatistics().getDataInputCost();
        double dataOutputCost = getMeanStatistics().getDataOutputCost();
        for (int i = getConfigDocuments().size(); i > 0; i--) {
          final ConfigDocument<FunctionInvocationStatistics> configDocument = getConfigDocuments().poll();
          if (configDocument.getConfigId() != null) {
            if (configDocument.getValue().getLastUpdateNanos() > lastUpdate) {
              s_logger.debug("Updating document {} with {}", configDocument.getConfigId(), configDocument.getName());
              getConfigDocuments().add(getTypedPersistence().update(configDocument));
              invocationCost += configDocument.getValue().getInvocationCost();
              dataInputCost += configDocument.getValue().getDataInputCost();
              dataOutputCost += configDocument.getValue().getDataOutputCost();
              count++;
            } else {
              getConfigDocuments().add(configDocument);
            }
          } else {
            s_logger.debug("Adding {} to store", configDocument.getName());
            getConfigDocuments().add(getTypedPersistence().add(configDocument));
            invocationCost += configDocument.getValue().getInvocationCost();
            dataInputCost += configDocument.getValue().getDataInputCost();
            dataOutputCost += configDocument.getValue().getDataOutputCost();
            count++;
          }
        }
        _meanStatistics.setCosts(invocationCost / (double) count, dataInputCost / (double) count, dataOutputCost / (double) count);
        if (count > 1) {
          if (_meanStatisticsDocument.getConfigId() != null) {
            s_logger.debug("Updating mean statistics {}", _meanStatistics);
            _meanStatisticsDocument = getTypedPersistence().update(_meanStatisticsDocument);
          } else {
            s_logger.debug("Adding mean statistics {}", _meanStatistics);
            _meanStatisticsDocument = getTypedPersistence().add(_meanStatisticsDocument);
          }
        }
      }
    };
  }

  // TODO [ENG-229] Would we want to be able to gather mean execution statistics without the persistence component?

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
