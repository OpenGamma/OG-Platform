/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * The type of a computation target that must be resolved within the context of outer types.
 */
/* package */final class NestedComputationTargetType extends ComputationTargetType {

  private static final long serialVersionUID = 1L;

  private final List<ComputationTargetType> _target;

  private static final ComputationTargetTypeVisitor<List<ComputationTargetType>, Boolean> s_construct = new ComputationTargetTypeVisitor<List<ComputationTargetType>, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final List<ComputationTargetType> data) {
      // Accept
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final List<ComputationTargetType> data) {
      // Flatten and accept the contents
      for (ComputationTargetType type : types) {
        if (type.accept(this, data)) {
          data.add(type);
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final List<ComputationTargetType> data) {
      // Invalid
      throw new IllegalArgumentException();
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final List<ComputationTargetType> data) {
      // Accept
      return Boolean.TRUE;
    }

  };

  private NestedComputationTargetType(final List<ComputationTargetType> target) {
    super(NestedComputationTargetType.class.getName().hashCode() * 31 + target.hashCode());
    _target = target;
  }

  private static List<ComputationTargetType> copy(final ComputationTargetType outerType, final ComputationTargetType innerType) {
    final List<ComputationTargetType> copy = new LinkedList<ComputationTargetType>();
    if (outerType.accept(s_construct, copy)) {
      copy.add(outerType);
    }
    if (innerType.accept(s_construct, copy)) {
      copy.add(innerType);
    }
    ArgumentChecker.isTrue(copy.size() >= 2, "target");
    return Collections.unmodifiableList(new ArrayList<ComputationTargetType>(copy));
  }

  /**
   * Creates a new instance.
   * 
   * @param outerType the type(s) of the outer context object(s), not null
   * @param innerType the type(s) of the target object(s), not null
   */
  public NestedComputationTargetType(final ComputationTargetType outerType, final ComputationTargetType innerType) {
    this(copy(outerType, innerType));
  }

  protected List<ComputationTargetType> getTypes() {
    return _target;
  }

  @Override
  public boolean isCompatible(final UniqueIdentifiable target) {
    final List<ComputationTargetType> types = getTypes();
    final ComputationTargetType type = types.get(types.size() - 1);
    return type.isCompatible(target);
  }

  private static final ComputationTargetTypeVisitor<NestedComputationTargetType, Boolean> s_isCompatible = new ComputationTargetTypeVisitor<NestedComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final NestedComputationTargetType self) {
      for (ComputationTargetType type : types) {
        if (self.isCompatible(type)) {
          return Boolean.TRUE;
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final NestedComputationTargetType self) {
      final List<ComputationTargetType> selfTypes = self.getTypes();
      if (selfTypes.size() > types.size()) {
        return Boolean.FALSE;
      }
      for (int i = 1; i <= selfTypes.size(); i++) {
        if (!selfTypes.get(selfTypes.size() - i).isCompatible(types.get(types.size() - i))) {
          return Boolean.FALSE;
        }
      }
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final NestedComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final NestedComputationTargetType self) {
      return Boolean.FALSE;
    }

  };

  @Override
  public boolean isCompatible(final ComputationTargetType type) {
    return type.accept(s_isCompatible, this);
  }

  @Override
  public boolean isCompatible(final Class<? extends UniqueIdentifiable> clazz) {
    final List<ComputationTargetType> types = getTypes();
    final ComputationTargetType type = types.get(types.size() - 1);
    return type.isCompatible(clazz);
  }

  private static final ComputationTargetTypeVisitor<NestedComputationTargetType, Boolean> s_isTargetType = new ComputationTargetTypeVisitor<NestedComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final NestedComputationTargetType self) {
      final List<ComputationTargetType> selfTypes = self.getTypes();
      final ComputationTargetType selfType = selfTypes.get(selfTypes.size() - 1);
      for (ComputationTargetType type : types) {
        if (selfType.isTargetType(type)) {
          return Boolean.TRUE;
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final NestedComputationTargetType self) {
      final List<ComputationTargetType> selfTypes = self.getTypes();
      if (types.size() > selfTypes.size()) {
        return Boolean.FALSE;
      }
      for (int i = 1; i <= types.size(); i++) {
        if (!selfTypes.get(selfTypes.size() - i).isTargetType(types.get(types.size() - i))) {
          return Boolean.FALSE;
        }
      }
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final NestedComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final NestedComputationTargetType self) {
      final List<ComputationTargetType> types = self.getTypes();
      return types.get(types.size() - 1).isTargetType(type);
    }

  };

  @Override
  public boolean isTargetType(final ComputationTargetType type) {
    return type.accept(s_isTargetType, this);
  }

  @Override
  public boolean isTargetType(final Class<? extends UniqueIdentifiable> type) {
    final List<ComputationTargetType> types = getTypes();
    return types.get(types.size() - 1).isTargetType(type);
  }

  @Override
  public <D, T> T accept(final ComputationTargetTypeVisitor<D, T> visitor, final D data) {
    return visitor.visitNestedComputationTargetTypes(getTypes(), data);
  }

  protected void toStringImpl(final StringBuilder sb) {
    boolean sep = false;
    for (ComputationTargetType type : getTypes()) {
      if (sep) {
        sb.append('/');
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

  protected void getNameImpl(final StringBuilder sb) {
    boolean sep = false;
    for (ComputationTargetType type : getTypes()) {
      if (sep) {
        sb.append('/');
      } else {
        sep = true;
      }
      type.getNameNested(sb);
    }
  }

  @Override
  protected void getNameNested(final StringBuilder sb) {
    sb.append('(');
    getNameImpl(sb);
    sb.append(')');
  }

  @Override
  public String getName() {
    final StringBuilder sb = new StringBuilder();
    getNameImpl(sb);
    return sb.toString();
  }

  private static final ComputationTargetTypeVisitor<NestedComputationTargetType, Boolean> s_equals = new ComputationTargetTypeVisitor<NestedComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final NestedComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final NestedComputationTargetType self) {
      return self.getTypes().equals(types);
    }

    @Override
    public Boolean visitNullComputationTargetType(final NestedComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final NestedComputationTargetType self) {
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
