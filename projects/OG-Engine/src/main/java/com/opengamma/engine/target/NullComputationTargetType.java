/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.List;
import java.util.Set;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Implementation of the {@code NULL} computation target type. This is an explicit type for null or empty targets.
 */
/* package */final class NullComputationTargetType extends ComputationTargetType implements ObjectResolver<UniqueIdentifiable> {

  private static final long serialVersionUID = 1L;

  public NullComputationTargetType() {
    super(NullComputationTargetType.class.getName().hashCode());
  }

  // ComputationTargetType

  @Override
  public boolean isCompatible(final UniqueIdentifiable target) {
    return target == null;
  }

  @Override
  public boolean isCompatible(final ComputationTargetType type) {
    return type == this;
  }

  @Override
  public boolean isCompatible(final Class<? extends UniqueIdentifiable> clazz) {
    return false;
  }

  @Override
  public <D, T> T accept(final ComputationTargetTypeVisitor<D, T> visitor, final D data) {
    return visitor.visitNullComputationTargetType(data);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  protected void toStringNested(final StringBuilder sb) {
    sb.append(toString());
  }

  @Override
  public String getName() {
    return "NULL";
  }

  @Override
  protected void getNameNested(final StringBuilder sb) {
    sb.append(getName());
  }

  private static final ComputationTargetTypeVisitor<NullComputationTargetType, Boolean> s_equals = new ComputationTargetTypeVisitor<NullComputationTargetType, Boolean>() {

    @Override
    public Boolean visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final NullComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final NullComputationTargetType self) {
      return Boolean.FALSE;
    }

    @Override
    public Boolean visitNullComputationTargetType(final NullComputationTargetType self) {
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final NullComputationTargetType self) {
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

  @Override
  public boolean isTargetType(final ComputationTargetType type) {
    return equals(type);
  }

  @Override
  public boolean isTargetType(final Class<? extends UniqueIdentifiable> type) {
    return false;
  }

  // ObjectResolver

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public UniqueIdentifiable resolveObject(UniqueId uniqueId, VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public DeepResolver deepResolver() {
    return null;
  }

}
