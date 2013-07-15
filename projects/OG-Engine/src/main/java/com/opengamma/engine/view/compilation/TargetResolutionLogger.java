/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver.AtVersionCorrection;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Wraps an existing specification resolver to log all of the resolutions calls. This allows any values that were considered during the compilation to be recorded and returned as part of the compiled
 * context. If the resolution of any of these would be different for a different version/correction then this compilation is no longer valid.
 */
/* package */final class TargetResolutionLogger implements ComputationTargetResolver.AtVersionCorrection {

  private static final Logger s_logger = LoggerFactory.getLogger(TargetResolutionLogger.class);

  private static final class LoggingSpecificationResolver implements ComputationTargetSpecificationResolver.AtVersionCorrection, ComputationTargetReferenceVisitor<ComputationTargetReference> {

    private final ComputationTargetSpecificationResolver.AtVersionCorrection _underlying;
    private final ConcurrentMap<ComputationTargetReference, UniqueId> _resolutions;

    private LoggingSpecificationResolver(final ComputationTargetSpecificationResolver.AtVersionCorrection underlying, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
      _underlying = underlying;
      _resolutions = resolutions;
    }

    private static final ComputationTargetTypeVisitor<Void, ComputationTargetType> s_getLeafType = new ComputationTargetTypeVisitor<Void, ComputationTargetType>() {

      @Override
      public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
        final ComputationTargetType[] result = new ComputationTargetType[types.size()];
        int i = 0;
        boolean different = false;
        for (final ComputationTargetType type : types) {
          final ComputationTargetType leafType = type.accept(this, null);
          if (leafType != null) {
            result[i++] = leafType;
            different = true;
          } else {
            result[i++] = type;
          }
        }
        if (different) {
          return ComputationTargetType.multiple(result);
        } else {
          return null;
        }
      }

      @Override
      public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
        final ComputationTargetType leafType = types.get(types.size() - 1);
        final ComputationTargetType newLeafType = leafType.accept(this, null);
        if (newLeafType != null) {
          return newLeafType;
        } else {
          return leafType;
        }
      }

      @Override
      public ComputationTargetType visitNullComputationTargetType(final Void data) {
        return null;
      }

      @Override
      public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
        return null;
      }

    };

    private ComputationTargetType getLeafType(final ComputationTargetType type) {
      // TODO: Ought to reduce the type to its simplest form. This hasn't been a problem with the views & function repository I've been testing
      // with but might be necessary in the general case as there will be duplicate values in the resolver cache.
      return type.accept(s_getLeafType, null);
    }

    private void storeResolution(final ComputationTargetReference reference, final ComputationTargetSpecification resolved) {
      final ComputationTargetReference key = reference.accept(this);
      if (key != null) {
        final UniqueId resolvedId = resolved.getUniqueId();
        final UniqueId previousId = _resolutions.put(key, resolvedId);
        if ((previousId != null) && !resolvedId.equals(previousId)) {
          // Note than when following LATEST/LATEST, there may be new resolutions at the v/c we're using but we haven't received
          // the change manager notifications yet so aren't expecting them. The resulting graph will be inconsistent at this point.
          // Options are:
          //   1) Abort this build, invalidate the whole graph and retry
          //   2) Abort this build, invalidate more of the graph and retry
          //   3) Ignore it
          s_logger.error("Unexpected resolution of {} to {} instead of {}", new Object[] {key, resolvedId, previousId });
        }
      }
    }

    // ComputationTargetSpecificationResolver.AtVersionCorrection

    @Override
    public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference) {
      final ComputationTargetSpecification resolved = _underlying.getTargetSpecification(reference);
      if (resolved != null) {
        storeResolution(reference, resolved);
      }
      return resolved;
    }

    @Override
    public Map<ComputationTargetReference, ComputationTargetSpecification> getTargetSpecifications(final Set<ComputationTargetReference> references) {
      final Map<ComputationTargetReference, ComputationTargetSpecification> resolveds = _underlying.getTargetSpecifications(references);
      for (final Map.Entry<ComputationTargetReference, ComputationTargetSpecification> resolved : resolveds.entrySet()) {
        storeResolution(resolved.getKey(), resolved.getValue());
      }
      return resolveds;
    }

    // ComputationTargetReferenceVisitor

    @Override
    public ComputationTargetReference visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      final ComputationTargetType leafType = getLeafType(requirement.getType());
      if (leafType != null) {
        return MemoryUtils.instance(new ComputationTargetRequirement(leafType, requirement.getIdentifiers()));
      } else {
        return requirement;
      }
    }

    @Override
    public ComputationTargetReference visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      if ((specification.getUniqueId() != null) && specification.getUniqueId().isLatest()) {
        final ComputationTargetType leafType = getLeafType(specification.getType());
        if (leafType != null) {
          return MemoryUtils.instance(new ComputationTargetSpecification(leafType, specification.getUniqueId()));
        } else {
          return specification;
        }
      } else {
        return null;
      }
    }

  }

  private final ComputationTargetResolver.AtVersionCorrection _underlying;
  private final ComputationTargetSpecificationResolver.AtVersionCorrection _specificationResolver;

  public static TargetResolutionLogger of(final ComputationTargetResolver.AtVersionCorrection underlying, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
    return new TargetResolutionLogger(underlying, new LoggingSpecificationResolver(underlying.getSpecificationResolver(), resolutions));
  }

  private TargetResolutionLogger(final ComputationTargetResolver.AtVersionCorrection underlying, final ComputationTargetSpecificationResolver.AtVersionCorrection specificationResolver) {
    _underlying = underlying;
    _specificationResolver = specificationResolver;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    return _underlying.resolve(specification);
  }

  @Override
  public AtVersionCorrection getSpecificationResolver() {
    return _specificationResolver;
  }

  @Override
  public ComputationTargetType simplifyType(final ComputationTargetType type) {
    return _underlying.simplifyType(type);
  }

  @Override
  public VersionCorrection getVersionCorrection() {
    return _underlying.getVersionCorrection();
  }

}
