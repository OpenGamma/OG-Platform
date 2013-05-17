/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Visitor to remap the unique identifier from a target specification.
 */
/* package */final class ComputationTargetIdentifierRemapVisitor implements ComputationTargetReferenceVisitor<ComputationTargetReference> {

  private static final ComputationTargetTypeVisitor<Void, ComputationTargetType> s_getLeafType = new ComputationTargetTypeVisitor<Void, ComputationTargetType>() {

    @Override
    public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
      throw new IllegalStateException();
    }

    @Override
    public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
      return types.get(types.size() - 1);
    }

    @Override
    public ComputationTargetType visitNullComputationTargetType(final Void data) {
      throw new IllegalStateException();
    }

    @Override
    public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
      throw new IllegalStateException();
    }

  };

  private final Map<UniqueId, UniqueId> _map;

  public ComputationTargetIdentifierRemapVisitor(final Map<UniqueId, UniqueId> map) {
    _map = map;
  }

  public ComputationTargetRequirement remap(final ComputationTargetRequirement requirement) {
    ComputationTargetReference parent = requirement.getParent();
    if (parent != null) {
      final ComputationTargetReference rewriteParent = parent.accept(this);
      if (rewriteParent != null) {
        return rewriteParent.containing(requirement.getType().accept(s_getLeafType, null), requirement.getIdentifiers());
      }
    }
    return null;
  }

  public ComputationTargetSpecification remap(final ComputationTargetSpecification specification) {
    UniqueId rewriteSelf = _map.get(specification.getUniqueId());
    ComputationTargetReference parent = specification.getParent();
    if (parent != null) {
      final ComputationTargetReference rewriteParent = parent.accept(this);
      if (rewriteParent != null) {
        if (rewriteSelf != null) {
          return rewriteParent.containing(specification.getType().accept(s_getLeafType, null), rewriteSelf);
        } else {
          return rewriteParent.containing(specification.getType().accept(s_getLeafType, null), specification.getUniqueId());
        }
      }
    }
    if (rewriteSelf != null) {
      return specification.replaceIdentifier(rewriteSelf);
    }
    return null;
  }

  // ComputationTargetReferenceVisitor

  @Override
  public ComputationTargetReference visitComputationTargetRequirement(ComputationTargetRequirement requirement) {
    return remap(requirement);
  }

  @Override
  public ComputationTargetReference visitComputationTargetSpecification(ComputationTargetSpecification specification) {
    return remap(specification);
  }

}
