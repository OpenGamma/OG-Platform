/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import org.fudgemsg.FudgeMsg;

import ch.qos.logback.core.spi.LifeCycle;

/**
 * Abstraction of a Live Data Server that receives a "fire hose" of data for the market.
 */
public interface FireHoseLiveData extends LifeCycle {

  /**
   * Callback interface to receive values as they arrive.
   */
  interface ValueUpdateListener {

    /**
     * Called when a value has been updated.
     * 
     * @param uniqueId the string identifying the value, not null
     * @param msg the full message describing the value, not null
     */
    void updatedValue(String uniqueId, FudgeMsg msg);

  }

  /**
   * Callback interface to receive notification of the full market state being available.
   */
  interface DataStateListener {

    /**
     * Called when a refresh has been completed.
     */
    void valuesRefreshed();

  }

  /**
   * Return the latest value for a market data line.
   * 
   * @param uniqueId the string identifying the value, not null
   * @return the message value or null if the identifier is invalid
   */
  FudgeMsg getLatestValue(String uniqueId);

  /**
   * Returns true if the full market data is available, false if it is still in transit (e.g.
   * as the server starts).
   * 
   * @return true if full market data is available, false otherwise
   */
  boolean isMarketDataComplete();

  /**
   * Registers a listener to receive values as they are updated.
   * 
   * @param listener the listener to receive values as they arrive
   */
  void setValueUpdateListener(ValueUpdateListener listener);

  /**
   * Registers a listener to receive notification of the market data state being
   * completely loaded. E.g. if a server is capable of detecting when the full state
   * of the market has been received.
   * 
   * @param listener the listener to receive the callback
   */
  void setDataStateListener(DataStateListener listener);

}
