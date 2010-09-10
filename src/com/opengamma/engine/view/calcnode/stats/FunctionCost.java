/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * Gather function invocation statistics and supplies the data to anything
 * that needs it. 
 */
public class FunctionCost implements FunctionInvocationStatisticsGatherer {

  /**
   * 
   */
  public static final class ForConfiguration {

    private final ConcurrentMap<String, FunctionInvocationStatistics> _data = new ConcurrentHashMap<String, FunctionInvocationStatistics>();

    private ForConfiguration() {
    }

    public FunctionInvocationStatistics getStatistics(final String functionIdentifier) {
      FunctionInvocationStatistics stats = _data.get(functionIdentifier);
      if (stats == null) {
        stats = new FunctionInvocationStatistics(functionIdentifier);
        FunctionInvocationStatistics newStats = _data.putIfAbsent(functionIdentifier, stats);
        if (newStats != null) {
          stats = newStats;
        }
      }
      return stats;
    }

  }

  private final ConcurrentMap<String, ForConfiguration> _data = new ConcurrentHashMap<String, ForConfiguration>();

  public ForConfiguration getStatistics(final String calculationConfiguration) {
    ForConfiguration data = _data.get(calculationConfiguration);
    if (data == null) {
      data = new ForConfiguration();
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
