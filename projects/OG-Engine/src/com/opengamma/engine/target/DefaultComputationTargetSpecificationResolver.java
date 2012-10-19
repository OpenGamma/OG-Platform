/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Standard implementation of a {@link ComputationTargetSpecificationResolver}.
 */
public class DefaultComputationTargetSpecificationResolver implements ComputationTargetSpecificationResolver {

  /**
   * Implementation stub to be associated with a type.
   */
  public interface Strategy {

    UniqueId resolve(ExternalIdBundle identifiers, VersionCorrection versionCorrection);

    UniqueId resolve(ObjectId identifier, VersionCorrection versionCorrection);

  }

  private final ComputationTargetTypeMap<Strategy> _resolve = new ComputationTargetTypeMap<Strategy>();
  private final ExternalIdOrderConfig _externalIdOrderConfig;

  public DefaultComputationTargetSpecificationResolver() {
    this(ExternalIdOrderConfig.DEFAULT_CONFIG);
  }

  public DefaultComputationTargetSpecificationResolver(final ExternalIdOrderConfig externalIdOrderConfig) {
    ArgumentChecker.notNull(externalIdOrderConfig, "externalIdOrderConfig");
    _externalIdOrderConfig = externalIdOrderConfig;
  }

  public void addStrategy(final ComputationTargetType type, final Strategy strategy) {
    _resolve.put(type, strategy);
  }

  public void addSecurityResolverStrategy(final SecuritySource securitySource) {
    addStrategy(ComputationTargetType.SECURITY, new Strategy() {

      @Override
      public UniqueId resolve(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
        final Security security = securitySource.getSecurity(identifiers, versionCorrection);
        if (security == null) {
          return null;
        } else {
          return security.getUniqueId();
        }
      }

      @Override
      public UniqueId resolve(final ObjectId identifier, final VersionCorrection versionCorrection) {
        try {
          return securitySource.getSecurity(identifier, versionCorrection).getUniqueId();
        } catch (DataNotFoundException e) {
          return null;
        }
      }

    });
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
              final Strategy strategy = _resolve.get(requirement.getType());
              if (strategy != null) {
                final UniqueId uid = strategy.resolve(requirement.getIdentifiers(), versionCorrection);
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
                final Strategy strategy = _resolve.get(specification.getType());
                if (strategy != null) {
                  final UniqueId newUID = strategy.resolve(uid.getObjectId(), versionCorrection);
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
