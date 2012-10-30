/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import javax.time.Instant;

import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.functional.Function2;

/**
 * {@link ObjectResolver} implementation that will use a sequence of resolvers in turn if the first fails.
 * 
 * @param <T> the type to be resolved
 */
public class ChainedResolver<T extends UniqueIdentifiable> implements ObjectResolver<T> {

  private final ObjectResolver<? extends T> _first;
  private final ObjectResolver<? extends T> _second;

  public ChainedResolver(final ObjectResolver<? extends T> first, final ObjectResolver<? extends T> second) {
    _first = first;
    _second = second;
  }

  protected ObjectResolver<? extends T> getFirst() {
    return _first;
  }

  protected ObjectResolver<? extends T> getSecond() {
    return _second;
  }

  @Override
  public T resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    T value = getFirst().resolveObject(uniqueId, versionCorrection);
    if (value == null) {
      value = getSecond().resolveObject(uniqueId, versionCorrection);
    }
    return value;
  }

  @Override
  public ChangeManager changeManager() {
    return new ChangeManager() {

      @Override
      public void addChangeListener(final ChangeListener listener) {
        getFirst().changeManager().addChangeListener(listener);
        getSecond().changeManager().addChangeListener(listener);
      }

      @Override
      public void removeChangeListener(final ChangeListener listener) {
        getFirst().changeManager().removeChangeListener(listener);
        getSecond().changeManager().removeChangeListener(listener);
      }

      @Override
      public void entityChanged(ChangeType type, ObjectId oid, Instant versionFrom, Instant versionTo, Instant versionInstant) {
        throw new UnsupportedOperationException();
      }

    };
  }

  /**
   * Utility function for creating a chain of resolvers, for example when adding them to a {@link ComputationTargetTypeMap}. The first parameter is the second resolver to use (the existing one in the
   * map), the second parameter is the first resolver (the new value to the map). The returned value is the resolver chain.
   */
  public static final Function2<ObjectResolver<?>, ObjectResolver<?>, ObjectResolver<?>> CREATE = new Function2<ObjectResolver<?>, ObjectResolver<?>, ObjectResolver<?>>() {
    @SuppressWarnings({"rawtypes", "unchecked" })
    @Override
    public ObjectResolver<?> execute(ObjectResolver<?> second, ObjectResolver<?> first) {
      return new ChainedResolver(first, second);
    }
  };

}
