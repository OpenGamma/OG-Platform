/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Pass through adapter to allow filtering of all {@link UserMessagePayload} messages.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public class UserMessagePayloadAdapter<T1, T2> implements UserMessagePayloadVisitor<T1, T2> {

  private final UserMessagePayloadVisitor<T1, T2> _underlying;

  protected UserMessagePayloadAdapter(final UserMessagePayloadVisitor<T1, T2> underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected UserMessagePayloadVisitor<T1, T2> getUnderlying() {
    return _underlying;
  }

  @Override
  public T1 visitUserMessagePayload(final UserMessagePayload payload, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitUserMessagePayload(payload, data);
  }

  @Override
  public T1 visitTest(final Test message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitTest(message, data);
  }

  @Override
  public T1 visitLiveData(final LiveData message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitLiveData(message, data);
  }

  @Override
  public T1 visitFunction(final Function message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitFunction(message, data);
  }

  @Override
  public T1 visitProcedure(final Procedure message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitProcedure(message, data);
  }

  @Override
  public T1 visitCustom(final Custom message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitCustom(message, data);
  }

}
