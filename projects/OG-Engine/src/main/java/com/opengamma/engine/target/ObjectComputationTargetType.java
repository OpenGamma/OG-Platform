/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.io.ObjectStreamException;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper around the {@link ComputationTargetType} instance that is typed for the leaf node. This can be used to extract the required type from a target using
 * {@link ComputationTarget#getValue(ObjectComputationTargetType)}.
 * 
 * @param <T> the target object type
 */
public class ObjectComputationTargetType<T extends UniqueIdentifiable> extends ComputationTargetType {

  private static final long serialVersionUID = 1L;

  private final ComputationTargetType _underlying;
  private final Class<T> _clazz;

  /* package */ObjectComputationTargetType(final ComputationTargetType type, final Class<T> clazz) {
    super(type.hashCode());
    _underlying = type;
    _clazz = clazz;
  }

  public static <T extends UniqueIdentifiable> ObjectComputationTargetType<T> of(final ComputationTargetType type, final Class<T> clazz) {
    assert type.isTargetType(clazz) : clazz + " is not valid for " + type;
    return new ObjectComputationTargetType<T>(type, clazz);
  }

  public static <T extends UniqueIdentifiable> ObjectComputationTargetType<T> of(final Class<T> clazz) {
    return new ObjectComputationTargetType<T>(ComputationTargetType.of(clazz), clazz);
  }

  private ComputationTargetType getUnderlying() {
    return _underlying;
  }

  public Class<T> getObjectClass() {
    return _clazz;
  }

  /**
   * Creates a specification for the given object. The specification will consist of this type and the object's unique identifier.
   * 
   * @param object the object to produce the specification for, not null
   * @return the target specification, never null
   */
  public ComputationTargetSpecification specification(final T object) {
    ArgumentChecker.notNull(object, "object");
    return new ComputationTargetSpecification(this, object.getUniqueId());
  }

  @Override
  public boolean isCompatible(final UniqueIdentifiable target) {
    return getUnderlying().isCompatible(target);
  }

  @Override
  public boolean isCompatible(final ComputationTargetType type) {
    return getUnderlying().isCompatible(type);
  }

  @Override
  public boolean isCompatible(final Class<? extends UniqueIdentifiable> clazz) {
    return getUnderlying().isCompatible(clazz);
  }

  @Override
  public <D, R> R accept(final ComputationTargetTypeVisitor<D, R> visitor, final D data) {
    return getUnderlying().accept(visitor, data);
  }

  @Override
  protected void toStringNested(final StringBuilder sb) {
    getUnderlying().toStringNested(sb);
  }

  @Override
  public String toString() {
    return getUnderlying().toString();
  }

  @Override
  protected void getNameNested(final StringBuilder sb) {
    getUnderlying().getNameNested(sb);
  }

  @Override
  public String getName() {
    return getUnderlying().getName();
  }

  @Override
  public boolean equals(final Object o) {
    return getUnderlying().equals(o);
  }

  @Override
  public boolean isTargetType(final ComputationTargetType type) {
    return getUnderlying().isTargetType(type);
  }

  @Override
  public boolean isTargetType(final Class<? extends UniqueIdentifiable> type) {
    return getUnderlying().isTargetType(type);
  }

  protected Object writeReplace() throws ObjectStreamException {
    return getUnderlying();
  }

}
