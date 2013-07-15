/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.resolver.IdentifierResolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PoolExecutor;

/**
 * Standard implementation of a {@link ComputationTargetSpecificationResolver}.
 * <p>
 * Note that this is a fairly cheap operation; looking up the resolver and calling that. It should not normally be necessary to provide any caching on top of a specification resolver - if a
 * requirement is being resolved regularly for the same version/correction then there is probably something wrong elsewhere. If a resolver implementation is costly (for example querying an underlying
 * data source) then that is where the caching should lie.
 */
public class DefaultComputationTargetSpecificationResolver implements ComputationTargetSpecificationResolver {

  private final ComputationTargetTypeMap<IdentifierResolver> _resolve = new ComputationTargetTypeMap<IdentifierResolver>();

  public void addResolver(final ComputationTargetType type, final IdentifierResolver strategy) {
    _resolve.put(type, strategy);
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
              final IdentifierResolver resolver = _resolve.get(requirement.getType());
              if (resolver != null) {
                final UniqueId uid = resolver.resolveExternalId(requirement.getIdentifiers(), versionCorrection);
                if (uid != null) {
                  return requirement.replaceIdentifier(uid);
                } else {
                  return null;
                }
              } else {
                return null;
              }
            }

            @Override
            public ComputationTargetSpecification visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
              final UniqueId uid = specification.getUniqueId();
              if ((uid != null) && uid.isLatest()) {
                final IdentifierResolver resolver = _resolve.get(specification.getType());
                if (resolver != null) {
                  final UniqueId newUID = resolver.resolveObjectId(uid.getObjectId(), versionCorrection);
                  if (newUID != null) {
                    return specification.replaceIdentifier(newUID);
                  } else {
                    return null;
                  }
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
        final Map<ComputationTargetReference, ComputationTargetSpecification> result = new HashMap<ComputationTargetReference, ComputationTargetSpecification>();
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
                    final UniqueId uid = _resolve.get(entry.getKey()).resolveExternalId(requirement.getIdentifiers(), versionCorrection);
                    if (uid != null) {
                      synchronized (result) {
                        result.put(requirement, requirement.replaceIdentifier(uid));
                      }
                    }
                  }
                });
              } else {
                final ComputationTargetRequirement requirement = entry.getValue().iterator().next();
                final UniqueId uid = _resolve.get(entry.getKey()).resolveExternalId(requirement.getIdentifiers(), versionCorrection);
                if (uid != null) {
                  result.put(requirement, requirement.replaceIdentifier(uid));
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
                    final Set<ExternalIdBundle> identifiers = Sets.newHashSetWithExpectedSize(entry.getValue().size());
                    for (final ComputationTargetRequirement requirement : entry.getValue()) {
                      identifiers.add(requirement.getIdentifiers());
                    }
                    final Map<ExternalIdBundle, UniqueId> uids = _resolve.get(entry.getKey()).resolveExternalIds(identifiers, versionCorrection);
                    for (final ComputationTargetRequirement requirement : entry.getValue()) {
                      final UniqueId uid = uids.get(requirement.getIdentifiers());
                      if (uid != null) {
                        synchronized (result) {
                          result.put(requirement, requirement.replaceIdentifier(uid));
                        }
                      }
                    }
                  }
                });
              } else {
                final Set<ExternalIdBundle> identifiers = Sets.newHashSetWithExpectedSize(entry.getValue().size());
                for (final ComputationTargetRequirement requirement : entry.getValue()) {
                  identifiers.add(requirement.getIdentifiers());
                }
                final Map<ExternalIdBundle, UniqueId> uids = _resolve.get(entry.getKey()).resolveExternalIds(identifiers, versionCorrection);
                for (final ComputationTargetRequirement requirement : entry.getValue()) {
                  final UniqueId uid = uids.get(requirement.getIdentifiers());
                  if (uid != null) {
                    result.put(requirement, requirement.replaceIdentifier(uid));
                  }
                }
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
                    final UniqueId uid = _resolve.get(entry.getKey()).resolveObjectId(specification.getUniqueId().getObjectId(), versionCorrection);
                    if (uid != null) {
                      synchronized (result) {
                        result.put(specification, specification.replaceIdentifier(uid));
                      }
                    }
                  }
                });
              } else {
                final ComputationTargetSpecification specification = entry.getValue().iterator().next();
                final UniqueId uid = _resolve.get(entry.getKey()).resolveObjectId(specification.getUniqueId().getObjectId(), versionCorrection);
                if (uid != null) {
                  result.put(specification, specification.replaceIdentifier(uid));
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
                    final Set<ObjectId> identifiers = Sets.newHashSetWithExpectedSize(entry.getValue().size());
                    for (final ComputationTargetSpecification specification : entry.getValue()) {
                      identifiers.add(specification.getUniqueId().getObjectId());
                    }
                    final Map<ObjectId, UniqueId> uids = _resolve.get(entry.getKey()).resolveObjectIds(identifiers, versionCorrection);
                    for (final ComputationTargetSpecification specification : entry.getValue()) {
                      final UniqueId uid = uids.get(specification.getUniqueId().getObjectId());
                      if (uid != null) {
                        synchronized (result) {
                          result.put(specification, specification.replaceIdentifier(uid));
                        }
                      }
                    }
                  }
                });
              } else {
                final Set<ObjectId> identifiers = Sets.newHashSetWithExpectedSize(entry.getValue().size());
                for (final ComputationTargetSpecification specification : entry.getValue()) {
                  identifiers.add(specification.getUniqueId().getObjectId());
                }
                final Map<ObjectId, UniqueId> uids = _resolve.get(entry.getKey()).resolveObjectIds(identifiers, versionCorrection);
                for (final ComputationTargetSpecification specification : entry.getValue()) {
                  final UniqueId uid = uids.get(specification.getUniqueId().getObjectId());
                  if (uid != null) {
                    result.put(specification, specification.replaceIdentifier(uid));
                  }
                }
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
