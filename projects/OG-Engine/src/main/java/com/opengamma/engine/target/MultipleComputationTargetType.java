/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * A computation target that can be of multiple underlying types.
 */
/* package */final class MultipleComputationTargetType extends ComputationTargetType {

  private static final long serialVersionUID = 1L;

  private final Set<ComputationTargetType> _target;

  private static final ComputationTargetTypeVisitor<Set<ComputationTargetType>, Boolean> s_construct = new ComputationTargetTypeVisitor<Set<ComputationTargetType>, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Set<ComputationTargetType> data) {
      // Flatten and accept the contents
      for (ComputationTargetType type : types) {
        if (type.accept(this, data)) {
          data.add(type);
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Set<ComputationTargetType> data) {
      // Accept
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final Set<ComputationTargetType> data) {
      // Invalid
      throw new IllegalArgumentException();
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Set<ComputationTargetType> data) {
      // Accept
      return Boolean.TRUE;
    }

  };

  private MultipleComputationTargetType(final Set<ComputationTargetType> target) {
    super(MultipleComputationTargetType.class.getName().hashCode() * 31 + target.hashCode());
    _target = target;
  }

  private static Set<ComputationTargetType> copy(final ComputationTargetType a, final ComputationTargetType b) {
    final Set<ComputationTargetType> copy = new HashSet<ComputationTargetType>();
    if (a.accept(s_construct, copy)) {
      copy.add(a);
    }
    if (b.accept(s_construct, copy)) {
      copy.add(b);
    }
    ArgumentChecker.isTrue(copy.size() >= 2, "target");
    return Collections.unmodifiableSet(copy);
  }

  /**
   * Creates a new instance.
   * 
   * @param a the first alternative for the construction, not null
   * @param b the second alternative for the construction, not null
   */
  public MultipleComputationTargetType(final ComputationTargetType a, final ComputationTargetType b) {
    this(copy(a, b));
  }

  private static Set<ComputationTargetType> copy(final ComputationTargetType[] types) {
    final Set<ComputationTargetType> copy = new HashSet<ComputationTargetType>();
    for (ComputationTargetType type : types) {
      if (type.accept(s_construct, copy)) {
        copy.add(type);
      }
    }
    ArgumentChecker.isTrue(copy.size() >= 2, "target");
    return Collections.unmodifiableSet(copy);
  }

  /**
   * Creates a new instance.
   * 
   * @param types the alternative types for the construction, not null and not containing null
   */
  public MultipleComputationTargetType(final ComputationTargetType[] types) {
    this(copy(types));
  }

  protected Set<ComputationTargetType> getTarget() {
    return _target;
  }

  @Override
  public boolean isCompatible(final UniqueIdentifiable target) {
    for (ComputationTargetType type : getTarget()) {
      if (type.isCompatible(target)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isCompatible(final ComputationTargetType type) {
    for (ComputationTargetType target : getTarget()) {
      if (target.isCompatible(type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isCompatible(final Class<? extends UniqueIdentifiable> clazz) {
    for (ComputationTargetType target : getTarget()) {
      if (target.isCompatible(clazz)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isTargetType(final ComputationTargetType type) {
    for (ComputationTargetType target : getTarget()) {
      if (target.isTargetType(type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isTargetType(final Class<? extends UniqueIdentifiable> type) {
    for (ComputationTargetType target : getTarget()) {
      if (target.isTargetType(type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public <D, T> T accept(final ComputationTargetTypeVisitor<D, T> visitor, final D data) {
    return visitor.visitMultipleComputationTargetTypes(getTarget(), data);
  }

  protected void toStringImpl(final StringBuilder sb) {
    boolean sep = false;
    for (ComputationTargetType type : getTarget()) {
      if (sep) {
        sb.append('|');
      } else {
        sep = true;
      }
      type.toStringNested(sb);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    toStringImpl(sb);
    return sb.toString();
  }

  @Override
  protected void toStringNested(final StringBuilder sb) {
    sb.append('(');
    toStringImpl(sb);
    sb.append(')');
  }

  @Override
  public String getName() {
    final StringBuilder sb = new StringBuilder();
    getNameImpl(sb);
    return sb.toString();
  }

  @Override
  protected void getNameNested(final StringBuilder sb) {
    sb.append('(');
    getNameImpl(sb);
    sb.append(')');
  }

  protected void getNameImpl(final StringBuilder sb) {
    boolean sep = false;
    for (ComputationTargetType type : getTarget()) {
      if (sep) {
        sb.append('|');
      } else {
        sep = true;
      }
      type.getNameNested(sb);
    }
  }

  private static final ComputationTargetTypeVisitor<MultipleComputationTargetType, Boolean> s_equals = new ComputationTargetTypeVisitor<MultipleComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final MultipleComputationTargetType self) {
      return self.getTarget().equals(types);
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final MultipleComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final MultipleComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final MultipleComputationTargetType self) {
      return Boolean.FALSE;
    }

  };

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ComputationTargetType) {
      return ((ComputationTargetType) o).accept(s_equals, this).booleanValue();
    } else {
      return false;
    }
  }

}
