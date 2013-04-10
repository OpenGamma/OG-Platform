/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.id.UniqueId;

/**
 * Visitor to remap the unique identifier from a target specification.
 */
/* package */final class ComputationTargetIdentifierRemapVisitor implements ComputationTargetReferenceVisitor<ComputationTargetReference> {

  private final Map<UniqueId, UniqueId> _map;

  public ComputationTargetIdentifierRemapVisitor(final Map<UniqueId, UniqueId> map) {
    _map = map;
  }

  public ComputationTargetRequirement remap(final ComputationTargetRequirement requirement) {
    ComputationTargetReference parent = requirement.getParent();
    if (parent != null) {
      final ComputationTargetReference rewriteParent = parent.accept(this);
      if (rewriteParent != null) {
        return rewriteParent.containing(requirement.getType(), requirement.getIdentifiers());
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
          return rewriteParent.containing(specification.getType(), rewriteSelf);
        } else {
          return rewriteParent.containing(specification.getType(), specification.getUniqueId());
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
