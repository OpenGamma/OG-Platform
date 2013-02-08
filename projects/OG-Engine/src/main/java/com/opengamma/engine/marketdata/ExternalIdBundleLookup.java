/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.lambdava.functions.Function1;

/**
 * Look up an external identifier bundle from a computation target.
 */
public class ExternalIdBundleLookup extends AbstractExternalIdentifierLookup<ExternalIdBundle> {

  public ExternalIdBundleLookup(final SecuritySource securitySource) {
    super(securitySource);
  }

  @Override
  protected void registerIdentifierLookup(final ComputationTargetType type, final Function1<UniqueId, ExternalId> operation) {
    registerBundleLookup(type, new Function1<UniqueId, ExternalIdBundle>() {
      @Override
      public ExternalIdBundle execute(final UniqueId a) {
        final ExternalId identifier = operation.execute(a);
        if (identifier != null) {
          return identifier.toBundle();
        } else {
          return null;
        }
      }
    });
  }

  public ExternalIdBundle getExternalIds(final ComputationTargetReference target) {
    return lookup(target);
  }

  @Override
  public ExternalIdBundle visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
    return requirement.getIdentifiers();
  }

  protected ExternalIdBundle fromUniqueId(final UniqueId uid) {
    return ExternalId.of(uid.getScheme(), uid.getValue()).toBundle();
  }

}
