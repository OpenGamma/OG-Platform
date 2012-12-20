/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.Data;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents an active connection to a live data component. Live data components may produce
 * one or more results asynchronously. A {@link LiveDataConnector} implementation will typically
 * return a sub-class of this so that it can handle the {@link #cancel} method.
 */
public class Connection {

  private volatile Listener _listener;
  private volatile Data _value;

  /**
   * Callback interface for receiving asynchronous values from the connection.
   */
  public interface Listener {

    void newValue(Data value);

  }

  /**
   * Sets the current value from the connection. If a listener is registered, that will be
   * called with the new value.
   * 
   * @param value the value, not null
   */
  protected void setValue(final Data value) {
    ArgumentChecker.notNull(value, "value");
    final Listener listener = _listener;
    _value = value;
    if (listener != null) {
      listener.newValue(value);
    }
  }

  /**
   * Retrieves the last value produced by this connection.
   * 
   * @return the value
   */
  public Data getValue() {
    return _value;
  }

  /**
   * Registers (or removes) a listener with the connection.
   * 
   * @param listener listener to receive results, or null to remove the existing listener
   */
  public void setListener(final Listener listener) {
    _listener = listener;
  }

  /**
   * Terminates the connection. A live data component must release any resources and not
   * call {@link #setValue} any more.
   */
  public void cancel() {
    setListener(null);
  }

}
