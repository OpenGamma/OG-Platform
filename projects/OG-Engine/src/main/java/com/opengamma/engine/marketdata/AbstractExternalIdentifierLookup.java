/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.functional.Function1;

/**
 * Look up an external identifier or identifier bundle from a computation target.
 */
/* package */abstract class AbstractExternalIdentifierLookup<T> implements ComputationTargetReferenceVisitor<T> {

  private final ComputationTargetTypeMap<Function1<UniqueId, T>> _lookup = new ComputationTargetTypeMap<Function1<UniqueId, T>>();

  @SuppressWarnings("unchecked")
  protected void registerBundleLookup(final ComputationTargetType type, final Function1<UniqueId, ExternalIdBundle> operation) {
    _lookup.put(type, (Function1<UniqueId, T>) operation);
  }

  @SuppressWarnings("unchecked")
  protected void registerIdentifierLookup(final ComputationTargetType type, final Function1<UniqueId, ExternalId> operation) {
    _lookup.put(type, (Function1<UniqueId, T>) operation);
  }

  public AbstractExternalIdentifierLookup(final SecuritySource securitySource) {
    if (securitySource != null) {
      registerBundleLookup(ComputationTargetType.SECURITY, new Function1<UniqueId, ExternalIdBundle>() {
        @Override
        public ExternalIdBundle execute(final UniqueId uid) {
          try {
            return securitySource.get(uid).getExternalIdBundle();
          } catch (DataNotFoundException e) {
            return null;
          }
        }
      });
    }
  }

  protected T lookup(final ComputationTargetReference target) {
    return target.accept(this);
  }

  protected abstract T fromUniqueId(final UniqueId uid);

  @Override
  public T visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
    final Function1<UniqueId, T> operation = _lookup.get(specification.getType());
    T result = null;
    if (operation != null) {
      result = operation.execute(specification.getUniqueId());
    }
    if (result == null && specification.getUniqueId() != null) {
      // Some code may still exist that is forcing an identifier into a unique id when constructing value requirements. We handle
      // that case here temporarily. This code should be removed when all of the functions have been fixed and there are no remaining
      // view definitions in the configuration database that have unique id forms for primitive requirements. There are also the
      // ValueSpecification keys used in the dependency graph for market data. Going from those to the original requirements is not
      // good but code may still be doing it - it should be querying the dependency graph for the ValueRequirement data.
      return fromUniqueId(specification.getUniqueId());
    } else {
      return result;
    }
  }

}
