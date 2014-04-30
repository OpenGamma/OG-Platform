/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.List;
import java.util.Set;

import com.opengamma.id.UniqueIdentifiable;

/**
 * A computation target that can be of a single Java type.
 */
/* package */final class ClassComputationTargetType extends ComputationTargetType {

  private static final long serialVersionUID = 1L;

  private final Class<? extends UniqueIdentifiable> _target;
  /**
   * Display/legacy name of the target type. This is not part of the equality or hash.
   */
  private final String _name;
  /**
   * Flag to use the display/legacy name as the formal string.
   */
  private final boolean _nameWellKnown;

  /**
   * Creates a new instance.
   * 
   * @param target the target class
   * @param name the preferred display name for the type, this must not contain {@code ()/|} characters
   */
  public ClassComputationTargetType(final Class<? extends UniqueIdentifiable> target, final String name, final boolean nameWellKnown) {
    //ok to use concrete class rather than getClass() since this class is final
    super(ClassComputationTargetType.class.getName().hashCode() * 31 + target.getName().hashCode());
    _target = target;
    _name = name;
    _nameWellKnown = nameWellKnown;
  }

  protected Class<? extends UniqueIdentifiable> getTarget() {
    return _target;
  }

  @Override
  public String getName() {
    return _name;
  }

  protected boolean isNameWellKnown() {
    return _nameWellKnown;
  }

  @Override
  public boolean isCompatible(final UniqueIdentifiable target) {
    return (target != null) && isCompatible(target.getClass());
  }

  @Override
  public boolean isCompatible(final Class<? extends UniqueIdentifiable> clazz) {
    return getTarget().isAssignableFrom(clazz);
  }

  private static final ComputationTargetTypeVisitor<ClassComputationTargetType, Boolean> s_isCompatible = new ComputationTargetTypeVisitor<ClassComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ClassComputationTargetType self) {
      for (ComputationTargetType type : types) {
        if (self.isCompatible(type)) {
          return Boolean.TRUE;
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ClassComputationTargetType self) {
      return self.isCompatible(types.get(types.size() - 1));
    }

    @Override
    public Boolean visitNullComputationTargetType(final ClassComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ClassComputationTargetType self) {
      return self.getTarget().isAssignableFrom(type);
    }

  };

  @Override
  public boolean isCompatible(final ComputationTargetType type) {
    return type.accept(s_isCompatible, this);
  }

  private static final ComputationTargetTypeVisitor<ClassComputationTargetType, Boolean> s_isTargetType = new ComputationTargetTypeVisitor<ClassComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ClassComputationTargetType self) {
      for (ComputationTargetType type : types) {
        if (self.isTargetType(type)) {
          return Boolean.TRUE;
        }
      }
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ClassComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final ClassComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ClassComputationTargetType self) {
      return self.isTargetType(type);
    }

  };

  @Override
  public boolean isTargetType(final ComputationTargetType type) {
    return type.accept(s_isTargetType, this);
  }

  @Override
  public boolean isTargetType(final Class<? extends UniqueIdentifiable> type) {
    return type.isAssignableFrom(getTarget());
  }

  @Override
  public <D, T> T accept(final ComputationTargetTypeVisitor<D, T> visitor, final D data) {
    return visitor.visitClassComputationTargetType(getTarget(), data);
  }

  @Override
  public String toString() {
    return isNameWellKnown() ? getName() : getTarget().getName();
  }

  @Override
  protected void toStringNested(final StringBuilder sb) {
    sb.append(toString());
  }

  @Override
  protected void getNameNested(final StringBuilder sb) {
    sb.append(getName());
  }

  private static final ComputationTargetTypeVisitor<ClassComputationTargetType, Boolean> s_equals = new ComputationTargetTypeVisitor<ClassComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ClassComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ClassComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final ClassComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ClassComputationTargetType self) {
      return self.getTarget().equals(type);
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
