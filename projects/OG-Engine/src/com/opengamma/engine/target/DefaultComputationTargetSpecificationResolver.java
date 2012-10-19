/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.target.resolver.IdentifierResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Standard implementation of a {@link ComputationTargetSpecificationResolver}.
 */
public class DefaultComputationTargetSpecificationResolver implements ComputationTargetSpecificationResolver {

  private final ComputationTargetTypeMap<IdentifierResolver> _resolve = new ComputationTargetTypeMap<IdentifierResolver>();
  private final ExternalIdOrderConfig _externalIdOrderConfig;

  public DefaultComputationTargetSpecificationResolver() {
    this(ExternalIdOrderConfig.DEFAULT_CONFIG);
  }

  public DefaultComputationTargetSpecificationResolver(final ExternalIdOrderConfig externalIdOrderConfig) {
    ArgumentChecker.notNull(externalIdOrderConfig, "externalIdOrderConfig");
    _externalIdOrderConfig = externalIdOrderConfig;
  }

  public void addResolver(final ComputationTargetType type, final IdentifierResolver strategy) {
    _resolve.put(type, strategy);
  }

  @Override
  public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference, final VersionCorrection versionCorrection) {
    return atVersionCorrection(versionCorrection).getTargetSpecification(reference);
  }

  @Override
  public AtVersionCorrection atVersionCorrection(final VersionCorrection versionCorrection) {
    return new AtVersionCorrection() {

      private final ComputationTargetReferenceVisitor<ComputationTargetSpecification> _getTargetSpecification =
          new ComputationTargetReferenceVisitor<ComputationTargetSpecification>() {
            @Override
            public ComputationTargetSpecification visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
              final IdentifierResolver resolver = _resolve.get(requirement.getType());
              if (resolver != null) {
                final UniqueId uid = resolver.resolve(requirement.getIdentifiers(), versionCorrection);
                if (uid != null) {
                  return requirement.replaceIdentifier(uid);
                } else {
                  return null;
                }
              } else {
                final ExternalId id = MarketDataUtils.getPreferredIdentifier(getExternalIdOrderConfig(), requirement.getIdentifiers());
                return requirement.replaceIdentifier(UniqueId.of(id.getScheme().getName(), id.getValue()));
              }
            }

            @Override
            public ComputationTargetSpecification visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
              final UniqueId uid = specification.getUniqueId();
              if (uid.isLatest()) {
                final IdentifierResolver resolver = _resolve.get(specification.getType());
                if (resolver != null) {
                  final UniqueId newUID = resolver.resolve(uid.getObjectId(), versionCorrection);
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

    };
  }

  private ExternalIdOrderConfig getExternalIdOrderConfig() {
    return _externalIdOrderConfig;
  }

}
