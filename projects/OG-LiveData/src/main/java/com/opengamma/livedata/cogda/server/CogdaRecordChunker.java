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
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.transport.FudgeMessageSender;

/**
 * A class that is able to decode the raw stream of bytes for a fire hose provider
 * and produce them in a binary form for another purpose.
 * The canonical example is one which will read a socket-based stream, decode that
 * stream into discrete messages, and then distribute onto an MOM system for later
 * processing into a full {@code FireHoseLiveData} object.
 */
public abstract class CogdaRecordChunker {
  /**
   * An estimate of the number of symbols that will be seen.
   * Used to establish the size of the internal statics maintenance structures.
   */
  private static final int ESTIMATE_SYMBOL_COUNT = 10000;
  private final ConcurrentMap<String, AtomicLong> _symbolStatistics = new ConcurrentHashMap<String, AtomicLong>(ESTIMATE_SYMBOL_COUNT);
  private final AtomicLong _totalTicks = new AtomicLong(0L);
  
  /**
   * The destination for decoded messsages.
   */
  private FudgeMessageSender _fudgeSender;

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
   * Should be called by the sub-class whenever a tick is received.
   * Allows the parent class to update statistics visible by the MBean.
   * @param symbol The symbol a tick is received on.
   */
  protected void symbolSeen(String symbol) {
    // Note the paradigm here. We avoid any overlap by the .putIfAbsent call,
    // but we really want to avoid excess garbage generation so we still do the .get
    // first. The case where map won't be populated is extremely rare and only
    // on startup.
    AtomicLong counter = _symbolStatistics.get(symbol);
    if (counter == null) {
      AtomicLong fresh = new AtomicLong(0L);
      counter = _symbolStatistics.putIfAbsent(symbol, fresh);
      if (counter == null) {
        counter = fresh;
      }
    }
    counter.incrementAndGet();
    _totalTicks.incrementAndGet();
  }
  
  public int getNumActiveSymbols() {
    return _symbolStatistics.size();
  }

  public long getNumTicksSeen() {
    return _totalTicks.get();
  }
  
  /**
   * Returns a copy of all symbols seen since startup.
   * @return All symbols seen since startup.
   */
  public Set<String> getAllSymbols() {
    Set<String> allSymbols = new TreeSet<String>(_symbolStatistics.keySet());
    return allSymbols;
  }
  
  public long getNumTicks(String symbol) {
    AtomicLong counter = _symbolStatistics.get(symbol);
    if (counter == null) {
      return 0L;
    }
    return counter.get();
  }
  
  /**
   * Return a description (e.g. hostname/port) for the remote endpoint
   * for this connection.
   * @return An MBean-visible remote connection name
   */
  public abstract String getRemoteServerConnectionName();

}
