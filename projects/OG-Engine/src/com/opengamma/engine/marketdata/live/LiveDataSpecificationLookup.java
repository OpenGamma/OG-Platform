/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.ExternalIdBundleLookup;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Lookup to find the required live data specification for a {@link ValueRequirement}.
 */
/* package */class LiveDataSpecificationLookup {

  private final ExternalIdBundleLookup _externalIdLookup;

  private final String _normalizationRules;

  /**
   * Constructs a new instance.
   * 
   * @param securitySource the source used to look up {@link Security} objects, not null
   * @param normalizationRules the normalization rules to use
   */
  public LiveDataSpecificationLookup(final SecuritySource securitySource, final String normalizationRules) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(normalizationRules, "normalizationRules");
    _externalIdLookup = new ExternalIdBundleLookup(securitySource);
    _normalizationRules = normalizationRules;
  }

  protected ExternalIdBundleLookup getExternalIdLookup() {
    return _externalIdLookup;
  }

  protected String getNormalizationRules() {
    return _normalizationRules;
  }

  /**
   * Gets the required live data specification.
   * <p>
   * This method can only be called for primitives and securities. For positions and nodes, there is no live data market line that would directly value positions or porfolios.
   * 
   * @return the specification of live data that directly produces a value for this computation target, not null
   * @throws OpenGammaRuntimeException If there is no live data directly corresponding to the target
   */
  public LiveDataSpecification getRequiredLiveData(final ValueRequirement requirement) {
    final ExternalIdBundle bundle = getExternalIdLookup().getExternalIds(requirement.getTargetReference());
    if (bundle == null) {
      throw new OpenGammaRuntimeException("No live data is needed for " + requirement);
    }
    return new LiveDataSpecification(getNormalizationRules(), bundle);
  }

}
