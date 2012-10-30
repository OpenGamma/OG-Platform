/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeMsg;


/**
 * Partial implementation of the {@link FireHoseLiveData}.
 */
public abstract class AbstractFireHoseLiveData implements FireHoseLiveData {

  private final ConcurrentMap<String, FudgeMsg> _marketValues = new ConcurrentHashMap<String, FudgeMsg>();

  private volatile boolean _marketDataComplete;
  private volatile ValueUpdateListener _valueUpdateListener;
  private volatile DataStateListener _dataStateListener;

  protected Map<String, FudgeMsg> getMarketValues() {
    return Collections.unmodifiableMap(_marketValues);
  }

  protected void storeValue(final String uniqueId, final FudgeMsg msg) {
    _marketValues.put(uniqueId, msg);
    ValueUpdateListener listener = getValueUpdateListener();
    if (listener != null) {
      listener.updatedValue(uniqueId, msg);
    }
  }

  @Override
  public FudgeMsg getLatestValue(final String uniqueId) {
    return _marketValues.get(uniqueId);
  }

  @Override
  public void setValueUpdateListener(final ValueUpdateListener listener) {
    _valueUpdateListener = listener;
  }

  private ValueUpdateListener getValueUpdateListener() {
    return _valueUpdateListener;
  }

  @Override
  public void setDataStateListener(final DataStateListener listener) {
    _dataStateListener = listener;
    if (listener != null) {
      if (isMarketDataComplete()) {
        listener.valuesRefreshed();
      }
    }
  }

  private DataStateListener getDataStateListener() {
    return _dataStateListener;
  }

  @Override
  public boolean isMarketDataComplete() {
    return _marketDataComplete;
  }

  protected void setMarketDataComplete(final boolean marketDataComplete) {
    _marketDataComplete = marketDataComplete;
    if (marketDataComplete) {
      final DataStateListener listener = getDataStateListener();
      if (listener != null) {
        listener.valuesRefreshed();
      }
    }
  }

  @Override
  public boolean isDataAvailable(String uniqueId) {
    return _marketValues.containsKey(uniqueId);
  }

}
