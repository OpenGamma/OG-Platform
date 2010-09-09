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

  private final ConcurrentMap<String, ConcurrentMap<String, FunctionInvocationStatistics>> _data = new ConcurrentHashMap<String, ConcurrentMap<String, FunctionInvocationStatistics>>();

  protected ConcurrentMap<String, FunctionInvocationStatistics> getConfigurationData(final String calculationConfiguration) {
    ConcurrentMap<String, FunctionInvocationStatistics> data = _data.get(calculationConfiguration);
    if (data == null) {
      data = new ConcurrentHashMap<String, FunctionInvocationStatistics>();
      ConcurrentMap<String, FunctionInvocationStatistics> newData = _data.putIfAbsent(calculationConfiguration, data);
      if (newData != null) {
        data = newData;
      }
    }
    return data;
  }

  public FunctionInvocationStatistics getFunctionCost(final String calculationConfiguration, final String functionIdentifier) {
    final ConcurrentMap<String, FunctionInvocationStatistics> data = getConfigurationData(calculationConfiguration);
    FunctionInvocationStatistics stats = data.get(functionIdentifier);
    if (stats == null) {
      stats = new FunctionInvocationStatistics(functionIdentifier);
      FunctionInvocationStatistics newStats = data.putIfAbsent(functionIdentifier, stats);
      if (newStats != null) {
        stats = newStats;
      }
    }
    return stats;
  }

  @Override
  public void functionInvoked(final String configurationName, final String functionIdentifier, final int count, final double invocationTime, final double dataInput, final double dataOutput) {
    getFunctionCost(configurationName, functionIdentifier).recordInvocation(count, invocationTime, dataInput, dataOutput);
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeMessageFactory factory) {
    final MutableFudgeFieldContainer message = factory.newMessage();
    for (Map.Entry<String, ConcurrentMap<String, FunctionInvocationStatistics>> configuration : _data.entrySet()) {
      final MutableFudgeFieldContainer configurationMessage = factory.newMessage();
      for (Map.Entry<String, FunctionInvocationStatistics> function : configuration.getValue().entrySet()) {
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
