/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * A reference to a particular computation target that will be resolved later to a real target. The reference may be "strict" and refer to a specific object or concept by {@link UniqueId} or "loose"
 * and refer to it by a broader identifier bundle that must first be resolved.
 */
@PublicAPI
public abstract class ComputationTargetReference implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The type of the target.
   */
  private final ComputationTargetType _type;
  /**
   * The reference to the immediate parent that puts this target into its typed context, or null if there is no typed context.
   */
  private final ComputationTargetReference _parent;

  protected/* [PLAT-444]: should be package visible */ComputationTargetReference(final ComputationTargetType type) {
    assert type != null;
    assert getTypeDepth(type) <= 1;
    _type = type;
    _parent = null;
  }

  protected/* [PLAT-444]: should be package visible */ComputationTargetReference(final ComputationTargetType type, final ComputationTargetReference parent) {
    assert type != null;
    assert parent != null;
    assert getTypeDepth(type) == 1;
    _type = parent.getType().containing(type);
    _parent = parent;
  }

  protected/* [PLAT-444]: should be package visible */ComputationTargetReference(final ComputationTargetReference parent, final ComputationTargetType type) {
    _type = type;
    _parent = parent;
  }

  private static final Integer ZERO = 0;
  private static final Integer ONE = 1;

  private static final ComputationTargetTypeVisitor<Void, Integer> s_getTypeDepth = new ComputationTargetTypeVisitor<Void, Integer>() {

    @Override
    public Integer visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
      Integer depth = null;
      for (ComputationTargetType type : types) {
        final Integer typeDepth = type.accept(this, data);
        if (depth == null) {
          depth = typeDepth;
        } else if (!depth.equals(typeDepth)) {
          throw new IllegalArgumentException();
        }
      }
      return depth;
    }

    @Override
    public Integer visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
      int depth = 0;
      for (ComputationTargetType type : types) {
        depth += type.accept(this, data);
      }
      return depth;
    }

    @Override
    public Integer visitNullComputationTargetType(final Void data) {
      return ZERO;
    }

    @Override
    public Integer visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
      return ONE;
    }

  };

  /**
   * Tests the depth of a typed context. If the type does not describe an object of uniform depth it is rejected.
   * 
   * @param type the type to test, not null
   * @return the depth
   * @throws IllegalArgumentException if the type does not have a uniform depth
   */
  public/* should be package visible */static int getTypeDepth(final ComputationTargetType type) {
    return type.accept(s_getTypeDepth, null);
  }

  public ComputationTargetRequirement containing(final ComputationTargetType type, final ExternalId identifier) {
    return containing(type, identifier.toBundle());
  }

  public ComputationTargetRequirement containing(final ComputationTargetType type, final ExternalIdBundle identifiers) {
    return new ComputationTargetRequirement(type, identifiers, this);
  }

  public ComputationTargetSpecification containing(final ComputationTargetType type, final UniqueId identifier) {
    return new ComputationTargetSpecification(type, identifier, this);
  }

  /**
   * Gets the type of the target.
   * 
   * @return the type, not null
   */
  public ComputationTargetType getType() {
    return _type;
  }

  /**
   * Gets the parent reference when this is used in context.
   * 
   * @return the parent reference or null if the target is not within a context
   */
  public ComputationTargetReference getParent() {
    return _parent;
  }

  /**
   * Returns this as a target requirement if it is one. If it is not a requirement, an exception will be thrown. Use the visitor pattern to deal with the alternative types; such as to resolve a
   * requirement to a specification using the compilation context.
   * 
   * @return the computation target requirement instance, not null
   * @throws IllegalStateException if this is not a {@link ComputationTargetRequirement}
   */
  public ComputationTargetRequirement getRequirement() {
    throw new IllegalStateException(toString() + " already resolved");
  }

  /**
   * Returns this as a target specification if it is one. If it is not a specification, an exception will be thrown. Use the visitor pattern to deal with the alternative types; such as to resolve a
   * requirement to a specification using the compilation context.
   * 
   * @return the computation target specification instance, not null
   * @throws IllegalStateException if this is not a {@link ComputationTargetSpecification}
   */
  public ComputationTargetSpecification getSpecification() {
    throw new IllegalStateException(toString() + " is not resolved");
  }

  @Override
  public boolean equals(final Object obj) {
    // Sub-classes must override and call back after the type has been checked
    final ComputationTargetReference other = (ComputationTargetReference) obj;
    return ObjectUtils.equals(getParent(), other.getParent()) && getType().equals(other.getType());
  }

  @Override
  public int hashCode() {
    // Sub-classes must override
    //getName() and hashCode() results are cached on their objects
    return (getClass().getName().hashCode() * 31 * 31) + ObjectUtils.hashCode(getParent()) * 31 + getType().hashCode();
  }

  /**
   * Applies the visitor operation to this reference.
   * 
   * @param <T> the return type of the visitor
   * @param visitor the visitor operation, not null
   * @return the result of the visitor operation
   */
  public abstract <T> T accept(ComputationTargetReferenceVisitor<T> visitor);

  protected/* [PLAT-444]: should be package visible */abstract ComputationTargetReference create(ComputationTargetReference parent, ComputationTargetType type);

  /**
   * Normalizes the parent reference using {@link MemoryUtils} to reduce the memory footprint.
   * 
   * @return this instance or an equivalent object that references a shared parent instance
   */
  public ComputationTargetReference normalize() {
    if (getParent() == null) {
      return this;
    } else {
      final ComputationTargetReference parent = MemoryUtils.instance(getParent());
      if (parent == getParent()) {
        return this;
      } else {
        return create(parent, getType());
      }
    }
  }

  private static final ComputationTargetTypeVisitor<Void, ComputationTargetType> s_getParentType = new ComputationTargetTypeVisitor<Void, ComputationTargetType>() {

    @Override
    public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
      throw new IllegalStateException();
    }

    @Override
    public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
      ComputationTargetType result = types.get(0);
      final int l = types.size() - 1;
      for (int i = 1; i < l; i++) {
        result = result.containing(types.get(i));
      }
      return result;
    }

    @Override
    public ComputationTargetType visitNullComputationTargetType(final Void data) {
      throw new IllegalStateException();
    }

    @Override
    public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
      return null;
    }

  };

  /**
   * Returns a new reference with the same identifiers but a different target type.
   * 
   * @param newType the type of the new object, not null
   * @return the new reference object, not null
   */
  public ComputationTargetReference replaceType(final ComputationTargetType newType) {
    if (newType == getType()) {
      return this;
    } else {
      // TODO: should be checking the type
      final ComputationTargetReference parent = getParent();
      if (parent != null) {
        final ComputationTargetType parentType = newType.accept(s_getParentType, null);
        if (parentType == null) {
          // Truncate the parent
          return create(null, newType);
        } else {
          // Update the parent
          return create(parent.replaceType(parentType), newType);
        }
      } else {
        return create(null, newType);
      }
    }
  }

  protected abstract String getIdStringImpl();

  protected String getIdString() {
    if (getParent() != null) {
      return getParent().getIdString() + "/" + getIdStringImpl();
    } else {
      return getIdStringImpl();
    }
  }

  /**
   * Returns a new reference with the same type (and parent) but a different leaf identifier.
   * 
   * @param identifier the new identifier, not null
   * @return the new reference object
   */
  public ComputationTargetSpecification replaceIdentifier(final UniqueId identifier) {
    return new ComputationTargetSpecification(getParent(), getType(), identifier);
  }

}
