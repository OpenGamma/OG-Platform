/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.connector.LiveData;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Pass through adapter to allow filtering of all incoming live-data messages.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public class LiveDataAdapter<T1, T2> implements LiveDataVisitor<T1, T2> {

  private final LiveDataVisitor<T1, T2> _underlying;

  protected LiveDataAdapter(final LiveDataVisitor<T1, T2> underlying) {
    _underlying = underlying;
  }

  protected LiveDataVisitor<T1, T2> getUnderlying() {
    return _underlying;
  }

  @Override
  public T1 visitConnect(final Connect message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitConnect(message, data);
  }

  @Override
  public T1 visitCustom(final Custom message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitCustom(message, data);
  }

  @Override
  public T1 visitDisconnect(final Disconnect message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitDisconnect(message, data);
  }

  @Override
  public T1 visitQueryAvailable(final QueryAvailable message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitQueryAvailable(message, data);
  }

  @Override
  public T1 visitQueryValue(final QueryValue message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitQueryValue(message, data);
  }

  @Override
  public T1 visitUnexpected(final LiveData message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitUnexpected(message, data);
  }

}
