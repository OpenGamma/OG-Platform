/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.concurrent.CancellationException;

import com.opengamma.util.async.AbstractHousekeeper;

/**
 * Housekeeper thread/service for the dependency graph builders.
 */
public final class Housekeeper extends AbstractHousekeeper<DependencyGraphBuilder> {

  /**
   * Callback for receiving housekeeping notifications.
   * <p>
   * Note that the data object that is registered with the housekeeper must not have a strong reference to the dependency graph builder. The data will be held from the timer thread which can prevent
   * garbage collection of the graph builder.
   */
  public interface Callback<D> {

    boolean tick(DependencyGraphBuilder builder, D data);

    boolean cancelled(DependencyGraphBuilder builder, D data);

    boolean completed(DependencyGraphBuilder builder, D data);

  }

  private final Callback<Object> _callback;
  private final Object _data;

  @SuppressWarnings("unchecked")
  private <D> Housekeeper(final DependencyGraphBuilder builder, final Callback<D> callback, final D data) {
    super(builder);
    _callback = (Callback<Object>) callback;
    _data = data;
  }

  public static <D> Housekeeper of(final DependencyGraphBuilder builder, final Callback<D> callback, final D data) {
    return new Housekeeper(builder, callback, data);
  }

  public static Housekeeper of(final DependencyGraphBuilder builder, final Callback<Void> callback) {
    return new Housekeeper(builder, callback, null);
  }

  private Callback<Object> getCallback() {
    return _callback;
  }

  private Object getData() {
    return _data;
  }

  @Override
  protected boolean housekeep(final DependencyGraphBuilder builder) {
    final boolean isGraphBuilt;
    try {
      isGraphBuilt = builder.isGraphBuilt();
    } catch (CancellationException e) {
      return getCallback().cancelled(builder, getData());
    }
    if (isGraphBuilt) {
      if (builder.getScheduledSteps() > 0) {
        return getCallback().completed(builder, getData());
      } else {
        // Hasn't started yet -- issue as a normal tick
        return getCallback().tick(builder, getData());
      }
    } else {
      return getCallback().tick(builder, getData());
    }
  }

  @Override
  public String toString() {
    Callback<Object> callback = getCallback();
    if (callback != null) {
      return callback.toString();
    } else {
      return "<creating>";
    }
  }

}
