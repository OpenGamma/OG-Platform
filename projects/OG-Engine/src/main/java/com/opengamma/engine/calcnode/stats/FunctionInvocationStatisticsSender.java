/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Duration;

import com.opengamma.engine.calcnode.msg.Invocations;
import com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration;
import com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction;
import com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Gatherer collecting invocation information and sending statistics onwards.
 * <p>
 * This runs on each calculation node to collect statistics.
 * It gathers the data and from time-to-time forwards the information centrally.
 */
public class FunctionInvocationStatisticsSender implements FunctionInvocationStatisticsGatherer {

  /**
   * The storage of the statistics not yet sent to the server.
   */
  private final ConcurrentMap<String, ConcurrentMap<String, PerFunction>> _data = new ConcurrentHashMap<String, ConcurrentMap<String, PerFunction>>();
  private final AtomicLong _lastSent = new AtomicLong();
  private FudgeMessageSender _messageSender;
  private ExecutorService _executorService;
  private double _convergenceFactor = 0.8;
  private double _serverScalingHint = 1.0;
  private volatile double _invocationTimeScale = 1.0;
  private long _frequencyNanos = 5000000000L; // 5_sec

  /**
   * Creates an instance.
   */
  public FunctionInvocationStatisticsSender() {
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the Fudge message sender.
   * 
   * @return the sender
   */
  protected FudgeMessageSender getFudgeMessageSender() {
    return _messageSender;
  }

  /**
   * Sets the Fudge message sender.
   * 
   * @param messageSender  the sender
   */
  public void setFudgeMessageSender(final FudgeMessageSender messageSender) {
    _messageSender = messageSender;
  }

  /**
   * Gets the executor service.
   * 
   * @return the executor service
   */
  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  /**
   * Sets the executor service.
   * 
   * @param executorService  the executor service
   */
  public void setExecutorService(final ExecutorService executorService) {
    _executorService = executorService;
  }

  /**
   * Gets the update period for sending statistics.
   * 
   * @return the duration of the update period, not null
   */
  public Duration getUpdatePeriod() {
    return Duration.ofNanos(_frequencyNanos);
  }

  /**
   * Sets the update period for sending statistics.
   * 
   * @param duration  the duration of the update period, not null
   */
  public void setUpdatePeriod(final Duration duration) {
    ArgumentChecker.notNull(duration, "duration");
    _frequencyNanos = duration.toNanos();
  }

  // -------------------------------------------------------------------------
  /**
   * Sets the scale for the invocation time metric.
   * <p>
   * This is the ratio of local node performance to the "standard" (typically a node running on
   * the view processor). The default initial value is 1.0.
   * <p>
   * If a manual value is set (other than {@code 1.0}) and hints from the server are disabled,
   * convergence should also be disabled to prevent it being shifted towards {@code 1.0}.</p>
   * 
   * @param invocationTimeScale  the scaling factor, must be positive and non-zero
   */
  public void setInvocationTimeScale(final double invocationTimeScale) {
    ArgumentChecker.notNegativeOrZero(invocationTimeScale, "invocationTimeScale");
    _invocationTimeScale = invocationTimeScale;
  }

  /**
   * Sets the convergence factor metric.
   * <p>
   * If set to {@code 1.0} has no effect, otherwise forces the scales to attempt to converge towards
   * {@code 1.0}. This is useful if there are no local nodes to act as reference point - a set of purely
   * remote (and identical) notes all automatically tuning their parameters will converge on similar
   * scales but the exact value may drift if there is no fixed reference. The default value is {@code 0.8}.
   * 
   * @param convergenceFactor power, must be in the range {@code (0..1]}.
   */
  public void setConvergenceFactor(final double convergenceFactor) {
    ArgumentChecker.isInRangeExcludingLow(0d, 1d, convergenceFactor);
    _convergenceFactor = convergenceFactor;
  }

  /**
   * Sets the server scaling hint metric.
   * <p>
   * This is set to {@code 1.0} to use the hints from the server, or {@code 0.0} to completely ignore
   * the hints from the server. Values in between will have a partial effect. The default value is {@code 1.0}.
   * 
   * @param serverScalingHint power, must be in the range {@code [0,1]}.
   */
  public void setServerScalingHint(final double serverScalingHint) {
    ArgumentChecker.isInRangeInclusive(0d, 1d, serverScalingHint);
    _serverScalingHint = serverScalingHint;
  }

  /**
   * Sets the scaling from a single value using the server hint and convergence.
   * 
   * @param invocationTimeScale  the scaling value
   */
  public void setScaling(final double invocationTimeScale) {
    // Doesn't matter that these three aren't updated and used atomically
    // Note the scale we get sent is relative to the values we previously sent so is used to adjust the scales we previously used.
    // We also want a reluctance to head away from 1.0 to avoid the creep that otherwise occurs
    setInvocationTimeScale(Math.pow(_invocationTimeScale * Math.pow(invocationTimeScale, _serverScalingHint), _convergenceFactor));
  }

  // -------------------------------------------------------------------------
  @Override
  public void functionInvoked(final String configurationName, final String functionId, final int invocationCount,
      final double executionNanos, final double dataInputBytes, final double dataOutputBytes) {
    final ConcurrentMap<String, PerFunction> statsMap = getConfigurationData(configurationName);
    PerFunction stats = statsMap.get(functionId);
    if (stats == null) {
      stats = new PerFunction(functionId, invocationCount, executionNanos, dataInputBytes, dataOutputBytes);
      PerFunction newStats = statsMap.putIfAbsent(functionId, stats);
      if (newStats == null) {
        return; // data stored in constructor of PerFunction above
      }
      stats = newStats;
    }
    updateStatistics(stats, invocationCount, executionNanos, dataInputBytes, dataOutputBytes);
    checkAndSendStatistics();
  }

  /**
   * Gets the configuration data.
   * 
   * @param calculationConfiguration  the configuration key, not null
   * @return the configuration map, not null
   */
  protected ConcurrentMap<String, PerFunction> getConfigurationData(final String calculationConfiguration) {
    ConcurrentMap<String, PerFunction> data = _data.get(calculationConfiguration);
    if (data == null) {
      _data.putIfAbsent(calculationConfiguration, new ConcurrentHashMap<String, PerFunction>());
      data = _data.get(calculationConfiguration);
    }
    return data;
  }

  /**
   * Updates the statistics after a successful function invocation.
   * 
   * @param stats  the statistics to update, not null
   * @param invocationCount  the number of invocations the data is for
   * @param executionNanos  the execution time, in nanoseconds, of the invocation(s)
   * @param dataInputBytes  the mean data input, bytes per input node, or {@code NaN} if unavailable
   * @param dataOutputBytes  the mean data output, bytes per output node, or {@code NaN} if unavailable
   */
  protected void updateStatistics(final PerFunction stats, final int invocationCount, final double executionNanos, final double dataInputBytes, final double dataOutputBytes) {
    synchronized (stats) {
      stats.setInvocation(stats.getInvocation() + executionNanos * _invocationTimeScale);
      if (Double.isNaN(dataInputBytes)) {
        // no data available, so increase at previous rate to keep average the same
        stats.setDataInput(stats.getDataInput() * (1.0 + (double) invocationCount / (double) stats.getCount()));
      } else {
        stats.setDataInput(stats.getDataInput() + dataInputBytes);
      }
      if (Double.isNaN(dataOutputBytes)) {
        // no data available, so increase at previous rate to keep average the same
        stats.setDataOutput(stats.getDataOutput() * (1.0 + (double) invocationCount / (double) stats.getCount()));
      } else {
        stats.setDataOutput(stats.getDataOutput() + dataOutputBytes);
      }
      stats.setCount(stats.getCount() + invocationCount);
    }
  }

  /**
   * Checks if it is time to send the statistics, if so then send them.
   */
  protected void checkAndSendStatistics() {
    long timeNow = System.nanoTime();
    long lastSent = _lastSent.get();
    if (lastSent + _frequencyNanos < timeNow) {
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

  /**
   * Sends the statistics to the central location.
   */
  protected void sendStatistics() {
    final List<PerConfiguration> configurations = new ArrayList<PerConfiguration>(_data.size());
    final Iterator<Map.Entry<String, ConcurrentMap<String, PerFunction>>> configurationIterator = _data.entrySet().iterator();
    while (configurationIterator.hasNext()) {
      final Map.Entry<String, ConcurrentMap<String, PerFunction>> configuration = configurationIterator.next();
      // Note the race condition in this logic; it is possible we may lose data if functionInvoked is called
      // while we're doing this. Hopefully it won't happen often enough to be problematic.
      // We're only gathering heuristics so as long as it isn't a rarely executing function that always gets missed we'll be okay!
      configurationIterator.remove();
      if (!configuration.getValue().isEmpty()) {
        final List<PerFunction> functionData = new ArrayList<PerFunction>(configuration.getValue().size());
        for (PerFunction function : configuration.getValue().values()) {
          synchronized (function) {
            functionData.add(function.clone());
          }
        }
        configurations.add(new PerConfiguration(configuration.getKey(), functionData));
      }
    }
    final MutableFudgeMsg message = getFudgeMessageSender().getFudgeContext().newMessage();
    FudgeSerializer.addClassHeader(message, Invocations.class, RemoteCalcNodeMessage.class);
    new Invocations(configurations).toFudgeMsg(new FudgeSerializer(getFudgeMessageSender().getFudgeContext()), message);
    getFudgeMessageSender().send(message);
  }

  /**
   * Flushes by sending statistics.
   */
  public void flush() {
    sendStatistics();
  }

}
