/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.connector.Function;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Pass through adapter to allow filtering of all function messages
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public class FunctionAdapter<T1, T2> implements FunctionVisitor<T1, T2> {

  private final FunctionVisitor<T1, T2> _underlying;

  protected FunctionAdapter(final FunctionVisitor<T1, T2> underlying) {
    _underlying = underlying;
  }

  protected FunctionVisitor<T1, T2> getUnderlying() {
    return _underlying;
  }

  @Override
  public T1 visitCustom(final Custom message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitCustom(message, data);
  }

  @Override
  public T1 visitInvoke(final Invoke message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitInvoke(message, data);
  }

  @Override
  public T1 visitQueryAvailable(final QueryAvailable message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitQueryAvailable(message, data);
  }

  @Override
  public T1 visitUnexpected(final Function message, final T2 data) throws AsynchronousExecution {
    return getUnderlying().visitUnexpected(message, data);
  }

}
