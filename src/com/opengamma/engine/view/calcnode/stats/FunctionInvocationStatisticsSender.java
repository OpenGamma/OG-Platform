/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.calcnode.msg.Invocations;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.Invocations.PerConfiguration;
import com.opengamma.engine.view.calcnode.msg.Invocations.PerConfiguration.PerFunction;
import com.opengamma.transport.FudgeMessageSender;

/**
 * Sends statistics from the local node to a master.
 */
public class FunctionInvocationStatisticsSender implements FunctionInvocationStatisticsGatherer {

  private final ConcurrentMap<String, ConcurrentMap<String, PerFunction>> _data = new ConcurrentHashMap<String, ConcurrentMap<String, PerFunction>>();
  private final AtomicLong _lastSent = new AtomicLong();
  private FudgeMessageSender _messageSender;
  private ExecutorService _executorService;
  private volatile double _invocationTimeScale = 1.0;
  private volatile double _dataInputScale = 1.0;
  private volatile double _dataOutputScale = 1.0;
  private long _updatePeriod = 5000000000L; // 5s

  public FunctionInvocationStatisticsSender() {
  }

  public void setFudgeMessageSender(final FudgeMessageSender messageSender) {
    _messageSender = messageSender;
  }

  protected FudgeMessageSender getFudgeMessageSender() {
    return _messageSender;
  }

  public void setExecutorService(final ExecutorService executorService) {
    _executorService = executorService;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  public void setScaling(final double invocationTimeScale, final double dataInputScale, final double dataOutputScale) {
    // Doesn't matter that these three aren't updated and used atomically
    _invocationTimeScale = invocationTimeScale;
    _dataInputScale = dataInputScale;
    _dataOutputScale = dataOutputScale;
  }

  public void setUpdatePeriod(final long seconds) {
    _updatePeriod = seconds * 1000000000;
  }

  public long getUpdatePeriod() {
    return _updatePeriod;
  }

  protected ConcurrentMap<String, PerFunction> getConfigurationData(final String calculationConfiguration) {
    ConcurrentMap<String, PerFunction> data = _data.get(calculationConfiguration);
    if (data == null) {
      data = new ConcurrentHashMap<String, PerFunction>();
      ConcurrentMap<String, PerFunction> newData = _data.putIfAbsent(calculationConfiguration, data);
      if (newData != null) {
        data = newData;
      }
    }
    return data;
  }

  @Override
  public void functionInvoked(final String configurationName, final String functionIdentifier, final int count, final double invocationTime, final double dataInput, final double dataOutput) {
    final ConcurrentMap<String, PerFunction> data = getConfigurationData(configurationName);
    PerFunction stats = data.get(functionIdentifier);
    if (stats == null) {
      stats = new PerFunction(functionIdentifier, count, invocationTime, dataInput, dataOutput);
      PerFunction newStats = data.putIfAbsent(functionIdentifier, stats);
      if (newStats == null) {
        return;
      }
      stats = newStats;
    }
    synchronized (stats) {
      stats.setCount(stats.getCount() + count);
      stats.setInvocation(stats.getInvocation() + invocationTime * _invocationTimeScale);
      stats.setDataInput(stats.getDataInput() + dataInput * _dataInputScale);
      stats.setDataOutput(stats.getDataOutput() + dataOutput * _dataOutputScale);
    }
    long timeNow = System.nanoTime();
    long lastSent = _lastSent.get();
    if (lastSent + getUpdatePeriod() < timeNow) {
      if (_lastSent.compareAndSet(lastSent, timeNow)) {
        getExecutorService().execute(new Runnable() {
          @Override
          public void run() {
            sendStatistics();
          }
        });
      }
    }
  }

  protected void sendStatistics() {
    // Note the race condition in this logic; it is possible we may lose data if functionInvoked is called while we're doing this. Hopefully
    // it won't happen often enough to be problematic. We're only gathering heuristics so as long as it isn't a rarely executing function
    // that always gets missed we'll be okay!
    final List<PerConfiguration> configurations = new ArrayList<PerConfiguration>(_data.size());
    final Iterator<Map.Entry<String, ConcurrentMap<String, PerFunction>>> configurationIterator = _data.entrySet().iterator();
    while (configurationIterator.hasNext()) {
      final Map.Entry<String, ConcurrentMap<String, PerFunction>> configuration = configurationIterator.next();
      if (!configuration.getValue().isEmpty()) {
        configurations.add(new PerConfiguration(configuration.getKey(), configuration.getValue().values()));
      }
      configurationIterator.remove();
    }
    final MutableFudgeFieldContainer message = getFudgeMessageSender().getFudgeContext().newMessage();
    FudgeSerializationContext.addClassHeader(message, Invocations.class, RemoteCalcNodeMessage.class);
    new Invocations(configurations).toFudgeMsg(getFudgeMessageSender().getFudgeContext(), message);
    getFudgeMessageSender().send(message);
  }

  public void flush() {
    sendStatistics();
  }

}
