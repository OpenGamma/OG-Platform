/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.opengamma.livedata.firehose.InputStreamFactory;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.metric.MetricProducer;

/**
 * A class that is able to decode the raw stream of bytes for a fire hose provider
 * and produce them in a binary form for another purpose.
 * The canonical example is one which will read a socket-based stream, decode that
 * stream into discrete messages, and then distribute onto an MOM system for later
 * processing into a full {@code FireHoseLiveData} object.
 */
public abstract class CogdaRecordChunker implements MetricProducer {
  /**
   * An estimate of the number of symbols that will be seen.
   * Used to establish the size of the internal statics maintenance structures.
   */
  private static final int ESTIMATE_SYMBOL_COUNT = 10000;
  private final ConcurrentMap<String, Meter> _symbolStatistics = new ConcurrentHashMap<String, Meter>(ESTIMATE_SYMBOL_COUNT);
  private MetricRegistry _detailedRegistry;
  private String _metricNamePrefix;
  private Meter _tickMeter;
  private Lock _metricsModificationLock = new ReentrantLock();
  
  /**
   * The destination for decoded messsages.
   */
  private FudgeMessageSender _fudgeSender;
  /**
   * The source for new connections for data to parse.
   */
  private InputStreamFactory _inputStreamFactory;

  @Override
  public synchronized void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    _detailedRegistry = detailedRegistry;
    _metricNamePrefix = namePrefix;
    _tickMeter = summaryRegistry.meter(namePrefix + ".total");
  }

  /**
   * Gets the fudgeSender.
   * @return the fudgeSender
   */
  public FudgeMessageSender getFudgeSender() {
    return _fudgeSender;
  }

  /**
   * Sets the fudgeSender.
   * @param fudgeSender  the fudgeSender
   */
  public void setFudgeSender(FudgeMessageSender fudgeSender) {
    _fudgeSender = fudgeSender;
  }
  
  /**
   * Gets the inputStreamFactory.
   * @return the inputStreamFactory
   */
  public InputStreamFactory getInputStreamFactory() {
    return _inputStreamFactory;
  }

  /**
   * Sets the inputStreamFactory.
   * @param inputStreamFactory  the inputStreamFactory
   */
  public void setInputStreamFactory(InputStreamFactory inputStreamFactory) {
    _inputStreamFactory = inputStreamFactory;
  }

  /**
   * Should be called by the sub-class whenever a tick is received.
   * Allows the parent class to update statistics visible by the MBean.
   * @param symbol The symbol a tick is received on.
   */
  protected void symbolSeen(String symbol) {
    // Note the paradigm here. We avoid any overlap by the .putIfAbsent call,
    // but we really want to avoid excess garbage generation so we still do the .get
    // first. The case where map won't be populated is extremely rare and only
    // on startup.
    Meter perSymbolMeter = _symbolStatistics.get(symbol);
    if (perSymbolMeter == null) {
      _metricsModificationLock.lock();
      try {
        perSymbolMeter = _symbolStatistics.get(symbol);
        if (perSymbolMeter == null) {
          perSymbolMeter = _detailedRegistry.meter(_metricNamePrefix + "." + symbol);
          _symbolStatistics.put(symbol, perSymbolMeter);
        }
      } finally {
        _metricsModificationLock.unlock();
      }
    }
    perSymbolMeter.mark();
    if (_tickMeter != null) {
      _tickMeter.mark();
    }
  }
  
  public int getNumActiveSymbols() {
    return _symbolStatistics.size();
  }

  /**
   * Returns a copy of all symbols seen since startup.
   * @return All symbols seen since startup.
   */
  public Set<String> getAllSymbols() {
    Set<String> allSymbols = new TreeSet<String>(_symbolStatistics.keySet());
    return allSymbols;
  }
  
  /**
   * Return a description (e.g. hostname/port) for the remote endpoint
   * for this connection.
   * @return An MBean-visible remote connection name
   */
  public String getRemoteServerConnectionName() {
    return getInputStreamFactory().getDescription();
  }

}
