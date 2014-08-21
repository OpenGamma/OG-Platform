/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.resolver.IdentifierResolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.function.BinaryOperator;

/**
 * Standard implementation of a {@link ComputationTargetSpecificationResolver}.
 * <p>
 * Note that this is a fairly cheap operation; looking up the resolver and calling that. It should not normally be necessary to provide any caching on top of a specification resolver - if a
 * requirement is being resolved regularly for the same version/correction then there is probably something wrong elsewhere. If a resolver implementation is costly (for example querying an underlying
 * data source) then that is where the caching should lie.
 */
public class DefaultComputationTargetSpecificationResolver implements ComputationTargetSpecificationResolver {

  private abstract static class SpecificationResolver {

    public abstract ComputationTargetSpecification resolveRequirement(ComputationTargetRequirement requirement, VersionCorrection versionCorrection);

    public abstract void resolveRequirements(Collection<ComputationTargetRequirement> requirements, VersionCorrection versionCorrection,
        Map<ComputationTargetReference, ComputationTargetSpecification> result);

    public abstract ComputationTargetSpecification resolveObjectId(ComputationTargetReference parent, ObjectId identifier, VersionCorrection versionCorrection);

    public abstract void resolveSpecifications(Collection<ComputationTargetSpecification> specifications, VersionCorrection versionCorrection,
        Map<ComputationTargetReference, ComputationTargetSpecification> result);

  }

  private static class SingleSpecificationResolver extends SpecificationResolver {

    private final ComputationTargetType _type;
    private final IdentifierResolver _resolver;

    public SingleSpecificationResolver(final ComputationTargetType type, final IdentifierResolver resolver) {
      _type = type;
      _resolver = resolver;
    }

    private ComputationTargetSpecification resolved(final ComputationTargetReference parent, final UniqueId uid) {
      if (parent != null) {
        return parent.containing(_type, uid);
      } else {
        return new ComputationTargetSpecification(_type, uid);
      }
    }

    @Override
    public ComputationTargetSpecification resolveRequirement(final ComputationTargetRequirement requirement, final VersionCorrection versionCorrection) {
      final UniqueId uid = _resolver.resolveExternalId(requirement.getIdentifiers(), versionCorrection);
      if (uid != null) {
        return resolved(requirement.getParent(), uid);
      } else {
        return null;
      }
    }

    @Override
    public void resolveRequirements(final Collection<ComputationTargetRequirement> requirements, final VersionCorrection versionCorrection,
        final Map<ComputationTargetReference, ComputationTargetSpecification> result) {
      final Set<ExternalIdBundle> identifiers = Sets.newHashSetWithExpectedSize(requirements.size());
      for (ComputationTargetRequirement requirement : requirements) {
        identifiers.add(requirement.getIdentifiers());
      }
      final Map<ExternalIdBundle, UniqueId> resolved = _resolver.resolveExternalIds(identifiers, versionCorrection);
      for (ComputationTargetRequirement requirement : requirements) {
        final UniqueId uid = resolved.get(requirement.getIdentifiers());
        if (uid != null) {
          result.put(requirement, resolved(requirement.getParent(), uid));
        }
      }
    }

    @Override
    public ComputationTargetSpecification resolveObjectId(final ComputationTargetReference parent, final ObjectId identifier, final VersionCorrection versionCorrection) {
      final UniqueId uid = _resolver.resolveObjectId(identifier, versionCorrection);
      if (uid != null) {
        return resolved(parent, uid);
      } else {
        return null;
      }
    }

    @Override
    public void resolveSpecifications(final Collection<ComputationTargetSpecification> specifications, final VersionCorrection versionCorrection,
        final Map<ComputationTargetReference, ComputationTargetSpecification> result) {
      final Set<ObjectId> identifiers = Sets.newHashSetWithExpectedSize(specifications.size());
      for (ComputationTargetSpecification specification : specifications) {
        identifiers.add(specification.getUniqueId().getObjectId());
      }
      final Map<ObjectId, UniqueId> resolved = _resolver.resolveObjectIds(identifiers, versionCorrection);
      for (ComputationTargetSpecification specification : specifications) {
        final UniqueId uid = resolved.get(specification.getUniqueId().getObjectId());
        if (uid != null) {
          result.put(specification, resolved(specification.getParent(), uid));
        }
      }
    }

  }

  private static class FoldedSpecificationResolver extends SpecificationResolver {

    private final SpecificationResolver _a;
    private final SpecificationResolver _b;

    public FoldedSpecificationResolver(final SpecificationResolver a, final SpecificationResolver b) {
      _a = a;
      _b = b;
    }

    @Override
    public ComputationTargetSpecification resolveRequirement(final ComputationTargetRequirement requirement, final VersionCorrection versionCorrection) {
      final ComputationTargetSpecification a = _a.resolveRequirement(requirement, versionCorrection);
      if (a != null) {
        return a;
      }
      return _b.resolveRequirement(requirement, versionCorrection);
    }

    @Override
    public void resolveRequirements(final Collection<ComputationTargetRequirement> requirements, final VersionCorrection versionCorrection,
        final Map<ComputationTargetReference, ComputationTargetSpecification> result) {
      int remaining = requirements.size() + result.size();
      _a.resolveRequirements(requirements, versionCorrection, result);
      remaining -= result.size();
      if (remaining > 0) {
        final List<ComputationTargetRequirement> pending = new ArrayList<ComputationTargetRequirement>(remaining);
        for (ComputationTargetRequirement requirement : requirements) {
          if (!result.containsKey(requirement)) {
            pending.add(requirement);
          }
        }
        _b.resolveRequirements(pending, versionCorrection, result);
      }
    }

    @Override
    public ComputationTargetSpecification resolveObjectId(final ComputationTargetReference parent, final ObjectId identifier, final VersionCorrection versionCorrection) {
      final ComputationTargetSpecification a = _a.resolveObjectId(parent, identifier, versionCorrection);
      if (a != null) {
        return a;
      }
      return _b.resolveObjectId(parent, identifier, versionCorrection);
    }

    @Override
    public void resolveSpecifications(final Collection<ComputationTargetSpecification> specifications, final VersionCorrection versionCorrection,
        final Map<ComputationTargetReference, ComputationTargetSpecification> result) {
      int remaining = specifications.size() + result.size();
      _a.resolveSpecifications(specifications, versionCorrection, result);
      remaining -= result.size();
      if (remaining > 0) {
        final List<ComputationTargetSpecification> pending = new ArrayList<ComputationTargetSpecification>(remaining);
        for (ComputationTargetSpecification specification : specifications) {
          if (!result.containsKey(specification)) {
            pending.add(specification);
          }
        }
        _b.resolveSpecifications(pending, versionCorrection, result);
      }
    }

  }

  private static final BinaryOperator<SpecificationResolver> s_fold = new BinaryOperator<SpecificationResolver>() {
    @Override
    public SpecificationResolver apply(final SpecificationResolver a, final SpecificationResolver b) {
      return new FoldedSpecificationResolver(a, b);
    }
  };

  private final ComputationTargetTypeMap<SpecificationResolver> _resolve = new ComputationTargetTypeMap<SpecificationResolver>(s_fold);

  public void addResolver(final ComputationTargetType type, final IdentifierResolver strategy) {
    _resolve.put(type, new SingleSpecificationResolver(type, strategy));
  }

  @Override
  public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference, final VersionCorrection versionCorrection) {
    return atVersionCorrection(versionCorrection).getTargetSpecification(reference);
  }

  @Override
  public Map<ComputationTargetReference, ComputationTargetSpecification> getTargetSpecifications(final Set<ComputationTargetReference> references, final VersionCorrection versionCorrection) {
    return atVersionCorrection(versionCorrection).getTargetSpecifications(references);
  }

  @Override
  public AtVersionCorrection atVersionCorrection(final VersionCorrection versionCorrection) {
    return new AtVersionCorrection() {

      private PoolExecutor.Service<Void> createService() {
        final PoolExecutor executor = PoolExecutor.instance();
        if (executor != null) {
          return executor.createService(null);
        } else {
          return null;
        }
      }

      private final ComputationTargetReferenceVisitor<ComputationTargetSpecification> _getTargetSpecification =
          new ComputationTargetReferenceVisitor<ComputationTargetSpecification>() {

            @Override
            public ComputationTargetSpecification visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
              final SpecificationResolver resolver = _resolve.get(requirement.getType());
              if (resolver != null) {
                return resolver.resolveRequirement(requirement, versionCorrection);
              } else {
                return null;
              }
            }

            @Override
            public ComputationTargetSpecification visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
              final UniqueId uid = specification.getUniqueId();
              if ((uid != null) && uid.isLatest()) {
                final SpecificationResolver resolver = _resolve.get(specification.getType());
                if (resolver != null) {
                  return resolver.resolveObjectId(specification.getParent(), uid.getObjectId(), versionCorrection);
                } else {
                  return specification;
                }
              } else {
                return specification;
              }
            }

          };

      @Override
      public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference) {
        return reference.accept(_getTargetSpecification);
      }

      @Override
      public Map<ComputationTargetReference, ComputationTargetSpecification> getTargetSpecifications(final Set<ComputationTargetReference> references) {
        final Map<ComputationTargetReference, ComputationTargetSpecification> result = new ConcurrentHashMap<ComputationTargetReference, ComputationTargetSpecification>();
        final Map<ComputationTargetType, Set<ComputationTargetRequirement>> requirementByType = new HashMap<ComputationTargetType, Set<ComputationTargetRequirement>>();
        final Map<ComputationTargetType, Set<ComputationTargetSpecification>> specificationByType = new HashMap<ComputationTargetType, Set<ComputationTargetSpecification>>();
        final ComputationTargetReferenceVisitor<Void> visitor = new ComputationTargetReferenceVisitor<Void>() {

          @Override
          public Void visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
            Set<ComputationTargetRequirement> requirements = requirementByType.get(requirement.getType());
            if (requirements == null) {
              if (_resolve.get(requirement.getType()) != null) {
                requirements = new HashSet<ComputationTargetRequirement>();
                requirementByType.put(requirement.getType(), requirements);
                // Add to the bulk resolution set
                requirements.add(requirement);
              } else {
                // No resolver for this type
                requirementByType.put(requirement.getType(), Collections.<ComputationTargetRequirement>emptySet());
              }
            } else {
              if (!requirements.isEmpty()) {
                // Add to the bulk resolution set. If the set is empty, we've cached the "no resolver" status
                requirements.add(requirement);
              }
            }
            return null;
          }

          @Override
          public Void visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
            if ((specification.getUniqueId() != null) && (specification.getUniqueId().isLatest())) {
              Set<ComputationTargetSpecification> specifications = specificationByType.get(specification.getType());
              if (specifications == null) {
                if (_resolve.get(specification.getType()) != null) {
                  specifications = new HashSet<ComputationTargetSpecification>();
                  specificationByType.put(specification.getType(), specifications);
                  // Add to the bulk resolution set
                  specifications.add(specification);
                } else {
                  // No resolver for this type
                  specificationByType.put(specification.getType(), Collections.<ComputationTargetSpecification>emptySet());
                  result.put(specification, specification);
                }
              } else {
                if (specifications.isEmpty()) {
                  // No resolver for this type
                  result.put(specification, specification);
                } else {
                  // Add to the bulk resolution set
                  specifications.add(specification);
                }
              }
            } else {
              // Already strictly versioned
              result.put(specification, specification);
            }
            return null;
          }

        };
        for (final ComputationTargetReference reference : references) {
          reference.accept(visitor);
        }
        final PoolExecutor.Service<Void> jobs = createService();
        // TODO: sort the target types - some resolvers will cause caching behavior that will help others out (e.g. resolving Portfolio OID will cache all component Position OID/UIDs).
        // TODO: should there be a threshold for single vs bulk - e.g. are two calls in succession quicker than the map/set operations?
        for (final Map.Entry<ComputationTargetType, Set<ComputationTargetRequirement>> entry : requirementByType.entrySet()) {
          switch (entry.getValue().size()) {
            case 0:
              // No resolver for this type
              break;
            case 1: {
              // Single item
              if (jobs != null) {
                jobs.execute(new Runnable() {
                  @Override
                  public void run() {
                    final ComputationTargetRequirement requirement = entry.getValue().iterator().next();
                    final ComputationTargetSpecification specification = _resolve.get(entry.getKey()).resolveRequirement(requirement, versionCorrection);
                    if (specification != null) {
                      synchronized (result) {
                        result.put(requirement, specification);
                      }
                    }
                  }
                });
              } else {
                final ComputationTargetRequirement requirement = entry.getValue().iterator().next();
                final ComputationTargetSpecification specification = _resolve.get(entry.getKey()).resolveRequirement(requirement, versionCorrection);
                if (specification != null) {
                  result.put(requirement, specification);
                }
              }
              break;
            }
            default: {
              // Bulk lookup
              if (jobs != null) {
                jobs.execute(new Runnable() {
                  @Override
                  public void run() {
                    _resolve.get(entry.getKey()).resolveRequirements(entry.getValue(), versionCorrection, result);
                  }
                });
              } else {
                _resolve.get(entry.getKey()).resolveRequirements(entry.getValue(), versionCorrection, result);
              }
              break;
            }
          }
        }
        for (final Map.Entry<ComputationTargetType, Set<ComputationTargetSpecification>> entry : specificationByType.entrySet()) {
          switch (entry.getValue().size()) {
            case 0:
              // No resolver for this type
              break;
            case 1: {
              // Single item
              if (jobs != null) {
                jobs.execute(new Runnable() {
                  @Override
                  public void run() {
                    final ComputationTargetSpecification specification = entry.getValue().iterator().next();
                    final ComputationTargetSpecification resolved = _resolve.get(entry.getKey()).resolveObjectId(specification.getParent(), specification.getUniqueId().getObjectId(),
                        versionCorrection);
                    if (resolved != null) {
                      result.put(specification, resolved);
                    }
                  }
                });
              } else {
                final ComputationTargetSpecification specification = entry.getValue().iterator().next();
                final ComputationTargetSpecification resolved = _resolve.get(entry.getKey()).resolveObjectId(specification.getParent(), specification.getUniqueId().getObjectId(), versionCorrection);
                if (resolved != null) {
                  result.put(specification, resolved);
                }
              }
              break;
            }
            default: {
              // Bulk lookup
              if (jobs != null) {
                jobs.execute(new Runnable() {
                  @Override
                  public void run() {
                    _resolve.get(entry.getKey()).resolveSpecifications(entry.getValue(), versionCorrection, result);
                  }
                });
              } else {
                _resolve.get(entry.getKey()).resolveSpecifications(entry.getValue(), versionCorrection, result);
              }
              break;
            }
          }
        }
        if (jobs != null) {
          try {
            jobs.join();
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("Interrupted", e);
          }
        }
        return result;
      }

    };
  }
}
