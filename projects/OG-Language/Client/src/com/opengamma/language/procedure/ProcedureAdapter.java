/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import com.opengamma.language.connector.Procedure;

/**
 * Pass through adapter for filtering of all incoming procedure messages.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public class ProcedureAdapter<T1, T2> implements ProcedureVisitor<T1, T2> {

  private final ProcedureVisitor<T1, T2> _underlying;

  protected ProcedureAdapter(final ProcedureVisitor<T1, T2> underlying) {
    _underlying = underlying;
  }

  protected ProcedureVisitor<T1, T2> getUnderlying() {
    return _underlying;
  }

  @Override
  public T1 visitCustom(final Custom message, final T2 data) {
    return getUnderlying().visitCustom(message, data);
  }

  @Override
  public T1 visitQueryAvailable(final QueryAvailable message, final T2 data) {
    return getUnderlying().visitQueryAvailable(message, data);
  }

  @Override
  public T1 visitUnexpected(final Procedure message, final T2 data) {
    return getUnderlying().visitUnexpected(message, data);
  }

}
