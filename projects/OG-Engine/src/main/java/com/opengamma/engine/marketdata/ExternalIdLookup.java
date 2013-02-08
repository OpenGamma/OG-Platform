/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.lambdava.functions.Function1;

/**
 * Look up an external identifier from a computation target.
 */
public class ExternalIdLookup extends AbstractExternalIdentifierLookup<ExternalId> {

  private final ExternalIdOrderConfig _ordering;

  public ExternalIdLookup(final ExternalIdOrderConfig ordering, final SecuritySource securitySource) {
    super(securitySource);
    _ordering = (ordering == null) ? ExternalIdOrderConfig.DEFAULT_CONFIG : ordering;
  }

  @Override
  protected void registerBundleLookup(final ComputationTargetType type, final Function1<UniqueId, ExternalIdBundle> operation) {
    registerIdentifierLookup(type, new Function1<UniqueId, ExternalId>() {
      @Override
      public ExternalId execute(final UniqueId a) {
        final ExternalIdBundle bundle = operation.execute(a);
        if (bundle != null) {
          return preferredIdentifier(bundle);
        } else {
          return null;
        }
      }
    });
  }

  private ExternalIdOrderConfig getOrdering() {
    return _ordering;
  }

  public ExternalId getIdentifier(final ComputationTargetReference target) {
    return lookup(target);
  }

  protected ExternalId preferredIdentifier(final ExternalIdBundle bundle) {
    return MarketDataUtils.getPreferredIdentifier(getOrdering(), bundle);
  }

  @Override
  public ExternalId visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
    return preferredIdentifier(requirement.getIdentifiers());
  }

  @Override
  protected ExternalId fromUniqueId(final UniqueId uid) {
    return ExternalId.of(uid.getScheme(), uid.getValue());
  }

}
