/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.custom;

import com.opengamma.util.ArgumentChecker;

/**
 * Manages custom visitors registered with message handlers. Custom visitors allow a specific language
 * implementation to extend the range of messages passed without modifying the core framework.
 *
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public class CustomVisitors<T1, T2> implements CustomFunctionVisitorRegistry<T1, T2>,
    CustomLiveDataVisitorRegistry<T1, T2>, CustomMessageVisitorRegistry<T1, T2>, CustomProcedureVisitorRegistry<T1, T2> {

  private void registerImpl(final Class<?> clazz, final Object visitor) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(visitor, "visitor");
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException if there is no registered visitor for the custom class
   */
  private <M> M getVisitor(final Class<?> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <M extends com.opengamma.language.function.Custom> void register(final Class<M> clazz,
      final CustomFunctionVisitor<M, T1, T2> visitor) {
    registerImpl(clazz, visitor);
  }

  @Override
  public <M extends com.opengamma.language.livedata.Custom> void register(final Class<M> clazz,
      final CustomLiveDataVisitor<M, T1, T2> visitor) {
    registerImpl(clazz, visitor);
  }

  @Override
  public <M extends com.opengamma.language.connector.Custom> void register(final Class<M> clazz,
      final CustomMessageVisitor<M, T1, T2> visitor) {
    registerImpl(clazz, visitor);
  }

  @Override
  public <M extends com.opengamma.language.procedure.Custom> void register(final Class<M> clazz,
      final CustomProcedureVisitor<M, T1, T2> visitor) {
    registerImpl(clazz, visitor);
  }

  public <M extends com.opengamma.language.function.Custom> T1 visit(final M message, final T2 data) {
    return this.<CustomFunctionVisitor<M, T1, T2>> getVisitor(message.getClass()).visit(message, data);
  }

  public <M extends com.opengamma.language.livedata.Custom> T1 visit(final M message, final T2 data) {
    return this.<CustomLiveDataVisitor<M, T1, T2>> getVisitor(message.getClass()).visit(message, data);
  }

  public <M extends com.opengamma.language.connector.Custom> T1 visit(final M message, final T2 data) {
    return this.<CustomMessageVisitor<M, T1, T2>> getVisitor(message.getClass()).visit(message, data);
  }

  public <M extends com.opengamma.language.procedure.Custom> T1 visit(final M message, final T2 data) {
    return this.<CustomProcedureVisitor<M, T1, T2>> getVisitor(message.getClass()).visit(message, data);
  }

}
