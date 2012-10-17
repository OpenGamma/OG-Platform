/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.functional.Function1;

/**
 * Lookup a {@link ComputationTargetSpecification} from a {@link ComputationTargetReference}.
 */
public class ComputationTargetSpecificationResolver implements ComputationTargetReferenceVisitor<ComputationTargetSpecification> {

  private final ComputationTargetTypeMap<Function1<ExternalIdBundle, UniqueId>> _lookup = new ComputationTargetTypeMap<Function1<ExternalIdBundle, UniqueId>>();
  private final ExternalIdOrderConfig _externalIdOrderConfig;

  /**
   * Creates a new specification resolver.
   * 
   * @param externalIdOrderConfig the selector to use when coercing an external identifier from a bundle into a unique identifier. The one from the highest ranking scheme will be used. Omit for a
   *          system default.
   * @param securitySource the security source to use when resolving securities referenced by external identifiers, omit to not resolve securities
   */
  public ComputationTargetSpecificationResolver(final ExternalIdOrderConfig externalIdOrderConfig, final SecuritySource securitySource) {
    _externalIdOrderConfig = (externalIdOrderConfig != null) ? externalIdOrderConfig : ExternalIdOrderConfig.DEFAULT_CONFIG;
    if (securitySource != null) {
      _lookup.put(ComputationTargetType.SECURITY, new Function1<ExternalIdBundle, UniqueId>() {
        @Override
        public UniqueId execute(final ExternalIdBundle identifiers) {
          final Security security = securitySource.getSecurity(identifiers);
          if (security == null) {
            return null;
          } else {
            return security.getUniqueId();
          }
        }
      });
    }
  }

  // TODO: change this to an interface and put this in DefaultComputationTargetSpecificationResolver

  // TODO: methods to add other resolver strategies

  /**
   * Returns the computation target specification that corresponds to the reference. If the reference is already a resolved specification then the same object is returned. If the reference contains an
   * external identifier bundle the appropriate source will be used to look up the object and a specification constructed from the unique identifier of the returned object, if any.
   * 
   * @param reference the reference to lookup
   * @return the target specification, or null if the reference could not be resolved
   */
  public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference) {
    return reference.accept(this);
  }

  private ExternalIdOrderConfig getExternalIdOrderConfig() {
    return _externalIdOrderConfig;
  }

  @Override
  public ComputationTargetSpecification visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
    final Function1<ExternalIdBundle, UniqueId> operation = _lookup.get(requirement.getType());
    if (operation != null) {
      final UniqueId uid = operation.execute(requirement.getIdentifiers());
      if (uid != null) {
        return new ComputationTargetSpecification(requirement.getType(), uid);
      } else {
        return null;
      }
    } else {
      final ExternalId id = MarketDataUtils.getPreferredIdentifier(getExternalIdOrderConfig(), requirement.getIdentifiers());
      return new ComputationTargetSpecification(requirement.getType(), UniqueId.of(id.getScheme().getName(), id.getValue()));
    }
  }

  @Override
  public ComputationTargetSpecification visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
    return specification;
  }

}
